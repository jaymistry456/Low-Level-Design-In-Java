package multithreadingandconcurrency.concurrency;

import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    // Only 3 permits available
    private final Semaphore semaphore = new Semaphore(3);

    public void accessDatabase() {
        try {
            System.out.println(Thread.currentThread().getName() + " is waiting for a permit...");

            // 1. Try to take a permit
            semaphore.acquire();

            try {
                System.out.println(Thread.currentThread().getName() + " ACQUIRED permit. Accessing DB...");
                Thread.sleep(2000); // Simulating work
            } finally {
                // 2. Always release in finally
                System.out.println(Thread.currentThread().getName() + " is RELEASING permit.");
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SemaphoreExample pool = new SemaphoreExample();

        // Launch 7 threads
        for (int i = 1; i <= 7; i++) {
            new Thread(() -> pool.accessDatabase()).start();
        }
    }
}