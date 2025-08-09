
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FJcalc extends RecursiveTask<Integer>  {
	  int lo, hi, seek;
	  int[] arr;
	  static final int CUT=1000;
	  FJcalc(int[] a, int l, int h, int s) { lo=l; hi=h; arr=a;seek=s;}
	  
	  protected Integer compute(){
		  if((hi-lo) <= CUT) {
			  int ans = -1;
			  for(int i=hi-1; i >=lo; i--) {
				  if (arr[i]==seek) return i;
			  }
			  return ans;
		  }
		    else {
		    	FJcalc left = new FJcalc(arr,lo,(hi+lo)/2,seek);
		    	FJcalc right= new FJcalc(arr,(hi+lo)/2,hi,seek);
		    	left.fork(); 
		    	int rightAns = right.compute(); 
		    	int leftAns  = left.join();   
		    	if (rightAns>leftAns) return rightAns;
		    	return leftAns;     
		    }}
	  public static void main(String[] args) throws Exception {
			int max =8000;
			int [] arr = new int[max];
			for (int i=0;i<max;i++) { arr[i]=i%10;}
			final ForkJoinPool fjPool = ForkJoinPool.commonPool();
			int ans = fjPool.invoke(new FJcalc(arr,0,arr.length,8));
			System.out.println(ans);
	  }
}


