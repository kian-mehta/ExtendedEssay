package finalCode;

public class TestWilsons {
    public static void main(String[] args) {
        System.out.println("Testing Wilson's algorithm with candidate walls...");

        // Create a small maze for testing
        WilsonsSimple wilson = new WilsonsSimple(501, 501);
        System.out.println("Generating");
        boolean[][] perfectMaze = wilson.generatePerfectMaze();

        System.out.println("Perfect maze:");
        printMaze(perfectMaze);

        for (float k : DataCollector.kFACTORS) {

            wilson.setImperfection(k);
            boolean[][] imperfectMaze = wilson.imperfectifyMaze(perfectMaze);

            System.out.println("Imperfect maze k = " + k + ":");
            printMaze(imperfectMaze);
        }

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
