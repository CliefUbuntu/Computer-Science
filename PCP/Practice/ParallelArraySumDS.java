public class ParallelArraySumDS {
    private static final int NUM_THREADS = 4;
    
    public static void main(String[] args) {
        // Create a large array for testing
        int[] array = new int[1_000_000];
        for (int i = 0; i < array.length; i++) {
            array[i] = i + 1; // Fill with 1 to 1,000,000
        }
        
        // Calculate sum sequentially for verification
        long seqSum = sequentialSum(array);
        System.out.println("Sequential sum: " + seqSum);
        
        // Calculate sum in parallel
        long parallelSum = parallelSum(array);
        System.out.println("Parallel sum: " + parallelSum);
    }
    
    // Sequential implementation for verification
    public static long sequentialSum(int[] arr) {
        long sum = 0;
        for (int num : arr) {
            sum += num;
        }
        return sum;
    }
    
    // Parallel implementation
    public static long parallelSum(int[] arr) {
        // Create an array to hold partial results
        long[] partialSums = new long[NUM_THREADS];
        Thread[] threads = new Thread[NUM_THREADS];
        
        int segmentSize = arr.length / NUM_THREADS;
        
        // Create and start threads
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadIndex = i;
            int start = threadIndex * segmentSize;
            int end = (threadIndex == NUM_THREADS - 1) ? arr.length : (threadIndex + 1) * segmentSize;
            
            threads[i] = new Thread(() -> {
                partialSums[threadIndex] = sumRange(arr, start, end);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Combine partial results
        long total = 0;
        for (long partial : partialSums) {
            total += partial;
        }
        return total;
    }
    
    // Helper method to sum a range of the array
    private static long sumRange(int[] arr, int start, int end) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += arr[i];
        }
        return sum;
    }
}