package Model;

/**
 * Thrown when the game setup is not valid (P-SET-4).
 * It says which field is wrong so the setup screen can mark it.
 */
public class SetupException extends RuntimeException {

    /** The setup field that has the problem. */
    public enum Field { PLAYER_COUNT, DIFFICULTY, NAME, COLOR }

    private final Field field;
    private final int seat;

    public SetupException(String message, Field field, int seat) {
        super(message);
        this.field = field;
        this.seat = seat;
    }

    public SetupException(String message, Field field) {
        this(message, field, -1);
    }

    public Field field() {
        return field;
    }

    /** Index of the player row with the problem, or -1 if it is not about one player. */
    public int seat() {
        return seat;
    }
}
