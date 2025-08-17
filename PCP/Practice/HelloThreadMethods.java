/**
 * HelloThreadMethods - Demonstrates thread behavior with different characteristics
 * 
 * This class extends Thread to create threads that can be:
 * - sleepy: sleeps for 1 second during execution
 * - polite: yields execution to other threads
 * 
 * Author: [Your Name]
 * Date: [Date]
 */
public class HelloThreadMethods extends java.lang.Thread {
    
    // Instance variables
    private int i;           // Thread identifier number
    private boolean sleepy;  // Whether this thread should sleep
    private boolean polite;  // Whether this thread should yield to others
    
    /**
     * Default constructor - creates a normal thread (not sleepy or polite)
     * @param i the thread identifier number
     */
    HelloThreadMethods(int i) {
        this(i, false, false);  // Call the full constructor with default values
    }
    
    /**
     * Full constructor - creates a thread with specified characteristics
     * @param i the thread identifier number
     * @param slpy whether the thread should sleep during execution
     * @param plt whether the thread should yield to other threads
     */
    HelloThreadMethods(int i, boolean slpy, boolean plt) {
        this.i = i;
        this.sleepy = slpy;
        this.polite = plt;
    }
    
    /**
     * The main execution method for each thread
     * Demonstrates different thread behaviors based on characteristics
     */
    public void run() {
        // Every thread says hello first
        System.out.println("Thread " + i + " says hi");
        
        // Polite threads yield execution to others
        if (polite) {
            Thread.yield();  // Thread more likely to finish last due to yielding
        }
        
        // Sleepy threads take a nap
        if (sleepy) {
            try {
                System.out.println("Thread " + i + " snoozing");
                sleep(1000); // Sleep for 1 second (1000 milliseconds)
            } catch (InterruptedException e) {
                // Handle interruption during sleep
                System.err.println("Thread " + i + " was interrupted while sleeping!");
                e.printStackTrace();
            }
        }
        
        // Every thread says goodbye before finishing
        System.out.println("Thread " + i + " says bye");
    }
    
    /**
     * Main method - creates and manages multiple threads with different behaviors
     * @param args command line arguments (not used)
     * @throws InterruptedException if main thread is interrupted while waiting
     */
    public static void main(String[] args) throws InterruptedException {
        
        // Configuration
        int noThrds = 10; // Number of threads to create
        
        // Create array to hold all thread objects
        HelloThreadMethods[] thrds = new HelloThreadMethods[noThrds];
        
        // Create and start all threads
        for (int i = 0; i < noThrds; ++i) {
            
            // Create different types of threads based on position
            if (i == 0) {
                // First thread is sleepy (will take longer to complete)
                thrds[i] = new HelloThreadMethods(i, true, false);
                System.out.println("Thread " + i + " is sleepy");
                
            } else if (i == (noThrds - 2)) {
                // Last thread is polite (will yield to others)
                thrds[i] = new HelloThreadMethods(i, false, true);
                System.out.println("Thread " + i + " is polite");
                
            } else {
                // All other threads are normal
                thrds[i] = new HelloThreadMethods(i);
            }
            
            // Start the thread (calls run() method in separate thread)
            thrds[i].start();
        }
        
        // Wait for all threads to complete before continuing
        for (int i = 0; i < noThrds; ++i) {
            thrds[i].join(); // Main thread waits for HelloThread i to finish
        }
        
        // All threads have completed
        System.out.println("We are all done!");
    }
}