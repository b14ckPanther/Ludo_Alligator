package Model;

/**
 * The four player colors, in clockwise order around the board
 * (Red top-left, Green top-right, Yellow bottom-right, Blue bottom-left).
 * Turns follow this order too (P-SET-5).
 */
public enum PlayerColor {
    RED("Red"),
    GREEN("Green"),
    YELLOW("Yellow"),
    BLUE("Blue");

    private final String displayName;

    PlayerColor(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** Position of this color's castle: 0 top-left, 1 top-right, 2 bottom-right, 3 bottom-left. */
    public int quadrant() {
        return ordinal();
    }
}
