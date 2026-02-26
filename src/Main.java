import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {
    // Shared variables
    static int P;                    // number of philosophers
    static int M;                    // total meals to be eaten (shared)
    static int mealsRemaining;       // protected by mealsMutex
    static int totalMealsEaten = 0;  // protected by mealsMutex

    static Semaphore[] chopsticks;   // chopsticks array of semaphores

    // Protect shared meal counters
    static Semaphore mealsMutex = new Semaphore(1);

    // Deadlock prevention (semaphore-only)
    // At most P-1 philosophers can attempt to pick up chopsticks simultaneously.
    static Semaphore room;

    // Start barrier (enter together)
    static Semaphore startBarrier;
    static Semaphore startCountMutex = new Semaphore(1);
    static int startCount = 0;

    // End barrier (leave together)
    static Semaphore endBarrier;
    static Semaphore endCountMutex = new Semaphore(1);
    static int endCount = 0;

    public static void main(String[] args) {
        // Task 3: Manual command-line parsing
        if (args == null || args.length != 2 || !args[0].equals("-A")) {
            System.out.println("Error: Invalid arguments.");
            System.out.println("Usage: java Main -A 1   (Task 1: Dining Philosophers)");
            System.out.println("       java Main -A 2   (Task 2: Readers-Writers)");
            return;
        }

        if (args[1].equals("1")) {
            runTask1();
        } else if (args[1].equals("2")) {
            Task2Runner.runTask2();
        } else {
            System.out.println("Error: Invalid task number. Valid options are 1 or 2.");
        }
    }
    static void runTask1(){
        // Prompt user for P and M
        Scanner scanner = new Scanner(System.in);

        // Get valid P
        while (true) {
            System.out.print("Enter total number of philosophers: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Error: Please enter a valid integer.");
                scanner.next(); // clear invalid input
                continue;
            }

            P = scanner.nextInt();

            if (P < 1) {
                System.out.println("Error: Number of philosophers must be at least 1.");
            } else {
                break;
            }
        }

        // Get valid M
        while (true) {
            System.out.print("Enter total number of meals: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Error: Please enter a valid integer.");
                scanner.next();
                continue;
            }

            M = scanner.nextInt();

            if (M < 0) {
                System.out.println("Error: Number of meals cannot be negative.");
            } else if (P == 1 && M > 0) {
                System.out.println("Error: A single philosopher cannot eat because two chopsticks are required.");
            } else {
                break;
            }
        }

        mealsRemaining = M;

        // Create chopsticks
        chopsticks = new Semaphore[P];
        for (int i = 0; i < P; i++) {
            chopsticks[i] = new Semaphore(1);
        }

        // Create barriers
        startBarrier = new Semaphore(0);
        endBarrier = new Semaphore(0);

        // Deadlock prevention semaphore
        // If P == 1 we already guarded the impossible case (M>0); allow P==1, M==0.
        room = new Semaphore(Math.max(P - 1, 1));

        //start measuring time
        long startTime = System.nanoTime();

        Philosopher[] philosophers = new Philosopher[P];
        for (int i = 0; i < P; i++) {
            philosophers[i] = new Philosopher(i);
            philosophers[i].start();
        }

        // Wait for all threads so we can print final report
        for (int i = 0; i < P; i++) {
            try {
                philosophers[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        //measure end time
        long endTime = System.nanoTime();
        double runtimeMs = (endTime - startTime) / 1_000_000.0;

        // Final report
        System.out.println("\n=== FINAL REPORT ===");
        for (int i = 0; i < P; i++) {
            System.out.println("Philosopher " + i + " ate " + philosophers[i].mealsEatenByMe + " meals.");
        }
        System.out.println("Total meals eaten = " + totalMealsEaten);

        //print runtime
        System.out.println("Runtime in milliseconds = " + runtimeMs);
    }

    // Barrier to make sure all threads enter together
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

    static int leftChopstick(int id) {
        return id;
    }

    static int rightChopstick(int id) {
        return (id + 1) % P;
    }

    static int randomCycles(Random r) {
        // 3-6 cycles inclusive
        return 3 + r.nextInt(4);
    }

    static void doCycles(int cycles) {
        for (int i = 0; i < cycles; i++) {
            Thread.yield();
        }
    }
}

class Philosopher extends Thread {
    private final int id;
    int mealsEatenByMe = 0;

    private final Random rng;

    Philosopher(int id) {
        this.id = id;
        this.rng = new Random(System.nanoTime() + id);
    }

    @Override
    public void run() {
        // Etiquette: enter together
        Main.awaitStartBarrier(id);

        // Step 1
        System.out.println("Philosopher " + id + " sits down at the table. (Step 1)");

        while (true) {
            // If no meals remain, stop trying to eat (they may proceed to leave normally)
            Main.mealsMutex.acquireUninterruptibly();
            boolean done = (Main.mealsRemaining <= 0);
            Main.mealsMutex.release();
            if (done) break;

            int left = Main.leftChopstick(id);
            int right = Main.rightChopstick(id);

            // Deadlock prevention: limit contenders
            Main.room.acquireUninterruptibly();

            // Take a locked snapshot for clean logging (recommended)
            Main.mealsMutex.acquireUninterruptibly();
            int mealsSoFarBeforePickup = Main.totalMealsEaten;
            Main.mealsMutex.release();

            try {
                // Step 2: Pick up left chopstick (blocks if unavailable)
                Main.chopsticks[left].acquireUninterruptibly();
                System.out.println("Philosopher " + id + " picked up LEFT chopstick " + left
                        + ". (Step 2) Meals eaten so far=" + mealsSoFarBeforePickup);


                // Step 3: Pick up right chopstick (blocks if unavailable)
                Main.chopsticks[right].acquireUninterruptibly();
                System.out.println("Philosopher " + id + " picked up RIGHT chopstick " + right
                        + ". (Step 3) Meals eaten so far=" + mealsSoFarBeforePickup);

                // Now decide if we can actually eat a meal (protect shared meal counter)
                boolean willEat;
                Main.mealsMutex.acquireUninterruptibly();
                if (Main.mealsRemaining > 0) {
                    Main.mealsRemaining--;
                    Main.totalMealsEaten++;
                    mealsEatenByMe++;
                    willEat = true;
                } else {
                    willEat = false;
                }
                int eatenSoFarSnapshot = Main.totalMealsEaten;
                int remainingSnapshot = Main.mealsRemaining;
                Main.mealsMutex.release();

                if (willEat) {
                    // Step 4: Begin eating
                    System.out.println("Philosopher " + id + " begins EATING. (Step 4) TotalEaten="
                            + eatenSoFarSnapshot + " Remaining=" + remainingSnapshot);

                    // Step 5: Eat 3-6 cycles
                    Main.doCycles(Main.randomCycles(rng));
                }

                // Step 6: Put down left chopstick
                Main.chopsticks[left].release();
                System.out.println("Philosopher " + id + " put down LEFT chopstick " + left
                        + ". (Step 6) TotalEaten=" + eatenSoFarSnapshot);

                // Step 7: Put down right chopstick
                Main.chopsticks[right].release();
                System.out.println("Philosopher " + id + " put down RIGHT chopstick " + right
                        + ". (Step 7) TotalEaten=" + eatenSoFarSnapshot);

                // Step 8: Begin thinking
                System.out.println("Philosopher " + id + " begins THINKING. (Step 8) TotalEaten="
                        + eatenSoFarSnapshot + " Remaining=" + remainingSnapshot);

                // Step 9: Think 3-6 cycles
                Main.doCycles(Main.randomCycles(rng));

                // Step 10 is the loop condition (handled at top)

            } finally {
                // Always allow another philosopher to attempt pickup
                Main.room.release();
            }
        }

        // Etiquette: leave together
        Main.awaitEndBarrier(id);

        // Step 11
        System.out.println("Philosopher " + id + " leaves the table. (Step 11)");
    }
}