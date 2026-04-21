package multithreadingandconcurrency.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockExample {
    private final Map<String, String> data = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public String getValue(String key) {
        // 1. Acquire the Read Lock
        rwLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " is READING");
            return data.get(key);
        } finally {
            // 2. Release Read Lock
            rwLock.readLock().unlock();
        }
    }

    public void putValue(String key, String value) {
        // 3. Acquire the Write Lock
        rwLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " is WRITING (Exclusive)");
            data.put(key, value);
        } finally {
            // 4. Release Write Lock
            rwLock.writeLock().unlock();
        }
    }
}
