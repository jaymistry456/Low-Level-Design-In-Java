package multithreadingandconcurrency.concurrency;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class SafeAccount {
    private double balance = 1000;
    // Every account has its own personal lock
    public final ReentrantLock lock = new ReentrantLock();

    public void withdraw(double amount) {
        balance -= amount;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}

public class ReentrantLockTryLockExample {
    public static void main(String[] args) {
        SafeAccount acc1 = new SafeAccount();
        SafeAccount acc2 = new SafeAccount();

        // Thread 1: Wants to move 100 from Acc1 to Acc2
        Thread t1 = new Thread(() -> transfer(acc1, acc2, 100), "TransferThread-1");

        // Thread 2: Wants to move 50 from Acc2 to Acc1
        Thread t2 = new Thread(() -> transfer(acc2, acc1, 50), "TransferThread-2");

        t1.start();
        t2.start();
    }

    public static void transfer(SafeAccount from, SafeAccount to, double amount) {
        while (true) {
            boolean gotFromLock = from.lock.tryLock();
            boolean gotToLock = to.lock.tryLock();

            // 1. Success Case: We grabbed both locks!
            if (gotFromLock && gotToLock) {
                try {
                    from.withdraw(amount);
                    to.deposit(amount);
                    System.out.println(Thread.currentThread().getName() + " successfully transferred " + amount);
                    return; // Task finished, exit the loop
                } finally {
                    from.lock.unlock();
                    to.lock.unlock();
                }
            }

            // 2. Failure Case: If we only got one (or zero), we MUST back off.
            // This prevents us from "Holding" while "Waiting".
            if (gotFromLock) from.lock.unlock();
            if (gotToLock) to.lock.unlock();

            // 3. Wait a tiny bit before retrying (prevents "Livelock")
            // Randomized back-off
            try {
                // Use a random delay (Jitter) to break the "sync"
                Random random = new Random();
                int delay = random.nextInt(20); // 0 to 19ms
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
