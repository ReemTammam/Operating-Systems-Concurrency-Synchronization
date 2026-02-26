import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {

    static int[] file = {1,2,3,4,5,6};

    static Semaphore mutex = new Semaphore(1);
    static Semaphore writer = new Semaphore(1);

    static int readcount = 0;

    static int N;





    public static void main(String[] args){

        Scanner input = new Scanner(System.in);
        System.out.print("How many reading agents?: ");
        int R = input.nextInt();
        System.out.print("How many coordinating agents?: ");
        int W = input.nextInt();
        System.out.print("How many reading agents are allowed to access the resource?: ");
        N = input.nextInt();

        //making reading agent threads
        for(int i =0; i<R; i++){
            Reader r = new Reader(file);
            r.start();
        }

        //making writing agent threads
        for(int i =0; i<W; i++){
            Writer w = new Writer(file);
            w.start();
        }






    }
}
class Reader extends Thread{
    int [] file;



    public Reader(int[] file) {
        this.file = Main.file;

    }

    @Override
    public void run() {

        //reader logic


        //doing the initial mutex acquire and release before letting reader in to read
            if (Main.N != 0) {
            Main.N--;
            while(!Main.mutex.hasQueuedThreads()) {

                if (Main.mutex.tryAcquire()) {

                        Main.readcount++;
                    if (Main.readcount == 1) {
                        while (!Main.writer.hasQueuedThreads()) {
                            if (Main.writer.tryAcquire()) {
                                Main.mutex.release();
                                for (int m : file) {
                                    if (m == file.length) {
                                        System.out.println("Reader has read the file");

                                    }
                                }
                                break;
                            }
                        }
                        break;
                    } else if (Main.readcount > 1) {
                        Main.mutex.release();
                        for (int m : file) {
                            if (m == file.length) {
                                System.out.println("Reader has read the file");
                            }
                        }
                        break;
                    }
                }
            }


            while (!Main.mutex.hasQueuedThreads()) {
                if (Main.mutex.tryAcquire()) {
                    Main.readcount--;
                    if (Main.readcount == 0) {
                        Main.writer.release();
                    }
                    Main.mutex.release();
                    break;
                }
            }


        }
    }


}
class Writer extends Thread {
    int [] file;

    public Writer(int[] file) {
        this.file = Main.file;
    }

    @Override
    public void run() {
        Random rand = new Random();

        //writer logic
        while(!Main.writer.hasQueuedThreads()) {
            if (Main.writer.tryAcquire()) {
                int fileLength = file.length;
                int fileNum = rand.nextInt(fileLength);
                for (int m : file) {
                    if (m == fileNum) {
                        file[m] = rand.nextInt(9);
                    }
                }
                System.out.println("Writer " +currentThread().getName() + Arrays.toString(file));
                Main.writer.release();
                break;
            }

        }


    }



}

