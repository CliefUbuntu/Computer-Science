// Import necessary libraries for image processing and parallel computing
import java.awt.image.BufferedImage;  // For handling image data
import java.io.File;                  // For file operations
import java.util.concurrent.ForkJoinPool;     // Thread pool for parallel execution
import java.util.concurrent.RecursiveAction;  // Base class for divide-and-conquer tasks
import javax.imageio.ImageIO;         // For reading/writing image files

/**
 * Parallel image processing class that applies a brightness threshold effect
 * Uses Fork-Join framework to process image regions in parallel
 */
public class ParallelTest1 extends RecursiveAction {

    // Image processing parameters
    private BufferedImage img;        // Source image to process
    private int x_start, x_stop;     // X-axis boundaries for this task's region
    private int y_start, y_stop;     // Y-axis boundaries for this task's region
    private BufferedImage dstImg;    // Destination image for results
    
    // Threshold for switching from parallel to sequential processing
    // If region area < sThreshold pixels, process sequentially
    protected static int sThreshold = 10000;
    
    /**
     * Constructor: Define the image region this task will process
     * @param img - source image
     * @param x_strt - starting X coordinate (inclusive)
     * @param x_stp - ending X coordinate (exclusive)
     * @param y_strt - starting Y coordinate (inclusive)
     * @param y_stp - ending Y coordinate (exclusive)
     * @param dst - destination image for processed results
     */
    public ParallelTest1(BufferedImage img, int x_strt, int x_stp, int y_strt, 
                        int y_stp, BufferedImage dst) {
        this.img = img;
        x_start = x_strt; 
        x_stop = x_stp;
        y_start = y_strt; 
        y_stop = y_stp;
        this.dstImg = dst;
    }

    /**
     * Sequential image processing method
     * Applies brightness threshold effect to assigned pixel region
     */
    protected void work() {
        int R, G, B, clr;  // Variables for RGB color components and combined color
        
        // Process each pixel in the assigned rectangular region
        for (int y = y_start; y < y_stop; y++) {           // Iterate through rows
            for (int x = x_start; x < x_stop; x++) {       // Iterate through columns
                
                // Extract pixel color from source image
                clr = img.getRGB(x, y);  
                
                // Extract individual RGB components using bit manipulation
                R = (clr >> 16) & 0xff;  // Red: shift right 16 bits, mask lower 8 bits
                G = (clr >> 8) & 0xff;   // Green: shift right 8 bits, mask lower 8 bits
                B = (clr) & 0xff;        // Blue: mask lower 8 bits directly
                
                // Apply brightness threshold effect
                // Boost any color component to minimum value of 200 (brightening effect)
                R = Math.max(R, 200);    // Ensure red >= 200
                G = Math.max(G, 200);    // Ensure green >= 200
                B = Math.max(B, 200);    // Ensure blue >= 200
                
                // Reconstruct color value from modified RGB components
                // Format: 0xAARRGGBB (Alpha=0xFF, Red, Green, Blue)
                clr = (0xff000000) | (R << 16) | (G << 8) | B;
                
                // Write processed pixel to destination image
                dstImg.setRGB(x, y, clr);
            }
        }
    }

    /**
     * Fork-Join framework method: decides whether to split task or process directly
     */
    protected void compute() {
        // Calculate area of current region
        int regionArea = (x_stop - x_start) * (y_stop - y_start);
        
        // Base case: if region is small enough, process sequentially
        if (regionArea < sThreshold) {
            work();  // Process this region directly
            return;
        }
        
        // Recursive case: split region vertically (divide X-axis)
        int splt = (x_stop - x_start) / 2;  // Find midpoint of X range
        
        // Create two subtasks for left and right halves
        ParallelTest1 left = new ParallelTest1(img, x_start, x_start + splt, 
                                              y_start, y_stop, dstImg);
        ParallelTest1 right = new ParallelTest1(img, x_start + splt, x_stop,
                                               y_start, y_stop, dstImg);
        
        // Execute subtasks in parallel
        left.fork();        // Queue left task for parallel execution
        right.compute();    // Execute right task in current thread
        left.join();        // Wait for left task to complete
    }

    /**
     * Main method: Load image, process it in parallel, save result
     */
    public static void main(String[] args) throws Exception {
        // Load source image from file
        File f = new File("Images/image5.jpeg");            
        BufferedImage img = ImageIO.read(f);
        
        // Get image dimensions
        int w = img.getWidth();   // Image width in pixels
        int h = img.getHeight();  // Image height in pixels
        
        // Create destination image with same dimensions
        // TYPE_INT_ARGB supports transparency and full color range
        BufferedImage dstImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Execute parallel image processing
        ForkJoinPool pool = ForkJoinPool.commonPool();  // Get shared thread pool
        // Create main task covering entire image (0,0) to (w,h)
        pool.invoke(new ParallelTest1(img, 0, w, 0, h, dstImg));
        
        // Save processed image to file
        File dstFile = new File("Images/output5_threshold.png");
        ImageIO.write(dstImg, "png", dstFile);  // Write as PNG format
    }
}