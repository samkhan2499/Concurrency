package org.example.prime_numbers_variations;

import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadWithoutFairness {
    private static final AtomicInteger countOfPrimes = new AtomicInteger(1);

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        var limit = 100000000; // 100 million

        Thread[] threads = new Thread[10];
        var range = limit / threads.length;

        for (int t = 0; t < threads.length; t++) {
            final int start = t * range + 1;
            final int end = (t == threads.length - 1) ? limit : (t + 1) * range;
            threads[t] = new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                for (var number = start; number <= end; number++) {
                   isPrime(number);
                        // Uncomment the next line to see the prime numbers
                        // System.out.println(number);
                }
                System.out.printf("Thread %s completed processing range from %d to %d%n", threadName, start, end);
            });
            threads[t].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Multi-threaded execution time without fairness: %d ms%n with count: %d", (endTime - startTime), countOfPrimes.get());
    }

    private static void isPrime(int x) {
        if ((x & 1) == 0) return;

        for (int i = 3; i <= Math.sqrt(x); i++) {
            if (x % i == 0) return;
        }
        countOfPrimes.incrementAndGet();
    }
}
