package lowleveldesigns.ratelimiter;

/*
classes
* */
/*
RateLimitStrategy (interface)
TokenBucket implements RateLimitStrategy
SlidingWindow implements RateLimitStrategy
RateLimiter
* */

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/*
RateLimitStrategy
    knows:
    does:
        isAllowed() -> boolean
* */
interface RateLimitStrategy {
    boolean isAllowed();
}

/*
TokenBucket
    knows:
        maxCapacity
        refillRate(int)
        currTokens
        lastRefillAt (LocalDataTime)
        Reentrant lock
    does:
        isAllowed() -> boolean
* */
class TokenBucket implements RateLimitStrategy {
    private final int maxCapacity;
    private final int refillRate;
    private int currTokens;
    private LocalDateTime lastRefillAt;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucket(int maxCapacity, int refillRate, int currTokens) {
        this.maxCapacity = maxCapacity;
        this.refillRate = refillRate;
        this.currTokens = currTokens;
        this.lastRefillAt = LocalDateTime.now();
    }

    @Override
    public boolean isAllowed() {
        lock.lock();
        try {
            // 1. Refill the bucket
            long secondsElapsed = Duration.between(lastRefillAt, LocalDateTime.now()).getSeconds();
            currTokens += (int) Math.min(maxCapacity, currTokens + secondsElapsed * refillRate);
            lastRefillAt = LocalDateTime.now();

            // 2. Check whether the bucket contains any tokens, if no tokens then reject (false), else accept (true)
            if(currTokens == 0) {
                return false;
            }
            currTokens--;
            return true;
        } finally {
            lock.unlock();
        }
    }
}

/*
SlidingWindow
    knows:
        Deque<LocalDateTime> queue
        int maxRequestsAllowed
        int windowSizeInSeconds
        Reentrant lock
    does:
        isAllowed() -> boolean
* */
class SlidingWindow implements RateLimitStrategy {
    private Deque<LocalDateTime> queue;
    private final int maxRequestsAllowed;
    private final int windowSizeInSeconds;
    private final ReentrantLock lock = new ReentrantLock();

    public SlidingWindow(int maxRequestsAllowed, int windowSizeInSeconds) {
        this.queue = new ArrayDeque<>();
        this.maxRequestsAllowed = maxRequestsAllowed;
        this.windowSizeInSeconds = windowSizeInSeconds;
    }

    @Override
    public boolean isAllowed() {
        lock.lock();
        try {
            // 1. Remove all the older timestamps which are greater than the current time - windowSizeInSeconds
            LocalDateTime oldestTimestampAllowed = LocalDateTime.now().minusSeconds(windowSizeInSeconds);
            while(!queue.isEmpty() && queue.peekFirst().isBefore(oldestTimestampAllowed)) {
                queue.pollFirst();
            }
            // 2. Check the size of the queue, if it is >= windowSizeInSeconds, reject (false), else accept (true)
            if(queue.size() >= maxRequestsAllowed) {
                return false;
            }
            queue.offerLast(LocalDateTime.now());
            return true;
        } finally {
            lock.unlock();
        }
    }
}

/*
RateLimiter
    knows:
        Map<String, RateLimitStrategy> map   // clientId -> RateLimitStrategy
    does:
        addClient(String, RateLimitStrategy)
        removeClient(String)
        isAllowed(clientId) -> boolean
* */
public class RateLimiter {
    private Map<String, RateLimitStrategy> map;

    public RateLimiter() {
        this.map = new HashMap<>();
    }

    public void addClient(String clientId, RateLimitStrategy strategy) {
        map.put(clientId, strategy);
    }

    public void removeClient(String clientId) {
        map.remove(clientId);
    }

    public boolean isAllowed(String clientId) {
        if(!map.containsKey(clientId)) {
            return false;
        }
        return map.get(clientId).isAllowed();
    }
}

/*

TokenBucket is-a RateLimitStrategy
SlidingWindow is-a RateLimitStrategy

RateLimiter has-a Mapping of clientId -> RateLimitStrategy

* */