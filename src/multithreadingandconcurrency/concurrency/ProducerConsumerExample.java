package multithreadingandconcurrency.concurrency;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerExample {
    private final Deque<Integer> buffer = new ArrayDeque<>();
    private final int CAPACITY = 5;

    private final Lock lock = new ReentrantLock();
    private final Condition bufferNotFull = lock.newCondition();
    private final Condition bufferNotEmpty = lock.newCondition();

    public void produce(int item) {
        lock.lock();

        try {
            while (buffer.size() == CAPACITY) {
                System.out.println("Buffer full. Producer is waiting...");
                bufferNotFull.await();
            }

            buffer.offer(item);
            System.out.println("Produced: " + item);

            bufferNotEmpty.signal();   // can use signalAll() as well to wake up multiple consumers
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public int consumer() {
        lock.lock();

        try {
            while(buffer.isEmpty()) {
                System.out.println("Buffer empty. Consumer is waiting...");
                bufferNotEmpty.await();
            }

            int item = buffer.poll();
            System.out.println("Consumed: " + item);

            bufferNotFull.signal();   // can use signalAll() as well to wake up multiple producers

            return item;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
