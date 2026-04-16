package multithreading;

class SMSThread extends Thread {
    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            System.out.println("Subtask of sending SMS completed using: " + Thread.currentThread().getName());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class EmailThread extends Thread {
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

public class ExtendingThread {
    static void main(String[] args) {
        Thread smsThread = new SMSThread();
        Thread emailThread = new EmailThread();

        smsThread.setName("SMS Thread");
        emailThread.setName("Email Thread");

        System.out.println("Task started using: " + Thread.currentThread().getName());
        smsThread.start();
        System.out.println("Subtask of sending SMS ongoing");
        emailThread.start();
        System.out.println("Subtask of sending Email ongoing");
        try {
            smsThread.join();
            emailThread.join();
            System.out.println("Task completed");
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
