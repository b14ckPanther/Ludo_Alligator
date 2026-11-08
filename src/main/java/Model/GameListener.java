package Model;

/**
 * Observer for game events. The screens implement it to update themselves
 * when the model changes. All methods have an empty default, so a listener
 * only overrides what it needs.
 */
public interface GameListener {

    /** A new turn started for this player. */
    default void turnStarted(Player player) { }

    /** The current player rolled. */
    default void rolled(Player player, DiceRoll roll) { }

    /**
     * A piece moved along its route.
     *
     * @param from route index before ({@link Piece#IN_CASTLE} if it came out of the castle)
     * @param to   route index after ({@link Piece#IN_CASTLE} if it was captured)
     */
    default void pieceMoved(Piece piece, int from, int to) { }

    /** {@code attacker} captured {@code victim}; a pieceMoved event back to the castle follows. */
    default void captured(Piece attacker, Piece victim) { }

    /** A player brought all four pieces home and got this place (1 = first). */
    default void playerFinished(Player player, int rank) { }

    /** The game is over. The list has every player in finishing order. */
    default void gameOver(java.util.List<Player> ranking) { }

    /** The game now waits for something else (see {@link TurnPhase}). */
    default void phaseChanged(TurnPhase phase) { }

    /** A line for the game log, for example why a turn was passed. */
    default void log(String message) { }
}
