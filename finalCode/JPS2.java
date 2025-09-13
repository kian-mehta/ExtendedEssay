package finalCode;

import java.util.*;

/**
 * A Jump-Point-Search-like implementation restricted to 4-connected grids.
 *
 * Notes/assumptions:
 * - The maze is a boolean[][] passed to findPath().
 * **true** = free/passable, **false** = blocked/wall.
 * - Movement cost for every step is 1 (uniform-cost grid).
 * - Heuristic: Manhattan distance.
 * - Maze is square of odd size, but algorithm does not strictly need the
 * odd-size property.
 * - This implementation uses a simplified 4-connected JPS: it "jumps" along
 * straight
 * cardinal directions until hitting the goal, a blocked cell, or a junction
 * (where the
 * local passable-neighbour pattern changes).
 */
public class JPS2 {

    // Directions: up, down, left, right
    private final int[][] DIRS = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    private int rows, cols;

    /**
     * Find a path using a 4-connected Jump-Point-Search-like algorithm.
     *
     * @param maze boolean[][] maze where true = free, false = wall
     * @return Result containing path (list of Nodes from start to target),
     *         execution time (ns), and explored node count
     */
    public Result findPath(boolean[][] maze) {
        long t0 = System.nanoTime();
        int sr, sc, tr, tc;

        if (maze == null)
            throw new IllegalStateException("maze must be provided");
        rows = maze.length;
        cols = maze[0].length;
        sr = 1;
        sc = 1;
        tr = rows - 2;
        tc = cols - 2;

        if (!inBounds(sr, sc) || !inBounds(tr, tc) || !maze[sr][sc] || !maze[tr][tc]) {
            return new Result(Collections.emptyList(), 0L, 0);
        }

        Node[][] nodes = new Node[rows][cols];
        int INF = Integer.MAX_VALUE / 4;
        int[][] gScore = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            Arrays.fill(gScore[i], INF);
        boolean[][] closed = new boolean[rows][cols];

        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.<Node>comparingInt(a -> a.f).thenComparingInt(a -> a.h));

        Node start = new Node(sr, sc);
        start.g = 0;
        start.h = manhattan(sr, sc, tr, tc);
        start.f = start.g + start.h;
        nodes[sr][sc] = start;
        gScore[sr][sc] = 0;
        open.add(start);

        int explored = 0;
        Node goalNode = null;

        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (closed[cur.row][cur.col])
                continue; // skip stale entries
            closed[cur.row][cur.col] = true;
            explored++;

            if (cur.row == tr && cur.col == tc) {
                goalNode = cur;
                break;
            }

            // Generate successors by attempting a "jump" in all 4 cardinal directions
            for (int[] d : DIRS) {
                int nr = cur.row + d[0];
                int nc = cur.col + d[1];
                if (!inBounds(nr, nc) || !maze[nr][nc])
                    continue; // immediate neighbor blocked or out

                Node jumpPoint = jump(maze, nr, nc, d[0], d[1], tr, tc);
                if (jumpPoint == null)
                    continue;

                int jr = jumpPoint.row;
                int jc = jumpPoint.col;

                // create or reuse node
                Node jp = nodes[jr][jc];
                if (jp == null) {
                    jp = new Node(jr, jc, d[0], d[1]);
                    nodes[jr][jc] = jp;
                }

                int stepCost = Math.abs(jr - cur.row) + Math.abs(jc - cur.col); // Manhattan steps
                int tentativeG = cur.g + stepCost;
                if (tentativeG < gScore[jr][jc]) {
                    gScore[jr][jc] = tentativeG;
                    jp.parent = cur;
                    jp.dx = d[0];
                    jp.dy = d[1];
                    jp.g = tentativeG;
                    jp.h = manhattan(jr, jc, tr, tc);
                    jp.f = jp.g + jp.h;
                    open.add(jp);
                }
            }
        }

        long t1 = System.nanoTime();
        List<Node> path = Collections.emptyList();
        if (goalNode != null) {
            path = reconstructPath(goalNode);
        }

        return new Result(path, t1 - t0, explored);
    }

    // The jump routine
    private Node jump(boolean[][] maze, int r, int c, int dx, int dy, int tr, int tc) {
        int x = r;
        int y = c;
        while (true) {
            if (!inBounds(x, y) || !maze[x][y])
                return null;
            if (x == tr && y == tc)
                return new Node(x, y, dx, dy);

            // Check whether current cell is a junction/forced neighbor
            List<int[]> passableDirs = new ArrayList<>();
            for (int[] d : DIRS) {
                int nx = x + d[0];
                int ny = y + d[1];
                if (inBounds(nx, ny) && maze[nx][ny]) {
                    passableDirs.add(d);
                }
            }

            boolean isCorridor = false;
            if (passableDirs.size() == 2) {
                int[] a = passableDirs.get(0);
                int[] b = passableDirs.get(1);
                if (a[0] == -b[0] && a[1] == -b[1]) {
                    isCorridor = true;
                }
            }

            if (!isCorridor) {
                return new Node(x, y, dx, dy);
            }

            x += dx;
            y += dy;
        }
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private int manhattan(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    private List<Node> reconstructPath(Node goal) {
        LinkedList<Node> path = new LinkedList<>();
        Node cur = goal;
        while (cur.parent != null) {
            int dx = Integer.signum(cur.row - cur.parent.row);
            int dy = Integer.signum(cur.col - cur.parent.col);
            int r = cur.row;
            int c = cur.col;
            while (r != cur.parent.row || c != cur.parent.col) {
                path.addFirst(new Node(r, c));
                r -= dx;
                c -= dy;
            }
            cur = cur.parent;
        }
        path.addFirst(cur); // add start
        return path;
    }
}
