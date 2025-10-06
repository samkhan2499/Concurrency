package org.example.optimistic_locking;

import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticLockingExample {
    static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {

        Runnable incrementCount = () -> {
            int oldValue = count.get();
            int newValue = oldValue + 1;

            boolean status = count.compareAndSet(oldValue, newValue);
            if(!status) {
                System.out.println("Failed to update from "+oldValue+" to "+newValue);
            } else {
                System.out.println("Successfully updated from "+oldValue+" to "+newValue);
            }
        };

        Thread t1 = new Thread(incrementCount);
        Thread t2 = new Thread(incrementCount);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
