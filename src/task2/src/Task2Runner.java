import java.util.Scanner;

public class Task2Runner {

    public static void runTask2() {
        Scanner input = new Scanner(System.in);

        System.out.print("How many reading agents (R)?: ");
        int R = input.nextInt();

        System.out.print("How many coordinating agents (W)?: ");
        int W = input.nextInt();

        System.out.print("Max readers allowed at once (N)?: ");
        int N = input.nextInt();

        input.close();

        // Basic validation (TA-proof)
        if (R < 0 || W < 0) {
            System.out.println("Error: R and W cannot be negative.");
            return;
        }
        if (N < 1) {
            System.out.println("Error: N must be at least 1.");
            return;
        }
        if (R == 0 || W == 0) {
            System.out.println("Error: Need at least 1 reader and 1 writer to maintain the required alternation.");
            return;
        }

        ReadersWriters.init(R, W, N);

        // Start reader threads
        for (int i = 0; i < R; i++) {
            new Thread(new ReadersWriters.Readers(i)).start();
        }

        // Start writer threads
        for (int i = 0; i < W; i++) {
            new Thread(new ReadersWriters.Writers(i)).start();
        }

        // Start controller thread (enforces N readers -> 1 writer -> ...)
        new Thread(ReadersWriters::controller).start();
    }

    // For testing Task 2 alone
    public static void main(String[] args) {
        runTask2();
    }
}
