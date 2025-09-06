package finalCode;

public class TestWilsons {
    public static void main(String[] args) {
        System.out.println("Testing Wilson's algorithm with candidate walls...");

        // Create a small maze for testing
        WilsonsSimple wilson = new WilsonsSimple(21, 21);
        System.out.println("Generating");
        boolean[][] perfectMaze = wilson.generatePerfectMaze();

        System.out.println("Perfect maze:");
        printMaze(perfectMaze);

        wilson.setImperfection(0.6f);
        boolean[][] imperfectMaze = wilson.imperfectifyMaze();

        System.out.println("Imperfect maze:");
        printMaze(imperfectMaze);

        System.out.println("\nMaze generation completed successfully!");

        testWalls(perfectMaze, imperfectMaze);
    }

    private static void printMaze(boolean[][] maze) {
        // System.out.println("\t0 1 2 3 4 5 6 7 8 9 10");
        for (int y = 0; y < maze.length; y++) {
            // System.out.print(y + "\t");
            for (int x = 0; x < maze[y].length; x++) {
                System.out.print(maze[y][x] ? "  " : "██");
            }
            System.out.println();
        }
    }

    private static void testWalls(boolean[][] perfect, boolean[][] imperfect) {
        for (int y = 0; y < perfect.length; y++) {
            for (int x = 0; x < perfect[0].length; x++) {
                if (perfect[y][x] && !imperfect[y][x]) {
                    System.out.println("imperfection at (" + x + ", " + y + ")");
                }
            }
        }
    }
}
