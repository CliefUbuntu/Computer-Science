package Attempt2;
public class SumAll {

	public static void main(String[] args) {
		int max =100000;
		int noThreads =4;
		int [] arr = new int[max];
		for (int i=0;i<max;i++) {
			arr[i]=1;
		}
		try {
		int sumArr = sum(arr,noThreads);
		System.out.println("Sum is:");
		System.out.println(sumArr);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static int sum(int[] arr, int numTs) throws InterruptedException {
		  int ans = 0;
		  SumThread[] ts = new SumThread[numTs];
		  for(int i=0; i < numTs; i++){
			  ts[i] = new SumThread(arr,(i*arr.length)/numTs,
		                             ((i+1)*arr.length)/numTs);
			  ts[i].start();  //start, not run
		  }
		  for(int i=0; i < numTs; i++) { 
		    ans += ts[i].ans;
		  }
		  return ans;
	}

	


}
