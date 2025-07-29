package unsafeCounterInt;

public class Counter {
	private int value;
	Counter() {
		value=0;
	}
	 public int get() {
		return value;
	}
	public void set(int newVal) {
		value=newVal;
	}
	public void incr() {
		value++;
	}
}
