import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DungeonHunterParallel.java
 *
 * Usage:
 *   java DungeonHunterParallel <gateSize> <searchFactor> <seed> [parallelism] [batchSize]
 *
 * - gateSize (int)  : half-extent of x and y (like before)
 * - searchFactor (double) : multiplier to (gateSize*2)^2*RESOLUTION to compute number of searches
 * - seed (int) : random seed (0 = nondeterministic)
 * - parallelism (optional int) : number of worker threads to use (default = available processors)
 * - batchSize (optional int) : number of searches a worker grabs per batch (default = 16)
 *
 */
public class DungeonHunterParallel {
    static final boolean DEBUG = false;

    static long startTime = 0;
    static long endTime = 0;
    private static void tick() { startTime = System.currentTimeMillis(); }
    private static void tock() { endTime = System.currentTimeMillis(); }

    // WorkerTask: each task repeatedly takes a batch index from nextIndex and processes that range.
    static class WorkerTask extends RecursiveTask<int[]> {
        private final HuntParallel[] searches;
        private final AtomicInteger nextIndex;
        private final int batchSize;

        public WorkerTask(HuntParallel[] searches, AtomicInteger nextIndex, int batchSize) {
            this.searches = searches;
            this.nextIndex = nextIndex;
            this.batchSize = batchSize;
        }

        @Override
        protected int[] compute() {
            int localMax = Integer.MIN_VALUE;
            int finder = -1;
            final int n = searches.length;

            while (true) {
                int start = nextIndex.getAndAdd(batchSize);
                if (start >= n) break;
                int end = Math.min(start + batchSize, n);
                for (int i = start; i < end; i++) {
                    int lm = searches[i].findManaPeak();
                    if (lm > localMax) {
                        localMax = lm;
                        finder = i;
                    }
                }
            }
            return new int[] { localMax, finder };
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java DungeonHunterParallel <gateSize> <searchFactor> <seed> [parallelism] [batchSize]");
            System.exit(1);
        }

        int gateSize = 10;
        double searchFactor = 0.1;
        int randomSeed = 0;
        int parallelism = Runtime.getRuntime().availableProcessors();
        int batchSize = 16;

        try {
            gateSize = Integer.parseInt(args[0]);
            searchFactor = Double.parseDouble(args[1]);
            randomSeed = Integer.parseInt(args[2]);

            if (args.length >= 4) {
                parallelism = Integer.parseInt(args[3]);
                if (parallelism <= 0) parallelism = Runtime.getRuntime().availableProcessors();
            }
            if (args.length >= 5) {
                batchSize = Integer.parseInt(args[4]);
                if (batchSize <= 0) batchSize = 16;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: invalid numeric argument.");
            System.exit(1);
        }

        double xmin = -gateSize;
        double xmax = gateSize;
        double ymin = -gateSize;
        double ymax = gateSize;

        DungeonMapParallel dungeon = new DungeonMapParallel(xmin, xmax, ymin, ymax, randomSeed);

        int dungeonRows = dungeon.getRows();
        int dungeonColumns = dungeon.getColumns();

        int numSearches = (int) (searchFactor * (gateSize * 2) * (gateSize * 2) * DungeonMapParallel.RESOLUTION);
        if (numSearches < 1) numSearches = 1;

        HuntParallel[] searches = new HuntParallel[numSearches];

        Random rand = new Random();
        if (randomSeed > 0) rand = new Random(randomSeed);

        for (int i = 0; i < numSearches; i++) {
            int r = rand.nextInt(dungeonRows);
            int c = rand.nextInt(dungeonColumns);
            searches[i] = new HuntParallel(i + 1, r, c, dungeon);
        }

        System.out.printf("Gate size: %d. Grid: %dx%d. Num searches: %d. Parallelism: %d. BatchSize: %d. Seed: %d\n",
                gateSize, dungeonRows, dungeonColumns, numSearches, parallelism, batchSize, randomSeed);

        AtomicInteger nextIndex = new AtomicInteger(0);

        // create ForkJoinPool with explicit parallelism (if possible)
        ForkJoinPool pool = null;
        if (parallelism > 0) {
            pool = new ForkJoinPool(parallelism);
        } else {
            pool = ForkJoinPool.commonPool();
        }

        try {
            // create worker tasks equal to 'parallelism'
            WorkerTask[] tasks = new WorkerTask[Math.max(1, parallelism)];
            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = new WorkerTask(searches, nextIndex, batchSize);
            }

            tick();
            // submit all tasks
            ForkJoinTask<int[]>[] futures = new ForkJoinTask[tasks.length];
            for (int i = 0; i < tasks.length; i++) {
                futures[i] = pool.submit(tasks[i]);
            }

            // join results and combine
            int globalMax = Integer.MIN_VALUE;
            int finder = -1;
            for (int i = 0; i < futures.length; i++) {
                int[] res = futures[i].join();
                if (res != null && res[0] > globalMax) {
                    globalMax = res[0];
                    finder = res[1];
                }
            }
            tock();

            System.out.printf("\t dungeon size: %d\n", gateSize);
            System.out.printf("\t rows: %d, columns: %d\n", dungeonRows, dungeonColumns);
            System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax);
            System.out.printf("\t Number searches: %d\n", numSearches);

            System.out.printf("\n\t time: %d ms\n", endTime - startTime);
            int tmp = dungeon.getGridPointsEvaluated();
            System.out.printf("\tnumber dungeon grid points evaluated: %d  (%2.0f%s)\n",
                    tmp, (tmp * 1.0 / (dungeonRows * dungeonColumns * 1.0)) * 100.0, "%");

            if (finder >= 0 && finder < searches.length) {
                System.out.printf("Dungeon Master (mana %d) found at:  ", globalMax);
                System.out.printf("x=%.1f y=%.1f\n\n",
                        dungeon.getXcoord(searches[finder].getPosRow()),
                        dungeon.getYcoord(searches[finder].getPosCol()));
            } else {
                System.out.println("No finder (no searches ran)");
            }

            // Save visualisations (not timed)
            dungeon.visualisePowerMap("visualiseSearch.png", false);
            dungeon.visualisePowerMap("visualiseSearchPath.png", true);

        } finally {
            pool.shutdown();
        }
    }
}
