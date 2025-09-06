package finalCode;

import java.util.List;

/**
 * A container for the results of the A* algorithm execution.
 * It includes the found path, the execution time in nanoseconds,
 * and the number of nodes explored.
 */
public class Result {
    public List<Node> path;
    public long executionTime;
    public int exploredNodes;
    

    public Result(List<Node> path, long executionTime, int exploredNodes) {
        this.path = path;
        this.executionTime = executionTime;
        this.exploredNodes = exploredNodes;
    }

}
