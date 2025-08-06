import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * HelloMany - Demonstrates the Fork/Join Framework for parallel computation
 * 
 * The Fork/Join framework is designed for problems that can be broken down
 * recursively into smaller subproblems (divide-and-conquer approach).
 * 
 * Key Components:
 * - ForkJoinPool: A thread pool optimized for fork/join tasks
 * - RecursiveAction: For tasks that don't return a result (like this one)
 * - RecursiveTask<T>: For tasks that return a result
 * 
 * Work-Stealing Algorithm:
 * - Each thread has its own work queue (deque)
 * - When a thread runs out of work, it "steals" work from other threads
 * - This provides excellent load balancing and utilization
 * 
 * @author [Your Name]
 */
public class HelloMany extends RecursiveAction {
    
    // Instance variables for this task
    private int greetings;  // Total number of greetings to print
    private int offset;     // Starting number for this task's greetings
    
    // Threshold for when to stop dividing work (can be tuned for performance)
    private static final int THRESHOLD = 1;
    
    /**
     * Constructor for creating a HelloMany task
     * @param g Total number of greetings this task should handle
     * @param start The starting offset number for greetings
     */
    HelloMany(int g, int start) {
        this.greetings = g;
        this.offset = start;
    }
    
    /**
     * The main computation method - this is where the divide-and-conquer happens
     * 
     * Fork/Join Pattern:
     * 1. Check if task is small enough to solve directly (base case)
     * 2. If not, divide the problem into subtasks
     * 3. Fork one subtask (run asynchronously)
     * 4. Compute the other subtask directly
     * 5. Join (wait for) the forked subtask to complete
     */
    @Override
    protected void compute() {
        
        // BASE CASE: If work is small enough, do it directly
        if (greetings <= THRESHOLD) {
            // Sequential execution - just print the greeting
            System.out.println("hello" + offset + " [Thread: " + 
                             Thread.currentThread().getName() + "]");
            return; // Task complete
        }
        
        // RECURSIVE CASE: Divide the work into smaller subtasks
        
        // Calculate how to split the work
        int split = greetings / 2;
        
        // Create two subtasks:
        // Left task: handles first half of greetings
        HelloMany leftTask = new HelloMany(split, offset);
        
        // Right task: handles remaining greetings, starting from offset + split
        HelloMany rightTask = new HelloMany(greetings - split, offset + split);
        
        /*
         * FORK/JOIN PATTERN:
         * 
         * Option 1 (used here): Fork left, compute right, join left
         * - leftTask.fork() - submit left task to thread pool (asynchronous)
         * - rightTask.compute() - execute right task in current thread
         * - leftTask.join() - wait for left task to complete
         */
        
        // Fork the left subtask (runs asynchronously in thread pool)
        leftTask.fork();
        
        // Execute the right subtask directly in current thread
        // This is more efficient than forking both tasks
        rightTask.compute();
        
        // Wait for the forked left subtask to complete
        // join() blocks until the task finishes and handles any exceptions
        leftTask.join();
        
        /*
         * Alternative patterns you could use:
         * 
         * Option 2: Fork both tasks
         * leftTask.fork();
         * rightTask.fork();
         * leftTask.join();
         * rightTask.join();
         * 
         * Option 3: Use invokeAll() for multiple tasks
         * invokeAll(leftTask, rightTask);
         */
    }
    
    /**
     * Main method - demonstrates how to use the Fork/Join framework
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        
        // Configuration
        final int TOTAL_GREETINGS = 8;  // Total number of greetings to print
        final int STARTING_OFFSET = 0;  // Starting number
        
        System.out.println("=== Fork/Join Framework Demo ===");
        System.out.println("Printing " + TOTAL_GREETINGS + " greetings using parallel divide-and-conquer");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println();
        
        // Create the main task that encompasses all the work
        HelloMany mainTask = new HelloMany(TOTAL_GREETINGS, STARTING_OFFSET);
        
        // Create the ForkJoinPool - the specialized thread pool for fork/join tasks
        // By default, it creates as many threads as there are available processors
        ForkJoinPool pool = new ForkJoinPool();
        
        System.out.println("ForkJoinPool parallelism level: " + pool.getParallelism());
        System.out.println("Starting parallel execution...\n");
        
        // Record start time for performance measurement
        long startTime = System.currentTimeMillis();
        
        // Execute the task and wait for completion
        // invoke() submits the task and blocks until it completes
        pool.invoke(mainTask);
        
        // Record end time
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n=== Execution Complete ===");
        System.out.println("Execution time: " + (endTime - startTime) + "ms");
        System.out.println("Pool statistics:");
        System.out.println("- Active thread count: " + pool.getActiveThreadCount());
        System.out.println("- Pool size: " + pool.getPoolSize());
        System.out.println("- Steal count: " + pool.getStealCount());
        
        // Shutdown the pool (good practice)
        pool.shutdown();
        
        /*
         * EXECUTION TREE VISUALIZATION:
         * 
         * For HelloMany(8, 0):
         * 
         *                    HelloMany(8,0)
         *                   /             \
         *              fork(4,0)       compute(4,4)
         *             /        \       /         \
         *        fork(2,0)  compute(2,2)  fork(2,4)  compute(2,6)
         *        /     \    /      \     /     \     /      \
         *   fork(1,0) (1,1) fork(1,2) (1,3) fork(1,4) (1,5) fork(1,6) (1,7)
         *   
         * Each leaf node prints: "hello[number]"
         * 
         * The actual execution order depends on thread scheduling and work-stealing,
         * so the output order may vary between runs.
         */
    }
}
