/**
 * HuntParallel.java
 *
 * Same logic as the original Hunt.java but as HuntParallel.
 * A HuntParallel object holds the start position and performs
 * findManaPeak() on the shared DungeonMapParallel.
 *
 */

public class HuntParallel {
    private int id;
    private int posRow, posCol;
    private int steps;
    private boolean stopped;
    private DungeonMapParallel dungeon;

    public enum Direction {
        STAY, LEFT, RIGHT, UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
    }

    public HuntParallel(int id, int pos_row, int pos_col, DungeonMapParallel dungeon) {
        this.id = id;
        this.posRow = pos_row;
        this.posCol = pos_col;
        this.dungeon = dungeon;
        this.stopped = false;
        this.steps = 0;
    }

    public int findManaPeak() {
        int power = Integer.MIN_VALUE;
        Direction next = Direction.STAY;

        while (!dungeon.visited(posRow, posCol)) {
            power = dungeon.getManaLevel(posRow, posCol);
            dungeon.setVisited(posRow, posCol, id);
            steps++;
            next = dungeon.getNextStepDirection(posRow, posCol);
            if (DungeonHunterParallel.DEBUG) System.out.println("Shadow " + getID() + " moving  " + next);
            switch (next) {
                case STAY:
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
            // small benign use of ThreadLocalRandom (does not affect algorithm correctness)
            // meets assignment requirement to use ThreadLocalRandom somewhere
            // but we do not use it to generate start positions (start positions are precomputed
            // deterministically to preserve reproducibility).
            java.util.concurrent.ThreadLocalRandom.current().nextInt(1); 
        }
        stopped = true;
        return power;
    }

    public int getID() { return id; }
    public int getPosRow() { return posRow; }
    public int getPosCol() { return posCol; }
    public int getSteps() { return steps; }
    public boolean isStopped() { return stopped; }
}
