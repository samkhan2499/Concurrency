package org.example.threadpool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolImpl {
    private final ExecutorService executorService;
    private final CountDownLatch latch;

    public ThreadPoolImpl(int workerThread, int jobCount) {
        this.executorService = Executors.newFixedThreadPool(workerThread);
        this.latch = new CountDownLatch(jobCount);
    }

    public static void main(String[] args) throws InterruptedException {
        int workerThreads = 4;
        int jobCount = 30;
        ThreadPoolImpl pool = new ThreadPoolImpl(workerThreads, jobCount);

        for(int i=0;i<jobCount;i++) {
            pool.addJob(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                System.out.println("Job done by "+Thread.currentThread().getName());
            });
        }

        pool.waitForCompletion();

    }

    public void waitForCompletion() throws InterruptedException {
        latch.await();
        executorService.shutdown();
    }

    private void addJob(Runnable job) {
        executorService.submit(() -> {
           try {
               job.run();
           } finally {
               latch.countDown();
           }
        });
    }
}
