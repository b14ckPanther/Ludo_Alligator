package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The settings of a new game: difficulty and the players.
 * It is made with {@link Builder}, which checks the rules in P-SET-2 and P-SET-3,
 * so a GameConfig object is always valid.
 */
public final class GameConfig {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int MAX_NAME_LENGTH = 16;

    /** One player row of the setup screen. */
    public record Seat(String name, PlayerColor color) { }

    private final Difficulty difficulty;
    private final List<Seat> seats;

    private GameConfig(Difficulty difficulty, List<Seat> seats) {
        this.difficulty = difficulty;
        this.seats = Collections.unmodifiableList(seats);
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public List<Seat> seats() {
        return seats;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Builder pattern: collect the values, then build() checks them all at once. */
    public static final class Builder {
        private Difficulty difficulty;
        private final List<Seat> seats = new ArrayList<>();

        public Builder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        /** Adds a player. Spaces around the name are removed. */
        public Builder seat(String name, PlayerColor color) {
            seats.add(new Seat(name == null ? "" : name.strip(), color));
            return this;
        }

        /**
         * Checks everything and returns the config.
         *
         * @throws SetupException with the first problem found
         */
        public GameConfig build() {
            if (seats.size() < MIN_PLAYERS || seats.size() > MAX_PLAYERS) {
                throw new SetupException("A game needs 2 to 4 players", SetupException.Field.PLAYER_COUNT);
            }
            if (difficulty == null) {
                throw new SetupException("Choose a difficulty", SetupException.Field.DIFFICULTY);
            }
            if (!difficulty.isAvailable()) {
                throw new SetupException(difficulty.displayName() + " is not available yet, please choose Easy",
                        SetupException.Field.DIFFICULTY);
            }

            Set<String> names = new HashSet<>();
            Set<PlayerColor> colors = EnumSet.noneOf(PlayerColor.class);
            for (int i = 0; i < seats.size(); i++) {
                Seat seat = seats.get(i);
                if (seat.name().isEmpty()) {
                    throw new SetupException("Every player needs a name", SetupException.Field.NAME, i);
                }
                if (seat.name().length() > MAX_NAME_LENGTH) {
                    throw new SetupException("A name can have at most " + MAX_NAME_LENGTH + " characters",
                            SetupException.Field.NAME, i);
                }
                if (!names.add(seat.name().toLowerCase())) {
                    throw new SetupException("Two players can't have the same name", SetupException.Field.NAME, i);
                }
                if (seat.color() == null) {
                    throw new SetupException("Every player needs a color", SetupException.Field.COLOR, i);
                }
                if (!colors.add(seat.color())) {
                    throw new SetupException("Each color can be taken only once", SetupException.Field.COLOR, i);
                }
            }
            return new GameConfig(difficulty, new ArrayList<>(seats));
        }
    }
}
