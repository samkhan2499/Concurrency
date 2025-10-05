package org.example.queue_thread_safe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeQueue {
    private static final ReentrantLock lock = new ReentrantLock();

    public static void addToQueue(List<Integer> queue, int value) {
        lock.lock();
        try {
            queue.add(value);
        } finally {
            lock.unlock();
        }
    }

    public static int removeFromQueue(List<Integer> queue) {
        lock.lock();
        int item;
        try {
            if (queue.isEmpty()) {
                throw new IllegalArgumentException("Queue is empty");
            }
            item = queue.remove(0);
        } finally {
            lock.unlock();
        }
        return item;
    }

    public static void main(String[] args) throws InterruptedException {
//        Queue<Integer> queue =  new ConcurrentLinkedQueue<>();
        List<Integer> queue = new ArrayList<>();
        Random rand = new Random(1000);
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latchEnque = new CountDownLatch(1000000);
            CountDownLatch latchDeque = new CountDownLatch(1000000);

            for (int i = 0; i < 1000000; i++) {
                executorService.submit(() -> {
                    addToQueue(queue, rand.nextInt());
                    latchEnque.countDown();
                });
            }

            latchEnque.await();
            System.out.println("items inserted....");

            for (int i = 0; i < 1000000; i++) {
                executorService.submit(() -> {
                    removeFromQueue(queue);
                    latchDeque.countDown();
                });
            }

            latchDeque.await();
            System.out.println("items removed....");

            executorService.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Error in executor service", e);
        }


        System.out.println(queue.size());

//        addToQueue(queue,  1);
//        addToQueue(queue, 2);
//
//        System.out.println(removeFromQueue(queue));
//        System.out.println(removeFromQueue(queue));
//        System.out.println(removeFromQueue(queue));

        /*
        * Advantages of Concurrent Queues:
        * 1. Thread Safety: Safer than native queues
        * 2. Scalability: Improves program throughput and performance
        * 3. Data Integrity: Correctness of data is maintained
        *
        * Where it lacks ?
        * 1. Synchonization Overhead: Locking and unlocking can add latency
        * 2. Wait/block time : Our Pessimistic Implementation
        *
        * Real World Applications
        * 1. Producer Consumer Pattern
        *   - Batch Processing
        * 2. Internal implementation of Thread Pools
        * */


    }
}
