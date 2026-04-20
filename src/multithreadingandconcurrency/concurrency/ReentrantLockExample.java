package multithreadingandconcurrency.concurrency;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExample {
    // 1. Create the lock object
    private static final ReentrantLock lock = new ReentrantLock();
    private static int counter = 0;

    public static void main(String[] args) {
        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                // 2. Explicitly acquire the lock
                lock.lock();
                try {
                    // 3. Critical Section
                    counter++;
                } finally {
                    // 4. MUST release in finally block
                    lock.unlock();
                }
            }
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

        System.out.println("Final Count: " + counter); // Should be 2000
    }
}
