package multithreadingandconcurrency.concurrency;

class CounterExampleBlock {
    private int count;

    public CounterExampleBlock() {
        count = 0;
    }

    public void increment() {
        synchronized (this) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}

public class SynchronizedBlock {
    public static void main(String[] args) {
        CounterExampleBlock counter = new CounterExampleBlock();

        Runnable task = () -> {
            for(int i = 0; i < 1000; i++) {
                counter.increment();
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

        System.out.println("Counter value: " + counter.getCount());
    }
}
