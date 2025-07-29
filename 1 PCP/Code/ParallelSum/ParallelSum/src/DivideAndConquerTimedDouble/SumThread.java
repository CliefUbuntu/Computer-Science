package DivideAndConquerTimedDouble;

public class SumThread extends Thread {
	  int lo; // arguments
	  int hi;
	  double[] arr;
	  static int SEQUENTIAL_CUTOFF=500;

	 double ans = 0; // result 
	    
	  SumThread(double[] a, int l, int h) { 
	    lo=l; hi=h; arr=a;
	  }


	  public void run() { //override must have this type
		  if((hi-lo) < SEQUENTIAL_CUTOFF)
		      for(int i=lo; i < hi; i++)
		        ans += arr[i];
		    else {
		    	try {
		    		SumThread left = new SumThread(arr,lo,(hi+lo)/2);
		    		SumThread right= new SumThread(arr,(hi+lo)/2,hi);
		    		// order of next 4 lines
		    		// essential - why?
		    		left.start();
		    		right.run();  //Q: why run and not start?
		    		left.join();
		    		 ans = left.ans + right.ans;
		      }
		      catch (InterruptedException e) {
					
					e.printStackTrace();
				}
		     
		    }
		  }

	  }


