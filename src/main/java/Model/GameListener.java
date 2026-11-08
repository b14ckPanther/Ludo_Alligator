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

    /** The game now waits for something else (see {@link TurnPhase}). */
    default void phaseChanged(TurnPhase phase) { }

    /** A line for the game log, for example why a turn was passed. */
    default void log(String message) { }
}
