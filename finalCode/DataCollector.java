package finalCode;

public class DataCollector {
    static int SIZES[] = { 101, 501 };
    static float[] kFACTORS = { 0.01f, 0.05f, 0.1f, 0.2f, 0.3f };
    // static float[] kFACTORS = { 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f };
    static int SAMPLES = 1;

    public static void main(String[] args) throws InterruptedException {
        WilsonsSimple mazeGenerator = new WilsonsSimple();

        AStar aStar = new AStar();
        JPS2 jps = new JPS2();

        boolean[][] maze;
        boolean[][][] mazes = new boolean[SAMPLES][][];
        for (int size : SIZES) {
            mazeGenerator.setSize(size, size);
            mazes = new boolean[SAMPLES][][];

            for (int i = 0; i < SAMPLES; i++) {
                System.out.print("|");
                mazes[i] = mazeGenerator.generatePerfectMaze();
            }
            System.out.println();
            Thread.sleep((long) 0.1);

            Summary aStarSummary = new Summary(size);
            Summary jpsSummary = new Summary(size);
            printHeader(aStarSummary, jpsSummary);
            for (int i = 0; i < SAMPLES; i++) {
                maze = mazes[i];
                Result aResult = aStar.findPath(maze);
                Result jResult = jps.findPath(maze);
                System.out.print((i + 1) + ", " +
                        aResult.executionTime + ", " + aResult.exploredNodes + ", " +
                        aResult.path.size());
                System.out.println(
                        ", " + jResult.executionTime + ", " + jResult.exploredNodes + ", " +
                                jResult.path.size());
                // aStarSummary.addResult(aResult);
                // jpsSummary.addResult(jResult);
            }
            // aStarSummary.evaluate();
            // jpsSummary.evaluate();
            System.out.println();
            // printTable(aStarSummary, jpsSummary);

            for (float k : kFACTORS) {
                aStarSummary = new Summary(size, k);
                jpsSummary = new Summary(size, k);
                mazeGenerator.setImperfection(k);
                printHeader(aStarSummary, jpsSummary);
                for (int i = 0; i < SAMPLES; i++) {

                    maze = mazeGenerator.imperfectifyMaze(mazes[i]);
                    Thread.sleep((long) 0.1);

                    Result aResult = aStar.findPath(maze);
                    Thread.sleep((long) 0.1);
                    Result jResult = jps.findPath(maze);
                    // aStarSummary.addResult(aResult);
                    // jpsSummary.addResult(jResult);
                    System.out.print((i + 1) + ", " +
                            aResult.executionTime + ", " + aResult.exploredNodes + ", " +
                            aResult.path.size());
                    System.out.println(
                            ", " + jResult.executionTime + ", " + jResult.exploredNodes + ", " +
                                    jResult.path.size());
                }
                // aStarSummary.evaluate();
                // jpsSummary.evaluate();
                System.out.println();
                // printTable(aStarSummary, jpsSummary);
            }

        }
    }

    static void printTable(Summary aStar, Summary jps) {
        System.out.println("\t\t\tA*\t\tJPS");
        System.out.println("Avg. Execution:\t\t" + aStar.averageRuntime + " ns\t" + jps.averageRuntime + " ns");
        System.out.println("Avg. Nodes Explored:\t" + aStar.averageNodesExplored + "\t\t" + jps.averageNodesExplored);
        System.out.println("Avg. Path Length:\t" + aStar.averagePathLength + "\t\t" + jps.averagePathLength);
        System.out.println("\n");
    }

    static void printHeader(Summary aStar, Summary jps) {
        System.out.println("\t\t" + aStar.type + " " + aStar.size + "x" + aStar.size + " "
                + (aStar.type == MazeType.IMPERFECT ? "k=" + aStar.kFactor : ""));
    }

    static class Summary {
        int size;
        float kFactor;
        MazeType type;
        Result[] results;

        private long averageRuntime;
        private float averageNodesExplored;
        private float averagePathLength;

        private int counter;

        Summary(int size, float kFactor) {
            this.size = size;
            this.kFactor = kFactor;
            type = kFactor == 0 ? MazeType.PERFECT : MazeType.IMPERFECT;
            results = new Result[DataCollector.SAMPLES];
        }

        Summary(int size) {
            this(size, 0f);
        }

        void addResult(Result r) {
            results[counter++] = r;
        }

        void evaluate() {
            if (counter == results.length) {
                for (Result r : results) {
                    averageRuntime += r.executionTime;
                    averageNodesExplored += r.exploredNodes;
                    averagePathLength += r.path.size();
                }
                averageRuntime /= results.length;
                averageNodesExplored /= results.length;
                averagePathLength /= results.length;
            }
        }

        public long getAverageRuntime() {
            return averageRuntime;
        }

        public float getAverageNodesExplored() {
            return averageNodesExplored;
        }
    }

    enum MazeType {
        PERFECT, IMPERFECT
    }

}
