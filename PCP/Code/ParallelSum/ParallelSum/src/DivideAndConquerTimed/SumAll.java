package DivideAndConquerTimed;
public class SumAll {
	static long startTime = 0;
	static long endTime = 0;

	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	private static float toc(){
		endTime=System.currentTimeMillis(); 
		//return (endTime - startTime) / 1000.0f; 
		return (endTime - startTime);
	}
	static int sum(int[] arr) throws InterruptedException {
		   SumThread t = new SumThread(arr,0,arr.length);
		   t.run();
		   return t.ans;

		}

	
	public static void main(String[] args) {
		int max =1000000;
		//get correct number of processors
		 Runtime runtime = Runtime.getRuntime();
		 int nrOfProcessors = runtime.availableProcessors();
		System.out.println("Number of processors available: " + nrOfProcessors);
		
		int [] arr = new int[max];
		for (int i=0;i<max;i++) {
			arr[i]=100;
		}
		try {
			
		int sumArr = 0;
		int sCuttoff;
		for (sCuttoff=500;sCuttoff<=max/2;sCuttoff*=10) {
			System.out.println(sCuttoff+" sequential cuttoff");
			SumThread.SEQUENTIAL_CUTOFF=sCuttoff;
			for (int i=1;i<4;i++) {
				tick();
				sumArr = sum(arr);
				float time = toc();
				System.out.println("Run took "+ time +" milli seconds");
				System.out.println("Sum = "+sumArr);
			}
			System.out.println();
		}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
