/**
 * HuntParallel.java
 *
 * Same logic as the original Hunt.java but as HuntParallel.
 * A HuntParallel object holds the start position and performs
 * findManaPeak() on the shared DungeonMapParallel.
 *
 */

public class HuntParallel {
    // Store the unique identifier and current position of this hunter
    private int id;
    private int posRow, posCol;
    private int steps;          // Count how many steps this hunter has taken
    private boolean stopped;    // Track whether this hunter has finished searching
    private DungeonMapParallel dungeon;  // Keep a reference to the shared dungeon

    // Define all possible movement directions for the hunter
    public enum Direction {
        STAY, LEFT, RIGHT, UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
    }

    // Initialize a new hunter with a starting position and dungeon reference
    public HuntParallel(int id, int pos_row, int pos_col, DungeonMapParallel dungeon) {
        this.id = id;
        this.posRow = pos_row;
        this.posCol = pos_col;
        this.dungeon = dungeon;
        this.stopped = false;
        this.steps = 0;
    }

    // Implement the main search algorithm - a greedy hill-climbing approach
    public int findManaPeak() {
        int power = Integer.MIN_VALUE;
        Direction next = Direction.STAY;

        // Continue searching until reaching a cell that another hunter has already visited
        while (!dungeon.visited(posRow, posCol)) {
            // Get the mana level at current position
            power = dungeon.getManaLevel(posRow, posCol);
            // Mark this cell as visited by this hunter
            dungeon.setVisited(posRow, posCol, id);
            steps++;
            
            // Determine which direction leads to the highest mana in neighboring cells
            next = dungeon.getNextStepDirection(posRow, posCol);
            if (DungeonHunterParallel.DEBUG) System.out.println("Shadow " + getID() + " moving  " + next);
            
            // Move in the direction of highest mana, or stay if already at a local peak
            switch (next) {
                case STAY:
                    // Found a local maximum - no neighbor has higher mana
                    return power;
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
                    posCol = posCol + 1;
                    posRow--;
                    break;
                case DOWN_RIGHT:
                    posCol = posCol + 1;
                    posRow = posRow + 1;
                    break;
            }
        }
        // Reached a cell that another hunter already visited, so stop here
        stopped = true;
        return power;
    }

    // Provide getter methods to access this hunter's state
    public int getID() { return id; }
    public int getPosRow() { return posRow; }
    public int getPosCol() { return posCol; }
    public int getSteps() { return steps; }
    public boolean isStopped() { return stopped; }
}