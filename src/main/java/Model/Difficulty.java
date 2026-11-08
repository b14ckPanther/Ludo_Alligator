package Model;

/**
 * Difficulty level of a game. Only Easy is playable in iteration 1,
 * Medium and Hard are added in iterations 2 and 3.
 */
public enum Difficulty {
    EASY("Easy", true),
    MEDIUM("Medium", false),
    HARD("Hard", false);

    private final String displayName;
    private final boolean available;

    Difficulty(String displayName, boolean available) {
        this.displayName = displayName;
        this.available = available;
    }

    public String displayName() {
        return displayName;
    }

    /** False for levels that are not implemented yet. */
    public boolean isAvailable() {
        return available;
    }

    /** The dice used on this level (Strategy pattern). */
    public DiceStrategy diceStrategy() {
        return switch (this) {
            case EASY -> new StandardDiceStrategy();
            case MEDIUM, HARD -> throw new UnsupportedOperationException(displayName + " dice come in a later iteration");
        };
    }

    /** The value written in the difficulty column of the history file (easy, medium, hard). */
    public String csvName() {
        return name().toLowerCase();
    }
}
