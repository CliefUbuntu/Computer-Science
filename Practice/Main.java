import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

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
        if ((hi - lo) <= SEQUENTIAL_CUTOFF) {
            int sum = 0;
            for (int i = lo; i < hi; i++) sum += arr[i];
            return sum;
        } else {
            int mid = (lo + hi) / 2;
            SumTask left = new SumTask(arr, lo, mid);
            SumTask right = new SumTask(arr, mid, hi);
            left.fork();                  // run left asynchronously
            int rightAns = right.compute(); // run right in this thread
            int leftAns = left.join();    // wait for left
            return leftAns + rightAns;
        }
    }
}

public class Main {
    static final ForkJoinPool fjPool = new ForkJoinPool(); // default

    public static void main(String[] args) {
        int[] arr = new int[1000000];
        for (int i = 0; i < arr.length; i++) arr[i] = 1;

        int sum = fjPool.invoke(new SumTask(arr, 0, arr.length));
        System.out.println("Sum: " + sum);
    }
}

