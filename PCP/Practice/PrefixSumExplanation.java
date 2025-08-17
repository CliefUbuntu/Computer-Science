import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * COMPREHENSIVE EXPLANATION OF THE PREFIX SUM PROBLEM
 * 
 * WHAT IS PREFIX SUM?
 * Given: input = [3, 1, 4, 2, 5]
 * Want:  output[i] = sum of all elements from input[0] to input[i]
 * 
 * Result: output = [3, 4, 8, 10, 15]
 *         output[0] = 3           (just input[0])
 *         output[1] = 3+1 = 4     (input[0] + input[1])
 *         output[2] = 3+1+4 = 8   (input[0] + input[1] + input[2])
 *         output[3] = 3+1+4+2 = 10 (input[0] + input[1] + input[2] + input[3])
 *         output[4] = 3+1+4+2+5 = 15 (sum of all elements)
 */

public class PrefixSumExplanation {
    
    /**
     * SEQUENTIAL VERSION - The "Obvious" Approach
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     * 
     * WHY IT SEEMS NON-PARALLELIZABLE:
     * Each output[i] depends on output[i-1], creating a dependency chain:
     * output[1] needs output[0]
     * output[2] needs output[1] 
     * output[3] needs output[2]
     * ... and so on
     */
    public static int[] sequentialPrefixSum(int[] input) {
        System.out.println("🔄 SEQUENTIAL PREFIX SUM");
        System.out.println("Input: " + Arrays.toString(input));
        
        int[] output = new int[input.length];
        output[0] = input[0];
        
        System.out.printf("Step 0: output[0] = input[0] = %d\n", output[0]);
        
        // Each iteration DEPENDS on the previous result
        for (int i = 1; i < input.length; i++) {
            output[i] = output[i-1] + input[i];  // 👈 DEPENDENCY!
            System.out.printf("Step %d: output[%d] = output[%d] + input[%d] = %d + %d = %d\n", 
                             i, i, i-1, i, output[i-1], input[i], output[i]);
        }
        
        System.out.println("Result: " + Arrays.toString(output));
        return output;
    }
    
    /**
     * THE BREAKTHROUGH: Up-Sweep + Down-Sweep Algorithm
     * 
     * KEY INSIGHT: We can break the dependency by using a tree-based approach
     * 
     * PHASE 1 - UP-SWEEP (Reduce): Build partial sums in a tree structure
     * PHASE 2 - DOWN-SWEEP (Distribute): Propagate results back down
     * 
     * This is the foundation of parallel prefix sum algorithms!
     */
    
    /**
     * PARALLEL PREFIX SUM using Fork-Join
     * 
     * STRATEGY: 
     * 1. Divide array into chunks
     * 2. Compute prefix sum within each chunk (parallel)
     * 3. Compute prefix sum of chunk totals (sequential - small)
     * 4. Add chunk prefix to each element in subsequent chunks (parallel)
     */
    static class ParallelPrefixSum extends RecursiveAction {
        private static final int THRESHOLD = 1000;
        
        private final int[] input;
        private final int[] output;
        private final int start, end;
        private final int offset;  // Value to add to all elements in this range
        
        public ParallelPrefixSum(int[] input, int[] output, int start, int end, int offset) {
            this.input = input;
            this.output = output;
            this.start = start;
            this.end = end;
            this.offset = offset;
        }
        
        @Override
        protected void compute() {
            int length = end - start;
            
            if (length <= THRESHOLD) {
                // Base case: compute prefix sum sequentially for small range
                computeSequential();
            } else {
                // Recursive case: divide and conquer
                int mid = start + length / 2;
                
                // Phase 1: Compute left half
                ParallelPrefixSum left = new ParallelPrefixSum(input, output, start, mid, offset);
                
                // Phase 2: Compute right half with offset from left half's total
                int leftSum = computeRangeSum(start, mid);
                ParallelPrefixSum right = new ParallelPrefixSum(input, output, mid, end, offset + leftSum);
                
                // Execute in parallel
                left.fork();
                right.compute();
                left.join();
            }
        }
        
        private void computeSequential() {
            String threadName = Thread.currentThread().getName();
            System.out.printf("Thread %s: Processing range [%d, %d) with offset %d\n", 
                             threadName, start, end, offset);
            
            if (start < end) {
                output[start] = input[start] + offset;
                for (int i = start + 1; i < end; i++) {
                    output[i] = output[i-1] + input[i];
                }
            }
        }
        
        private int computeRangeSum(int start, int end) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += input[i];
            }
            return sum;
        }
    }
    
    /**
     * ALTERNATIVE: Simple Parallel Approach with Two Phases
     * This is easier to understand but less efficient than optimal algorithms
     */
    public static int[] twoPhaseParallelPrefixSum(int[] input) {
        System.out.println("\n🚀 TWO-PHASE PARALLEL PREFIX SUM");
        System.out.println("Input: " + Arrays.toString(input));
        
        int[] output = new int[input.length];
        
        if (input.length == 0) return output;
        
        // Use parallel implementation
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ParallelPrefixSum task = new ParallelPrefixSum(input, output, 0, input.length, 0);
        pool.invoke(task);
        
        System.out.println("Result: " + Arrays.toString(output));
        return output;
    }
    
    /**
     * DEMONSTRATION: Why This Works
     * 
     * Example with input = [3, 1, 4, 2, 5, 6, 2, 8]
     * 
     * PHASE 1: Divide into chunks and compute local prefix sums
     * Chunk 1: [3, 1, 4, 2] → Local prefix: [3, 4, 8, 10], Sum = 10
     * Chunk 2: [5, 6, 2, 8] → Local prefix: [5, 11, 13, 21], Sum = 21
     * 
     * PHASE 2: Compute prefix of chunk sums
     * Chunk sums: [10, 21] → Chunk prefix: [10, 31]
     * 
     * PHASE 3: Add chunk offsets to local results
     * Chunk 1: [3, 4, 8, 10] + 0 = [3, 4, 8, 10]
     * Chunk 2: [5, 11, 13, 21] + 10 = [15, 21, 23, 31]
     * 
     * Final result: [3, 4, 8, 10, 15, 21, 23, 31]
     */
    
    public static void demonstrateAlgorithm() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMONSTRATION: HOW PARALLEL PREFIX SUM WORKS");
        System.out.println("=".repeat(60));
        
        int[] input = {3, 1, 4, 2, 5, 6, 2, 8};
        System.out.println("Original input: " + Arrays.toString(input));
        
        // Manual demonstration of the chunking approach
        System.out.println("\n📝 STEP-BY-STEP BREAKDOWN:");
        
        // Phase 1: Process chunks locally
        System.out.println("\nPHASE 1: Local prefix sums within chunks");
        int[] chunk1 = {3, 1, 4, 2};
        int[] chunk2 = {5, 6, 2, 8};
        
        int[] prefix1 = sequentialPrefixSum(chunk1);
        int[] prefix2 = sequentialPrefixSum(chunk2);
        
        int sum1 = prefix1[prefix1.length - 1];
        int sum2 = prefix2[prefix2.length - 1];
        
        System.out.printf("Chunk 1 sum: %d, Chunk 2 sum: %d\n", sum1, sum2);
        
        // Phase 2: Adjust second chunk
        System.out.println("\nPHASE 2: Add chunk offset to second chunk");
        for (int i = 0; i < prefix2.length; i++) {
            prefix2[i] += sum1;
        }
        
        System.out.println("Adjusted chunk 2: " + Arrays.toString(prefix2));
        
        // Combine results
        int[] finalResult = new int[input.length];
        System.arraycopy(prefix1, 0, finalResult, 0, prefix1.length);
        System.arraycopy(prefix2, 0, finalResult, prefix1.length, prefix2.length);
        
        System.out.println("\n✅ FINAL RESULT: " + Arrays.toString(finalResult));
        
        // Verify correctness
        int[] expected = sequentialPrefixSum(input);
        boolean correct = Arrays.equals(finalResult, expected);
        System.out.println("✓ Correctness check: " + (correct ? "PASSED" : "FAILED"));
    }
    
    /**
     * PERFORMANCE COMPARISON
     */
    public static void performanceComparison() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PERFORMANCE COMPARISON");
        System.out.println("=".repeat(60));
        
        int[] sizes = {1000, 10000, 100000, 1000000};
        
        for (int size : sizes) {
            System.out.printf("\n📊 Array size: %,d elements\n", size);
            
            // Generate test data
            int[] input = new int[size];
            for (int i = 0; i < size; i++) {
                input[i] = (i % 10) + 1;  // Values 1-10
            }
            
            // Sequential timing
            long startTime = System.nanoTime();
            int[] seqResult = sequentialPrefixSum(input);
            long seqTime = System.nanoTime() - startTime;
            
            // Parallel timing
            startTime = System.nanoTime();
            int[] parResult = twoPhaseParallelPrefixSum(input);
            long parTime = System.nanoTime() - startTime;
            
            // Verify correctness
            boolean correct = Arrays.equals(seqResult, parResult);
            
            System.out.printf("Sequential time: %.2f ms\n", seqTime / 1_000_000.0);
            System.out.printf("Parallel time:   %.2f ms\n", parTime / 1_000_000.0);
            System.out.printf("Speedup: %.2fx\n", (double) seqTime / parTime);
            System.out.printf("Correctness: %s\n", correct ? "✅ PASS" : "❌ FAIL");
            
            if (size <= 1000) {
                // Show actual results for small arrays
                System.out.println("Sequential result: " + Arrays.toString(Arrays.copyOf(seqResult, Math.min(10, seqResult.length))) + "...");
                System.out.println("Parallel result:   " + Arrays.toString(Arrays.copyOf(parResult, Math.min(10, parResult.length))) + "...");
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("🎯 THE PREFIX SUM PROBLEM: FROM SEQUENTIAL TO PARALLEL");
        System.out.println("=".repeat(70));
        
        // Basic example
        int[] example = {3, 1, 4, 2, 5};
        
        System.out.println("📖 UNDERSTANDING THE PROBLEM:");
        sequentialPrefixSum(example.clone());
        
        System.out.println("\n🧠 THE PARALLELIZATION CHALLENGE:");
        System.out.println("- Each output[i] depends on output[i-1]");
        System.out.println("- Creates a dependency chain that seems to prevent parallelism");
        System.out.println("- BUT: We can break this using divide-and-conquer!");
        
        twoPhaseParallelPrefixSum(example.clone());
        
        demonstrateAlgorithm();
        performanceComparison();
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🎉 KEY TAKEAWAYS:");
        System.out.println("• Problems that seem sequential can often be parallelized");
        System.out.println("• Divide-and-conquer can break dependency chains");
        System.out.println("• Parallel algorithms may be more complex but can provide speedup");
        System.out.println("• Always verify correctness when developing parallel algorithms");
        System.out.println("=".repeat(70));
    }
}