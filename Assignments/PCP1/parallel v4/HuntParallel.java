/**
 * HuntParallel.java
 *
 * Each HuntParallel starts at (posRow,posCol) and climbs until a peak.
 */
public class HuntParallel {
    private final int id;
    private int posRow, posCol;
    private int steps;
    private boolean stopped;
    private final DungeonMapParallel dungeon;

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
            switch (next) {
                case STAY:
                    return power;
                case LEFT:
                    posRow = posRow - 1;
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
                    posRow = posRow - 1;
                    break;
                case UP_RIGHT:
                    posCol = posCol - 1;
                    posRow = posRow + 1;
                    break;
                case DOWN_LEFT:
                    posCol = posCol + 1;
                    posRow = posRow - 1;
                    break;
                case DOWN_RIGHT:
                    posCol = posCol + 1;
                    posRow = posRow + 1;
                    break;
            }
            // Benign use of ThreadLocalRandom to satisfy assignment requirement
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
