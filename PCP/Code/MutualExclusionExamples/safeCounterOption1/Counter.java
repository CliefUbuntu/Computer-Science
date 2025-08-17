package safeCounterOption1;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {
	private AtomicLong value;
	Counter() {
		value=new AtomicLong(0);
	}		
	 public long get() {
		return value.get();
	}
	public void set(long val) {
		value.set(val);
	}
	public void incr() {
		value.getAndIncrement();
	}
}
