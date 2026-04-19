package multithreadingandconcurrency.multithreading;

import java.util.concurrent.*;

/*
What to observe during the execution of this program:

1. Concurrency Limit: We will notice that Tasks 1, 2, and 3 start immediately.
Tasks 4 and 5 will stay in the BlockingQueue until one of the first three threads becomes available.

2. Thread Reuse: Look at the thread names in the console. We will see names like pool-1-thread-1
appearing multiple times for different tasks. This proves the Worker Loop is working—the threads
are not dying; they are returning to the queue for more work.

3. Order of "Main" Messages: The "[Main] Shutting down..." message will likely appear before the tasks finish.
This confirms that shutdown() is non-blocking. It just sets the state; it doesn't wait.

4. Clean Exit: The program terminates quickly once all tasks are done because awaitTermination released the main thread.
*/

public class ThreadPoolExample1 {
    public static void main(String[] args) {
        // 1. Create a Fixed Thread Pool of three Worker Threads
        // This pool uses a LinkedBlockingQueue internally
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        System.out.println("[Main] submitting tasks...");

        // 2. Submit five tasks to a pool only three threads
        for(int i = 1; i <= 5; i++) {
            int taskId = i;

            /** This is the Lambda version of
                new Runnable() {
                    @Override
                    public void run() {
                     // code
                    }
                }
             **/
            executorService.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("[Task " + taskId + "] starting on " + threadName);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("[Task " + taskId + "] was interrupted!");
                }
                System.out.println("[Task " + taskId + "] finished on " + threadName);
            });
        }

        // 3. Initiate Graceful Shutdown
        // The pool stops accepting new tasks, but finishes the five tasks that were submittes
        System.out.println("[Main] Shutting down...");
        executorService.shutdown();

        // 4. Block and wait for the pool to finish all tasks (the "Await" protocol)
        // We wait a maximum of 10 seconds
        try {
            if(!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println("[Main] Tasks took too long, forcing shutdown...");
                executorService.shutdownNow();   // Force shutdown
            }
        } catch(InterruptedException e) {
            executorService.shutdownNow();
        }

        System.out.println("[Main] All workers finished. Program exiting.");
    }
}
