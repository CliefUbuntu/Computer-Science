package SerialSumDouble;
public class SerialSumTimed {

	static double sum(double[] arr)  {
		double ans = 0;
		for(int i=0; i < arr.length; i++)
			ans += arr[i];
		return ans;
		}
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
	
	public static void main(String[] args) {
		int max =10000000;
		double [] arr = new double[max];
		double sumArr;
		for (int i=0;i<max;i++) {  // for checking purposes
			arr[i]=1.0;
		}
		for (int i=1;i<4;i++) {
			tick();
			sumArr = sum(arr);
			
			float time = toc();
			System.out.println("Adding " +max+ " doubles serially took "+ time +" milliseconds");
			System.out.println("Sum = "+sumArr);
		}
		
	}

}
