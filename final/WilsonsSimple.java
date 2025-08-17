package simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WilsonsSimple {

    private static int WIDTH = 25;
    private static int HEIGHT = 25;
    private float IMPERFECTION = 0.3f;

    private boolean[][] maze;
    private boolean[][] inMaze;
    private List<Point> unvisited;
    private List<Point> currentPath;
    private Point currentWalker;
    private Random random;

    public WilsonsSimple(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
    }

    private void initializeMaze() {
        random = new Random();
        maze = new boolean[HEIGHT][WIDTH];
        inMaze = new boolean[HEIGHT][WIDTH];
        unvisited = new ArrayList<>();
        currentPath = new ArrayList<>();
        currentWalker = null;

        // Initialize maze with all walls (false = wall, true = path)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                maze[y][x] = false;
                inMaze[y][x] = false;
                // Add odd coordinates as potential cells
                if (x % 2 == 1 && y % 2 == 1 && x >= 1 && x <= WIDTH - 2 && y >= 1 && y <= HEIGHT - 2) {
                    unvisited.add(new Point(x, y));
                }
            }
        }
    }

    public boolean[][] generateMaze() {
        initializeMaze();

        // Wilson's algorithm: Start with random cell in maze
        if (!unvisited.isEmpty()) {
            Point start = unvisited.get(random.nextInt(unvisited.size()));
            addToMaze(start);
        }

        // Loop until all cells are in maze
        while (!unvisited.isEmpty()) {
            // Start random walk from unvisited cell
            Point walkStart = unvisited.get(random.nextInt(unvisited.size()));
            performRandomWalk(walkStart);
        }

        // Ensure start and end points are accessible for AI training
        ensureStartEndConnectivity();

        // Add imperfections to the maze
        addExtraEdges();

        return maze;
    }

    private void performRandomWalk(Point start) {
        currentPath.clear();
        currentWalker = new Point(start.x, start.y);
        currentPath.add(new Point(currentWalker.x, currentWalker.y));

        // Perform loop-erased random walk
        int maxSteps = WIDTH * HEIGHT * 10; // Safety limit to prevent infinite loops
        int stepCount = 0;

        while (!inMaze[currentWalker.y][currentWalker.x] && stepCount < maxSteps) {
            // Get random neighbor
            Point nextStep = getRandomNeighbor(currentWalker);

            // Check if we're creating a loop
            int loopIndex = findInPath(nextStep);
            if (loopIndex >= 0) {
                // Erase loop by removing everything after loop point
                currentPath = new ArrayList<>(currentPath.subList(0, loopIndex + 1));
            } else {
                currentPath.add(nextStep);
            }

            currentWalker = nextStep;
            stepCount++;
        }

        if (stepCount >= maxSteps) {
            System.err.println("Warning: Random walk exceeded maximum steps, terminating");
        }

        // Add entire path to maze
        for (int i = 0; i < currentPath.size() - 1; i++) {
            Point p1 = currentPath.get(i);
            Point p2 = currentPath.get(i + 1);

            addToMaze(p1);

            // Add connecting wall
            int wallX = (p1.x + p2.x) / 2;
            int wallY = (p1.y + p2.y) / 2;
            if (wallX >= 0 && wallX < WIDTH && wallY >= 0 && wallY < HEIGHT) {
                maze[wallY][wallX] = true;
            }
        }

        // Make sure the last cell in path is also added
        if (!currentPath.isEmpty()) {
            Point lastCell = currentPath.get(currentPath.size() - 1);
            if (!inMaze[lastCell.y][lastCell.x]) {
                addToMaze(lastCell);
            }
        }

        currentWalker = null;
    }

    private Point getRandomNeighbor(Point p) {
        int[][] directions = { { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, 0 } };
        List<Point> neighbors = new ArrayList<>();

        for (int[] dir : directions) {
            int nx = p.x + dir[0];
            int ny = p.y + dir[1];

            // Check bounds - keep border walls intact
            if (nx >= 1 && nx <= WIDTH - 2 && ny >= 1 && ny <= HEIGHT - 2) {
                neighbors.add(new Point(nx, ny));
            }
        }

        // If no valid neighbors, try adjacent cells (fallback)
        if (neighbors.isEmpty()) {
            for (int[] dir : directions) {
                int nx = p.x + dir[0] / 2;
                int ny = p.y + dir[1] / 2;
                if (nx >= 1 && nx <= WIDTH - 2 && ny >= 1 && ny <= HEIGHT - 2) {
                    neighbors.add(new Point(nx, ny));
                }
            }
        }

        // Final fallback - if still no neighbors, return current point (should not
        // happen)
        if (neighbors.isEmpty()) {
            System.err.println("Warning: No valid neighbors found for point (" + p.x + ", " + p.y + ")");
            return new Point(p.x, p.y);
        }

        return neighbors.get(random.nextInt(neighbors.size()));
    }

    private int findInPath(Point p) {
        for (int i = 0; i < currentPath.size(); i++) {
            Point pathPoint = currentPath.get(i);
            if (pathPoint.x == p.x && pathPoint.y == p.y) {
                return i;
            }
        }
        return -1;
    }

    // Helper method to check if a point is in the current path
    private boolean isPointInPath(int x, int y) {
        for (Point p : currentPath) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }

    private void addToMaze(Point p) {
        maze[p.y][p.x] = true;
        inMaze[p.y][p.x] = true;
        unvisited.remove(p);
    }

    private void ensureStartEndConnectivity() {
        // Ensure start point (1,1) and end point (WIDTH-2, HEIGHT-2) are paths
        maze[1][1] = true;
        maze[HEIGHT - 2][WIDTH - 2] = true;

        // Create entrance and exit by clearing border walls
        maze[0][1] = true; // Top entrance
        maze[HEIGHT - 1][WIDTH - 2] = true; // Bottom exit
    }

    private void addExtraEdges() {
        int E0 = HEIGHT * WIDTH - 1;
        int k = Math.round(IMPERFECTION * E0);

        // Get all candidate walls
        List<Pair> candidateWalls = getCandidateWalls();

        // Randomly select k walls to remove for imperfection
        for (int i = 0; i < k && !candidateWalls.isEmpty(); i++) {
            int randomIndex = random.nextInt(candidateWalls.size());
            Pair wallPair = candidateWalls.get(randomIndex);

            // Remove the wall between the two cells
            int wallX = (wallPair.a.x + wallPair.b.x) / 2;
            int wallY = (wallPair.a.y + wallPair.b.y) / 2;

            if (wallX >= 0 && wallX < WIDTH && wallY >= 0 && wallY < HEIGHT) {
                maze[wallY][wallX] = true; // Remove wall (make it a path)
            }

            candidateWalls.remove(randomIndex);
        }
    }

    private List<Pair> getCandidateWalls() {
        List<Pair> candidateWalls = new ArrayList<>();

        // Check all cells in the maze
        for (int y = 1; y < HEIGHT - 1; y += 2) { // Only check odd positions (actual cells)
            for (int x = 1; x < WIDTH - 1; x += 2) {
                Point cellA = new Point(x, y);

                // Check if this cell is a path (part of the maze)
                if (maze[y][x]) {
                    // Check 4-neighbors (up, down, left, right)
                    int[][] directions = { { 0, -2 }, { 0, 2 }, { -2, 0 }, { 2, 0 } }; // up, down, left, right

                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];

                        // Check bounds
                        if (nx >= 1 && nx < WIDTH - 1 && ny >= 1 && ny < HEIGHT - 1) {
                            Point cellB = new Point(nx, ny);

                            // Check if neighbor is also a path
                            if (maze[ny][nx]) {
                                // Check if there's still a wall between them
                                int wallX = (x + nx) / 2;
                                int wallY = (y + ny) / 2;

                                if (!maze[wallY][wallX]) { // Wall still exists
                                    Pair candidatePair = new Pair(cellA, cellB);

                                    // Add only if not already in list (avoid duplicates due to unordered nature)
                                    boolean alreadyExists = false;
                                    for (Pair existing : candidateWalls) {
                                        if (existing.equals(candidatePair)) {
                                            alreadyExists = true;
                                            break;
                                        }
                                    }

                                    if (!alreadyExists) {
                                        candidateWalls.add(candidatePair);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return candidateWalls;
    }

    class Point {
        int x;
        int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Pair {
        Point a, b;

        Pair(Point a, Point b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Pair pair = (Pair) obj;
            // Unordered pair: {a,b} equals {b,a}
            return (a.x == pair.a.x && a.y == pair.a.y && b.x == pair.b.x && b.y == pair.b.y) ||
                    (a.x == pair.b.x && a.y == pair.b.y && b.x == pair.a.x && b.y == pair.a.y);
        }

        @Override
        public int hashCode() {
            // Ensure unordered pairs have same hash code
            int hash1 = a.x * 31 + a.y;
            int hash2 = b.x * 31 + b.y;
            return Math.min(hash1, hash2) * 31 + Math.max(hash1, hash2);
        }
    }
}
