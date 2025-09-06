package finalCode;

import java.util.*;

public class AStar {

    static final int SAMPLES = 10;
    static final int HEIGHT = 501;
    static final int WIDTH = 501;

    /**
     * Represents a node in the maze for the A* algorithm.
     * Each node has a position (row, col), cost from the start (g),
     * heuristic cost to the end (h), total cost (f = g + h),
     * and a reference to its parent node for path reconstruction.
     */

    /**
     * Finds the shortest path in a maze from 'S' to 'E' using the A* algorithm.
     *
     * @param maze A 2D char array representing the maze. 'S' is start, 'E' is end,
     *             '0' is a wall, and '.' is a path.
     * @return An Result object containing the path, execution time, and number
     *         of explored nodes.
     */
    public Result findPath(boolean[][] maze) {
        long startTime = System.nanoTime();

        Node startNode = null;
        Node endNode = null;

        startNode = new Node(1, 1);
        endNode = new Node(maze.length - 2, maze[0].length - 2);

        if (startNode == null || endNode == null) {
            return new Result(null, System.nanoTime() - startTime, 0);
        }

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(node -> node.f));
        Set<Node> closedList = new HashSet<>();

        startNode.g = 0;
        startNode.h = calculateHeuristic(startNode, endNode);
        startNode.f = startNode.g + startNode.h;
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (closedList.contains(currentNode)) {
                continue;
            }

            if (currentNode.equals(endNode)) {
                List<Node> path = reconstructPath(currentNode);
                long endTime = System.nanoTime();
                return new Result(path, endTime - startTime, closedList.size());
            }

            closedList.add(currentNode);

            // Explore neighbors (Up, Down, Left, Right)
            int[] dr = { -1, 1, 0, 0 };
            int[] dc = { 0, 0, -1, 1 };

            for (int i = 0; i < 4; i++) {
                int newRow = currentNode.row + dr[i];
                int newCol = currentNode.col + dc[i];

                if (newRow < 0 || newRow >= maze.length || newCol < 0 || newCol >= maze[0].length || // bound check
                        !maze[newRow][newCol]) { // wall check
                    continue;
                }

                Node neighbor = new Node(newRow, newCol);
                if (closedList.contains(neighbor)) {
                    continue;
                }

                // A better path to the neighbor has been found
                neighbor.parent = currentNode;
                neighbor.g = currentNode.g + 1; // Cost of 1 for each step
                neighbor.h = calculateHeuristic(neighbor, endNode);
                neighbor.f = neighbor.g + neighbor.h;

                openList.add(neighbor);
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
