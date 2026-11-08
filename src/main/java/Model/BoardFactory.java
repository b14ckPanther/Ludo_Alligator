package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Builds the board for a difficulty (Factory pattern). Only the Easy board
 * exists in iteration 1 (P-BRD-1).
 *
 * <p>The Easy board is the classic 15x15 cross: a 52-square track, and for
 * each color a castle in its corner, a start square, an arrow square and a
 * 5-square home column that ends at the finish in the middle.
 *
 * <p>We only write the squares for Red (top-left castle). The other colors
 * get the same squares turned 90 degrees clockwise once (Green), twice
 * (Yellow) or three times (Blue), so all four routes are the same length.
 */
public final class BoardFactory {
    public static final int HOME_SQUARES = 5;
    public static final int SAFE_SQUARES = 4;
    static final int EASY_SIZE = 15;

    private static final int[] RED_START = {6, 1};
    private static final int[] RED_ARROW = {7, 0};

    private BoardFactory() { }

    public static Board create(Difficulty difficulty, Random random) {
        if (difficulty != Difficulty.EASY) {
            throw new IllegalArgumentException(difficulty.displayName() + " board is not available yet");
        }
        int n = EASY_SIZE;
        Board board = new Board(n);

        // shared track
        List<Square> track = new ArrayList<>();
        for (int[] cell : crossTrack()) {
            Square square = new Square(cell[0], cell[1], true);
            board.put(square);
            track.add(square);
        }
        board.setTrack(track);

        // routes, start and arrow squares, home columns (P-BRD-4, P-BRD-7)
        for (PlayerColor color : PlayerColor.values()) {
            int turns = color.quadrant();
            int[] start = rotate(RED_START, n, turns);
            int[] arrow = rotate(RED_ARROW, n, turns);
            board.at(start[0], start[1]).mark(SquareType.START, color);
            board.at(arrow[0], arrow[1]).mark(SquareType.ARROW, color);

            List<Square> route = new ArrayList<>();
            // clockwise from the start square to the own arrow
            int i = indexOf(track, board.at(start[0], start[1]));
            while (true) {
                Square square = track.get(i);
                route.add(square);
                if (square.row() == arrow[0] && square.col() == arrow[1]) {
                    break;
                }
                i = (i + 1) % track.size();
            }
            // from the arrow into the home column (Red: row 7, columns 1 to 5), then the finish
            for (int k = 1; k <= HOME_SQUARES + 1; k++) {
                int[] cell = rotate(new int[]{7, k}, n, turns);
                Square square = new Square(cell[0], cell[1], false);
                if (k <= HOME_SQUARES) {
                    square.mark(SquareType.HOME, color);
                    board.put(square);
                } else {
                    square.mark(SquareType.FINISH, color);
                }
                route.add(square);
            }
            board.setRoute(color, route);
        }

        placeSafeSquares(board, track, random);
        return board;
    }

    /**
     * Puts the 4 safe squares on normal track squares, never on a start or
     * arrow square, and never right next to another special square so they
     * are spread around the board. They change every game.
     */
    private static void placeSafeSquares(Board board, List<Square> track, Random random) {
        List<Square> pool = new ArrayList<>(track);
        Collections.shuffle(pool, random);
        int placed = 0;
        for (Square square : pool) {
            if (placed == SAFE_SQUARES) {
                break;
            }
            if (square.type() == SquareType.NORMAL && !nextToSpecial(track, square)) {
                square.mark(SquareType.SAFE, null);
                placed++;
            }
        }
    }

    private static boolean nextToSpecial(List<Square> track, Square square) {
        int i = track.indexOf(square);
        Square before = track.get((i - 1 + track.size()) % track.size());
        Square after = track.get((i + 1) % track.size());
        return before.type() != SquareType.NORMAL || after.type() != SquareType.NORMAL;
    }

    /**
     * The 52 squares of the cross, clockwise, starting on the left arm.
     * Rows and columns go from 0 to 14, the castles take the 6x6 corners.
     */
    static List<int[]> crossTrack() {
        List<int[]> cells = new ArrayList<>();
        for (int c = 0; c <= 5; c++) cells.add(new int[]{6, c});    // left arm, top row
        for (int r = 5; r >= 0; r--) cells.add(new int[]{r, 6});    // top arm, going up
        cells.add(new int[]{0, 7});
        cells.add(new int[]{0, 8});
        for (int r = 1; r <= 5; r++) cells.add(new int[]{r, 8});    // top arm, going down
        for (int c = 9; c <= 14; c++) cells.add(new int[]{6, c});   // right arm, top row
        cells.add(new int[]{7, 14});
        cells.add(new int[]{8, 14});
        for (int c = 13; c >= 9; c--) cells.add(new int[]{8, c});   // right arm, bottom row
        for (int r = 9; r <= 14; r++) cells.add(new int[]{r, 8});   // bottom arm, going down
        cells.add(new int[]{14, 7});
        cells.add(new int[]{14, 6});
        for (int r = 13; r >= 9; r--) cells.add(new int[]{r, 6});   // bottom arm, going up
        for (int c = 5; c >= 0; c--) cells.add(new int[]{8, c});    // left arm, bottom row
        cells.add(new int[]{7, 0});
        return cells;
    }

    /** Turns a cell clockwise around the middle of the grid, 90 degrees per turn. */
    static int[] rotate(int[] cell, int n, int turns) {
        int r = cell[0];
        int c = cell[1];
        for (int i = 0; i < turns; i++) {
            int newRow = c;
            int newCol = n - 1 - r;
            r = newRow;
            c = newCol;
        }
        return new int[]{r, c};
    }

    private static int indexOf(List<Square> track, Square square) {
        int i = track.indexOf(square);
        if (i < 0) {
            throw new IllegalStateException("Square is not on the track: " + square);
        }
        return i;
    }
}
