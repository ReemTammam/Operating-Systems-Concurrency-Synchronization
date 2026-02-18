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
            System.out.println("Philosopher " + id + " is last to arrive. Opening start barrier.");
            startBarrier.release(P);
        } else {
            System.out.println("Philosopher " + id + " arrived; waiting at start barrier.");
        }
        startCountMutex.release();
        startBarrier.acquireUninterruptibly();
    }
}
class Philosopher extends Thread {

    int id;

    public Philosopher(int id) {
       this.id = id;
    }

    @Override
    public void run(){
        Main.awaitStartBarrier(id); // start barrier all must enter before anyone sits
        System.out.println("Thread " + id + " made it past the barrier!");
    }
}
