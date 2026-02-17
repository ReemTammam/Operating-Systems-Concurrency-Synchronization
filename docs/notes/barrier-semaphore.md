\# Barrier Implementation Using Semaphores from the explanation video on youtube



\## Concept

A barrier forces all threads to wait until every thread reaches

a synchronization point before any are allowed to proceed.



\## Idea

\- Each thread increments a shared counter.

\- The last arriving thread releases permits on a semaphore.

\- All waiting threads then pass the barrier.



\## Example Code in java



import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            TestThread t = new TestThread(i);
            t.start();
        }
    }
}

class TestThread extends Thread {

    static Semaphore barrierSem = new Semaphore(0);
    static Semaphore countMutex = new Semaphore(1);
    static int count = 0;

    int tID;

    public TestThread(int id) {
        tID = id;
    }

    @Override
    public void run() {

        // update the count of threads that have started
        countMutex.acquireUninterruptibly();
        count++;

        // if this thread is the final one to arrive, release the barrier permits
        if (count == 5) {
            System.out.println("Thread " + tID +
                    " is the last thread to arrive and will open the barrier.");
            barrierSem.release(5);
        } else {
            System.out.println("Thread " + tID +
                    " has arrived and is waiting for the barrier to be opened.");
        }

        countMutex.release();

        barrierSem.acquireUninterruptibly();
        System.out.println("Thread " + tID + " made it past the barrier!");
    }
}
