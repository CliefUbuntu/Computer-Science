public class ParallelArraySumGPT {
    static int[] arr;
    static int[] res = new int[4];

    public static void main(String[] args) throws InterruptedException {
        // Initialize test array
        arr = new int[1000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + 1;  // Fill with 1 to 1000
        }

        int len = arr.length;

        // Create 4 threads to compute parts of the sum
        Thread[] threads = new Thread[4];

        for (int i = 0; i < 4; i++) {
            final int idx = i; // required for inner class
            threads[i] = new Thread(() -> {
                res[idx] = sumRange(arr, idx * len / 4, (idx + 1) * len / 4);
            });
            threads[i].start();
        }

        // Wait for all threads to finish
        for (int i = 0; i < 4; i++) {
            threads[i].join();
        }

        // Combine results
        int total = res[0] + res[1] + res[2] + res[3];
        System.out.println("Total sum: " + total); // Expected: 500500 for 1 to 1000
    }

    public static int sumRange(int[] arr, int lo, int hi) {
        int result = 0;
        for (int j = lo; j < hi; j++) {
            result += arr[j];
        }
        return result;
    }
}
