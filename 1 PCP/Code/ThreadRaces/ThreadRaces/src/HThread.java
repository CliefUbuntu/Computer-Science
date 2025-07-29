import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class HThread implements Runnable {
	private BufferedImage img;
    private int x_start;
    private int x_stop;
    private int y_start;
    private int y_stop;
    private Color c;
    private int id;
    private static int counter=0;
    private CountDownLatch done;
    private CyclicBarrier middle_barrier;
    
    public HThread(int x, int xend, int y, int yend, 
    		BufferedImage img, Color c, CountDownLatch dL,
    		CyclicBarrier mB) {
    	this.img=img;
    	x_start=x;
    	x_stop=xend;
    	y_start = y;
    	y_stop = yend;
    	this.c=c;
    	id=counter;
    	counter++;
    	done=dL;
    	middle_barrier=mB;
    }
   
    public void run() {
        int dpixel = c.getRGB();
        int r, g, b;
		r = (dpixel>>16) & 0xff; //extract red component, put in list
        g = (dpixel>> 8) & 0xff;//extract green component, put in list
        b = (dpixel)& 0xff;//extract blue component, put in list

        int red, green, blue;
        int middle=(x_stop-x_start)/2;
        
        for (int x=x_start;x<x_stop;x++) {
    		for (int y=y_start;y<y_stop;y++) {
    			int p = img.getRGB(x,y);
	   			red = (p>>16) & 0xff; //extract red component, put in list
             	green = (p>> 8) & 0xff;//extract green component, put in list
             	blue= (p)& 0xff;//extract blue component, put in list


	  			dpixel = (0xff000000)
 	                       | ((r+red) << 16)
 	                       | ((g+green)<< 8)
 	                       | (b+blue); 
	   	//	synchronized (img) { //this will really slow things down
   				img.setRGB(x, y, dpixel); //race condition, but appears to be synchronized
    		//	}
    			
    	}
    		try {
				Thread.sleep((id+1)*20);
				if (id==0) {Thread.yield();}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if (x==middle)
				try {
					middle_barrier.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	}
        done.countDown();
    }
}
