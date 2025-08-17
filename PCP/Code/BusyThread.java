public class BusyThread extends java.lang.Thread {
	private int i;
	public static boolean ovenBusyLightOn=false;
	
	BusyThread(int i) { this.i = i; }
	
	public void run() {
		while (ovenBusyLightOn);
		ovenBusyLightOn=true;
		bakeCake();
		ovenBusyLightOn=false;
	}
	public void bakeCake() {
		System.out.println("Baker " + i+ " is baking!");
		System.out.println("Baker " + i+ " finished baking!");
	}

	public static void main(String[] args) {
		for(int i=1; i <= 10; ++i) {
			BusyThread c = new BusyThread(i);
			c.start();
		}
	}
}
