package org.example.deadlock;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class HittingDeadlock {

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
        Record [] records = new Record[NUM_RECORDS];

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
/*  Sample Log Output for deadlocak case

/opt/homebrew/Cellar/openjdk/23.0.1/libexec/openjdk.jdk/Contents/Home/bin/java -javaagent:/Applications/IntelliJ IDEA CE.app/Contents/lib/idea_rt.jar=60199 -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/sam/Documents/Java Learning/Concurrency/target/classes org.example.deadlock.HittingDeadlock
Database initialized with 3 records
In mimic load.....
In mimic load.....
Transaction F started
In mimic load.....
In mimic load.....
Transaction A started
In mimic load.....
Transaction D started
In mimic load.....
Transaction E started
Transaction C started
Transaction B started
rec1 -> 1 rec2 -> 2
rec1 -> 0 rec2 -> 1
rec1 -> 1 rec2 -> 1
Transaction D started
rec1 -> 2 rec2 -> 0
Transaction C wants to acquire lock on record 2
rec1 -> 0 rec2 -> 2
Transaction F wants to acquire lock on record 0
Transaction B wants to acquire lock on record 1
rec1 -> 1 rec2 -> 1
Transaction B acquired lock on record 1
Transaction B wants to acquire lock on record 2
rec1 -> 1 rec2 -> 2
Transaction E wants to acquire lock on record 1
Transaction A wants to acquire lock on record 0
Transaction F acquired lock on record 0
Transaction F wants to acquire lock on record 2
Transaction C acquired lock on record 2
Transaction C wants to acquire lock on record 0
Transaction D started
rec1 -> 2 rec2 -> 2
Transaction D started
rec1 -> 0 rec2 -> 0
Transaction D started
rec1 -> 0 rec2 -> 1
Transaction D wants to acquire lock on record 0

Process finished with exit code 130 (interrupted by signal 2:SIGINT)

B -> 1
F -> 0
C -> 2

* */