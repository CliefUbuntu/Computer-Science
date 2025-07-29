package Attempt3;

public class SumThread extends Thread {
	  int lo; // arguments
	  int hi;
	  int[] arr;

	  int ans = 0; // result 
	    
	  SumThread(int[] a, int l, int h) { 
	    lo=l; hi=h; arr=a;
	  }

	  public void run() { //override must have this type
	    for(int i=lo; i < hi; i++)
	      ans += arr[i];
	  }

}
