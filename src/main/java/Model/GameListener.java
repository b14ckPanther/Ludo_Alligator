package Model;

/**
 * Observer for game events. The screens implement it to update themselves
 * when the model changes. All methods have an empty default, so a listener
 * only overrides what it needs. More events are added with the turn and move tasks.
 */
public interface GameListener {

    /** A new turn started for this player. */
    default void turnStarted(Player player) { }
}
