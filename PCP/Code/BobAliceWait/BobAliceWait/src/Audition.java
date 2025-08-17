import java.util.concurrent.atomic.AtomicBoolean;

// class to show effect of interleaving
public class Audition {
	public static void main(String[] args) {
		AtomicBoolean bSH = new AtomicBoolean(false);
		AtomicBoolean aSH = new AtomicBoolean(false);
		
		Bob bob = new Bob(aSH,bSH);
		Alice alice = new Alice(aSH,bSH);
		bob.start(); 
		alice.start(); //in Java threads don't run until you start them
		//main program should actually wait until threads are don't, but not doing that yet
		
	}
}

