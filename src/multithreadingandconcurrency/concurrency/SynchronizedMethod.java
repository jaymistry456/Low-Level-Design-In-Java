package multithreadingandconcurrency.concurrency;

class CounterExampleMethod {
    private int count;

    public CounterExampleMethod() {
        count = 0;
    }

    public synchronized void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}

public class SynchronizedMethod {
    public static void main(String[] args) {
        CounterExampleMethod counter = new CounterExampleMethod();

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
