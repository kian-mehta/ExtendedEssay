package finalCode;

public class DataCollector {
    static int SIZES[] = { 11, 51, 101, 201 };
    static float[] kFACTORS = { 0.01f, 0.05f, 0.1f, 0.3f };
    static int SAMPLES = 100;

    public static void main(String[] args) {
        WilsonsSimple mazeGenerator = new WilsonsSimple();

        AStar aStar = new AStar();
        JPS jps = new JPS();

        boolean[][] maze;
        for (int size : SIZES) {
            mazeGenerator.setSize(size, size);
            Summary aStarSummary = new Summary(size);
            Summary jpsSummary = new Summary(size);
            for (int i = 0; i < SAMPLES; i++) {
                maze = mazeGenerator.generatePerfectMaze();
                aStarSummary.addResult(aStar.findPath(maze));
                jpsSummary.addResult(jps.findPath(maze));
            }
            aStarSummary.evaluate();
            jpsSummary.evaluate();
            printTable(aStarSummary, jpsSummary);

            for(float k : kFACTORS){
                aStarSummary = new Summary(size, k);
                jpsSummary = new Summary(size, k);
                mazeGenerator.setImperfection(k);
                for (int i = 0; i < SAMPLES; i++) {
                    maze = mazeGenerator.generateImperfectMaze();
                    aStarSummary.addResult(aStar.findPath(maze));
                    jpsSummary.addResult(jps.findPath(maze));
                }
                aStarSummary.evaluate();
                jpsSummary.evaluate();
                printTable(aStarSummary, jpsSummary);
            }
            
        }
    }

    static void printTable(Summary aStar, Summary jps) {
        System.out.println("\t\t" + aStar.type + " " + aStar.size + "x" + aStar.size +" " + (aStar.type==MazeType.IMPERFECT?"k="+aStar.kFactor:""));
        System.out.println("\t\t\tA*\t\tJPS");
        System.out.println("Avg. Execution:\t\t" + aStar.averageRuntime + " ns\t" + jps.averageRuntime + " ns");
        System.out.println("Avg. Nodes Explored:\t" + aStar.averageNodesExplored + "\t\t" + jps.averageNodesExplored);
        System.out.println("\n");
    }

    static class Summary {
        int size;
        float kFactor;
        MazeType type;
        Result[] results;

        private long averageRuntime;
        private float averageNodesExplored;

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
                }
                averageRuntime /= results.length;
                averageNodesExplored /= results.length;
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
