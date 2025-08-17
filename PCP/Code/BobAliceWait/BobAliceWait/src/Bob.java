import java.util.concurrent.atomic.AtomicBoolean;

public class Bob extends Thread {
	private AtomicBoolean aliceSaidHi;
	private AtomicBoolean bobSaidHi;
	
	Bob(AtomicBoolean aSH, AtomicBoolean bSH) {
		aliceSaidHi =aSH;
		bobSaidHi =bSH;
	}
	public void run() {
		System.out.println("Hi Alice!");

		synchronized (bobSaidHi) { 
			bobSaidHi.set(true);
			bobSaidHi.notify();}
		synchronized (aliceSaidHi) {
		try {
			while(!aliceSaidHi.get()) 
			aliceSaidHi.wait();
		} catch (InterruptedException e) {
		}}
		System.out.println("Bye  Alice!");
	}

}	