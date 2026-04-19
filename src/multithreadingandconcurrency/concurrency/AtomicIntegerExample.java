package multithreadingandconcurrency.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

class AtomicIntegerCounter {
    private final AtomicInteger count;

    public AtomicIntegerCounter() {
        count = new AtomicInteger(0);
    }

    public void incrementCount() {
        int prev;
        int next;

        do{
            prev = count.get();
            next = prev + 1;
        } while (!count.compareAndSet(prev, next));
    }

    public int getCount() {
        return count.get();
    }
}

public class AtomicIntegerExample {
    public static void main(String[] args) {
        AtomicIntegerCounter counter = new AtomicIntegerCounter();

        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                counter.incrementCount();
            };
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Final count: " + counter.getCount());
    }
}
