import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {
    // Shared variables
    static int P;                    // number of philosophers
    static int M;                    // total meals (shared)
    static Semaphore[] chopsticks;   // chopsticks array of semaphores

    // Start barrier (enter together)
    static Semaphore startBarrier;
    static Semaphore startCountMutex = new Semaphore(1);
    static int startCount = 0;

    // End barrier (leave together)
    static Semaphore endBarrier;
    static Semaphore endCountMutex = new Semaphore(1);
    static int endCount = 0;

    public static void main(String[] args) {
        // Prompt user for P and M
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter total number of philosophers: ");
        P = scanner.nextInt();
        System.out.print("Enter total number of meals: ");
        M = scanner.nextInt();
        scanner.close();

        // Input guards
        if (P < 1) {
            System.out.println("Error: Number of philosophers must be at least 1.");
            return;
        }
        if (M < 0) {
            System.out.println("Error: Number of meals cannot be negative.");
            return;
        }
        // Q&A edge case
        if (P == 1 && M > 0) {
            System.out.println("Error: A single philosopher cannot eat because two chopsticks are required.");
            return;
        }

        // Create chopsticks
        chopsticks = new Semaphore[P];
        for (int i = 0; i < P; i++) {
            chopsticks[i] = new Semaphore(1);
        }

        // Create barriers
        startBarrier = new Semaphore(0);
        endBarrier = new Semaphore(0);

        // Start philosopher threads (store them so we can join later if needed)
        Philosopher[] philosophers = new Philosopher[P];
        for (int i = 0; i < P; i++) {
            philosophers[i] = new Philosopher(i);
            philosophers[i].start();
        }

        // (Optional for later) Join threads so main waits for completion.
        // We'll enable this when we start timing runtime for the report.
        /*
        for (int i = 0; i < P; i++) {
            try {
                philosophers[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        */
    }

    // Barrier to ensure all threads enter together
    static void awaitStartBarrier(int id) {
        startCountMutex.acquireUninterruptibly();
        startCount++;

        if (startCount == P) {
            System.out.println("Philosopher " + id + " is last to arrive. Opening start barrier.");
            startBarrier.release(P);
        } else {
            System.out.println("Philosopher " + id + " arrived; waiting at start barrier.");
        }

        startCountMutex.release();
        startBarrier.acquireUninterruptibly();
    }

    // Barrier to ensure all threads leave together
    static void awaitEndBarrier(int id) {
        endCountMutex.acquireUninterruptibly();
        endCount++;

        if (endCount == P) {
            System.out.println("Philosopher " + id + " is last to finish. Opening end barrier.");
            endBarrier.release(P);
        } else {
            System.out.println("Philosopher " + id + " finished and is waiting to leave.");
        }

        endCountMutex.release();
        endBarrier.acquireUninterruptibly();
    }
}

class Philosopher extends Thread {
    private final int id;

    Philosopher(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        // Etiquette: everyone enters together
        Main.awaitStartBarrier(id);

        // Step 1 output (required by spec)
        System.out.println("Philosopher " + id + " sits down at the table. (Step 1)");

        // Placeholder for dining logic (Steps 2–10 will be added next)
        Thread.yield();

        // Etiquette: everyone leaves together
        Main.awaitEndBarrier(id);

        // Step 11 output (required by spec)
        System.out.println("Philosopher " + id + " leaves the table. (Step 11)");
    }
}
