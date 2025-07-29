package Attempt1;
public class SumAll {

	static int sum(int[] arr, int numTs)  {
		  int ans = 0;
		  SumThread[] ts = new SumThread[numTs];  
		  for(int i=0; i < numTs; i++){  // do parallel computations
			  ts[i] = new SumThread(arr,(i*arr.length)/numTs,
		                             ((i+1)*arr.length)/numTs);
		  }
		  for(int i=0; i < numTs; i++) { // combine results
		    ans += ts[i].ans;
		  }
		  return ans;
		}

	public static void main(String[] args) {
		int max =100000;
		int noThreads =4;
		int [] arr = new int[max];
		for (int i=0;i<max;i++) {  // for checking purposes
			arr[i]=1;
		}
		int sumArr = sum(arr,noThreads);
		System.out.println("Sum is:");
		System.out.println(sumArr);
		
	}

}
