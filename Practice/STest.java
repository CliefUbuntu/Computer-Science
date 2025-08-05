class AThread extends Thread {
    public void run() {
        System.out.println("A");
    }
}

class BThread extends Thread {
    public void run() {
        System.out.println("B");
    }
}

public class STest {
    public static void main(String[] args) {
        AThread threadA = new AThread();
        BThread threadB = new BThread();
        threadA.start();
        threadB.start();
        System.out.println("C");
    }
}