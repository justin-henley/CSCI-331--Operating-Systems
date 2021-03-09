import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class JustinHenleyCSCI331Proj2 {
    // Buffer size
    public static final int BUFF_SIZE = 100;
    // Maximum operations by a thread on each wake cycle
    public static final int MAX_OPS = 200;
    // Maximum sleep time by a thread between wake cycles
    public static final long MAX_SLEEP = 10;



    public static void main(String[] args) {
        int[] buffer = new int[BUFF_SIZE];
        Semaphore fullSlots = new Semaphore(0);
        Semaphore emptySlots = new Semaphore(BUFF_SIZE);

        Producer producer = new Producer(buffer, MAX_OPS, MAX_SLEEP, fullSlots, emptySlots);
        Consumer consumer = new Consumer(buffer, MAX_OPS, MAX_SLEEP, fullSlots, emptySlots);
        producer.start();
        consumer.start();

        while (consumer.isAlive() && producer.isAlive()) {
            System.out.println("Waiting on producer and consumer");
            try {
                //noinspection BusyWait
                sleep(MAX_SLEEP);

            }
            catch (Exception e) {
                System.out.println("eh?");
            }

        }
        System.out.println("Finished!");
    }
}

abstract class ProdCons extends Thread {
    private final int maxOps;
    private final long maxSleep;
    protected Semaphore fullSlots;
    protected Semaphore emptySlots;
    private Random rand;

    public ProdCons(int maxOps, long maxSleep, Semaphore fullSlots, Semaphore emptySlots) {
        this.maxOps = maxOps;
        this.maxSleep = maxSleep;
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
    private int[] buffer;

    public Producer(int[] buffer, int maxOps, long maxSleep, Semaphore fullSlots, Semaphore emptySlots) {
        super(maxOps, maxSleep, fullSlots, emptySlots);
        this.next_in = 0;
        this.buffer = buffer;
    }

    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while(true) {
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
    private int[] buffer;

    public Consumer(int[] buffer, int maxOps, long maxSleep, Semaphore fullSlots, Semaphore emptySlots) {
        super(maxOps, maxSleep, fullSlots, emptySlots);
        this.next_out = 0;
        this.buffer = buffer;
    }

    public void run() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                //noinspection BusyWait
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
            }
        }
        catch (Exception e) {
            System.out.println("Exception in Consumer: " + e);
        }
    }
}