/* Solo-levelling Hunt for Dungeon Master - Parallel Version
 * Parallel version using Fork/Join framework
 * Michelle Kuttel 2025, University of Cape Town
 * author of original Java code adapted with assistance from chatGPT for reframing 
 * and complex power - "mana" - function.
 * Inspired by  "Hill Climbing with Montecarlo"
 * EduHPC'22 Peachy Assignment developed by Arturo Gonzalez Escribano  (Universidad de Valladolid 2021/2022)
 */
/**
 * DungeonHunterParallel.java
 *
 * Main driver for the parallel Dungeon Hunter assignment.
 * This program initializes the dungeon map and performs a series of parallel searches
 * to locate the global maximum using Fork/Join framework.
 *
 * Usage:
 *   java DungeonHunterParallel <gridSize> <numSearches> <randomSeed>
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

class DungeonHunterParallel {
    static final boolean DEBUG = false;

    //timers for how long it all takes
    static long startTime = 0;
    static long endTime = 0;
    private static void tick() {
        startTime = System.currentTimeMillis(); 
    }
    private static void tock(){
        endTime = System.currentTimeMillis(); 
    }

    public static void main(String[] args)  {
        
        double xmin, xmax, ymin, ymax; //dungeon limits - dungeons are square
        DungeonMapParallel dungeon;  //object to store the dungeon as a grid
        
        int numSearches = 10, gateSize = 10;        
        List<HuntParallel> searches;        // List of searches for Fork/Join
        
        // Use ThreadLocalRandom for better parallel performance
        int randomSeed = 0;  //set seed to have predictability for testing
        
        if (args.length != 3) {
            System.out.println("Incorrect number of command line arguments provided.");
            System.exit(0);
        }
        
        /* Read argument values */
        try {
            gateSize = Integer.parseInt(args[0]);
            if (gateSize <= 0) {
                throw new IllegalArgumentException("Grid size must be greater than 0.");
            }
            
            numSearches = (int) (Double.parseDouble(args[1]) * (gateSize * 2) * (gateSize * 2) * DungeonMapParallel.RESOLUTION);
            
            randomSeed = Integer.parseInt(args[2]);
            if (randomSeed < 0) {
                throw new IllegalArgumentException("Random seed must be non-negative.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: All arguments must be numeric.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        xmin = -gateSize;
        xmax = gateSize;
        ymin = -gateSize;
        ymax = gateSize;
        dungeon = new DungeonMapParallel(xmin, xmax, ymin, ymax, randomSeed); // Initialize dungeon
        
        int dungeonRows = dungeon.getRows();
        int dungeonColumns = dungeon.getColumns();
        searches = new ArrayList<>(numSearches);

        // Create ThreadLocalRandom for each thread (better than shared Random)
        // Initialize searches at random locations in dungeon
        ThreadLocalRandom tlr;
        if (randomSeed == 0) {
            tlr = ThreadLocalRandom.current();
        } else {
            // For reproducible results, we need to use a seeded approach
            // ThreadLocalRandom doesn't support seeds directly, so we'll use a workaround
            tlr = ThreadLocalRandom.current();
            tlr.setSeed(randomSeed);
        }

        for (int i = 0; i < numSearches; i++) {
            searches.add(new HuntParallel(i + 1, 
                                        tlr.nextInt(dungeonRows),
                                        tlr.nextInt(dungeonColumns), 
                                        dungeon));
        }

        // Create ForkJoinPool and execute parallel searches
        ForkJoinPool fjPool = new ForkJoinPool();
        
        int max = Integer.MIN_VALUE;
        int finder = -1;
        
        tick();  //start timer
        
        try {
            // Submit all hunt tasks to the ForkJoinPool
            List<Integer> results = new ArrayList<>(numSearches);
            
            // Execute all hunts in parallel
            for (HuntParallel hunt : searches) {
                results.add(fjPool.submit(hunt).get());
            }
            
            // Find the maximum result and which hunt found it
            for (int i = 0; i < numSearches; i++) {
                int localMax = results.get(i);
                if (localMax > max) {
                    max = localMax;
                    finder = i; //keep track of who found it
                }
                if (DEBUG) System.out.println("Shadow " + searches.get(i).getID() + 
                                             " finished at " + localMax + 
                                             " in " + searches.get(i).getSteps());
            }
            
        } catch (Exception e) {
            System.err.println("Error during parallel execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fjPool.shutdown();
        }
        
        tock(); //end timer
        
        System.out.printf("\t dungeon size: %d,\n", gateSize);
        System.out.printf("\t rows: %d, columns: %d\n", dungeonRows, dungeonColumns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax );
        System.out.printf("\t Number searches: %d\n", numSearches );

        /*  Total computation time */
        System.out.printf("\n\t time: %d ms\n", endTime - startTime );
        int tmp = dungeon.getGridPointsEvaluated();
        System.out.printf("\tnumber dungeon grid points evaluated: %d  (%2.0f%s)\n", tmp, (tmp*1.0/(dungeonRows*dungeonColumns*1.0))*100.0, "%");

        /* Results*/
        System.out.printf("Dungeon Master (mana %d) found at:  ", max );
        System.out.printf("x=%.1f y=%.1f\n\n", dungeon.getXcoord(searches.get(finder).getPosRow()), 
                                                dungeon.getYcoord(searches.get(finder).getPosCol()) );
        
        // Generate visualization files
        dungeon.visualisePowerMap("visualiseSearch.png", false);
        dungeon.visualisePowerMap("visualiseSearchPath.png", true);
    }
}