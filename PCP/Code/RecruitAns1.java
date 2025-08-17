import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
public class  RecruitAns1 extends Thread {
		private int id;
		private static CountDownLatch latch;
		private static AtomicBoolean first= new AtomicBoolean(true);

		RecruitAns1(int n) { id=n;}
	
		public void run() {
			try {
				latch.await();
				sleep(100);
				synchronized (first) {
					if (first.get()) {
						//this.sleep(1000); //remove!!!!
						first.set(false);
						System.out.println("Private " + id + " won!");
					}
					else System.out.println("Private " + id + " lost!");
				}
			} catch (InterruptedException e) {}
		}
	
	public static void main(String[] args) {
		latch= new CountDownLatch(1); //Q3a
		RecruitAns1[] soldiers = new RecruitAns1[5];
		for (int i=0;i<5;i++) { 
			soldiers[i]=new RecruitAns1(i); 
			soldiers[i].start();
		}
		System.out.println("Ready, set, go!");
		latch.countDown();
	}
}
