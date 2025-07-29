package DivideAndConquer;
public class SumAll {

	static int sum(int[] arr, int numTs) throws InterruptedException {
		   SumThread t = new SumThread(arr,0,arr.length);
		   t.run();
		   return t.ans;
		}

	
	public static void main(String[] args) {
		int max =100000;
		int noThreads =4;
		int [] arr = new int[max];
		for (int i=0;i<max;i++) {
			arr[i]=10000;
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

}
