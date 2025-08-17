package dEmbraceIllustrated;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class AThread extends Thread {
	private int id;
	private Object lock1;
	private Object lock2;
	static private Random snooze_time= new Random();
	
	AThread(int n, Object l1, Object l2) {
		this.id = n;
		lock1=l1;
		lock2=l2;
	}
	public void run() {
	    	System.out.println("Thread "+id+" --- outside critical section.");
	    	synchronized (lock1) {
	    		System.out.println("Thread "+id+" --- got first lock.");
	    		synchronized (lock2) {
	    			System.out.println("Thread "+id+" --- got second lock");
	    		}
	    	}
	    System.out.println("Thread "+id+" out of critical section.");
	}
}	