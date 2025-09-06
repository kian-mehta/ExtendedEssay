package finalCode;

import java.util.Objects;

/**
 * Represents a node in the maze for the JPS algorithm.
 * Each node has a position (row, col), cost from the start (g),
 * heuristic cost to the end (h), total cost (f = g + h),
 * parent direction, and a reference to its parent node for path reconstruction.
 */
public class Node {
    int row, col;
    int g, h, f;
    Node parent;
    int dx, dy; // Direction from parent to this node

    public Node(int row, int col) {
        this.row = row;
        this.col = col;
        this.g = 0;
        this.h = 0;
        this.f = 0;
        this.parent = null;
        this.dx = 0;
        this.dy = 0;
    }

    public Node(int row, int col, int dx, int dy) {
        this.row = row;
        this.col = col;
        this.g = 0;
        this.h = 0;
        this.f = 0;
        this.parent = null;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Node node = (Node) obj;
        return row == node.row && col == node.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
