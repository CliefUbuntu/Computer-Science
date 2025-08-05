public class HelloThread extends Thread {
  private int i;
  HelloThread(int i) { this.i = i; }

  public void run() {
    System.out.println("Thread " + i + " says hi");
    System.out.println("Thread " + i + " says bye");
  }

  public static void main(String[] args) {
    for (int i = 1; i <= 10; ++i) {
      new HelloThread(i).start();
    }
  }
}
