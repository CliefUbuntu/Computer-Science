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
    public static void main(String[] args) throws InterruptedException {
        AThread threadA = new AThread();
        BThread threadB = new BThread();
        
        threadA.start();
        threadB.start();
        
        threadB.join();
        
        System.out.println("C");
    }
}