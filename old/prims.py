import tkinter as tk
import random
from enum import Enum

class CellType(Enum):
    WALL = 0
    PATH = 1

class PrimsMazeGenerator:
    def __init__(self, width=50, height=30, cell_size=15):
        self.width = width
        self.height = height
        self.cell_size = cell_size
        
        # Initialize maze with all walls
        self.maze = [[CellType.WALL for _ in range(width)] for _ in range(height)]
        
        # Setup tkinter
        self.root = tk.Tk()
        self.root.title("Prim's Algorithm Maze Generator")
        self.canvas = tk.Canvas(
            self.root, 
            width=width * cell_size, 
            height=height * cell_size + 60,
            bg='white'
        )
        self.canvas.pack()
        
        # Add buttons
        button_frame = tk.Frame(self.root)
        button_frame.pack(pady=10)
        
        self.generate_btn = tk.Button(
            button_frame, 
            text="Generate Maze", 
            command=self.generate_maze,
            bg='lightblue',
            font=('Arial', 12)
        )
        self.generate_btn.pack(side=tk.LEFT, padx=5)
        
        self.clear_btn = tk.Button(
            button_frame, 
            text="Clear", 
            command=self.clear_maze,
            bg='lightcoral',
            font=('Arial', 12)
        )
        self.clear_btn.pack(side=tk.LEFT, padx=5)
        
        self.step_btn = tk.Button(
            button_frame, 
            text="Step by Step", 
            command=self.toggle_step_by_step,
            bg='lightgreen',
            font=('Arial', 12)
        )
        self.step_btn.pack(side=tk.LEFT, padx=5)
        
        # Animation variables
        self.animating = False
        self.animation_speed = 50  # milliseconds
        
    def draw_maze(self):
        """Draw the current maze state"""
        self.canvas.delete("all")
        
        for y in range(self.height):
            for x in range(self.width):
                x1 = x * self.cell_size
                y1 = y * self.cell_size
                x2 = x1 + self.cell_size
                y2 = y1 + self.cell_size
                
                if self.maze[y][x] == CellType.WALL:
                    self.canvas.create_rectangle(x1, y1, x2, y2, fill='black', outline='gray')
                else:
                    self.canvas.create_rectangle(x1, y1, x2, y2, fill='white', outline='lightgray')
    
    def get_neighbors(self, x, y):
        """Get valid neighboring cells (2 steps away to maintain wall structure)"""
        neighbors = []
        directions = [(0, 2), (2, 0), (0, -2), (-2, 0)]  # Up, Right, Down, Left
        
        for dx, dy in directions:
            nx, ny = x + dx, y + dy
            if 0 <= nx < self.width and 0 <= ny < self.height:
                neighbors.append((nx, ny))
        return neighbors
    
    def get_wall_between(self, x1, y1, x2, y2):
        """Get the wall cell between two path cells"""
        wall_x = (x1 + x2) // 2
        wall_y = (y1 + y2) // 2
        return wall_x, wall_y
    
    def clear_maze(self):
        """Reset maze to all walls"""
        self.maze = [[CellType.WALL for _ in range(self.width)] for _ in range(self.height)]
        self.draw_maze()
        self.animating = False
    
    def generate_maze(self):
        """Generate maze using Prim's algorithm (instant)"""
        if self.animating:
            return
            
        self.clear_maze()
        
        # Start with a random cell (must be odd coordinates to maintain grid structure)
        start_x = random.randrange(1, self.width, 2)
        start_y = random.randrange(1, self.height, 2)
        
        # Mark starting cell as path
        self.maze[start_y][start_x] = CellType.PATH
        
        # Initialize frontier walls
        frontier = []
        for nx, ny in self.get_neighbors(start_x, start_y):
            if self.maze[ny][nx] == CellType.WALL:
                frontier.append((nx, ny, start_x, start_y))
        
        # Prim's algorithm main loop
        while frontier:
            # Pick a random frontier cell
            nx, ny, px, py = random.choice(frontier)
            frontier.remove((nx, ny, px, py))
            
            # If the frontier cell is still a wall
            if self.maze[ny][nx] == CellType.WALL:
                # Make it a path
                self.maze[ny][nx] = CellType.PATH
                
                # Remove the wall between the frontier cell and its parent
                wall_x, wall_y = self.get_wall_between(nx, ny, px, py)
                self.maze[wall_y][wall_x] = CellType.PATH
                
                # Add new frontier cells
                for nnx, nny in self.get_neighbors(nx, ny):
                    if (self.maze[nny][nnx] == CellType.WALL and 
                        (nnx, nny, nx, ny) not in frontier):
                        frontier.append((nnx, nny, nx, ny))
        
        self.draw_maze()
    
    def toggle_step_by_step(self):
        """Toggle step by step animation"""
        if self.animating:
            self.animating = False
            self.step_btn.config(text="Step by Step", bg='lightgreen')
        else:
            self.step_btn.config(text="Stop Animation", bg='orange')
            self.generate_step_by_step()
    
    def generate_step_by_step(self):
        """Generate maze with animation"""
        self.animating = True
        self.clear_maze()
        
        # Start with a random cell
        start_x = random.randrange(1, self.width, 2)
        start_y = random.randrange(1, self.height, 2)
        
        # Mark starting cell as path
        self.maze[start_y][start_x] = CellType.PATH
        
        # Initialize frontier walls
        self.frontier = []
        for nx, ny in self.get_neighbors(start_x, start_y):
            if self.maze[ny][nx] == CellType.WALL:
                self.frontier.append((nx, ny, start_x, start_y))
        
        # Draw initial state
        self.draw_maze()
        
        # Highlight starting cell
        x1 = start_x * self.cell_size
        y1 = start_y * self.cell_size
        x2 = x1 + self.cell_size
        y2 = y1 + self.cell_size
        self.canvas.create_rectangle(x1, y1, x2, y2, fill='green', outline='darkgreen', width=2)
        
        # Start animation after a short delay
        self.root.after(500, self.animate_step)
    
    def animate_step(self):
        """Animate one step of Prim's algorithm"""
        if not self.animating or not hasattr(self, 'frontier') or not self.frontier:
            self.animating = False
            self.step_btn.config(text="Step by Step", bg='lightgreen')
            if hasattr(self, 'frontier') and not self.frontier:
                print("Maze generation complete!")
            return
        
        # Pick a random frontier cell
        nx, ny, px, py = random.choice(self.frontier)
        self.frontier.remove((nx, ny, px, py))
        
        # If the frontier cell is still a wall
        if self.maze[ny][nx] == CellType.WALL:
            # Make it a path
            self.maze[ny][nx] = CellType.PATH
            
            # Remove the wall between the frontier cell and its parent
            wall_x, wall_y = self.get_wall_between(nx, ny, px, py)
            if 0 <= wall_x < self.width and 0 <= wall_y < self.height:
                self.maze[wall_y][wall_x] = CellType.PATH
            
            # Add new frontier cells
            for nnx, nny in self.get_neighbors(nx, ny):
                if (self.maze[nny][nnx] == CellType.WALL and 
                    not any(f[0] == nnx and f[1] == nny for f in self.frontier)):
                    self.frontier.append((nnx, nny, nx, ny))
        
        # Redraw maze
        self.draw_maze()
        
        # Highlight current cell being processed (only if it was made into a path)
        if self.maze[ny][nx] == CellType.PATH:
            x1 = nx * self.cell_size
            y1 = ny * self.cell_size
            x2 = x1 + self.cell_size
            y2 = y1 + self.cell_size
            self.canvas.create_rectangle(x1, y1, x2, y2, fill='yellow', outline='red', width=2)
        
        # Schedule next step
        if self.animating:
            self.root.after(self.animation_speed, self.animate_step)
    
    def run(self):
        """Start the tkinter main loop"""
        self.draw_maze()
        self.root.mainloop()

# Create and run the maze generator
if __name__ == "__main__":
    # Create maze generator (width, height, cell_size)
    maze_gen = PrimsMazeGenerator(width=51, height=31, cell_size=12)
    maze_gen.run()
