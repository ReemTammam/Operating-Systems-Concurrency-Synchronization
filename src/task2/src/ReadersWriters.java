import java.util.concurrent.Semaphore;

public class ReadersWriters {

    // N = maximum number of reading-agents allowed at once
    static int max;

    // Phase control semaphores (enforce pattern)
    static Semaphore rControl;                 // controller releases permits for readers
    static Semaphore wControl = new Semaphore(0); // controller releases exactly 1 permit for writers

    // Completion signals back to controller (no busy waiting)
    static Semaphore readersDone = new Semaphore(0);
    static Semaphore writerDone = new Semaphore(0);

    // Readers/Writers correctness:
    // - multiple readers allowed
    // - writer exclusive
    // - writer only when no readers active
    static Semaphore countMutex = new Semaphore(1);
    static Semaphore writerLock = new Semaphore(1);
    static int activeReaders = 0;

    // Stop condition: continue pattern until every reader OR every writer has completed
    static Semaphore remainingMutex = new Semaphore(1);
    static int readersRemaining;
    static int writersRemaining;

    // Initialize once from runner after reading R, W, N
    public static void init(int R, int W, int N) {
        if (R < 0 || W < 0) {
            throw new IllegalArgumentException("R and W cannot be negative.");
        }
        if (N < 1) {
            throw new IllegalArgumentException("N must be at least 1.");
        }

        max = N;
        readersRemaining = R;
        writersRemaining = W;

        rControl = new Semaphore(0); // controller starts by releasing the first batch
        wControl = new Semaphore(0);
        readersDone = new Semaphore(0);
        writerDone = new Semaphore(0);

        countMutex = new Semaphore(1);
        writerLock = new Semaphore(1);
        activeReaders = 0;

        remainingMutex = new Semaphore(1);
    }

    // Controller enforces: N readers -> 1 writer -> N readers -> 1 writer -> ...
    // Final reader batch can be smaller than N.
    public static void controller() {
        while (true) {
            remainingMutex.acquireUninterruptibly();
            boolean stop = (readersRemaining == 0) || (writersRemaining == 0);
            int batchSize = Math.min(max, readersRemaining);
            remainingMutex.release();

            if (stop) break;

            // Allow next batch of readers (batchSize can be < max near the end)
            rControl.release(batchSize);

            // Wait until exactly that many readers finish
            readersDone.acquireUninterruptibly(batchSize);

            remainingMutex.acquireUninterruptibly();
            stop = (readersRemaining == 0) || (writersRemaining == 0);
            remainingMutex.release();

            if (stop) break;

            // Allow exactly one writer
            wControl.release(1);

            // Wait for that writer to finish
            writerDone.acquireUninterruptibly(1);
        }

        System.out.println("\n[Controller] Done: all readers OR all writers completed.");
        System.out.println("[Controller] Readers remaining=" + readersRemaining
                + ", Writers remaining=" + writersRemaining);
    }

    public static class Readers implements Runnable {
        private final int readerID;

        public Readers(int tID) {
            this.readerID = tID;
        }

        @Override
        public void run() {
            // Each reader does exactly ONE read phase, when allowed by controller
            rControl.acquireUninterruptibly();

            // Reader entry: first reader blocks writers
            countMutex.acquireUninterruptibly();
            activeReaders++;
            if (activeReaders == 1) {
                writerLock.acquireUninterruptibly();
            }
            countMutex.release();

            System.out.println("Reader " + readerID + " is reading.");
            for (int i = 0; i < (int) (Math.random() * 3) + 3; i++) {
                Thread.yield();
            }
            System.out.println("Reader " + readerID + " has finished reading.");

            // Reader exit: last reader unblocks writers
            countMutex.acquireUninterruptibly();
            activeReaders--;
            if (activeReaders == 0) {
                writerLock.release();
            }
            countMutex.release();

            // Mark completion
            remainingMutex.acquireUninterruptibly();
            readersRemaining--;
            remainingMutex.release();

            // Signal controller that one reader completed
            readersDone.release();
        }
    }

    public static class Writers implements Runnable {
        private final int writerID;

        public Writers(int tID) {
            this.writerID = tID;
        }

        @Override
        public void run() {
            // Each writer does exactly ONE coordination phase, when allowed by controller
            wControl.acquireUninterruptibly();

            // Exclusive access, only when no readers are active
            writerLock.acquireUninterruptibly();

            System.out.println("Writer " + writerID + " is coordinating (writing).");
            for (int i = 0; i < (int) (Math.random() * 3) + 3; i++) {
                Thread.yield();
            }
            System.out.println("Writer " + writerID + " has finished coordinating (writing).");

            writerLock.release();

            // Mark completion
            remainingMutex.acquireUninterruptibly();
            writersRemaining--;
            remainingMutex.release();

            // Signal controller that one writer completed
            writerDone.release();
        }
    }
}
