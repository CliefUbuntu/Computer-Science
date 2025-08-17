package safeCounterOption2;

public class TestCounterSafety {
	
	
	public static void main(String args[]) throws InterruptedException {
		int noThrds = 100;
		int addPerThread=100;
		Counter sharedCount= new Counter();
		
		CounterUpdateThread [] thrds= new CounterUpdateThread[noThrds];
		for (int i=0;i<noThrds;i++) {
			thrds[i]=new CounterUpdateThread(sharedCount,addPerThread);
		}
		for (int i=0;i<noThrds;i++) {
			thrds[i].start();
		}

		for (int i=0;i<noThrds;i++) {
			thrds[i].join();
		}

		int expectedVal = noThrds*addPerThread;
		System.out.println("Final value of counter is:" + sharedCount.get() + " and should be:" + expectedVal);
	}
}
