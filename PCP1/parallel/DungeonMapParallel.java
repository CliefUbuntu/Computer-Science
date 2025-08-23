/**
 * DungeonMapParallel.java
 *
 * Essentially the same DungeonMap as provided for the serial solution,
 * renamed for the parallel assignment. This implementation intentionally
 * does not use synchronization primitives because the assignment states
 * the benign race condition may be ignored.
 *
 * (Copied and adapted from the original DungeonMap.java)
 */

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

public class DungeonMapParallel {

    // Constants for precision and resolution of mana calculations
    public static final int PRECISION = 10000;
    public static final int RESOLUTION = 5;

    // Grid dimensions and coordinate boundaries
    private int rows, columns;
    private double xmin, xmax, ymin, ymax;
    private int [][] manaMap;  // Stores calculated mana values
    private int [][] visit;   // Tracks which hunter visited each cell
    private int dungeonGridPointsEvaluated;  // Counts how many grid points have been calculated
    private double bossX;     // Boss location for mana calculations
    private double bossY;
    private double decayFactor;  // Used in the mana formula

    public DungeonMapParallel(double xmin, double xmax, double ymin, double ymax, int seed) {
        super();
        // Set up the coordinate system for the dungeon
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;

        // Calculate the grid size based on resolution
        this.rows = (int) Math.round((xmax - xmin) * RESOLUTION);
        this.columns = (int) Math.round((ymax - ymin) * RESOLUTION);

        // Set up random generation for boss placement
        Random rand;
        if (seed == 0) rand = new Random();
        else rand = new Random(seed);

        // Randomly place the boss within the dungeon bounds
        double xRange = xmax - xmin;
        this.bossX = xmin + (xRange) * rand.nextDouble();
        this.bossY = ymin + (ymax - ymin) * rand.nextDouble();
        this.decayFactor = 2.0 / (xRange * 0.1);

        // Initialize data structures
        manaMap = new int[rows][columns];
        visit = new int[rows][columns];
        dungeonGridPointsEvaluated = 0;

        // Set all cells to unvisited and uncomputed initially
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                manaMap[i][j] = Integer.MIN_VALUE;  // Indicates not computed yet
                visit[i][j] = -1;  // Indicates not visited by any hunter
            }
        }
    }

    // Check if a cell has been visited by any hunter
    boolean visited(int x, int y) {
        if (visit[x][y] == -1) return false;
        return true;
    }

    // Mark a cell as visited by a specific hunter
    void setVisited(int x, int y, int id) {
        if (visit[x][y] == -1) // Don't reset if already visited by someone else
            visit[x][y] = id;
    }

    // Calculate or retrieve the mana level at a specific grid position
    int getManaLevel(int x, int y) {
        // If already visited this cell, return the cached value
        if (visited(x, y)) return manaMap[x][y];
        // If already computed this cell's mana, return the cached value
        if (manaMap[x][y] > Integer.MIN_VALUE) return manaMap[x][y];

        // Convert grid coordinates to world coordinates
        double x_coord = xmin + ((xmax - xmin) / rows) * x;
        double y_coord = ymin + ((ymax - ymin) / columns) * y;
        double dx = x_coord - bossX;
        double dy = y_coord - bossY;
        double distanceSquared = dx * dx + dy * dy;

        // Calculate the complex mana formula with multiple sine/cosine components
        // and exponential decay based on distance from the boss
        double mana = (2 * Math.sin(x_coord + 0.1 * Math.sin(y_coord / 5.0) + Math.PI / 2) *
                Math.cos((y_coord + 0.1 * Math.cos(x_coord / 5.0) + Math.PI / 2) / 2.0) +
            0.7 * Math.sin((x_coord * 0.5) + (y_coord * 0.3) + 0.2 * Math.sin(x_coord / 6.0) + Math.PI / 2) +
            0.3 * Math.sin((x_coord * 1.5) - (y_coord * 0.8) + 0.15 * Math.cos(y_coord / 4.0)) +
            -0.2 * Math.log(Math.abs(y_coord - Math.PI * 2) + 0.1) +
            0.5 * Math.sin((x_coord * y_coord) / 4.0 + 0.05 * Math.sin(x_coord)) +
            1.5 * Math.cos((x_coord + y_coord) / 5.0 + 0.1 * Math.sin(y_coord)) +
            3.0 * Math.exp(-0.03 * ((x_coord - bossX - 15) * (x_coord - bossX - 15) +
                                    (y_coord - bossY + 10) * (y_coord - bossY + 10))) +
            8.0 * Math.exp(-0.01 * distanceSquared) +
            2.0 / (1.0 + 0.05 * distanceSquared));

        // Convert to fixed-point integer representation for consistency
        int fixedPoint = (int) (PRECISION * mana);
        manaMap[x][y] = fixedPoint;
        dungeonGridPointsEvaluated++;  // Count each evaluation
        return fixedPoint;
    }

    // Determine the best direction to move from the current position by checking all neighbors
    HuntParallel.Direction getNextStepDirection(int x, int y) {
        HuntParallel.Direction climbDirection = HuntParallel.Direction.STAY;
        int localMax = getManaLevel(x, y);

        // Define all 8 possible movement directions
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},       // cardinal directions
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}      // diagonal directions
        };

        HuntParallel.Direction[] directionEnums = {
            HuntParallel.Direction.LEFT,
            HuntParallel.Direction.RIGHT,
            HuntParallel.Direction.UP,
            HuntParallel.Direction.DOWN,
            HuntParallel.Direction.UP_LEFT,
            HuntParallel.Direction.UP_RIGHT,
            HuntParallel.Direction.DOWN_LEFT,
            HuntParallel.Direction.DOWN_RIGHT
        };

        // Check each neighboring cell to find the direction with highest mana
        for (int i = 0; i < directions.length; i++) {
            int newX = x + directions[i][0];
            int newY = y + directions[i][1];

            // Make sure the new position is within bounds
            if (newX >= 0 && newX < rows && newY >= 0 && newY < columns) {
                int power = getManaLevel(newX, newY);
                if (power > localMax) {
                    localMax = power;
                    climbDirection = directionEnums[i];
                }
            }
        }
        return climbDirection;
    }

    // Create a visual representation of the mana map as a PNG image
    public void visualisePowerMap(String filename, boolean path) {
        int width = manaMap.length;
        int height = manaMap[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Find the min and max mana values for color scaling
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int value = manaMap[x][y];
                if (value == Integer.MIN_VALUE) continue;  // Skip uncomputed cells
                if (value < min) min = value;
                if (value > max) max = value;
            }
        }

        double range = (max > min) ? (max - min) : 1.0;

        // Color each pixel based on mana level or visit status
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color;
                if (path && !visited(x, y)) color = Color.BLACK;  // Unvisited cells are black in path mode
                else if (manaMap[x][y] == Integer.MIN_VALUE) color = Color.BLACK;  // Uncomputed cells are black
                else {
                    // Normalize the mana value and map it to a color
                    double normalized = (manaMap[x][y] - min) / range;
                    color = mapHeightToColor(normalized);
                }
                image.setRGB(x, height - 1 - y, color.getRGB());  // Flip Y coordinate for proper orientation
            }
        }

        // Save the image to a file
        try {
            File output = new File(filename);
            ImageIO.write(image, "png", output);
            System.out.println("map saved to " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Map normalized mana values (0.0 to 1.0) to colors using a gradient
    private Color mapHeightToColor(double normalized) {
        normalized = Math.max(0, Math.min(1, normalized));  // Clamp the value to [0,1]
        int r = 0, g = 0, b = 0;

        // Use a three-stage color gradient: blue -> purple -> red -> yellow/white
        if (normalized < 0.33) {
            // Low values: dark blue to purple
            double t = normalized / 0.33;
            r = (int) (128 * t);
            g = 0;
            b = (int) (128 + 127 * t);
        } else if (normalized < 0.66) {
            // Medium values: purple to red
            double t = (normalized - 0.33) / 0.33;
            r = (int) (128 + 127 * t);
            g = 0;
            b = (int) (255 - 255 * t);
        } else {
            // High values: red to yellow/white
            double t = (normalized - 0.66) / 0.34;
            r = 255;
            g = (int) (255 * t);
            b = (int) (255 * t);
        }
        return new Color(r, g, b);
    }

    // Provide getter methods to access internal state
    public int getGridPointsEvaluated() {
        return dungeonGridPointsEvaluated;
    }

    // Convert grid coordinates back to world coordinates
    public double getXcoord(int x) {
        return xmin + ((xmax - xmin) / rows) * x;
    }

    public double getYcoord(int y) {
        return ymin + ((ymax - ymin) / columns) * y;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}