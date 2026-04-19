package multithreadingandconcurrency.multithreading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
What to observe during the execution of this program:

1. Tasks 1 & 2: Run on pool-1-thread-1 and pool-1-thread-2.

2. Tasks 3 & 4: Wait in the ArrayBlockingQueue.

3. Tasks 5 & 6: Run on pool-1-thread-3 and pool-1-thread-4 (the extra threads created because the queue was full).

4. Task 7: Runs on the main thread.
*/

public class ThreadPoolExample2 {
    public static void main(String[] args) {
        // 1. Create the Pool
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(
                2, 4,               // Core: 2, Max: 4
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2), // Queue: 2
                new ThreadPoolExecutor.CallerRunsPolicy() // Task 7 will run on 'main'
        );

        List<Future<String>> results = new ArrayList<>();

        System.out.println("[Main] Submitting 7 tasks...");

        // 2. Submit 7 tasks
        for (int i = 1; i <= 7; i++) {
            int taskId = i;

            Callable<String> task = () -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("  --> [Task " + taskId + "] started on " + threadName);
                Thread.sleep(3000); // Reduced to 3s for faster testing
                return "[Task " + taskId + "] complete on " + threadName;
            };

            results.add(executorService.submit(task));
        }

        // 3. The Shutdown Routine
        // Note: Main thread will only reach here AFTER it finishes running Task 7 itself
        System.out.println("[Main] All tasks submitted. Shutting down...");
        executorService.shutdown();

        try {
            // Wait up to 20 seconds (more than enough for 3s tasks)
            if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                System.out.println("[Main] Timeout reached! Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 4. Collect and Print Results
        System.out.println("\n--- FINAL RESULTS ---");
        for (Future<String> result : results) {
            try {
                System.out.println(result.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error retrieving result: " + e.getMessage());
            }
        }

        System.out.println("Program finished.");
    }
}