package Model;

/**
 * One square of the board, found by its row and column on the grid.
 * A square doesn't remember which pieces are on it; the game works that
 * out from the piece positions, so there is only one place to update.
 */
public final class Square {
    private final int row;
    private final int col;
    private final boolean track;
    private SquareType type = SquareType.NORMAL;
    private PlayerColor owner;

    /**
     * @param track true for squares of the shared track, false for home and finish squares
     */
    public Square(int row, int col, boolean track) {
        this.row = row;
        this.col = col;
        this.track = track;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public SquareType type() {
        return type;
    }

    /** The color this square belongs to (start, arrow, home, finish), otherwise null. */
    public PlayerColor owner() {
        return owner;
    }

    /** True if this square is on the track that all players share. */
    public boolean isTrack() {
        return track;
    }

    public boolean isProtected() {
        return type.isProtected();
    }

    void mark(SquareType type, PlayerColor owner) {
        this.type = type;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return type + "(" + row + "," + col + ")";
    }
}
