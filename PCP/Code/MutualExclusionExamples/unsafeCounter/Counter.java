package unsafeCounter;

public class Counter {
	private long value;
	Counter() {
		value=0;
	}
	 public long get() {
		return value;
	}
	public void set(long newVal) {
		value=newVal;
	}
	public void incr() {
		value++;
	}
}
