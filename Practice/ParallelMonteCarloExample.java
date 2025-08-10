import java.util.Random;
import java.util.concurrent.*;

/**
 * Parallel Monte Carlo Pi Estimation Example
 * Demonstrates the performance difference between shared Random vs ThreadLocalRandom
 */

/**
 * Monte Carlo task using SHARED Random (slower due to thread contention)
 */
class MonteCarloSharedRandom extends RecursiveTask<Long> {
    private static final Random SHARED_RANDOM = new Random(42); // Shared among ALL threads
    private static final int THRESHOLD = 100000;
    
    private final long samples;
    private final int taskId;
    
    public MonteCarloSharedRandom(long samples, int taskId) {
        this.samples = samples;
        this.taskId = taskId;
    }
    
    @Override
    protected Long compute() {
        if (samples <= THRESHOLD) {
            return computeDirectly();
        }
        
        // Split the work
        long half = samples / 2;
        MonteCarloSharedRandom left = new MonteCarloSharedRandom(half, taskId * 2);
        MonteCarloSharedRandom right = new MonteCarloSharedRandom(samples - half, taskId * 2 + 1);
        
        left.fork();
        Long rightResult = right.compute();
        Long leftResult = left.join();
        
        return leftResult + rightResult;
    }
    
    private Long computeDirectly() {
        long insideCircle = 0;
        String threadName = Thread.currentThread().getName();
        
        System.out.printf("Task %d on %s: Processing %d samples with SHARED Random\n", 
                         taskId, threadName, samples);
        
        // This is where the bottleneck occurs - all threads compete for the same Random
        synchronized (SHARED_RANDOM) {  // We need this to prevent corruption
            for (long i = 0; i < samples; i++) {
                double x = SHARED_RANDOM.nextDouble() * 2 - 1;  // Range: -1 to 1
                double y = SHARED_RANDOM.nextDouble() * 2 - 1;  // Range: -1 to 1
                
                if (x * x + y * y <= 1.0) {
                    insideCircle++;
                }
            }
        }
        
        return insideCircle;
    }
}

/**
 * Monte Carlo task using ThreadLocalRandom (faster, no contention)
 */
class MonteCarloThreadLocal extends RecursiveTask<Long> {
    private static final int THRESHOLD = 100000;
    
    private final long samples;
    private final int taskId;
    
    public MonteCarloThreadLocal(long samples, int taskId) {
        this.samples = samples;
        this.taskId = taskId;
    }
    
    @Override
    protected Long compute() {
        if (samples <= THRESHOLD) {
            return computeDirectly();
        }
        
        // Split the work
        long half = samples / 2;
        MonteCarloThreadLocal left = new MonteCarloThreadLocal(half, taskId * 2);
        MonteCarloThreadLocal right = new MonteCarloThreadLocal(samples - half, taskId * 2 + 1);
        
        left.fork();
        Long rightResult = right.compute();
        Long leftResult = left.join();
        
        return leftResult + rightResult;
    }
    
    private Long computeDirectly() {
        long insideCircle = 0;
        String threadName = Thread.currentThread().getName();
        
        System.out.printf("Task %d on %s: Processing %d samples with ThreadLocalRandom\n", 
                         taskId, threadName, samples);
        
        // Each thread has its own Random instance - NO synchronization needed!
        for (long i = 0; i < samples; i++) {
            double x = ThreadLocalRandom.current().nextDouble(-1, 1);  // Range: -1 to 1
            double y = ThreadLocalRandom.current().nextDouble(-1, 1);  // Range: -1 to 1
            
            if (x * x + y * y <= 1.0) {
                insideCircle++;
            }
        }
        
        return insideCircle;
    }
}

/**
 * Performance comparison and demonstration
 */
public class ParallelMonteCarloExample {
    
    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        long totalSamples = 10_000_000L; // 10 million samples
        
        System.out.println("=== PARALLEL MONTE CARLO PI ESTIMATION ===");
        System.out.println("Available cores: " + cores);
        System.out.println("Total samples: " + totalSamples);
        System.out.println("Estimating π using random points in a unit circle...\n");
        
        ForkJoinPool pool = new ForkJoinPool();
        
        // Test 1: Using shared Random (slower)
        System.out.println("🔴 TEST 1: Using SHARED Random (with synchronization bottleneck)");
        System.out.println("-".repeat(70));
        
        long startTime = System.nanoTime();
        MonteCarloSharedRandom sharedTask = new MonteCarloSharedRandom(totalSamples, 1);
        Long insideCircleShared = pool.invoke(sharedTask);
        long sharedTime = System.nanoTime() - startTime;
        
        double piEstimateShared = 4.0 * insideCircleShared / totalSamples;
        
        System.out.printf("\n📊 SHARED Random Results:\n");
        System.out.printf("   Points inside circle: %,d / %,d\n", insideCircleShared, totalSamples);
        System.out.printf("   π estimate: %.6f (error: %.6f)\n", piEstimateShared, Math.abs(piEstimateShared - Math.PI));
        System.out.printf("   Execution time: %.2f ms\n\n", sharedTime / 1_000_000.0);
        
        // Test 2: Using ThreadLocalRandom (faster)
        System.out.println("🟢 TEST 2: Using ThreadLocalRandom (no synchronization needed)");
        System.out.println("-".repeat(70));
        
        startTime = System.nanoTime();
        MonteCarloThreadLocal threadLocalTask = new MonteCarloThreadLocal(totalSamples, 1);
        Long insideCircleThreadLocal = pool.invoke(threadLocalTask);
        long threadLocalTime = System.nanoTime() - startTime;
        
        double piEstimateThreadLocal = 4.0 * insideCircleThreadLocal / totalSamples;
        
        System.out.printf("\n📊 ThreadLocalRandom Results:\n");
        System.out.printf("   Points inside circle: %,d / %,d\n", insideCircleThreadLocal, totalSamples);
        System.out.printf("   π estimate: %.6f (error: %.6f)\n", piEstimateThreadLocal, Math.abs(piEstimateThreadLocal - Math.PI));
        System.out.printf("   Execution time: %.2f ms\n\n", threadLocalTime / 1_000_000.0);
        
        // Performance comparison
        System.out.println("⚡ PERFORMANCE COMPARISON");
        System.out.println("=".repeat(50));
        double speedup = (double) sharedTime / threadLocalTime;
        double improvement = ((double) (sharedTime - threadLocalTime) / sharedTime) * 100;
        
        System.out.printf("ThreadLocalRandom is %.2fx faster\n", speedup);
        System.out.printf("Performance improvement: %.1f%%\n", improvement);
        
        if (speedup > 1.5) {
            System.out.println("✅ Significant performance improvement with ThreadLocalRandom!");
        } else {
            System.out.println("ℹ️  Improvement may vary based on system load and thread count");
        }
        
        // Demonstrate individual thread usage
        System.out.println("\n🧵 THREAD-SPECIFIC RANDOM DEMONSTRATION");
        System.out.println("=".repeat(50));
        demonstrateThreadSpecificRandom();
        
        pool.shutdown();
    }
    
    /**
     * Shows how each thread gets its own Random instance with ThreadLocalRandom
     */
    private static void demonstrateThreadSpecificRandom() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        for (int i = 0; i < 4; i++) {
            final int threadId = i;
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                
                // Each thread gets its own Random instance automatically
                ThreadLocalRandom random = ThreadLocalRandom.current();
                
                System.out.printf("Thread %s (ID:%d): Random instance = %s\n", 
                                 threadName, threadId, random.toString().substring(0, 50) + "...");
                
                // Generate some random numbers
                System.out.printf("Thread %s: Random numbers = [%.3f, %.3f, %.3f]\n", 
                                 threadName, random.nextDouble(), random.nextDouble(), random.nextDouble());
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}