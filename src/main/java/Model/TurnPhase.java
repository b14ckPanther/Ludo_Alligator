package Model;

/** What the game is waiting for right now. Every action checks the phase first. */
public enum TurnPhase {
    /** The current player has to roll. */
    ROLL,
    /** The current player has to pick one of the legal moves. */
    CHOOSE_MOVE,
    /** The game has ended. */
    GAME_OVER
}
