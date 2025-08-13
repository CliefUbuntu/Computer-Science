import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DungeonHunterParallel {
    static final boolean DEBUG = false;
    static long startTime = 0;
    static long endTime = 0;
    
    private static void tick() { startTime = System.currentTimeMillis(); }
    private static void tock() { endTime = System.currentTimeMillis(); }

    private static class HuntResult {
        final int maxMana;
        final int finderIndex;
        
        HuntResult(int maxMana, int finderIndex) {
            this.maxMana = maxMana;
            this.finderIndex = finderIndex;
        }
    }

    private static class HuntTask extends RecursiveTask<HuntResult> {
        private final HuntParallel[] searches;
        private final int start;
        private final int end;
        private final int threshold;

        public HuntTask(HuntParallel[] searches, int start, int end, int threshold) {
            this.searches = searches;
            this.start = start;
            this.end = end;
            this.threshold = threshold;
        }

        @Override
        protected HuntResult compute() {
            if (end - start <= threshold) {
                int maxMana = Integer.MIN_VALUE;
                int finderIndex = -1;
                
                for (int i = start; i < end; i++) {
                    int currentMana = searches[i].findManaPeak();
                    if (currentMana > maxMana) {
                        maxMana = currentMana;
                        finderIndex = i;
                    }
                }
                return new HuntResult(maxMana, finderIndex);
            } else {
                int mid = (start + end) / 2;
                HuntTask leftTask = new HuntTask(searches, start, mid, threshold);
                HuntTask rightTask = new HuntTask(searches, mid, end, threshold);
                
                leftTask.fork();
                HuntResult rightResult = rightTask.compute();
                HuntResult leftResult = leftTask.join();
                
                return (leftResult.maxMana >= rightResult.maxMana) ? 
                       leftResult : rightResult;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java DungeonHunterParallel <gridSize> <searchDensity> <randomSeed>");
            System.exit(0);
        }

        int gateSize = 10;
        int numSearches = 10;
        int randomSeed = 0;
        Random rand = new Random();

        try {
            gateSize = Integer.parseInt(args[0]);
            if (gateSize <= 0) throw new IllegalArgumentException("Grid size must be >0");
            
            double density = Double.parseDouble(args[1]);
            if (density <= 0 || density > 1) throw new IllegalArgumentException("Density must be (0,1]");
            
            randomSeed = Integer.parseInt(args[2]);
            if (randomSeed > 0) rand = new Random(randomSeed);
            
            numSearches = (int) (density * (gateSize * 2) * (gateSize * 2) * DungeonMapParallel.RESOLUTION);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        double xmin = -gateSize, xmax = gateSize;
        double ymin = -gateSize, ymax = gateSize;
        DungeonMapParallel dungeon = new DungeonMapParallel(xmin, xmax, ymin, ymax, randomSeed);
        HuntParallel[] searches = new HuntParallel[numSearches];
        
        int rows = dungeon.getRows();
        int cols = dungeon.getColumns();
        
        for (int i = 0; i < numSearches; i++) {
            searches[i] = new HuntParallel(i + 1, rand.nextInt(rows), rand.nextInt(cols), dungeon);
        }

        tick();
        int nProcessors = Runtime.getRuntime().availableProcessors();
        int threshold = Math.max(1, numSearches / (nProcessors * 4));
        
        ForkJoinPool pool = new ForkJoinPool();
        HuntResult result = pool.invoke(new HuntTask(searches, 0, numSearches, threshold));
        tock();
        
        int max = result.maxMana;
        int finder = result.finderIndex;

        System.out.printf("\t dungeon size: %d\n", gateSize);
        System.out.printf("\t rows: %d, columns: %d\n", rows, cols);
        System.out.printf("\t x: [%.1f, %.1f], y: [%.1f, %.1f]\n", xmin, xmax, ymin, ymax);
        System.out.printf("\t Number searches: %d\n", numSearches);
        System.out.printf("\n\t time: %d ms\n", endTime - startTime);
        
        int evalPoints = dungeon.getGridPointsEvaluated();
        System.out.printf("\t grid points evaluated: %d (%.0f%%)\n", 
                         evalPoints, (evalPoints * 100.0) / (rows * cols));
        
        System.out.printf("Dungeon Master (mana %d) found at: ", max);
        System.out.printf("x=%.1f y=%.1f\n\n", 
                         dungeon.getXcoord(searches[finder].getPosRow()),
                         dungeon.getYcoord(searches[finder].getPosCol()));
        
        dungeon.visualisePowerMap("visualiseSearch.png", false);
        dungeon.visualisePowerMap("visualiseSearchPath.png", true);
    }
}