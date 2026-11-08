package Model;

/**
 * One move the current player can make (Command pattern, SDP 6.2).
 * The game makes the list of legal moves, the player picks one, and the
 * game carries it out.
 *
 * @param piece the piece that moves
 * @param from  route index before the move ({@link Piece#IN_CASTLE} when entering)
 * @param to    route index after the move
 */
public record Move(Piece piece, int from, int to) {

    /** True if this move brings a piece out of the castle. */
    public boolean isEnter() {
        return from == Piece.IN_CASTLE;
    }
}
