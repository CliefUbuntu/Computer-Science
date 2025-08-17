package safeCounterOption2;

public class Counter {
	private long value;
	
	Counter() {
		value=0;
	}
	 public synchronized long get() {
		return value;
	}

	public synchronized void set(long newVal) {
		value=newVal;
	}
	
	public synchronized void incr() {
		value++;
	}
}
