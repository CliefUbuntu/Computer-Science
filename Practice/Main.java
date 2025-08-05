class PrintNumbers implements Runnable {
    public void run() {
        for(int i = 1; i <= 5; i++) {
            System.out.println(i);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Runnable task = new PrintNumbers();
        Thread thread = new Thread(task);
        thread.start();
    }
}
