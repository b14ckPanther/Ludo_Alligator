package Model;

/**
 * The kinds of squares on the board. Question and surprise squares
 * are added with the Medium board in iteration 2.
 */
public enum SquareType {
    /** Normal track square. A piece here can be captured. */
    NORMAL(false),
    /** Star square, pieces on it can't be captured. */
    SAFE(true),
    /** Where a color's pieces enter the track from the castle (#). */
    START(true),
    /** Square in a player's color. That player's pieces turn inwards here. */
    ARROW(false),
    /** One of the 5 colored squares before the finish. */
    HOME(true),
    /** Last square of a route, in the middle of the board. */
    FINISH(true);

    private final boolean safe;

    SquareType(boolean safe) {
        this.safe = safe;
    }

    /** True if a piece on this kind of square can't be captured. */
    public boolean isProtected() {
        return safe;
    }
}
