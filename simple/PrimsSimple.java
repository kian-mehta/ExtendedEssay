package simple;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PrimsSimple {

    private static int WIDTH = 25;
    private static int HEIGHT = 25;

    private boolean[][] maze;
    private List<Wall> frontier;
    private Random random;

    private class Wall {
        int x1, y1, x2, y2;

        Wall(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        // Get the wall position between two cells
        Point getWallPosition() {
            return new Point((x1 + x2) / 2, (y1 + y2) / 2);
        }
    }

    public PrimsSimple(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
    }

    private void initializeMaze() {
        random = new Random();
        maze = new boolean[HEIGHT][WIDTH];
        frontier = new ArrayList<>();
        // Initialize maze with all walls (false = wall, true = path)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                maze[y][x] = false;
            }
        }
    }

    public boolean[][] generateMaze() {
        initializeMaze();

        // Start with a random cell (must be odd coordinates, proper bounds)
        int startX = 1 + (random.nextInt(WIDTH / 2)) * 2; // Can be 1, 3, 5, ..., 95, 97
        int startY = 1 + (random.nextInt(HEIGHT / 2)) * 2; // Can be 1, 3, 5, ..., 95, 97

        maze[startY][startX] = true;

        // Add initial frontier walls
        addFrontierWalls(startX, startY);

        // Prim's algorithm main loop
        while (!frontier.isEmpty()) {
            // Pick random frontier wall
            Wall wall = frontier.remove(random.nextInt(frontier.size()));

            // Check if we can connect
            if (canConnect(wall)) {
                // Make the unvisited cell a path
                maze[wall.y2][wall.x2] = true;

                // Remove wall between cells
                Point wallPos = wall.getWallPosition();
                maze[wallPos.y][wallPos.x] = true;

                // Add new frontier walls
                addFrontierWalls(wall.x2, wall.y2);
            }
        }

        // Ensure start and end points are accessible for AI training
        ensureStartEndConnectivity();
        return maze;
    }

    private void ensureStartEndConnectivity() {
        // Ensure start point (1,1) and end point (WIDTH-2, HEIGHT-2) are paths
        maze[1][1] = true;
        maze[HEIGHT - 2][WIDTH - 2] = true;

        // Create entrance and exit by clearing border walls
        maze[0][1] = true; // Top entrance
        maze[HEIGHT - 1][WIDTH - 2] = true; // Bottom exit
    }

    private void addFrontierWalls(int x, int y) {
        // Check all four directions (2 steps away to maintain wall structure)
        int[][] directions = { { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, 0 } };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            // Check bounds - keep border walls intact
            if (nx >= 1 && nx <= WIDTH - 2 && ny >= 1 && ny <= HEIGHT - 2) {
                // If the neighbor is a wall (unvisited)
                if (!maze[ny][nx]) {
                    Wall wall = new Wall(x, y, nx, ny);
                    // Avoid duplicates
                    boolean exists = false;
                    for (Wall w : frontier) {
                        if ((w.x1 == x && w.y1 == y && w.x2 == nx && w.y2 == ny) ||
                                (w.x1 == nx && w.y1 == ny && w.x2 == x && w.y2 == y)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        frontier.add(wall);
                    }
                }
            }
        }
    }

    private boolean canConnect(Wall wall) {
        // Check if exactly one of the cells is visited
        boolean cell1Visited = maze[wall.y1][wall.x1];
        boolean cell2Visited = maze[wall.y2][wall.x2];

        return cell1Visited != cell2Visited;
    }
}
