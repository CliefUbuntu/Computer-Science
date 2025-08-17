public class HelloThread extends java.lang.Thread {
	private int i;
	private static String sharedString;

	
	HelloThread(int i) { this.i = i; }
	
	public void run() {
		System.out.println("Thread " + i + " says hi");
		sharedString="Thread " + i + " was here!";
		System.out.println("Thread " + i + " says bye");
	}
	
	public static void main(String[] args) {
		sharedString = "main thread string\n";
		for(int i=1; i <= 10; ++i) {
			HelloThread c = new HelloThread(i);
			c.start();
		}

		System.out.println(sharedString);
	}
}
