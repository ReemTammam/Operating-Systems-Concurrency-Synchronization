\# Barrier Implementation Using Semaphores from the explanation video on youtube



\## Concept

A barrier forces all threads to wait until every thread reaches

a synchronization point before any are allowed to proceed.



\## Idea

\- Each thread increments a shared counter.

\- The last arriving thread releases permits on a semaphore.

\- All waiting threads then pass the barrier.



\## Example Code



```java

import java.util.concurrent.Semaphore;



public class Main {

&nbsp;   public static void main(String\[] args) {

&nbsp;       for (int i = 0; i < 5; i++) {

&nbsp;           TestThread t = new TestThread(i);

&nbsp;           t.start();

&nbsp;       }

&nbsp;   }

}



class TestThread extends Thread {



&nbsp;   static Semaphore barrierSem = new Semaphore(0);

&nbsp;   static Semaphore countMutex = new Semaphore(1);

&nbsp;   static int count = 0;



&nbsp;   int tID;



&nbsp;   public TestThread(int id) {

&nbsp;       tID = id;

&nbsp;   }



&nbsp;   @Override

&nbsp;   public void run() {



&nbsp;       // update the count of threads that have started

&nbsp;       countMutex.acquireUninterruptibly();

&nbsp;       count++;



&nbsp;       // if this thread is the final one to arrive, release the barrier permits

&nbsp;       if (count == 5) {

&nbsp;           System.out.println("Thread " + tID +

&nbsp;                   " is the last thread to arrive and will open the barrier.");

&nbsp;           barrierSem.release(5);

&nbsp;       } else {

&nbsp;           System.out.println("Thread " + tID +

&nbsp;                   " has arrived and is waiting for the barrier to be opened.");

&nbsp;       }



&nbsp;       countMutex.release();



&nbsp;       barrierSem.acquireUninterruptibly();

&nbsp;       System.out.println("Thread " + tID + " made it past the barrier!");

&nbsp;   }

}



