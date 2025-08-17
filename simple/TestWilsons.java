package simple;

public class TestWilsons {
    public static void main(String[] args) {
        System.out.println("Testing Wilson's algorithm with candidate walls...");
        
        // Create a small maze for testing
        WilsonsSimple wilson = new WilsonsSimple(11, 11);
        boolean[][] maze = wilson.generateMaze();
        
        System.out.println("Generated maze:");
        printMaze(maze);
        
        System.out.println("\nMaze generation completed successfully!");
    }
    
    private static void printMaze(boolean[][] maze) {
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[y].length; x++) {
                System.out.print(maze[y][x] ? "  " : "██");
            }
            System.out.println();
        }
    }
}
