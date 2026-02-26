import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Task2 {

    static int[] file = {1, 2, 3, 4, 5, 6};

    // Classic RW protection
    static Semaphore mutex = new Semaphore(1);     // protects readcount + batch counters
    static Semaphore writer = new Semaphore(1);    // exclusive writer / excludes readers

    static int readcount = 0;

    // Required by task: max readers at once
    static int N;

    // NEW: enforce strict alternation: N readers -> 1 writer -> N readers -> ...
    static Semaphore readTurn;     // permits for current reader batch
    static Semaphore writeTurn;    // permit for writer after batch

    static int batchReadsDone = 0; // counts readers completed in current batch

    public static void run() { // <- changed from main() so Task 3 can call Task2.run()
        Scanner input = new Scanner(System.in);

        System.out.print("How many reading agents?: ");
        int R = input.nextInt();

        System.out.print("How many coordinating agents?: ");
        int W = input.nextInt();

        System.out.print("How many reading agents are allowed to access the resource?: ");
        N = input.nextInt();

        if (R <= 0 || W <= 0 || N <= 0) {
            System.out.println("Error: R, W, and N must all be >= 1.");
            return;
        }

        // start with N readers allowed to proceed
        readTurn = new Semaphore(N);
        writeTurn = new Semaphore(0);

        // making reading agent threads
        for (int i = 0; i < R; i++) {
            Reader r = new Reader(file);
            r.start();
        }

        // making writing agent threads
        for (int i = 0; i < W; i++) {
            Writer w = new Writer(file);
            w.start();
        }
    }

    // keep main if you want to run Task2 alone (optional)
    public static void main(String[] args) {
        run();
    }
}

class Reader extends Thread {
    int[] file;

    public Reader(int[] file) {
        this.file = Task2.file; // fixed (was Main.file)
    }

    @Override
    public void run() {

        // Wait until it's readers' phase (part of the next N readers)
        Task2.readTurn.acquireUninterruptibly();

        // Reader entry (classic readers-writers)
        Task2.mutex.acquireUninterruptibly();
        Task2.readcount++;
        if (Task2.readcount == 1) {
            Task2.writer.acquireUninterruptibly(); // first reader blocks writers
        }
        Task2.mutex.release();

        // "Read" (simulate with small yield loop)
        System.out.println("Reader " + getName() + " read: " + Arrays.toString(file));
        for (int i = 0; i < 3; i++) Thread.yield();

        // Reader exit
        Task2.mutex.acquireUninterruptibly();
        Task2.readcount--;
        if (Task2.readcount == 0) {
            Task2.writer.release(); // last reader unblocks writer
        }

        // Batch tracking for strict alternation
        Task2.batchReadsDone++;
        if (Task2.batchReadsDone == Task2.N) {
            Task2.batchReadsDone = 0;
            Task2.writeTurn.release(); // allow exactly one writer
        } else {
            Task2.readTurn.release(); // allow another reader in the same batch
        }

        Task2.mutex.release();
    }
}

class Writer extends Thread {
    int[] file;

    public Writer(int[] file) {
        this.file = Task2.file; // fixed (was Main.file)
    }

    @Override
    public void run() {
        Random rand = new Random();

        // Wait until it's writer phase
        Task2.writeTurn.acquireUninterruptibly();

        // Writer must be exclusive and only when no readers are active (writer semaphore ensures this)
        Task2.writer.acquireUninterruptibly();

        // "Write" (modify one random index)
        int idx = rand.nextInt(file.length);
        int newVal = rand.nextInt(10);
        file[idx] = newVal;

        System.out.println("Writer " + getName() + " updated index " + idx + " -> " + newVal
                + "  File=" + Arrays.toString(file));

        for (int i = 0; i < 3; i++) Thread.yield();

        Task2.writer.release();

        // After 1 writer, start next batch of N readers
        Task2.readTurn.release(Task2.N);
    }
}
