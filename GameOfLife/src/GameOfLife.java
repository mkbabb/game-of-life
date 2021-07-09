import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This program emulates Conway's game of life.
 */
public class GameOfLife {
    int rows;
    int cols;

    Integer[][] grid;
    Integer[][] futureGrid;

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

    public int countNeighbors(int row, int col) {
        var total = 0;

        for (var i = -1; i <= 1; i++) {
            for (var j = -1; j <= 1; j++) {
                var rowIx = row + i;
                var colIx = row + j;

                var isInvalid = (i == 0 && j == 0) || (rowIx >= rows) || (colIx >= cols) || (rowIx < 0) || (colIx < 0);

                if (!isInvalid) {
                    total += this.grid[rowIx][colIx];
                }

            }
        }

        return total;
    }

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

    public static void main(String[] args) throws Exception {
        final var rows = 5;
        final var cols = 5;
        final var generations = 10;

        final var game = new GameOfLife(rows, cols, Optional.empty());

        for (var i = 0; i < generations; i++) {
            game.printGrid();
            System.out.println();
            game.update();

        }

    }
}
