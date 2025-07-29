public class HelloThreadWithObject extends java.lang.Thread {
	private int i;
	private static String sharedString;
	HelloThreadWithObject(int i) { this.i = i; }
	
	public void run() {
		System.out.println("Thread " + i + " says hi");
		sharedString="Thread " + i + " was here!";
		System.out.println("Thread " + i + " says bye");
	}
	public static void main(String[] args) throws InterruptedException {
		sharedString = "main thread string\n";
		for(int i=1; i <= 10; ++i) {
			HelloThreadWithObject c = new HelloThreadWithObject(i);
			c.start();
		}
	
		System.out.println("The last value is:" +sharedString);
		System.out.println("The last value is:" +sharedString);

	}
}
