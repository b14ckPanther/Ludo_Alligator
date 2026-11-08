package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A player in the game: name, color and four pieces that start in the castle. */
public final class Player {
    public static final int PIECES = 4;

    private final String name;
    private final PlayerColor color;
    private final List<Piece> pieces = new ArrayList<>();
    private int rank;

    public Player(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
        for (int i = 0; i < PIECES; i++) {
            pieces.add(new Piece(color, i));
        }
    }

    public String name() {
        return name;
    }

    public PlayerColor color() {
        return color;
    }

    public List<Piece> pieces() {
        return Collections.unmodifiableList(pieces);
    }

    /** Finishing place (1 = first), or 0 while the player is still playing. */
    public int rank() {
        return rank;
    }

    void setRank(int rank) {
        this.rank = rank;
    }

    public boolean hasFinished() {
        return rank > 0;
    }

    @Override
    public String toString() {
        return name + " (" + color.displayName() + ")";
    }
}
