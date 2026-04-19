package multithreadingandconcurrency.multithreading;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class SMSThreadRunnable implements Runnable {
    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            System.out.println("Subtask of sending SMS completed using: " + Thread.currentThread().getName());

        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class EmailThreadRunnable implements Runnable {
    @Override
    public void run() {
        try {
            Thread.sleep(3000);
            System.out.println("Subtask of sending Email completed using: " + Thread.currentThread().getName());
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class ETACalculatorCallable implements Callable<String> {
    private final String location;

    public ETACalculatorCallable(String location) {
        this.location = location;
    }

    @Override
    public String call() throws Exception {
        try {
            Thread.sleep(5000);
            System.out.println("Subtask of calculationg ETA to: " + location + " completed using: " + Thread.currentThread().getName());
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "ETA to location: " + location + " is: 20 minutes";
    }
}

public class ImplementingRunnable {
    static void main(String[] args) {
        SMSThreadRunnable smsSubTask = new SMSThreadRunnable();
        EmailThreadRunnable emailSubTask = new EmailThreadRunnable();
        FutureTask<String> etaCalculatorSubTask = new FutureTask<>(new ETACalculatorCallable("Toronto"));

        // we need to call the Thread class because it is the one
        // that creates the actual separate thread in the OS and
        // executes the code in our class in that thread
        Thread smsThread = new Thread(smsSubTask);
        Thread emailThread = new Thread(emailSubTask);
        Thread etaThread = new Thread(etaCalculatorSubTask);

        smsThread.setName("SMS Thread");
        emailThread.setName("Email Thread");
        etaThread.setName("ETA Calculator Thread");

        System.out.println("Task started using: " + Thread.currentThread().getName());
        smsThread.start();
        System.out.println("Subtask of sending SMS ongoing");
        emailThread.start();
        System.out.println("Subtask of sending Email ongoing");
        etaThread.start();
        System.out.println("Subtask of calculating ETA ongoing");
        try {
            smsThread.join();
            emailThread.join();
            etaThread.join();
            System.out.println(etaCalculatorSubTask.get());
            System.out.println("Task completed");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
