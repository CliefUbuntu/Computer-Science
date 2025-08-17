package ForkJoinDC;

import java.util.concurrent.ForkJoinPool;

public class SumAll {
	static long startTime = 0;
	
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	private static float toc(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
	static final ForkJoinPool fjPool = ForkJoinPool.commonPool();
	static int sum(int[] arr){
	  return fjPool.invoke(new SumArray(arr,0,arr.length));
	}
//this is for Java 7&8 
	//Java 8's static method ForkJoinPool.commonPool() is simpler
	public static void main(String[] args) {
		int max =1000000;
		int [] arr = new int[max];
		for (int i=0;i<max;i++) {
			arr[i]=1;
		}
		tick();
		int sumArr = sum(arr);
		float time = toc();
		System.out.println("Run took "+ time +" seconds");
		
		System.out.println("Sum is:");
		System.out.println(sumArr);
		tick();
		sumArr = sum(arr);
		time = toc();
		System.out.println("Second run took "+ time +" seconds");
		
		System.out.println("Sum is:");
		System.out.println(sumArr);
		
	}

}
