package safeCounterOption1;

public class CounterUpdateThread extends Thread {
	
	Counter sharedCount;
	int rep;
	
	CounterUpdateThread(Counter c,int repeats) {
		sharedCount= c;
		rep=repeats;
	}
	public void run() {
		for (int i=0;i<rep;i++) {
			sharedCount.incr();
			yield();
		}
	}
}
