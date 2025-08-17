package Attempt3;
public class SumAll {
	static long startTime = 0;
	static long endTime = 0;

	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	private static float toc(){
		endTime=System.currentTimeMillis(); 
		return (endTime - startTime);
	}
	
	static long sum(int[] arr, int numTs) throws InterruptedException {
		  long ans = 0;
		  SumThread[] ts = new SumThread[numTs];
		  for(int i=0; i < numTs; i++){
			  ts[i] = new SumThread(arr,(i*arr.length)/numTs,
		                             ((i+1)*arr.length)/numTs);
			  ts[i].start();  //[1] why start, not run
		  }
		  for(int i=0; i < numTs; i++) { 
			ts[i].join(); 
		    ans += ts[i].ans;
		  }
		  return ans;
		}
	
	public static void main(String[] args) {
		int max =10000000;
		int noThreads =4;
		int [] arr = new int[max];
		for (int i=0;i<max;i++) {
			arr[i]=1;
		}
		try {
			long sumArr;
			for (int i=1;i<4;i++) {
				tick();
				sumArr = sum(arr,noThreads);
				float time = toc();
				System.out.println("Adding " +max+ " integers in parallel took "+ time +" milliseconds");
				System.out.println("Sum = "+sumArr);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
