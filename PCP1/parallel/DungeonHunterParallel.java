/**
 * DungeonHunterParallel.java
 *
 * Parallel driver using the Fork/Join framework. The program builds the
 * same inputs/output as the serial version and uses a ForkJoinPool to
 * execute multiple HuntParallel tasks in parallel. The starting positions
 * are generated deterministically using the same logic as the serial
 * solution (seeded Random) to allow validation.
 *
 * Usage:
 *   java DungeonHunterParallel <gridSize> <numSearchesFactor> <randomSeed>
 *
 * NOTE: numSearchesFactor is the same type of argument as in the serial
 * program: it is multiplied with (gateSize*2)^2*RESOLUTION to produce
 * a whole number count of searches.
 *
 * @author Nyiko Mathebula
 *         MTHNYI011
 */

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DungeonHunterParallel {
    static final boolean DEBUG = false;

    // Timing variables to measure performance
    static long startTime = 0;
    static long endTime = 0;
    private static void tick() { startTime = System.currentTimeMillis(); }
    private static void tock() { endTime = System.currentTimeMillis(); }

    // RecursiveTask that returns int[]{maxValue, finderIndex}
    // This is the core of the Fork/Join implementation
    static class HuntTask extends RecursiveTask<int[]> {
        private static final int THRESHOLD = 16; // chunk size to process sequentially
        private final HuntParallel[] searches;
        private final int lo, hi; // range [lo, hi) that this task will handle
        
        public HuntTask(HuntParallel[] searches, int lo, int hi) {
            this.searches = searches;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected int[] compute() {
            // If task is small enough, process it sequentially
            if (hi - lo <= THRESHOLD) {
                int localMax = Integer.MIN_VALUE;
                int finder = -1;
                // Go through each search in assigned range
                for (int i = lo; i < hi; i++) {
                    int lm = searches[i].findManaPeak();
                    // Keep track of which search found the highest mana peak
                    if (lm > localMax) {
                        localMax = lm;
                        finder = i;
                    }
                }
                return new int[]{localMax, finder};
            } else {
                // If task is too big, split it in half using Fork/Join
                int mid = lo + (hi - lo) / 2;
                HuntTask left = new HuntTask(searches, lo, mid);
                HuntTask right = new HuntTask(searches, mid, hi);
                left.fork(); // fork the left task to run in parallel
                int[] rightRes = right.compute(); // compute the right task directly
                int[] leftRes = left.join(); // wait for the left task to complete
                // Return the result with the higher mana value
                if (leftRes[0] >= rightRes[0]) return leftRes;
                else return rightRes;
            }
        }
    }

    public static void main(String[] args) {
        double xmin, xmax, ymin, ymax;
        DungeonMapParallel dungeon;

        // Default values for simulation parameters
        int numSearches = 10, gateSize = 10;
        HuntParallel[] searches;

        Random rand = new Random();
        int randomSeed = 0;

        // Validate that the user provided the correct number of arguments
        if (args.length != 3) {
            System.out.println("Incorrect number of command line arguments provided.");
            System.exit(0);
        }

        try {
            // Parse the command line arguments and validate them
            gateSize = Integer.parseInt(args[0]);
            if (gateSize <= 0) throw new IllegalArgumentException("Grid size must be greater than 0.");
            numSearches = (int) (Double.parseDouble(args[1]) * (gateSize * 2) * (gateSize * 2) * DungeonMapParallel.RESOLUTION);
            randomSeed = Integer.parseInt(args[2]);
            if (randomSeed < 0) throw new IllegalArgumentException("Random seed must be non-negative.");
            else if (randomSeed > 0) rand = new Random(randomSeed);
        } catch (NumberFormatException e) {
            System.err.println("Error: All arguments must be numeric.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        // Set up the coordinate boundaries for dungeon
        xmin = -gateSize;
        xmax = gateSize;
        ymin = -gateSize;
        ymax = gateSize;

        // Create dungeon map with the specified parameters
        dungeon = new DungeonMapParallel(xmin, xmax, ymin, ymax, randomSeed);

        int dungeonRows = dungeon.getRows();
        int dungeonColumns = dungeon.getColumns();

        searches = new HuntParallel[numSearches];

        // Ensure correctness by using the same starting locations as the serial program
        // Generate starting positions deterministically using a seeded Random
        for (int i = 0; i < numSearches; i++) {
            int r = rand.nextInt(dungeonRows);
            int c = rand.nextInt(dungeonColumns);
            // Create each hunt with a unique ID and starting position
            searches[i] = new HuntParallel(i + 1, r, c, dungeon);
        }

        // Create ForkJoinPool and run top-level task
        // Use the Fork/Join mechanism and join() to synchronize (left.join())
        ForkJoinPool pool = new ForkJoinPool();

        // Time the parallel execution
        tick();
        HuntTask root = new HuntTask(searches, 0, searches.length);
        int[] result = pool.invoke(root);
        tock();

        // Extract the results from parallel computation
        int max = result[0];
        int finder = result[1]; // index in searches array

        // Print out all the simulation results and statistics
        System.out.printf("\t dungeon size: %d,\n", gateSize);
        System.out.printf("\t rows: %d, columns: %d\n", dungeonRows, dungeonColumns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax);
        System.out.printf("\t Number searches: %d\n", numSearches);

        System.out.printf("\n\t time: %d ms\n", endTime - startTime);
        int tmp = dungeon.getGridPointsEvaluated();
        System.out.printf("\tnumber dungeon grid points evaluated: %d  (%2.0f%s)\n",
                tmp, (tmp * 1.0 / (dungeonRows * dungeonColumns * 1.0)) * 100.0, "%");

        // Display the location where the highest mana was found
        if (finder >= 0) {
            System.out.printf("Dungeon Master (mana %d) found at:  ", max);
            System.out.printf("x=%.1f y=%.1f\n\n",
                    dungeon.getXcoord(searches[finder].getPosRow()),
                    dungeon.getYcoord(searches[finder].getPosCol()));
        } else {
            System.out.println("No finder (no searches ran)");
        }

        // Generate visualization images of search results
        dungeon.visualisePowerMap("visualiseSearch.png", false);
        dungeon.visualisePowerMap("visualiseSearchPath.png", true);

        // Properly shut down the thread pool
        pool.shutdown();
    }
}