package finalCode;

import java.util.*;

public class JPS {

    static final int SAMPLES = 10;
    static final int HEIGHT = 501;
    static final int WIDTH = 501;

    /**
     * Finds the shortest path in a maze from 'S' to 'E' using the JPS algorithm.
     *
     * @param maze A 2D boolean array representing the maze. true is walkable, false
     *             is wall.
     * @return An Result object containing the path, execution time, and number
     *         of explored nodes.
     */
    public Result findPath(boolean[][] maze) {
        long startTime = System.nanoTime();

        Node startNode = new Node(1, 1);
        Node endNode = new Node(maze.length - 2, maze[0].length - 2);

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(node -> node.f));
        Set<String> closedList = new HashSet<>();
        Map<String, Node> openMap = new HashMap<>();

        startNode.g = 0;
        startNode.h = calculateHeuristic(startNode, endNode);
        startNode.f = startNode.g + startNode.h;
        openList.add(startNode);
        openMap.put(startNode.row + "," + startNode.col, startNode);

        int exploredNodes = 0;

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            String currentKey = currentNode.row + "," + currentNode.col;
            openMap.remove(currentKey);

            if (closedList.contains(currentKey)) {
                continue;
            }

            if (currentNode.row == endNode.row && currentNode.col == endNode.col) {
                List<Node> path = reconstructPath(currentNode);
                long endTime = System.nanoTime();
                return new Result(path, endTime - startTime, exploredNodes);
            }

            closedList.add(currentKey);
            exploredNodes++;

            // Get jump point successors
            List<Node> successors = getSuccessors(currentNode, maze);

            for (Node successor : successors) {
                String successorKey = successor.row + "," + successor.col;

                if (closedList.contains(successorKey)) {
                    continue;
                }

                // Calculate distance to successor (can be > 1 due to jumping)
                int distance = Math.abs(successor.row - currentNode.row) + Math.abs(successor.col - currentNode.col);
                int tentativeG = currentNode.g + distance;

                Node existingNode = openMap.get(successorKey);
                if (existingNode == null || tentativeG < existingNode.g) {
                    successor.parent = currentNode;
                    successor.g = tentativeG;
                    successor.h = calculateHeuristic(successor, endNode);
                    successor.f = successor.g + successor.h;

                    if (existingNode != null) {
                        openList.remove(existingNode);
                    }

                    openList.add(successor);
                    openMap.put(successorKey, successor);
                }
            }
        }

        long endTime = System.nanoTime();
        // No path found
        return new Result(null, endTime - startTime, closedList.size());
    }

    /**
     * Calculates the heuristic (Manhattan distance) from one node to another.
     * 
     * @param from The starting node.
     * @param to   The target node.
     * @return The Manhattan distance.
     */
    private int calculateHeuristic(Node from, Node to) {
        return Math.abs(from.row - to.row) + Math.abs(from.col - to.col);
    }

    /**
     * Gets jump point successors for JPS algorithm.
     */
    private List<Node> getSuccessors(Node node, boolean[][] maze) {
        List<Node> successors = new ArrayList<>();

        if (node.parent == null) {
            // Initial node - explore all 8 directions
            int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
            int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };

            for (int i = 0; i < 8; i++) {
                Node jumpPoint = jump(node.row, node.col, dx[i], dy[i], maze);
                if (jumpPoint != null) {
                    jumpPoint.dx = dx[i];
                    jumpPoint.dy = dy[i];
                    successors.add(jumpPoint);
                }
            }
        } else {
            // Get natural neighbors based on parent direction
            List<int[]> neighbors = getNeighbors(node);

            for (int[] dir : neighbors) {
                Node jumpPoint = jump(node.row, node.col, dir[0], dir[1], maze);
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
     * Jump function for JPS algorithm - finds next jump point in given direction.
     */
    private Node jump(int row, int col, int dx, int dy, boolean[][] maze) {
        int newRow = row + dx;
        int newCol = col + dy;

        // Check bounds and walls
        if (newRow < 0 || newRow >= maze.length || newCol < 0 || newCol >= maze[0].length || !maze[newRow][newCol]) {
            return null;
        }

        // Check if this is the goal
        if (newRow == maze.length - 2 && newCol == maze[0].length - 2) {
            return new Node(newRow, newCol, dx, dy);
        }

        // Check for forced neighbors
        if (hasForcedNeighbors(newRow, newCol, dx, dy, maze)) {
            return new Node(newRow, newCol, dx, dy);
        }

        // Diagonal movement - check horizontal and vertical jump points
        if (dx != 0 && dy != 0) {
            if (jump(newRow, newCol, dx, 0, maze) != null || jump(newRow, newCol, 0, dy, maze) != null) {
                return new Node(newRow, newCol, dx, dy);
            }
        }

        // Continue jumping in the same direction
        return jump(newRow, newCol, dx, dy, maze);
    }

    /**
     * Checks if a node has forced neighbors.
     */
    private boolean hasForcedNeighbors(int row, int col, int dx, int dy, boolean[][] maze) {
        if (dx != 0 && dy != 0) {
            // Diagonal movement
            return (isBlocked(row - dx, col, maze) && isWalkable(row - dx, col + dy, maze)) ||
                    (isBlocked(row, col - dy, maze) && isWalkable(row + dx, col - dy, maze));
        } else if (dx != 0) {
            // Horizontal movement
            return (isBlocked(row + 1, col - dx, maze) && isWalkable(row + 1, col, maze)) ||
                    (isBlocked(row - 1, col - dx, maze) && isWalkable(row - 1, col, maze));
        } else {
            // Vertical movement
            return (isBlocked(row - dy, col + 1, maze) && isWalkable(row, col + 1, maze)) ||
                    (isBlocked(row - dy, col - 1, maze) && isWalkable(row, col - 1, maze));
        }
    }

    /**
     * Gets natural neighbors for a node based on its parent direction.
     */
    private List<int[]> getNeighbors(Node node) {
        List<int[]> neighbors = new ArrayList<>();

        int dx = Integer.compare(node.row - node.parent.row, 0);
        int dy = Integer.compare(node.col - node.parent.col, 0);

        if (dx != 0 && dy != 0) {
            // Diagonal movement - continue diagonal and its components
            neighbors.add(new int[] { dx, dy }); // Continue diagonal
            neighbors.add(new int[] { dx, 0 }); // Horizontal component
            neighbors.add(new int[] { 0, dy }); // Vertical component
        } else if (dx != 0) {
            // Horizontal movement
            neighbors.add(new int[] { dx, 0 }); // Continue horizontal
        } else {
            // Vertical movement
            neighbors.add(new int[] { 0, dy }); // Continue vertical
        }

        return neighbors;
    }

    /**
     * Helper method to check if a position is blocked (wall or out of bounds).
     */
    private boolean isBlocked(int row, int col, boolean[][] maze) {
        return row < 0 || row >= maze.length || col < 0 || col >= maze[0].length || !maze[row][col];
    }

    /**
     * Helper method to check if a position is walkable.
     */
    private boolean isWalkable(int row, int col, boolean[][] maze) {
        return row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col];
    }

    /**
     * Reconstructs the path from the end node back to the start node.
     * 
     * @param endNode The end node of the path.
     * @return A list of nodes representing the path from start to end.
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
