package Model;

/**
 * One of the four pieces of a player.
 * The position is an index on the piece's route: -1 while it is in the castle,
 * 0 on its start square. The route itself comes from the board (T1.4).
 */
public final class Piece {
    public static final int IN_CASTLE = -1;

    private final PlayerColor color;
    private final int number;
    private int position = IN_CASTLE;

    public Piece(PlayerColor color, int number) {
        this.color = color;
        this.number = number;
    }

    public PlayerColor color() {
        return color;
    }

    /** 0 to 3. Used for the castle slot and for the 1-4 keys. */
    public int number() {
        return number;
    }

    public int position() {
        return position;
    }

    void moveTo(int position) {
        this.position = position;
    }

    public boolean inCastle() {
        return position == IN_CASTLE;
    }

    @Override
    public String toString() {
        return color.displayName() + " " + (number + 1);
    }
}
