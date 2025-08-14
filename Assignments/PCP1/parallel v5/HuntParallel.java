/**
 * HuntParallel.java
 *
 * Parallel version of Hunt for the Dungeon Hunter assignment.
 * Represents a search in the grid of a DungeonMapParallel to identify the local maximum from a start point.
 * Uses Fork/Join framework for parallel execution.
 *
 * M. Kuttel 2025
 */

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadLocalRandom;

public class HuntParallel extends RecursiveTask<Integer> {
    private int id;                     // identifier for this hunt
    private int posRow, posCol;         // Position in the dungeonMap
    private int steps;                  // number of steps to end of the search
    private boolean stopped;            // Did the search hit a previously searched location?
    private DungeonMapParallel dungeon;
    private int startRow, startCol;     // Store original starting position
    
    // Threshold for sequential cutoff - adjust based on experimentation
    private static final int SEQUENTIAL_CUTOFF = 1;
    
    public enum Direction {
        STAY,
        LEFT,
        RIGHT,
        UP,
        DOWN,
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT
    }

    public HuntParallel(int id, int pos_row, int pos_col, DungeonMapParallel dungeon) {
        this.id = id;
        this.posRow = pos_row; //randomly allocated
        this.posCol = pos_col; //randomly allocated
        this.startRow = pos_row; // store starting position
        this.startCol = pos_col; // store starting position
        this.dungeon = dungeon;
        this.stopped = false;
        this.steps = 0;
    }

    @Override
    protected Integer compute() {
        // Since each hunt is independent, we execute sequentially within each hunt
        // The parallelism comes from running multiple hunts simultaneously
        return findManaPeak();
    }

    /**
     * Find the local maximum mana from an initial starting point
     * 
     * @return the highest power/mana located
     */
    public int findManaPeak() {
        int power = Integer.MIN_VALUE;
        Direction next = Direction.STAY;
        
        while(!dungeon.visited(posRow, posCol)) { // stop when hit existing path
            power = dungeon.getManaLevel(posRow, posCol);
            dungeon.setVisited(posRow, posCol, id);
            steps++;
            next = dungeon.getNextStepDirection(posRow, posCol);
            if(DungeonHunterParallel.DEBUG) System.out.println("Shadow "+getID()+" moving  "+next);
            
            switch(next) {
                case STAY: 
                    return power; //found local valley
                case LEFT:
                    posRow--;
                    break;
                case RIGHT:
                    posRow = posRow + 1;
                    break;
                case UP:
                    posCol = posCol - 1;
                    break;
                case DOWN:
                    posCol = posCol + 1;
                    break;
                case UP_LEFT:
                    posCol = posCol - 1;
                    posRow--;
                    break;
                case UP_RIGHT:
                    posCol = posCol - 1;
                    posRow = posRow + 1;
                    break;
                case DOWN_LEFT:
                    posCol = posCol + 1; //fixed BUG!!!
                    posRow--;
                    break;
                case DOWN_RIGHT:
                    posCol = posCol + 1;
                    posRow = posRow + 1;
                    break;
            }
            
            // Check bounds to prevent array out of bounds
            if (posRow < 0 || posRow >= dungeon.getRows() || 
                posCol < 0 || posCol >= dungeon.getColumns()) {
                return power;
            }
        }
        stopped = true;
        return power;
    }

    public int getID() { 
        return id; 
    }

    public int getPosRow() { 
        return posRow;
    }

    public int getPosCol() { 
        return posCol;
    }

    public int getSteps() { 
        return steps;
    }
    
    public boolean isStopped() {
        return stopped;
    }

    // Get starting position (for reporting results)
    public int getStartRow() {
        return startRow;
    }
    
    public int getStartCol() {
        return startCol;
    }
}