package org.example.pessimistic_locking;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafetyDemo {
    private static CountDownLatch latch;
    private static final ReentrantLock mutex = new ReentrantLock();
    private static int count;
    private static final int NUM_THREADS = 1000000;
    
    public static void incCountWithoutLock() {
        count++;
        latch.countDown();
    }
    
    public static void incCountWithMutex() {
        mutex.lock();
        try {
            count++;
        } finally {
            mutex.unlock();
        }
        latch.countDown();
    }
    
    public static void countWithoutLock() throws InterruptedException {
        latch = new CountDownLatch(NUM_THREADS);
        
        for (int i = 0; i < NUM_THREADS; i++) {
            new Thread(() -> incCountWithoutLock()).start();
        }
        
        latch.await();
    }
    
    public static void countWithMutex() throws InterruptedException {
        latch = new CountDownLatch(NUM_THREADS);
        
        for (int i = 0; i < NUM_THREADS; i++) {
            new Thread(() -> incCountWithMutex()).start();
        }
        
        latch.await();
    }
    
    public static void main(String[] args) throws InterruptedException {
        count = 0;
        countWithoutLock();
        System.out.println("count (without lock): " + count);
        
        count = 0;
        countWithMutex();
        System.out.println("count (with mutex): " + count);
    }
}