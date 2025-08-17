
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParaExam extends RecursiveTask<Integer>  {
	  int lo, hi;
	  int[] arr;
	  static final int CUT=5000;
	
	  ParaExam(int[] a, int l, int h) { lo=l; hi=h; arr=a;}

	  protected Integer compute(){
		  if((hi-lo) < CUT) {
			  int ans = 0;
			  for(int i=lo; i < hi; i++) ans += arr[i];
			  return ans;
		  }
		    else {
		    	ParaExam left = new ParaExam(arr,lo,(hi+lo)/2);
		    	ParaExam right= new ParaExam(arr,(hi+lo)/2,hi);
		    	left.fork(); 
		    	int rightAns = right.compute(); 
		    	int leftAns  = left.join();   
		    	return leftAns + rightAns;     
		    }}

	  public static void main(String[] args) throws Exception {
			int max =10000000;
			int [] arr = new int[max];
			for (int i=0;i<max;i++) { arr[i]=2; }
			final ForkJoinPool fjPool = ForkJoinPool.commonPool();
			int ans = fjPool.invoke(new ParaExam(arr,0,arr.length));
			System.out.println(ans);
	  }
}


