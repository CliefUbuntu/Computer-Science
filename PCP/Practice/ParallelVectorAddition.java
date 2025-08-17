import java.util.concurrent.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class VecAdd extends RecursiveAction {
    static final int SEQUENTIAL_CUTOFF = 1000; // Define the cutoff threshold
    static final AtomicInteger taskCounter = new AtomicInteger(0);
    static final Set<String> threadsUsed = ConcurrentHashMap.newKeySet();
    
    int lo; 
    int hi; 
    int[] res; 
    int[] arr1; 
    int[] arr2;
    int taskId;
    
    VecAdd(int l, int h, int[] r, int[] a1, int[] a2) {
        lo = l; 
        hi = h; 
        res = r; 
        arr1 = a1; 
        arr2 = a2;
        taskId = taskCounter.incrementAndGet();
    }
    
    protected void compute() {
        String threadName = Thread.currentThread().getName();
        threadsUsed.add(threadName);
        
        System.out.printf("Task %d: Thread %s processing range [%d, %d]\n", 
                         taskId, threadName, lo, hi);
        
        if (hi - lo < SEQUENTIAL_CUTOFF) {
            // Sequential processing
            System.out.printf("Task %d: Sequential processing %d elements\n", 
                             taskId, hi - lo);
            for (int i = lo; i < hi; i++) {
                res[i] = arr1[i] + arr2[i];
            }
        } else {
            // Parallel processing
            int mid = (hi + lo) / 2;
            System.out.printf("Task %d: Splitting range [%d, %d] at %d\n", 
                             taskId, lo, hi, mid);
            
            VecAdd left = new VecAdd(lo, mid, res, arr1, arr2);
            VecAdd right = new VecAdd(mid, hi, res, arr1, arr2);
            
            left.fork();       // run left task in parallel
            right.compute();   // run right task in current thread
            left.join();       // wait for left to finish
        }
    }
    
    // Reset static counters for fresh runs
    static void reset() {
        taskCounter.set(0);
        threadsUsed.clear();
    }
}

public class ParallelVectorAddition {
    
    public static void main(String[] args) {
        // System information
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("=== SYSTEM INFORMATION ===");
        System.out.println("Available CPU cores: " + cores);
        
        // Create ForkJoinPool
        ForkJoinPool pool = new ForkJoinPool();
        System.out.println("ForkJoinPool parallelism level: " + pool.getParallelism());
        System.out.println("ForkJoinPool thread factory: " + pool.getFactory().getClass().getSimpleName());
        
        // Test with different array sizes
        int[] sizes = {500, 2000, 10000, 50000};
        
        for (int size : sizes) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("TESTING WITH ARRAY SIZE: " + size);
            System.out.println("=".repeat(50));
            
            // Reset counters
            VecAdd.reset();
            
            // Create test arrays
            int[] arr1 = new int[size];
            int[] arr2 = new int[size];
            int[] result = new int[size];
            
            // Fill arrays with test data
            for (int i = 0; i < size; i++) {
                arr1[i] = i;
                arr2[i] = i * 2;
            }
            
            System.out.println("\nStarting parallel vector addition...");
            
            // Measure execution time
            long startTime = System.nanoTime();
            
            // Create and execute the parallel task
            VecAdd task = new VecAdd(0, size, result, arr1, arr2);
            pool.invoke(task);
            
            long endTime = System.nanoTime();
            double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
            
            // Verify results (check first few and last few elements)
            System.out.println("\n--- RESULTS VERIFICATION ---");
            boolean correct = true;
            for (int i = 0; i < Math.min(5, size); i++) {
                int expected = arr1[i] + arr2[i];
                if (result[i] != expected) {
                    correct = false;
                    break;
                }
            }
            
            if (correct) {
                System.out.println("✓ Results verified correct!");
                System.out.printf("Sample results: [%d, %d, %d, ...]\n", 
                                 result[0], result[1], result[2]);
            } else {
                System.out.println("✗ Results incorrect!");
            }
            
            // Performance and thread usage summary
            System.out.println("\n--- PERFORMANCE SUMMARY ---");
            System.out.printf("Execution time: %.2f ms\n", executionTime);
            System.out.println("Total tasks created: " + VecAdd.taskCounter.get());
            System.out.println("Threads used: " + VecAdd.threadsUsed.size());
            System.out.println("Thread names: " + VecAdd.threadsUsed);
            
            // Calculate theoretical speedup
            if (VecAdd.threadsUsed.size() > 1) {
                System.out.printf("Threads utilized: %d out of %d available cores\n", 
                                 VecAdd.threadsUsed.size(), cores);
            } else {
                System.out.println("Sequential execution (array too small for parallel benefit)");
            }
        }
        
        // Test sequential vs parallel comparison
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SEQUENTIAL vs PARALLEL COMPARISON (Size: 100,000)");
        System.out.println("=".repeat(60));
        
        int compareSize = 100000;
        int[] arr1 = new int[compareSize];
        int[] arr2 = new int[compareSize];
        int[] parallelResult = new int[compareSize];
        int[] sequentialResult = new int[compareSize];
        
        // Fill test arrays
        for (int i = 0; i < compareSize; i++) {
            arr1[i] = i;
            arr2[i] = i * 3;
        }
        
        // Sequential execution
        long seqStart = System.nanoTime();
        for (int i = 0; i < compareSize; i++) {
            sequentialResult[i] = arr1[i] + arr2[i];
        }
        long seqEnd = System.nanoTime();
        double seqTime = (seqEnd - seqStart) / 1_000_000.0;
        
        // Parallel execution
        VecAdd.reset();
        long parStart = System.nanoTime();
        VecAdd parallelTask = new VecAdd(0, compareSize, parallelResult, arr1, arr2);
        pool.invoke(parallelTask);
        long parEnd = System.nanoTime();
        double parTime = (parEnd - parStart) / 1_000_000.0;
        
        // Results
        System.out.printf("Sequential time: %.2f ms\n", seqTime);
        System.out.printf("Parallel time: %.2f ms\n", parTime);
        System.out.printf("Speedup: %.2fx\n", seqTime / parTime);
        System.out.printf("Efficiency: %.1f%% (threads used: %d)\n", 
                         (seqTime / parTime) / VecAdd.threadsUsed.size() * 100, 
                         VecAdd.threadsUsed.size());
        
        // Shutdown pool
        pool.shutdown();
        System.out.println("\nForkJoinPool shutdown complete.");
    }
}