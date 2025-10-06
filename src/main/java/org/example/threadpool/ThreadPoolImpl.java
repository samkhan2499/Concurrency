package org.example.threadpool;

import java.util.concurrent.*;

public class ThreadPoolImpl {
    private final BlockingQueue<Runnable> workQueue;
    private final CountDownLatch latch;
    private final Thread[] workers;
    volatile boolean isShutdown = false;

    public ThreadPoolImpl(int workerThread) {
        this.workQueue = new LinkedBlockingQueue<>();
        this.workers = new Thread[workerThread];
        this.latch = new CountDownLatch(workerThread);

        for(int i=0;i<workerThread;i++) {
            workers[i] = new Thread(() -> {
                try {
                    while (!isShutdown || !workQueue.isEmpty()) {
                        Runnable job = workQueue.poll(100, TimeUnit.MILLISECONDS);
                        if(job != null)
                            job.run();
                    }
                } catch (InterruptedException e) {
                    // Thread interrupted, exit
                } finally {
                    latch.countDown();
                }
            });
            workers[i].start();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int workerThreads = 4;
        int jobCount = 30;
        ThreadPoolImpl pool = new ThreadPoolImpl(workerThreads);

        for(int i=0;i<jobCount;i++) {
            pool.addJob(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("Job done by "+Thread.currentThread().getName());
                } catch (InterruptedException e) {}

            });
        }

        pool.waitForCompletion();

    }

    public void waitForCompletion() throws InterruptedException {
        isShutdown = true;
        latch.await();
    }

    private void addJob(Runnable job) {
        try {
            if(!isShutdown)
                workQueue.put(job);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
