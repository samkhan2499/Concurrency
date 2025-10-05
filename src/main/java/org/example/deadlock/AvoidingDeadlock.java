package org.example.deadlock;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class AvoidingDeadlock {
    private static final Integer NUM_RECORDS = 3;
    private static final Integer NUM_CONN = 6;

    record RecordData( int id, int age, String name) {};

    static class Record {
        ReentrantLock lock = new ReentrantLock();
        RecordData data;

        public Record(RecordData data) {
            this.data = data;
        }
    }

    public static class Database {
        Record[] records = new Record[NUM_RECORDS];

        public Database() {
            for(int i=0;i<NUM_RECORDS;i++) {
                records[i] =new Record(new RecordData(i, 20+i, "name-"+i));
            }
            System.out.println("Database initialized with "+NUM_RECORDS+" records");
        }
    }

    static Database db = new Database();

    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < NUM_CONN; i++) {
                char txn = (char) ('A' + i);
                executorService.submit(() -> mimicLoad(txn));
            }
        }
    }

    private static void mimicLoad(char txn) {
        Random rand = new Random();
//        System.out.println("In mimic load.....");
        while (true) {
//                System.out.println("Transaction "+txn+" started");
            int rec1 = rand.nextInt(0,1000) % NUM_RECORDS;
            int rec2 = rand.nextInt(0,1000) % NUM_RECORDS;
//                System.out.println("rec1 -> "+rec1+" rec2 -> "+rec2);
            if (rec1 == rec2) continue;

            // adding condition for total ordering to avoid deadlock
            if(rec1 > rec2) {
                int temp = rec1;
                rec1 = rec2;
                rec2 = temp;
            }

            acquireLock(txn, rec1);
            acquireLock(txn, rec2);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}

            releaseLock(txn, rec2);
            releaseLock(txn, rec1);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    private static void releaseLock(char txn, int rec2) {
        db.records[rec2].lock.unlock();
        System.out.println("Transaction "+txn+" released lock on record "+rec2);
    }

    private static void acquireLock(char txn, int rec1) {
        System.out.println("Transaction "+txn+" wants to acquire lock on record "+rec1);
        db.records[rec1].lock.lock();
        System.out.println("Transaction "+txn+" acquired lock on record "+rec1);
    }
}
/*
* Program will run indefinitely. To stop it, terminate the process.
* */