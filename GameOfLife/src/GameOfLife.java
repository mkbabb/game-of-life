import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This program emulates Conway's game of life.
 */
public class GameOfLife {
    int rows;
    int cols;

    /**
     * Current game board. Each cell is represented as either alive (1) or dead (0).
     */
    Integer[][] grid;
    /** Next generation's game board (used after update). */
    Integer[][] futureGrid;

    /**
     * Create the game of life board (rows, cols) in size. If an initial grid is
     * provided, use that to seed the board. Else, randomize the game board.
     * 
     * @param rows        input rows.
     * @param cols        input cols.
     * @param initialGrid initial grid of values.
     */
    public GameOfLife(int rows, int cols, Optional<Integer[][]> initialGrid) {
        this.rows = rows;
        this.cols = cols;

        this.grid = new Integer[rows][cols];
        this.futureGrid = new Integer[rows][cols];

        if (initialGrid.isPresent()) {
            for (var i = 0; i < this.rows; i++) {
                for (var j = 0; j < this.cols; j++) {
                    this.grid[i][j] = initialGrid.get()[i][j];
                }
            }
        } else {
            for (var i = 0; i < this.rows; i++) {
                for (var j = 0; j < this.cols; j++) {
                    this.grid[i][j] = (int) Math.round(Math.random());
                }
            }
        }
    }

    /**
     * Prints the current game board (grid).
     */
    public void printGrid() {
        for (int i = 0; i < this.rows; i++) {
            System.out.print("[");
            for (int j = 0; j < this.cols; j++) {
                if (j > 0) {
                    System.out.print(", ");
                }
                System.out.print(this.grid[i][j]);
            }
            System.out.println("]");

        }
    }

    /**
     * Counts the total number of neighbors for a given cell at position [row, col].
     * 
     * @param row input y value.
     * @param col input x value.
     * @return number of neighbors.
     */
    public int countNeighbors(int row, int col) {
        var total = 0;

        for (var i = -1; i <= 1; i++) {
            for (var j = -1; j <= 1; j++) {
                var rowIx = row + i;
                var colIx = col + j;

                // Exclude the centroidal value + out of bounds cells.
                var isInvalid = (i == 0 && j == 0) || (rowIx >= rows) || (colIx >= cols) || (rowIx < 0) || (colIx < 0);

                if (!isInvalid) {
                    total += this.grid[rowIx][colIx];
                }
            }
        }

        return total;
    }

    /**
     * Applies the game's rule set to a given cell, given its neighbor count.
     * Updates the future grid with the cell's calculated value.
     * 
     * @param row       input row.
     * @param col       input col.
     * @param neighbors neighbor count.
     */
    public void evaluateCell(int row, int col, int neighbors) {
        var cell = this.grid[row][col];

        if (cell == 1) {
            if ((neighbors < 2) || (neighbors > 3)) {
                cell = 0;
            }
        } else {
            if (neighbors == 3) {
                cell = 1;
            }
        }

        this.futureGrid[row][col] = cell;
    }

    /**
     * Main update function for one generation: spawns a thread for each cell, (i,
     * j), and applies the game of life's rule set thereto.
     * 
     * After the generation is completed, the future game board is propagated to the
     * current board, and then reset.
     * 
     * @throws InterruptedException erm.
     */
    public void update() throws InterruptedException {
        final var threads = new ArrayList<Thread>();

        for (var i = 0; i < this.rows; i++) {
            for (var j = 0; j < this.cols; j++) {

                int row = i;
                int col = j;

                Runnable live = () -> {
                    final var neighbors = this.countNeighbors(row, col);
                    this.evaluateCell(row, col, neighbors);
                };

                final var t = new Thread(live);

                t.start();
                threads.add(t);
            }
        }

        for (final var t : threads) {
            t.join();
        }

        for (var i = 0; i < this.rows; i++) {
            for (var j = 0; j < this.cols; j++) {
                this.grid[i][j] = this.futureGrid[i][j];
                this.futureGrid[i][j] = 0;
            }
        }
    }

    /**
     * Parses an input file for seeding a game with initial values. This is a space
     * delimitated file, with the first line containing the row and column count of
     * the board.
     * 
     * @param filename input file name.
     * @return generated game of life.
     * @throws IOException erm.
     */
    public static GameOfLife parseFile(String filename) throws IOException {
        final var path = Paths.get(filename);

        final var header = Files.lines(path).findFirst().get().split(" ");
        final var rows = Integer.parseInt(header[0]);
        final var cols = Integer.parseInt(header[1]);

        final var initialGrid = new Integer[rows][cols];

        var i = 0;
        for (final var line : (Iterable<String>) Files.lines(path).skip(1)::iterator) {
            final var items = line.split(" ");
            var j = 0;

            for (final var item : items) {
                initialGrid[i][j] = Integer.parseInt(item);
                j += 1;
            }
            i += 1;

        }

        return new GameOfLife(rows, cols, Optional.of(initialGrid));
    }

    /**
     * Runs the game of life given an input file (described above), and number of
     * generations to run for.
     * 
     * Each generation is printed *before* being updated.
     * 
     * @param args input args: filename and generation count.
     * @throws Exception erm.
     */
    public static void main(String[] args) throws Exception {
        final var filename = args[0];
        final var generations = Integer.parseInt(args[1]);

        final var game = parseFile(filename);

        for (var i = 0; i < generations; i++) {
            game.printGrid();
            System.out.println();
            game.update();
        }
    }
}
