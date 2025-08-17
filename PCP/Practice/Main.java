import java.util.concurrent.*;

class SumTask extends RecursiveTask<Integer> {
    int[] arr;
    int lo, hi;
    static final int SEQUENTIAL_CUTOFF = 5000;

    SumTask(int[] arr, int lo, int hi) {
        this.arr = arr;
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    protected Integer compute() {
        System.out.println("Task [" + lo + "," + hi + ") running on: " + Thread.currentThread().getName());

        if ((hi - lo) <= SEQUENTIAL_CUTOFF) {
            int sum = 0;
            for (int i = lo; i < hi; i++) sum += arr[i];
            return sum;
        } else {
            int mid = (lo + hi) / 2;
            SumTask left = new SumTask(arr, lo, mid);
            SumTask right = new SumTask(arr, mid, hi);
            left.fork();
            int rightAns = right.compute();
            int leftAns = left.join();
            return leftAns + rightAns;
        }
    }
}

public class Main {
    static final ForkJoinPool fjPool = new ForkJoinPool();

    public static void main(String[] args) {
        int[] arr = new int[100_000];
        for (int i = 0; i < arr.length; i++) arr[i] = 1;

        System.out.println("Parallelism: " + fjPool.getParallelism());
        System.out.println("Pool Size: " + fjPool.getPoolSize());

        long start = System.nanoTime();
        int sum = fjPool.invoke(new SumTask(arr, 0, arr.length));
        long end = System.nanoTime();

        System.out.println("Sum: " + sum);
        System.out.println("Time taken: " + (end - start) / 1_000_000 + " ms");
    }
}