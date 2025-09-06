package old;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class WilsonsNew extends JFrame {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static final int CELL_SIZE = 12;
    private static final int ANIMATION_DELAY = 50;

    private boolean[][] maze;
    private boolean[][] inMaze;
    private boolean animating = false;
    private javax.swing.Timer timer;
    private Random random;
    private List<Point> unvisited;
    private List<Point> currentPath;
    private Point currentWalker;

    private JPanel mazePanel;
    private JButton generateBtn, stepBtn, clearBtn;

    // Custom panel for drawing the maze
    private class MazePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawMaze(g);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
        }
    }

    public WilsonsNew() {
        setupGUI();
    }

    private void initializeMaze() {
        random = new Random();
        maze = new boolean[HEIGHT][WIDTH];
        inMaze = new boolean[HEIGHT][WIDTH];
        unvisited = new ArrayList<>();
        currentPath = new ArrayList<>();
        currentWalker = null;

        // Initialize maze with all walls (false = wall, true = path)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                maze[y][x] = false;
                inMaze[y][x] = false;
                // Add odd coordinates as potential cells
                if (x % 2 == 1 && y % 2 == 1 && x >= 1 && x <= WIDTH - 2 && y >= 1 && y <= HEIGHT - 2) {
                    unvisited.add(new Point(x, y));
                }
            }
        }
    }

    private void setupGUI() {
        setTitle("Wilson's Algorithm Maze Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create maze panel
        mazePanel = new MazePanel();
        mazePanel.setBackground(Color.WHITE);
        add(mazePanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        generateBtn = new JButton("Generate Maze");
        generateBtn.setBackground(new Color(173, 216, 230));
        generateBtn.addActionListener(e -> generateMaze());

        stepBtn = new JButton("Step by Step");
        stepBtn.setBackground(new Color(144, 238, 144));
        stepBtn.addActionListener(e -> toggleStepByStep());

        clearBtn = new JButton("Clear");
        clearBtn.setBackground(new Color(240, 128, 128));
        clearBtn.addActionListener(e -> clearMaze());

        buttonPanel.add(generateBtn);
        buttonPanel.add(stepBtn);
        buttonPanel.add(clearBtn);

        // Add export button for AI training
        JButton exportBtn = new JButton("Export for AI");
        exportBtn.setBackground(new Color(255, 255, 150));
        exportBtn.addActionListener(e -> exportMazeForAI());
        buttonPanel.add(exportBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(2400, 1600));
        setSize(1000, 800);
        setVisible(true);
    }

    private void drawMaze(Graphics g) {
        // Draw the maze grid
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int drawX = x * CELL_SIZE;
                int drawY = y * CELL_SIZE;

                // Special coloring for start (top-left) and end (bottom-right)
                if (x == 1 && y == 1) {
                    g.setColor(Color.GREEN); // Start point
                } else if (x == WIDTH - 2 && y == HEIGHT - 2) {
                    g.setColor(Color.RED); // End point
                } else if (isPointInPath(x, y)) {
                    g.setColor(Color.YELLOW); // Current random walk path
                } else if (currentWalker != null && currentWalker.x == x && currentWalker.y == y) {
                    g.setColor(Color.ORANGE); // Current walker position
                } else if (maze[y][x]) {
                    g.setColor(Color.WHITE); // Path
                } else {
                    g.setColor(Color.BLACK); // Wall
                }

                g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void clearMaze() {
        if (timer != null) {
            timer.stop();
        }
        animating = false;
        stepBtn.setText("Step by Step");
        stepBtn.setBackground(new Color(144, 238, 144));

        initializeMaze();
        mazePanel.repaint();
    }

    private void generateMaze() {
        
        initializeMaze();
        if (animating)
            return;

        clearMaze();

        // Wilson's algorithm: Start with random cell in maze
        if (!unvisited.isEmpty()) {
            Point start = unvisited.get(random.nextInt(unvisited.size()));
            addToMaze(start);
        }

        // Loop until all cells are in maze
        while (!unvisited.isEmpty()) {
            // Start random walk from unvisited cell
            Point walkStart = unvisited.get(random.nextInt(unvisited.size()));
            performRandomWalk(walkStart);
        }

        // Ensure start and end points are accessible for AI training
        ensureStartEndConnectivity();
        mazePanel.repaint();
    }

    private void performRandomWalk(Point start) {
        currentPath.clear();
        currentWalker = new Point(start.x, start.y);
        currentPath.add(new Point(currentWalker.x, currentWalker.y));

        // Perform loop-erased random walk
        int maxSteps = WIDTH * HEIGHT * 10; // Safety limit to prevent infinite loops
        int stepCount = 0;

        while (!inMaze[currentWalker.y][currentWalker.x] && stepCount < maxSteps) {
            // Get random neighbor
            Point nextStep = getRandomNeighbor(currentWalker);

            // Check if we're creating a loop
            int loopIndex = findInPath(nextStep);
            if (loopIndex >= 0) {
                // Erase loop by removing everything after loop point
                currentPath = new ArrayList<>(currentPath.subList(0, loopIndex + 1));
            } else {
                currentPath.add(nextStep);
            }

            currentWalker = nextStep;
            stepCount++;
        }

        if (stepCount >= maxSteps) {
            System.err.println("Warning: Random walk exceeded maximum steps, terminating");
        }

        // Add entire path to maze
        for (int i = 0; i < currentPath.size() - 1; i++) {
            Point p1 = currentPath.get(i);
            Point p2 = currentPath.get(i + 1);

            addToMaze(p1);

            // Add connecting wall
            int wallX = (p1.x + p2.x) / 2;
            int wallY = (p1.y + p2.y) / 2;
            if (wallX >= 0 && wallX < WIDTH && wallY >= 0 && wallY < HEIGHT) {
                maze[wallY][wallX] = true;
            }
        }

        // Make sure the last cell in path is also added
        if (!currentPath.isEmpty()) {
            Point lastCell = currentPath.get(currentPath.size() - 1);
            if (!inMaze[lastCell.y][lastCell.x]) {
                addToMaze(lastCell);
            }
        }

        currentPath.clear();
        currentWalker = null;
    }

    private Point getRandomNeighbor(Point p) {
        int[][] directions = { { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, 0 } };
        List<Point> neighbors = new ArrayList<>();

        for (int[] dir : directions) {
            int nx = p.x + dir[0];
            int ny = p.y + dir[1];

            // Check bounds - keep border walls intact
            if (nx >= 1 && nx <= WIDTH - 2 && ny >= 1 && ny <= HEIGHT - 2) {
                neighbors.add(new Point(nx, ny));
            }
        }

        // If no valid neighbors, try adjacent cells (fallback)
        if (neighbors.isEmpty()) {
            for (int[] dir : directions) {
                int nx = p.x + dir[0] / 2;
                int ny = p.y + dir[1] / 2;
                if (nx >= 1 && nx <= WIDTH - 2 && ny >= 1 && ny <= HEIGHT - 2) {
                    neighbors.add(new Point(nx, ny));
                }
            }
        }

        // Final fallback - if still no neighbors, return current point (should not
        // happen)
        if (neighbors.isEmpty()) {
            System.err.println("Warning: No valid neighbors found for point (" + p.x + ", " + p.y + ")");
            return new Point(p.x, p.y);
        }

        return neighbors.get(random.nextInt(neighbors.size()));
    }

    private int findInPath(Point p) {
        for (int i = 0; i < currentPath.size(); i++) {
            Point pathPoint = currentPath.get(i);
            if (pathPoint.x == p.x && pathPoint.y == p.y) {
                return i;
            }
        }
        return -1;
    }

    // Helper method to check if a point is in the current path
    private boolean isPointInPath(int x, int y) {
        for (Point p : currentPath) {
            if (p.x == x && p.y == y) {
                return true;
            }
        }
        return false;
    }

    private void addToMaze(Point p) {
        maze[p.y][p.x] = true;
        inMaze[p.y][p.x] = true;
        unvisited.remove(p);
    }

    private void ensureStartEndConnectivity() {
        // Ensure start point (1,1) and end point (WIDTH-2, HEIGHT-2) are paths
        maze[1][1] = true;
        maze[HEIGHT - 2][WIDTH - 2] = true;

        // Create entrance and exit by clearing border walls
        maze[0][1] = true; // Top entrance
        maze[HEIGHT - 1][WIDTH - 2] = true; // Bottom exit
    }

    private void exportMazeForAI() {
        if (maze == null) {
            JOptionPane.showMessageDialog(this, "Generate a maze first!");
            return;
        }

        // Ensure start/end connectivity
        ensureStartEndConnectivity();
        mazePanel.repaint();

        // Create export dialog
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < HEIGHT; y++) {
            sb.append("{");
            for (int x = 0; x < WIDTH; x++) {
                if (x == 1 && y == 1) {
                    sb.append("\'S\'"); // Start
                } else if (x == WIDTH - 2 && y == HEIGHT - 2) {
                    sb.append("\'E\'"); // End
                } else if (maze[y][x]) {
                    sb.append("\'1\'"); // Path
                } else {
                    sb.append("\'0\'"); // Wall
                }
                if (x < WIDTH - 1)
                    sb.append(", ");
            }
            sb.append("}" + (y == (HEIGHT - 1) ? "" : ",") + "\n");
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);

        sb.append("Wilson's Maze for AI Training (").append(WIDTH).append("x").append(HEIGHT).append(")\n");
        sb.append("Format: 0=wall, 1=path, S=start, E=end\n\n");

        // Show in dialog
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Wilson's Maze Export for AI Training",
                JOptionPane.INFORMATION_MESSAGE);

        // Also print coordinates for programmatic use
        System.out.println("Start: (1, 1)");
        System.out.println("End: (" + (WIDTH - 2) + ", " + (HEIGHT - 2) + ")");
        System.out.println("Maze dimensions: " + WIDTH + "x" + HEIGHT);
        System.out.println("Algorithm: Wilson's (uniform spanning tree)");
    }

    private void toggleStepByStep() {
        if (animating) {
            // Stop animation
            timer.stop();
            animating = false;
            stepBtn.setText("Step by Step");
            stepBtn.setBackground(new Color(144, 238, 144));
        } else {
            // Start step-by-step animation
            clearMaze();
            animating = true;
            stepBtn.setText("Stop Animation");
            stepBtn.setBackground(new Color(255, 165, 0));

            // Wilson's algorithm: Start with random cell in maze
            if (!unvisited.isEmpty()) {
                Point start = unvisited.get(random.nextInt(unvisited.size()));
                addToMaze(start);
                mazePanel.repaint();
            }

            // Start animation timer
            timer = new javax.swing.Timer(ANIMATION_DELAY, new ActionListener() {
                private boolean inRandomWalk = false;
                private int walkStepCount = 0;
                private final int MAX_WALK_STEPS = WIDTH * HEIGHT * 2; // Safety limit

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (unvisited.isEmpty()) {
                        // Maze complete
                        ensureStartEndConnectivity();
                        timer.stop();
                        animating = false;
                        stepBtn.setText("Step by Step");
                        stepBtn.setBackground(new Color(144, 238, 144));
                        currentPath.clear();
                        currentWalker = null;
                        mazePanel.repaint();
                        return;
                    }

                    if (!inRandomWalk) {
                        // Start new random walk
                        Point walkStart = unvisited.get(random.nextInt(unvisited.size()));
                        currentPath.clear();
                        currentWalker = new Point(walkStart.x, walkStart.y);
                        currentPath.add(new Point(currentWalker.x, currentWalker.y));
                        inRandomWalk = true;
                        walkStepCount = 0; // Reset step counter
                    } else {
                        // Continue random walk with safety check
                        if (!inMaze[currentWalker.y][currentWalker.x] && walkStepCount < MAX_WALK_STEPS) {
                            // Get random neighbor
                            Point nextStep = getRandomNeighbor(currentWalker);

                            // Check if we're creating a loop
                            int loopIndex = findInPath(nextStep);
                            if (loopIndex >= 0) {
                                // Erase loop by removing everything after loop point
                                currentPath = new ArrayList<>(currentPath.subList(0, loopIndex + 1));
                            } else {
                                currentPath.add(nextStep);
                            }

                            currentWalker = nextStep;
                            walkStepCount++;
                        } else {
                            // Reached maze or exceeded steps, add path
                            if (walkStepCount >= MAX_WALK_STEPS) {
                                System.err.println("Warning: Animation random walk exceeded maximum steps");
                            }

                            for (int i = 0; i < currentPath.size() - 1; i++) {
                                Point p1 = currentPath.get(i);
                                Point p2 = currentPath.get(i + 1);

                                addToMaze(p1);

                                // Add connecting wall
                                int wallX = (p1.x + p2.x) / 2;
                                int wallY = (p1.y + p2.y) / 2;
                                if (wallX >= 0 && wallX < WIDTH && wallY >= 0 && wallY < HEIGHT) {
                                    maze[wallY][wallX] = true;
                                }
                            }

                            // Make sure the last cell in path is also added
                            if (!currentPath.isEmpty()) {
                                Point lastCell = currentPath.get(currentPath.size() - 1);
                                if (!inMaze[lastCell.y][lastCell.x]) {
                                    addToMaze(lastCell);
                                }
                            }

                            currentPath.clear();
                            currentWalker = null;
                            inRandomWalk = false;
                        }
                    }

                    mazePanel.repaint();
                }
            });
            timer.start();
        }
    }

    private void writeMazeToFile() {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < HEIGHT; y++) {
            sb.append("{");
            for (int x = 0; x < WIDTH; x++) {
                if (x == 1 && y == 1) {
                    sb.append("\'S\'"); // Start
                } else if (x == WIDTH - 2 && y == HEIGHT - 2) {
                    sb.append("\'E\'"); // End
                } else if (maze[y][x]) {
                    sb.append("\'1\'"); // Path
                } else {
                    sb.append("\'0\'"); // Wall
                }
                if (x < WIDTH - 1)
                    sb.append(", ");
            }
            sb.append("}" + (y == (HEIGHT - 1) ? "" : ","));
        }

        try {
            FileWriter fileWriter = new FileWriter("MazesWilsons.txt", true);
            fileWriter.append("{" + sb.toString() + "}\n");
            fileWriter.close();
            System.out.println("Wilson's maze written to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel if system L&F fails
                System.out.println("Using default look and feel");
            }
            new WilsonsNew();
        });
    }
}
