// Project 2:   Bounded Buffer Problem
// Description: Demonstrates multithreading and threadsafe implementation of the bounded buffer problem
// Author:      Justin Henley, jahenley@mail.fhsu.edu
// Date:        2021-03-09

// Notes:   - Version with race conditions is included in a block comment at the bottom.  I wasn't
//              sure if that should be included or not.

import java.util.Random;
import java.util.concurrent.Semaphore;

public class JustinHenleyCSCI331Proj2 {
    public static final int BUFF_SIZE = 100;    // Buffer size (n)
    public static final int MAX_OPS = 200;      // Maximum operations by a thread on each wake cycle (k)
    public static final long MAX_SLEEP = 10;    // Maximum sleep time by a thread between wake cycles (t)
    public static final int MAX_WAKE = 50;      // Maximum wake cycles for Consumer (Program exits after MAX_WAKE Consumer cycles)

    public static void main(String[] args) {
        // Create the buffer shared by producer and consumer
        int[] buffer = new int[BUFF_SIZE];
        // Semaphores for tracking empty and full slots, controlling access to the buffer
        Semaphore fullSlots = new Semaphore(0);
        Semaphore emptySlots = new Semaphore(BUFF_SIZE);

        // Create new Producer and Consumer thread instances
        Producer producer = new Producer(buffer, MAX_OPS, MAX_SLEEP, MAX_WAKE, fullSlots, emptySlots);
        Consumer consumer = new Consumer(buffer, MAX_OPS, MAX_SLEEP, MAX_WAKE, fullSlots, emptySlots);

        // Start execution of producer and consumer
        producer.start();
        consumer.start();
    }
}

abstract class ProdCons extends Thread {
    private final int maxOps;
    private final long maxSleep;
    protected final int maxWake;
    protected Semaphore fullSlots;
    protected Semaphore emptySlots;
    private Random rand;

    public ProdCons(int maxOps, long maxSleep, int maxWake, Semaphore fullSlots, Semaphore emptySlots) {
        this.maxOps = maxOps;
        this.maxSleep = maxSleep;
        this.maxWake = maxWake;
        this.fullSlots = fullSlots;
        this.emptySlots = emptySlots;
        this.rand = new Random();
    }

    protected int getOps() {
        return 1 + rand.nextInt(maxOps);
    }
    protected long getSleep() {
        return 1 + Math.abs(rand.nextLong()) % maxSleep;
    }
}

class Producer extends ProdCons {
    private int next_in;
    private final int[] buffer;

    public Producer(int[] buffer, int maxOps, long maxSleep, int maxWake, Semaphore fullSlots, Semaphore emptySlots) {
        super(maxOps, maxSleep, maxWake, fullSlots, emptySlots);
        this.next_in = 0;
        this.buffer = buffer;
    }

    public void run() {
        try {
            while (true) {
                int k1 = getOps();
                for(int i = 0; i < k1; i++) {
                    emptySlots.acquire();
                    int index = (next_in + i) % buffer.length;
                    if (buffer[index] < 0)
                        throw new Exception("Producer detected race condition");
                    buffer[index] += 1;
                    fullSlots.release();
                }
                next_in = (next_in + k1) % buffer.length;
                System.out.println("Producer OK");
                //noinspection BusyWait
                sleep(getSleep());
            }

        }
        catch (Exception e) {
            System.out.println("Exception in Producer: ");
        }
    }
}

class Consumer extends ProdCons {
    private int next_out;
    private final int[] buffer;

    public Consumer(int[] buffer, int maxOps, long maxSleep, int maxWake, Semaphore fullSlots, Semaphore emptySlots) {
        super(maxOps, maxSleep, maxWake, fullSlots, emptySlots);
        this.next_out = 0;
        this.buffer = buffer;
    }

    public void run() {
        try {
            for (int wakeCycles = 0; wakeCycles < maxWake; wakeCycles++) {
                sleep(getSleep());
                int k2 = getOps();
                for (int i = 0; i < k2; i++) {
                    fullSlots.acquire();
                    int index = (next_out + i) % buffer.length;
                    if (buffer[index] > 1)
                        throw new Exception("Consumer detected race condition");
                    buffer[index] -= 1;
                    emptySlots.release();
                }
                next_out = (next_out + k2) % buffer.length;
                System.out.println("Consumer OK: " + wakeCycles);
            }

            System.out.println("Consumer exits system without any race problems");
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("Exception in Consumer: " + e);
        }
    }
}