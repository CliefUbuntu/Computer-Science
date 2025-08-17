public class HelloThreadMethods extends java.lang.Thread {
	private int i;
	private boolean sleepy;
	private boolean polite;
	
	HelloThreadMethods(int i) { 
		this(i, false, false);
		}
	
	HelloThreadMethods(int i, boolean slpy, boolean plt) { 
		this.i = i; 
		sleepy=slpy;
		polite=plt;
		}
	public void run() {
		System.out.println("Thread " + i + " says hi");
		if(polite) yield(); //thread more likely to finish last
		if(sleepy)
			try {
				System.out.println("Thread " + i + " snoozing");
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		System.out.println("Thread " + i + " says bye");
	}
	
	public static void main(String[] args) throws InterruptedException {
		int noThrds=10;
		HelloThreadMethods [] thrds = new HelloThreadMethods[noThrds];
		for(int i=0; i < noThrds; ++i) {
			
			 if (i==0)  thrds[i] = new HelloThreadMethods(i,true,false); //first thread is slow
			 else if(i==(noThrds-1)) thrds[i] = new HelloThreadMethods(i,false,true); //last thread is polite
			 else  thrds[i] = new HelloThreadMethods(i);
			 thrds[i].start();
		}
		for(int i=0; i < noThrds; ++i) {
			thrds[i].join(); //main thread waits for HelloThread i
		}
		System.out.println("we are all done");
	}
}
