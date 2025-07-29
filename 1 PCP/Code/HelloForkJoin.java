
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

// RecursiveAction has no return value, RecursiveTask returns a value
public class HelloForkJoin extends RecursiveAction  { 
	  int greetings; // arguments
	  int offset;

	  HelloForkJoin(int g, int start) { 
	    greetings=g;
	    offset=start;
	  }

	  protected void compute(){
		  if((greetings) <=1) { //only one task left, do it. This cutoff would be bigger for proper programs
			  System.out.println("hello"+offset );
		  }
		    else {
		    		int split=(int) (greetings/2.0);
		    		//split work into two
		    		HelloForkJoin left = new HelloForkJoin(split,offset);  //first half
		    		HelloForkJoin right= new HelloForkJoin(greetings-split,offset+split ); //second half
		    		left.fork(); //give first half to new threas

		    	    right.compute(); //do second half in this thread	
		    	    left.join();

		    }
		  }
	  public static void main(String[] args) {
			HelloForkJoin sayhello = new HelloForkJoin(100,0); //the task to be done, divide and conquer
			ForkJoinPool pool  = ForkJoinPool.commonPool(); //the default pool of worker threads
			pool.invoke(sayhello); //start everything running - give the task to the pool
		}
	  }


