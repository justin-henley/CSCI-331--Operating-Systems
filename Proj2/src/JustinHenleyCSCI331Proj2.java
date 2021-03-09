// Project 2:   Bounded Buffer Problem
// Description: Demonstrates multithreading and threadsafe implementation of the bounded buffer problem
// Author:      Justin Henley, jahenley@mail.fhsu.edu
// Date:        2021-03-09

// Notes:   - Version with race conditions is included in a block comment at the bottom.  I wasn't
//              sure if that should be included or not.


import java.util.Random;
import java.util.concurrent.Semaphore;

public class JustinHenleyCSCI331Proj2 {
    public static final int BUFF_SIZE = 10;     // Buffer size (n)
    public static final int MAX_OPS = 200;      // Maximum operations by a thread on each wake cycle (k)
    public static final long MAX_SLEEP = 10;    // Maximum sleep time (in milliseconds) by a thread between wake cycles (t)
    public static final int MAX_WAKE = 50;      // Maximum wake cycles for Consumer (Program exits after MAX_WAKE Consumer cycles)

    public static void main(String[] args) {
        // Create the buffer shared by producer and consumer
        int[] buffer = new int[BUFF_SIZE];
        // Semaphores for tracking empty and full slots, controlling access to the buffer
        Semaphore fullSlots = new Semaphore(0);
        Semaphore emptySlots = new Semaphore(BUFF_SIZE);

        // Create new Producer and Consumer thread instances
        Producer producer = new Producer(buffer, MAX_OPS, MAX_SLEEP, fullSlots, emptySlots);
        Consumer consumer = new Consumer(buffer, MAX_OPS, MAX_SLEEP, MAX_WAKE, fullSlots, emptySlots);


        // Start execution of producer and consumer
        producer.start();
        consumer.start();
    }
}

// Provides the foundations of the Producer and Consumer classes
abstract class ProdCons extends Thread {
    // DATA FIELDS
    private final int MAX_OPS;       // Maximum operations by the thread on each wake cycle
    private final long MAX_SLEEP;    // Maximum sleep time (in milliseconds) by the thread between wake cycles
    protected Semaphore fullSlots;  // Semaphore for tracking full slots in buffer
    protected Semaphore emptySlots; // Semaphore for tracking empty slots in the buffer
    private final Random rand;      // Random number generator used to emulate unpredictable execution speeds

    // CONSTRUCTOR
    public ProdCons(int maxOps, long maxSleep, Semaphore fullSlots, Semaphore emptySlots) {
        this.MAX_OPS = maxOps;
        this.MAX_SLEEP = maxSleep;
        this.fullSlots = fullSlots;
        this.emptySlots = emptySlots;
        this.rand = new Random();
    }

    // METHODS

    // Gets a random number of ops for the Producer or Consumer to execute
    // Receives:    Nothing
    // Returns:     An int in the range [1, MAX_OPS]
    protected int getOps() {
        return 1 + rand.nextInt(MAX_OPS);
    }

    // Gets a random sleep time for the Producer or Consumer to execute
    // Receives:    Nothing
    // Returns:     An int in the range [1, MAX_SLEEP]
    protected long getSleep() {
        return 1 + Math.abs(rand.nextLong()) % MAX_SLEEP;
    }
}

// The Producer in the Bounded Buffer problem
class Producer extends ProdCons {
    // DATA FIELDS
    private int next_in;        // Tracks the index in buffer of the next element to be modified
    private final int[] buffer; // The buffer shared by producer and consumer

    // CONSTRUCTOR
    public Producer(int[] buffer, int maxOps, long maxSleep, Semaphore fullSlots, Semaphore emptySlots) {
        super(maxOps, maxSleep, fullSlots, emptySlots);
        this.next_in = 0;       // Start filling buffer at index 0
        this.buffer = buffer;   // Points buffer to the object passed to constructor (allows sharing with consumer)
    }

    // METHODS

    // Executes when the thread is run
    // Receives:    Nothing
    // Returns:     Nothing
    public void run() {
        try {
            // Producer continues producing indefinitely to avoid consumer indefinitely on a halted producer.
            while (true) {
                int k1 = getOps();  // Generate a random number of operations to perform. [1, MAX_OPS]

                // Perform k1 productions
                for(int i = 0; i < k1; i++) {
                    emptySlots.acquire();   // empty slots - 1, once a slot (permit) is available

                    // Add 1 to the next element of buffer, throwing an exception if the consumer has consumed more than producer has produced
                    int index = (next_in + i) % buffer.length;
                    if (buffer[index] < 0)
                        throw new Exception("Producer detected race condition");
                    buffer[index] += 1;

                    fullSlots.release();    // full slots + 1
                }

                next_in = (next_in + k1) % buffer.length;   // Move next_in to index after k1 productions
                System.out.println("Producer OK");          // Notify that producer completed a wake cycle without exception
                sleep(getSleep());                          // Sleep for a random time in range [1, MAX_SLEEP]
            }

        }
        catch (Exception e) {
            System.out.println("Exception in Producer: ");
            System.exit(1);
        }
    }
}

// The Consumer in the Bounded Buffer problem
class Consumer extends ProdCons {
    // DATA FIELDS
    private int next_out;           // Tracks the index in buffer of the next element to be modified
    private final int[] buffer;     // The buffer shared by producer and consumer
    private final int maxWake;      // Maximum wake cycles

    // CONSTRUCTOR
    public Consumer(int[] buffer, int maxOps, long maxSleep, int maxWake, Semaphore fullSlots, Semaphore emptySlots) {
        super(maxOps, maxSleep, fullSlots, emptySlots);
        this.next_out = 0;      // Start emptying buffer at index 0
        this.buffer = buffer;   // Points buffer to the object passed to constructor (allows sharing with producer)
        this.maxWake = maxWake;
    }

    // METHODS

    // Executes when the thread is run
    // Receives:    Nothing
    // Returns:     Nothing
    public void run() {
        try {
            // Consumer executes for MAX_WAKE number of wake cycles
            for (int wakeCycles = 0; wakeCycles < maxWake; wakeCycles++) {
                sleep(getSleep());  // Sleep for a random time in range [1, MAX_SLEEP]
                int k2 = getOps();  // Generate a random number of operations to perform in range [1, MAX_OPS]

                for (int i = 0; i < k2; i++) {
                    fullSlots.acquire();    // full slots - 1, once a slot (permit) is available

                    // Subtract 1 from the next element of buffer, throwing an exception if the producer has produced
                    //  multiple units without waiting for consumer.
                    int index = (next_out + i) % buffer.length;
                    if (buffer[index] > 1)
                        throw new Exception("Consumer detected race condition");
                    buffer[index] -= 1;

                    emptySlots.release();   // empty slots + 1
                }

                next_out = (next_out + k2) % buffer.length;         // Move next_out to index after k2 consumptions
                System.out.println("Consumer OK: " + wakeCycles);   // Notify that consumer completed a wake cycle without exception
            }

            // Once maxWake number of wake cycles completed, notify user and exit
            System.out.println("Consumer exits system without any race problems");
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("Exception in Consumer: " + e);
            System.exit(1);
        }
    }
}


/*
SAMPLE RUN:
    Producer OK
    Producer OK
    Consumer OK: 0
    Consumer OK: 1
    Producer OK
    Producer OK
    Consumer OK: 2
    Producer OK
    Consumer OK: 3
    Consumer OK: 4
    Producer OK
    Producer OK
    Consumer OK: 5
    Consumer OK: 6
    Producer OK
    Consumer OK: 7
    Consumer OK: 8
    Producer OK
    Consumer OK: 9
    Consumer OK: 10
    Producer OK
    Consumer OK: 11
    Producer OK
    Producer OK
    Consumer OK: 12
    Producer OK
    Producer OK
    Consumer OK: 13
    Consumer OK: 14
    Producer OK
    Producer OK
    Producer OK
    Consumer OK: 15
    Consumer OK: 16
    Consumer OK: 17
    Producer OK
    Producer OK
    Consumer OK: 18
    Producer OK
    Consumer OK: 19
    Consumer OK: 20
    Consumer OK: 21
    Producer OK
    Consumer OK: 22
    Consumer OK: 23
    Producer OK
    Consumer OK: 24
    Producer OK
    Consumer OK: 25
    Consumer OK: 26
    Consumer OK: 27
    Producer OK
    Consumer OK: 28
    Producer OK
    Consumer OK: 29
    Consumer OK: 30
    Producer OK
    Consumer OK: 31
    Consumer OK: 32
    Consumer OK: 33
    Producer OK
    Consumer OK: 34
    Producer OK
    Consumer OK: 35
    Producer OK
    Consumer OK: 36
    Producer OK
    Producer OK
    Consumer OK: 37
    Producer OK
    Consumer OK: 38
    Consumer OK: 39
    Producer OK
    Producer OK
    Consumer OK: 40
    Producer OK
    Consumer OK: 41
    Producer OK
    Consumer OK: 42
    Consumer OK: 43
    Producer OK
    Consumer OK: 44
    Consumer OK: 45
    Producer OK
    Consumer OK: 46
    Producer OK
    Producer OK
    Consumer OK: 47
    Consumer OK: 48
    Producer OK
    Consumer OK: 49
    Consumer exits system without any race problems

    Process finished with exit code 0
 */

/*
VERSION *WITH* RACE CONDITIONS (BEFORE ADDING SEMAPHORES)

    import java.util.Random;

    public class HasRaceConditions {
        // Buffer size
        public static final int BUFF_SIZE = 70;
        // Maximum operations by a thread on each wake cycle
        public static final int MAX_OPS = 20;
        // Maximum sleep time by a thread between wake cycles
        public static final long MAX_SLEEP = 10;
        // Maximum wake cycles for Producer and Consumer
        public static final int MAX_WAKE = 50;



        public static void main(String[] args) {
            int[] buffer = new int[BUFF_SIZE];

            RaceProducer producer = new RaceProducer(buffer, MAX_OPS, MAX_SLEEP);
            RaceConsumer consumer = new RaceConsumer(buffer, MAX_OPS, MAX_SLEEP, MAX_WAKE);

            producer.start();
            consumer.start();
        }
    }


    abstract class RaceProdCons extends Thread {
        private final int maxOps;
        private final long maxSleep;
        private Random rand;

        public RaceProdCons(int maxOps, long maxSleep) {
            this.maxOps = maxOps;
            this.maxSleep = maxSleep;
            this.rand = new Random();
        }

        protected int getOps() {
            return 1 + rand.nextInt(maxOps);
        }
        protected long getSleep() {
            return 1 + Math.abs(rand.nextLong()) % maxSleep;
        }
    }


    class RaceProducer extends RaceProdCons {
        private int next_in;
        private int[] buffer;

        public RaceProducer(int[] buffer, int maxOps, long maxSleep) {
            super(maxOps, maxSleep);
            this.next_in = 0;
            this.buffer = buffer;
        }

        public void run() {
            try {
                while (true) {
                    int k1 = getOps();
                    for(int i = 0; i < k1; i++) {
                        int index = (next_in + i) % buffer.length;
                        if (buffer[index] < 0)
                            throw new Exception("Producer detected race condition");
                        buffer[index] += 1;
                    }
                    next_in = (next_in + k1) % buffer.length;
                    System.out.println("Producer OK");
                    //noinspection BusyWait
                    sleep(getSleep());
                }

            }
            catch (Exception e) {
                System.out.println("Exception in Producer: " + e);
                System.exit(1);
            }
        }
    }

    class RaceConsumer extends RaceProdCons {
        private int next_out;
        private int[] buffer;
        private int maxWake;

        public RaceConsumer(int[] buffer, int maxOps, long maxSleep, int maxWake) {
            super(maxOps, maxSleep);
            this.next_out = 0;
            this.buffer = buffer;
            this.maxWake = maxWake;
        }

        public void run() {
            try {
                for (int wakeCycles = 0; wakeCycles < maxWake; wakeCycles++) {
                    //noinspection BusyWait
                    sleep(getSleep());
                    int k2 = getOps();
                    for (int i = 0; i < k2; i++) {
                        int index = (next_out + i) % buffer.length;
                        if (buffer[index] > 1)
                            throw new Exception("Consumer detected race condition");
                        buffer[index] -= 1;
                    }
                    next_out = (next_out + k2) % buffer.length;
                    System.out.println("Consumer OK: " + wakeCycles);
                }

                System.out.println("Consumer exits system without any race problems");
                System.exit(0);
            }
            catch (Exception e) {
                System.out.println("Exception in Consumer: " + e);
                System.exit(1);
            }
        }
    }

 */

/*
SAMPLE RUN OF VERSION *WITH* RACE CONDITIONS (BEFORE ADDING SEMAPHORES)

SAMPLE RUN 1:
    Producer OK
    Producer OK
    Producer OK
    Producer OK
    Producer OK
    Consumer OK: 0
    Producer OK
    Producer OK
    Consumer OK: 1
    Producer OK
    Producer OK
    Producer OK
    Producer OK
    Exception in Consumer: java.lang.Exception: Consumer detected race condition

SAMPLE RUN 2:
    Producer OK
    Producer OK
    Producer OK
    Producer OK
    Consumer OK: 0
    Consumer OK: 1
    Producer OK
    Consumer OK: 2
    Consumer OK: 3
    Producer OK
    Consumer OK: 4
    Consumer OK: 5
    Exception in Producer: java.lang.Exception: Producer detected race condition

    Process finished with exit code 1
 */