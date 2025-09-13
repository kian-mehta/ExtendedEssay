package finalCode;

import java.util.*;

public class JPS {

    // (Unused constants from your snippet left here in case you use them elsewhere)
    static final int SAMPLES = 10;
    static final int HEIGHT = 501;
    static final int WIDTH = 501;

    /**
     * Finds the shortest path in a maze from (1,1) to (rows-2, cols-2) using the JPS algorithm.
     * Assumes boundary walls if you used that convention. Change start/end if you need.
     *
     * @param maze A 2D boolean array representing the maze. true = walkable, false = wall.
     * @return A Result object containing the path (list of Nodes), execution time (ns), and number
     *         of explored nodes.
     */
    public Result findPath(boolean[][] maze) {
        long startTime = System.nanoTime();

        // Default start & goal (you can adapt these to parameters if required)
        Node startNode = new Node(1, 1);
        Node endNode = new Node(maze.length - 2, maze[0].length - 2);

        Comparator<Node> cmp = Comparator
                .comparingInt((Node n) -> n.f)
                .thenComparingInt(n -> n.h); // tie-breaker on heuristic

        PriorityQueue<Node> openList = new PriorityQueue<>(cmp);
        Set<String> closedList = new HashSet<>();
        Map<String, Node> openMap = new HashMap<>();

        startNode.g = 0;
        startNode.h = calculateHeuristic(startNode, endNode);
        startNode.f = startNode.g + startNode.h;
        openList.add(startNode);
        openMap.put(key(startNode.row, startNode.col), startNode);

        int exploredNodes = 0;

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            String currentKey = key(currentNode.row, currentNode.col);
            openMap.remove(currentKey);

            if (closedList.contains(currentKey)) {
                continue;
            }

            // Check goal
            if (currentNode.row == endNode.row && currentNode.col == endNode.col) {
                List<Node> path = reconstructPath(currentNode);
                long endTime = System.nanoTime();
                System.out.println("Path Found");
                return new Result(path, endTime - startTime, exploredNodes);
            }

            closedList.add(currentKey);
            exploredNodes++;

            // Get jump point successors
            List<Node> successors = getSuccessors(currentNode, maze, endNode);

            for (Node successor : successors) {
                String successorKey = key(successor.row, successor.col);

                if (closedList.contains(successorKey)) {
                    continue;
                }

                // Distance from current to successor (Manhattan over grid steps)
                int distance = Math.abs(successor.row - currentNode.row) + Math.abs(successor.col - currentNode.col);
                int tentativeG = currentNode.g + distance;

                Node existingNode = openMap.get(successorKey);
                if (existingNode == null || tentativeG < existingNode.g) {
                    successor.parent = currentNode;
                    successor.g = tentativeG;
                    successor.h = calculateHeuristic(successor, endNode);
                    successor.f = successor.g + successor.h;

                    if (existingNode != null) {
                        openList.remove(existingNode); // O(n) remove, acceptable for small grids
                    }

                    openList.add(successor);
                    openMap.put(successorKey, successor);
                }
            }
        }

        long endTime = System.nanoTime();
        // No path found
        System.out.println("Path NOT Found");
        return new Result(null, endTime - startTime, exploredNodes);
    }

    private String key(int r, int c) {
        return r + "," + c;
    }

    /**
     * Manhattan heuristic.
     */
    private int calculateHeuristic(Node from, Node to) {
        return Math.abs(from.row - to.row) + Math.abs(from.col - to.col);
    }

    /**
     * Get successors for JPS from node.
     * If node.parent == null, explore all 8 directions; otherwise explore natural neighbors.
     */
    private List<Node> getSuccessors(Node node, boolean[][] maze, Node goal) {
        List<Node> successors = new ArrayList<>();

        if (node.parent == null) {
            // Initial node - explore all 8 directions
            int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
            int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };

            for (int i = 0; i < 8; i++) {
                Node jumpPoint = jump(node.row, node.col, dx[i], dy[i], maze, goal);
                if (jumpPoint != null) {
                    jumpPoint.dx = dx[i];
                    jumpPoint.dy = dy[i];
                    successors.add(jumpPoint);
                }
            }
        } else {
            // Natural neighbors based on parent direction
            List<int[]> neighbors = getNeighbors(node, maze);
            for (int[] dir : neighbors) {
                Node jumpPoint = jump(node.row, node.col, dir[0], dir[1], maze, goal);
                if (jumpPoint != null) {
                    jumpPoint.dx = dir[0];
                    jumpPoint.dy = dir[1];
                    successors.add(jumpPoint);
                }
            }
        }

        return successors;
    }

    /**
     * Jump function: recursively step in direction (dx,dy) until a jump point or obstacle/goal.
     */
    private Node jump(int row, int col, int dx, int dy, boolean[][] maze, Node goal) {
        int newRow = row + dx;
        int newCol = col + dy;

        // Check bounds and walls
        if (!isWalkable(newRow, newCol, maze)) {
            return null;
        }

        // Check if this is the goal
        if (newRow == goal.row && newCol == goal.col) {
            return new Node(newRow, newCol, dx, dy);
        }

        // Check for forced neighbors
        if (hasForcedNeighbors(newRow, newCol, dx, dy, maze)) {
            return new Node(newRow, newCol, dx, dy);
        }

        // If moving diagonally, check horizontal and vertical components for jump points
        if (dx != 0 && dy != 0) {
            if (jump(newRow, newCol, dx, 0, maze, goal) != null || jump(newRow, newCol, 0, dy, maze, goal) != null) {
                return new Node(newRow, newCol, dx, dy);
            }
        }

        // Continue jumping in same direction
        return jump(newRow, newCol, dx, dy, maze, goal);
    }

    /**
     * Forced neighbor detection (corrected).
     *
     * Using row/col indexing where dx affects row and dy affects col.
     */
    private boolean hasForcedNeighbors(int row, int col, int dx, int dy, boolean[][] maze) {
        // Diagonal movement
        if (dx != 0 && dy != 0) {
            // If one of the side neighbors is blocked but the corresponding diagonal is free -> forced
            boolean cond1 = !isWalkable(row - dx, col, maze) && isWalkable(row - dx, col + dy, maze);
            boolean cond2 = !isWalkable(row, col - dy, maze) && isWalkable(row + dx, col - dy, maze);
            return cond1 || cond2;
        }

        // Horizontal movement (dx == 0, dy != 0)
        if (dx == 0 && dy != 0) {
            // If either up or down is blocked while the corresponding shifted cell is free
            boolean cond1 = !isWalkable(row - 1, col, maze) && isWalkable(row - 1, col + dy, maze);
            boolean cond2 = !isWalkable(row + 1, col, maze) && isWalkable(row + 1, col + dy, maze);
            return cond1 || cond2;
        }

        // Vertical movement (dx != 0, dy == 0)
        if (dx != 0 && dy == 0) {
            boolean cond1 = !isWalkable(row, col - 1, maze) && isWalkable(row + dx, col - 1, maze);
            boolean cond2 = !isWalkable(row, col + 1, maze) && isWalkable(row + dx, col + 1, maze);
            return cond1 || cond2;
        }

        return false;
    }

    /**
     * Get natural neighbors based on parent direction. (Simplified prune.)
     * Returns only natural neighbors - forced neighbors are detected in jump().
     */
    private List<int[]> getNeighbors(Node node, boolean[][] maze) {
        List<int[]> neighbors = new ArrayList<>();

        int dx = Integer.signum(node.row - node.parent.row);
        int dy = Integer.signum(node.col - node.parent.col);

        // Diagonal movement - include diagonal and its straight components if walkable
        if (dx != 0 && dy != 0) {
            if (isWalkable(node.row + dx, node.col + dy, maze)) neighbors.add(new int[] { dx, dy });
            if (isWalkable(node.row + dx, node.col, maze)) neighbors.add(new int[] { dx, 0 });
            if (isWalkable(node.row, node.col + dy, maze)) neighbors.add(new int[] { 0, dy });
        } else if (dx != 0) {
            // Vertical movement
            if (isWalkable(node.row + dx, node.col, maze)) neighbors.add(new int[] { dx, 0 });
            // Optionally include diagonal moves when side cells are free
            if (isWalkable(node.row + dx, node.col + 1, maze)) neighbors.add(new int[] { dx, 1 });
            if (isWalkable(node.row + dx, node.col - 1, maze)) neighbors.add(new int[] { dx, -1 });
        } else if (dy != 0) {
            // Horizontal movement
            if (isWalkable(node.row, node.col + dy, maze)) neighbors.add(new int[] { 0, dy });
            // Optionally include diagonal moves when side cells are free
            if (isWalkable(node.row + 1, node.col + dy, maze)) neighbors.add(new int[] { 1, dy });
            if (isWalkable(node.row - 1, node.col + dy, maze)) neighbors.add(new int[] { -1, dy });
        }

        return neighbors;
    }

    /**
     * Helper: true if out-of-bounds or wall -> treated as blocked.
     */
    private boolean isBlocked(int row, int col, boolean[][] maze) {
        return row < 0 || row >= maze.length || col < 0 || col >= maze[0].length || !maze[row][col];
    }

    /**
     * Helper: true if inside bounds and walkable.
     */
    private boolean isWalkable(int row, int col, boolean[][] maze) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col];
    }

    /**
     * Reconstruct path by following parent pointers.
     */
    private List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
