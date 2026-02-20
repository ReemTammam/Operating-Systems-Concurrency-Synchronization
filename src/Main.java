import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {
    //shared variables
    static int P;           //number of philosophers
    static int M;           //number of meals
    static Semaphore[] chopsticks;  //chopsticks are an array of semaphores

    //start semaphore barrier
    static Semaphore startBarrier;
    static Semaphore startCountMutex = new Semaphore(1);
    static int startCount = 0;

    //ens semaphore barrier
    static Semaphore endBarrier;
    static Semaphore endCountMutex = new Semaphore(1);
    static int endCount = 0;

    public static void main(String[] args) {
        //prompt user for P and M
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter total number of philosophers: ");
        P = scanner.nextInt();
        System.out.print("Enter total number of meals : ");
        M = scanner.nextInt();

        scanner.close();


        //input guards
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

        //create chopsticks
        chopsticks = new Semaphore[P];
        for (int i = 0; i < P; i++) {
            chopsticks[i] = new Semaphore(1);
        }

        //create starting barrier
        startBarrier = new Semaphore(0);

        //create ending barrier
        endBarrier = new Semaphore(0);

        //start philosopher threads
        for (int i = 0; i < P; i++) {
            Philosopher t = new Philosopher(i);
            t.start();
        }
    }

    //a barrier to ensure all threads enter together
    static void awaitStartBarrier(int id) {
        startCountMutex.acquireUninterruptibly();
        startCount++;
        if (startCount == P) {
            System.out.println("Philosopher " + (id + 1) + " is last to arrive. Opening start barrier.");
            startBarrier.release(P);
        } else {
            System.out.println("Philosopher " + (id + 1) + " arrived; waiting at start barrier.");
        }
        startCountMutex.release();
        startBarrier.acquireUninterruptibly();
    }

    static void awaitEndBarrier(int id) {
        endCountMutex.acquireUninterruptibly();
        endCount++;
        if (endCount == P) {
            System.out.println("Philosopher " + (id + 1) + " is last to finish. Opening end barrier.");
            endBarrier.release(P);
        } else {
            System.out.println("Philosopher " + (id + 1) + " finished and is waiting to leave.");
        }
        endCountMutex.release();
        endBarrier.acquireUninterruptibly();
    }


}
class Philosopher extends Thread {

    int id;
    Semaphore[] chopstick = Main.chopsticks;

    public Philosopher(int id) {
       this.id = id;
    }

    @Override
    public void run(){
        Main.awaitStartBarrier(id);

        System.out.println("Philosopher " + (id +1) + " sits down at the table.");

        // placeholder for dining logic
        //making sure there's still meals left
        Thread.yield();
        Thread.yield();
        while(Main.M > 0){
            try{
            //seeing if both chopsticks are available
            Semaphore cs1 = chopstick[id];
            Semaphore cs2;

                if (!((id + 1) < Main.P)) {
                    int newId = ((id + 1) % Main.P);
                    cs2 = chopstick[newId];
                } else {
                    cs2 = chopstick[id + 1];
                }

                if (cs1.availablePermits() != 0 && cs2.availablePermits() != 0) {

                    //getting the chopsticks to eat
                    cs1.acquire();
                    cs2.acquire();
                    Thread.yield();
                    Thread.yield();
                    if(Main.M > 0) {
                        System.out.println("Philosopher " + (id +1) + " is eating");
                        Main.M--;
                        Thread.yield();
                        Thread.yield();
                        Thread.yield();
                        Thread.yield();

                        //Philosopher finished eating releasing the chopsticks
                        System.out.println("Philosopher " + (id + 1) + " is finished eating and is thinking");

                        cs1.release();
                        cs2.release();
                        for(int i =0; i < 6; i++) {
                            Thread.yield();
                            Thread.yield();
                            Thread.yield();
                            Thread.yield();
                        }
                    }


                }

            }
            catch(InterruptedException e){
                throw new RuntimeException(e);
            }




        }

        Thread.yield();

        Main.awaitEndBarrier(id);

        System.out.println("Philosopher " + (id + 1) + " leaves the table.");

    }
}
