package org.example.prime_numbers_variations;

import java.math.BigInteger;

public class SingleThread {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        var limit = 100000000; // 100 million
        var countOfPrimes = 1; // counting 2 as first prime
        for (var number = 3; number <= limit; number += 2) { // optimize by checking only odd numbers
            if (isPrime(number)) {
                countOfPrimes++;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Single-threaded execution time: %d ms%n with count: %d", (endTime - startTime), countOfPrimes);
    }

    private static boolean isPrime(int x) {
        for (int i = 3; i <= Math.sqrt(x); i++) {
            if (x % i == 0) return false;
        }
        return true;
    }
}
