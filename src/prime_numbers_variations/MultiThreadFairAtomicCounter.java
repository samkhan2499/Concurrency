package prime_numbers_variations;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class MultiThreadFairAtomicCounter {
    private static final int MAX_INT = 100000000;
    private static final int CONCURRENCY = 10;
    private static final AtomicInteger totalPrimeNumbers = new AtomicInteger(0);
    private static final AtomicInteger currentNum = new AtomicInteger(2);

    private static void checkPrime(int x) {
        if ((x & 1) == 0) return;

        for (int i = 3; i <= Math.sqrt(x); i++) {
            if (x % i == 0) return;
        }
        totalPrimeNumbers.incrementAndGet();
    }

    private static void doWork(String name, CountDownLatch latch) {
        Instant start = Instant.now();
        try {
            while (true) {
                int x = currentNum.incrementAndGet();
                if (x > MAX_INT) {
                    break;
                }
                checkPrime(x);
            }
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            System.out.printf("Thread %s completed in %d ms%n", name, duration.toMillis());
            latch.countDown();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Instant start = Instant.now();
        CountDownLatch latch = new CountDownLatch(CONCURRENCY);

        for (int i = 0; i < CONCURRENCY; i++) {
            final String threadName = String.valueOf(i);
            Thread thread = new Thread(() -> doWork(threadName, latch));
            thread.start();
        }

        latch.await();
        Duration duration = Duration.between(start, Instant.now());
        System.out.printf("Checking till %d found %d prime numbers. Took %d ms%n",
                MAX_INT, totalPrimeNumbers.get() + 1, duration.toMillis());
    }
}
