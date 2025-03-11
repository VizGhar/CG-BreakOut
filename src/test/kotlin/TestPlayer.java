import java.util.Scanner;

public class TestPlayer {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            int remainingTurns = scanner.nextInt();
            int paddleX = scanner.nextInt();
            int ballX = scanner.nextInt();
            int ballAngle = scanner.nextInt();
            int n = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < n; i++) {
                scanner.nextLine();
            }
            System.out.println("300");
        }
    }
}
