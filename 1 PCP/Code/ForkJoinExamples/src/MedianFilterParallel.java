//MedianFilterParallel.java
//M. Kuttel 2022

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import javax.imageio.ImageIO;



public class MedianFilterParallel extends RecursiveAction {

    private BufferedImage img;
    private int x_start;
    private int x_stop;
    private int y_start;
    private int y_stop;
    private BufferedImage dstImage;
    private int windowWidth= 15; // Processing window size, should be odd.
    protected static int sThreshold = 10000;
    
    public MedianFilterParallel(BufferedImage img,  int x_strt, int x_stp, int y_strt, 
    		int y_stp, int wWidth, BufferedImage dstImage) {
        this.img = img;
        x_start = x_strt;
        x_stop= x_stp;
        y_start = y_strt;
        y_stop= y_stp;
        this.dstImage = dstImage;
        windowWidth = wWidth;
    }

    // Average pixels from source, write results into destination.
    protected void computeDirectly() {
        int surroundPixels = (windowWidth - 1) / 2;
        int buffA[] = new int[windowWidth * windowWidth]; //alpha value
       int buffR[] = new int[windowWidth * windowWidth]; //red values
       int buffG[] = new int[windowWidth * windowWidth]; //green values
       int buffB[] = new int[windowWidth * windowWidth]; //blue values

       for (int y = y_start; y < y_stop; y++) {
           for (int x = x_start; x < x_stop; x++) {
               int count = 0;
               //cycle through window pixels
                for (int m = y-surroundPixels; m <= y+surroundPixels; m++) {
                    for (int n = x-surroundPixels; n <= x+surroundPixels; n++) {
                   	          	 
                     	 
                   int  clr   = img.getRGB(n, m);  //fix this
                   buffA[count] = (clr>> 24) & 0xff; //extract alpha component, put in list                 
                   buffR[count] = (clr>>16) & 0xff; //extract red component, put in list
                	buffG[count] = (clr>> 8) & 0xff;//extract green component, put in list
                	buffB[count] = (clr)& 0xff;//extract blue component, put in list
                   count++;
               }}
               
               /** sort buff array */
                
               java.util.Arrays.sort(buffA);
               java.util.Arrays.sort(buffR);
               java.util.Arrays.sort(buffG);
               java.util.Arrays.sort(buffB);
               
               
               int a=buffA[count/2];
               int r=buffR[count/2];
               int g=buffG[count/2];
               int b=buffB[count/2];
               
               // Set destination pixel to median
               // Re-assemble destination pixel.
             int dpixel = (0xff000000)
               		| (a << 24)
                       | (r << 16)
                       | (g<< 8)
                       | b; 

             dstImage.setRGB(x, y, dpixel);
           }
       }
    }


    //this is 
    @Override
    protected void compute() {
    //	computeDirectly(); //hack for now
    	int w=x_stop-x_start;
    	int h=y_stop-y_start;
        if (w*h < sThreshold) {
            computeDirectly();
            return;
        }

        int splitW = w / 2;
        //System.out.println("Splitting in 2 of size " +splitW);
       // int splitH = = (y_stop-y_start) / 2;
        MedianFilterParallel left = new MedianFilterParallel(img, x_start,x_start+splitW, 
        		y_start, y_stop, windowWidth, dstImage);
        MedianFilterParallel right = new MedianFilterParallel(img, x_start+splitW, x_stop,
        		y_start, y_stop, windowWidth, dstImage);
        //add in here
	/*	SumArray left = new SumArray(arr,lo,(hi+lo)/2);
		
		SumArray right= new SumArray(arr,(hi+lo)/2,hi);*/
		left.fork(); //this
	    right.compute(); //order
	    left.join();   //is very
	    return;      //important.
    	
    }

 
    
    // Main method
    public static void main(String[] args) throws Exception {
    	
    	String srcName = "Images/image1.jpg";
    	String dstName = "Images/outputMedian.png";
        int windowWidth = 5; // window size, should be odd.
    	
		//deal with command line arguments if provided
		if (args.length==2) {
			srcName = args[0];  //input file name
			dstName =args[1]; // output file name
			} 
		else if (args.length==3) {
				srcName = args[0];  //input file name
				dstName =args[1]; // output file name
				windowWidth=Integer.parseInt(args[2]); // windowWidth provided
		}
		System.out.println("Using filter of size "+windowWidth + "x"+windowWidth );
        File f=new File(srcName);            
        BufferedImage img=ImageIO.read(f);
        int w = img.getWidth();
        int h = img.getHeight();
        System.out.println("Image size=" +(w*h));
         BufferedImage dstImage =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
  
        int surroundPixels = (windowWidth - 1) / 2;
        int processors = Runtime.getRuntime().availableProcessors();
        
        System.out.println(Integer.toString(processors) + " processor"
                + (processors != 1 ? "s are " : " is ")
                + "available");
  
        MedianFilterParallel fb = new MedianFilterParallel(img, surroundPixels, w-surroundPixels,surroundPixels, h-surroundPixels, windowWidth, dstImage);

        ForkJoinPool pool = ForkJoinPool.commonPool();

        //timing
        long startTime = System.currentTimeMillis();
        pool.invoke(fb);
        long endTime = System.currentTimeMillis();
        System.out.println("Run1 parallel median filter took " + (endTime - startTime) + 
                " milliseconds  with threshold " + sThreshold);
        dstImage =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        fb = new MedianFilterParallel(img, surroundPixels, w-surroundPixels,surroundPixels,
        		h-surroundPixels, windowWidth, dstImage);
        startTime = System.currentTimeMillis();
        pool.invoke(fb);
        endTime = System.currentTimeMillis();
        System.out.println("Run2 parallel median filter took " + (endTime - startTime) + 
                " milliseconds  with threshold " + sThreshold);

       // BufferedImage blurredImage = blur(image);
        

        File dstFile = new File(dstName);
        System.out.println("Writing median filtered image...");
        ImageIO.write(dstImage, "png", dstFile);
        System.out.println("Done");
        
    }
}
