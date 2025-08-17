public class OutputEg extends Thread {
	
	String outS;
	public OutputEg(String outS) {
		this.outS = outS;
	}
	
	public void run() {System.out.print(outS); }

	public static void main(String[] args) throws InterruptedException {
		OutputEg threadA = new OutputEg("A");
		OutputEg threadB = new OutputEg("B");
		threadA.start();
		threadB.start();
		threadA.join();
		System.out.print("C");
	}
}



