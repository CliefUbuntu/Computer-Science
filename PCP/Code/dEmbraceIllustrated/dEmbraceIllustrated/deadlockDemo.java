package dEmbraceIllustrated;
public class deadlockDemo {
	static private Object lock1 = new Object();
	static private Object lock2 = new Object();

	public static void main(String[] args) {
		AThread thread1 = new AThread(0,lock1,lock2);
		AThread thread2 = new AThread(1,lock2,lock1); //switched order, NOTE
	
		System.out.println("Starting simulation with 2 threads");
		thread1.start(); // start thread
		thread2.start(); // start thread
		try {
			thread1.join();
			thread2.join();
		} catch (InterruptedException e) {//not doing anything	
		}
		System.out.println("No deadlock this time - Parent thread completed");
	}
}
