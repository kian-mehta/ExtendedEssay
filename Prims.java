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

public class Prims extends JFrame {
    private static final int WIDTH = 9;
    private static final int HEIGHT = 9;
    private static final int CELL_SIZE = 12;
    private static final int ANIMATION_DELAY = 500;

    private boolean[][] maze;
    private boolean animating = false;
    private javax.swing.Timer timer;
    private List<Wall> frontier;
    private Random random; 

    private JPanel mazePanel;
    private JButton generateBtn, stepBtn, clearBtn;

    // Wall class to represent connections between cells
    private class Wall {
        int x1, y1, x2, y2;

        Wall(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        // Get the wall position between two cells
        Point getWallPosition() {
            return new Point((x1 + x2) / 2, (y1 + y2) / 2);
        }
    }

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

    public Prims() {
        random = new Random();
        initializeMaze();
        setupGUI();
    }

    private void initializeMaze() {
        maze = new boolean[HEIGHT][WIDTH];
        frontier = new ArrayList<>();
        // Initialize maze with all walls (false = wall, true = path)
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                maze[y][x] = false;
            }
        }
    }

    private void setupGUI() {
        setTitle("Prim's Algorithm Maze Generator");
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
        if (animating)
            return;

        clearMaze();

        // Start with a random cell (must be odd coordinates, proper bounds)
        int startX = 1 + (random.nextInt(WIDTH / 2)) * 2; // Can be 1, 3, 5, ..., 95, 97
        int startY = 1 + (random.nextInt(HEIGHT / 2)) * 2; // Can be 1, 3, 5, ..., 95, 97

        maze[startY][startX] = true;

        // Add initial frontier walls
        addFrontierWalls(startX, startY);

        // Prim's algorithm main loop
        while (!frontier.isEmpty()) {
            // Pick random frontier wall
            Wall wall = frontier.remove(random.nextInt(frontier.size()));

            // Check if we can connect
            if (canConnect(wall)) {
                // Make the unvisited cell a path
                maze[wall.y2][wall.x2] = true;

                // Remove wall between cells
                Point wallPos = wall.getWallPosition();
                maze[wallPos.y][wallPos.x] = true;

                // Add new frontier walls
                addFrontierWalls(wall.x2, wall.y2);
            }
        }

        // Ensure start and end points are accessible for AI training
        ensureStartEndConnectivity();
        mazePanel.repaint();
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

        sb.append("Maze for AI Training (").append(WIDTH).append("x").append(HEIGHT).append(")\n");
        sb.append("Format: 0=wall, 1=path, S=start, E=end\n\n");

        // Show in dialog
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Maze Export for AI Training", JOptionPane.INFORMATION_MESSAGE);

        // Also print coordinates for programmatic use
        System.out.println("Start: (1, 1)");
        System.out.println("End: (" + (WIDTH - 2) + ", " + (HEIGHT - 2) + ")");
        System.out.println("Maze dimensions: " + WIDTH + "x" + HEIGHT);
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

            // Start with a random cell (proper bounds)
            int startX = 1 + (random.nextInt(WIDTH / 2)) * 2; // Can be 1, 3, 5, ..., 95, 97
            int startY = 1 + (random.nextInt(HEIGHT / 2)) * 2; // Can be 1, 3, 5, ..., 95, 97

            maze[startY][startX] = true;
            addFrontierWalls(startX, startY);

            // Start animation timer
            timer = new javax.swing.Timer(ANIMATION_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (frontier.isEmpty()) {
                        timer.stop();
                        animating = false;
                        stepBtn.setText("Step by Step");
                        stepBtn.setBackground(new Color(144, 238, 144));
                        return;
                    }

                    // Pick random frontier wall
                    Wall wall = frontier.remove(random.nextInt(frontier.size()));

                    // Check if we can connect
                    if (canConnect(wall)) {
                        // Make the unvisited cell a path
                        maze[wall.y2][wall.x2] = true;

                        // Remove wall between cells
                        Point wallPos = wall.getWallPosition();
                        maze[wallPos.y][wallPos.x] = true;

                        // Add new frontier walls
                        addFrontierWalls(wall.x2, wall.y2);
                    }

                    mazePanel.repaint();
                }
            });
            timer.start();
        }
    }

    private void addFrontierWalls(int x, int y) {
        // Check all four directions (2 steps away to maintain wall structure)
        int[][] directions = { { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, 0 } };

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            // Check bounds - keep border walls intact
            if (nx >= 1 && nx <= WIDTH - 2 && ny >= 1 && ny <= HEIGHT - 2) {
                // If the neighbor is a wall (unvisited)
                if (!maze[ny][nx]) {
                    Wall wall = new Wall(x, y, nx, ny);
                    // Avoid duplicates
                    boolean exists = false;
                    for (Wall w : frontier) {
                        if ((w.x1 == x && w.y1 == y && w.x2 == nx && w.y2 == ny) ||
                                (w.x1 == nx && w.y1 == ny && w.x2 == x && w.y2 == y)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        frontier.add(wall);
                    }
                }
            }
        }
    }

    private boolean canConnect(Wall wall) {
        // Check if exactly one of the cells is visited
        boolean cell1Visited = maze[wall.y1][wall.x1];
        boolean cell2Visited = maze[wall.y2][wall.x2];

        return cell1Visited != cell2Visited;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel if system L&F fails
                System.out.println("Using default look and feel");
            }
            new Prims();
        });
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
            sb.append("}" + (y == (HEIGHT - 1) ? "" : ",") /* +"\n" */);
        }

        try {
            FileWriter fileWriter = new FileWriter("MazesPrims.txt", true);
            fileWriter.append("{" + sb.toString() + "}\n");
            fileWriter.close();
            System.out.println("Wrote once");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}