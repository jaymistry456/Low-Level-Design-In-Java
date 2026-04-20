package multithreadingandconcurrency.concurrency;

class Account {
    private double balance;

    public Account() {
        balance = 1000;
    }

    public void withdraw(double amount) {
        balance -= amount;
    }

    public void deposit(double amount) {
        balance += amount;
    }
}

public class DeadlockExample {
    public static void main(String[] args) {
        Account accountA = new Account();
        Account accountB = new Account();

        // Thread 1: Transfer A -> B
        Thread t1 = new Thread(() -> {
            synchronized (accountA) { // Locks Account A
                System.out.println("Thread 1: Locked Account A");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Thread 1: Waiting for Account B...");
                synchronized (accountB) { // Needs Account B
                    accountA.withdraw(100);
                    accountB.deposit(100);
                }
            }
        });

        // Thread 2: Transfer B -> A
        Thread t2 = new Thread(() -> {
            synchronized (accountB) { // Locks Account B
                System.out.println("Thread 2: Locked Account B");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Thread 2: Waiting for Account A...");
                synchronized (accountA) { // Needs Account A
                    accountB.withdraw(100);
                    accountA.deposit(100);
                }
            }
        });

        t1.start();
        t2.start();
    }
}
