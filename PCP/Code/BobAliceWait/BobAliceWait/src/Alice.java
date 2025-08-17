import java.util.concurrent.atomic.AtomicBoolean;

public class Alice extends Thread {
	private AtomicBoolean aliceSaidHi;
	private AtomicBoolean bobSaidHi;
	
	
	Alice(AtomicBoolean aSH, AtomicBoolean bSH) {
		aliceSaidHi =aSH;
		bobSaidHi =bSH;
	}
	public void run() {
		System.out.println("Hi Bob!");
		synchronized (aliceSaidHi) {
			aliceSaidHi.set(true);
			aliceSaidHi.notify();}
		synchronized (bobSaidHi) {
		try {
			while(!bobSaidHi.get()) {
			bobSaidHi.wait();
			}
		} catch (InterruptedException e) {
		}
		}
		System.out.println("Bye  Bob!");
	}

}	