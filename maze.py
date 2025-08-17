import turtle as t
from dataclasses import dataclass
import math, warnings, random

cell_size=20
number_cells=900  # Reduced from 400 to make it manageable
u = int(math.sqrt(number_cells))
v = number_cells//u
rows, cols = (u, v)

cells = [[0 for i in range(cols)] for j in range(rows)]
walls=[]

@dataclass
class Cell():
    index: tuple
    visited: bool = False
    
@dataclass 
class Wall():
    index1: Cell
    index2: Cell
    state: bool = True

def draw_grid():
    t.color('light grey')
    start_pos = -int(math.sqrt(number_cells))*cell_size/2
    t.teleport(start_pos, start_pos)
    for y in range(0, u):
        for x in range(0, v):
            t.setx(start_pos+cell_size*x)
            t.sety(start_pos+cell_size*y)
            cells[x][y] = Cell((x,y))
            if not x == 0:
                walls.append(Wall(cells[x-1][y],cells[x][y])) #vertical
            if not y == 0:
                walls.append(Wall(cells[x][y-1],cells[x][y])) #horizontal
                # Draw only the walls just created
                t.penup()
                t.goto(start_pos + cell_size * x, start_pos + cell_size * y)
                t.pendown()
                if x != 0:
                    # Draw vertical wall (left side)
                    t.goto(start_pos + cell_size * x, start_pos + cell_size * (y + 1))
                    t.penup()
                    t.goto(start_pos + cell_size * x, start_pos + cell_size * y)
                    t.pendown()
                if y != 0:
                    # Draw horizontal wall (top side)
                    t.goto(start_pos + cell_size * (x + 1), start_pos + cell_size * y)
                    t.penup()
                    t.goto(start_pos + cell_size * x, start_pos + cell_size * y)
                    t.pendown()

def generate_maze():
    # Create a separate list for frontier walls to avoid modifying the main walls list
    frontier_walls = []
    
    start = cells[random.randint(0, u-1)][random.randint(0, v-1)]
    start.visited = True
    
    # Add walls connected to the starting cell to frontier
    for w in walls:
        if w.state and (w.index1==start or w.index2==start):
            frontier_walls.append(w)
            
    while len(frontier_walls) > 0:
        # Pick a random wall from frontier
        wall_index = random.randint(0, len(frontier_walls)-1)
        wall = frontier_walls[wall_index]
        
        # Check if exactly one of the cells is visited
        cell1_visited = wall.index1.visited
        cell2_visited = wall.index2.visited
        
        if cell1_visited != cell2_visited:  # Exactly one is visited
            # Mark the unvisited cell as visited
            unvisited_cell = wall.index2 if cell1_visited else wall.index1
            unvisited_cell.visited = True
            
            # Remove the wall (don't draw it)
            wall.state = False
            
            # Add new frontier walls from the newly visited cell
            for w in walls:
                if (w.state and 
                    (w.index1 == unvisited_cell or w.index2 == unvisited_cell) and
                    w not in frontier_walls):
                    frontier_walls.append(w)
        
        # Remove the processed wall from frontier
        frontier_walls.remove(wall)


wn = t.Screen()
t.hideturtle()
wn.setup(width=450, height=450, startx=10, starty=10)
wn.tracer(0)

draw_grid()

if(not(len(cells)==number_cells)):
    warnings.warn(f"Number of cells was not a perfect square. {str(len(cells))} cells have been generated.")
    number_cells = len(cells)

t.color('red')
t.pensize(2)

wn.update()

def draw_maze():
    """Draw the maze by drawing only walls that are still active"""
    t.color('black')
    t.pensize(2)
    
    for wall in walls:
        if wall.state:  # Only draw walls that haven't been removed
            cell1 = wall.index1
            cell2 = wall.index2
            
            start_pos = -int(math.sqrt(number_cells))*cell_size/2
            
            # Calculate wall position
            if cell1.index[0] == cell2.index[0]:  # Vertical wall
                x = start_pos + cell1.index[0] * cell_size + cell_size
                y1 = start_pos + min(cell1.index[1], cell2.index[1]) * cell_size
                y2 = y1 + cell_size
                t.penup()
                t.goto(x, y1)
                t.pendown()
                t.goto(x, y2)
            else:  # Horizontal wall
                y = start_pos + cell1.index[1] * cell_size + cell_size
                x1 = start_pos + min(cell1.index[0], cell2.index[0]) * cell_size
                x2 = x1 + cell_size
                t.penup()
                t.goto(x1, y)
                t.pendown()
                t.goto(x2, y)

generate_maze()
draw_maze()

t.mainloop()