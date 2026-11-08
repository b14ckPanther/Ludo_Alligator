package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A running game. It keeps the players and whose turn it is, and tells the
 * listeners (the screens) when something changes. No Swing code here.
 */
public final class Game {
    private final Difficulty difficulty;
    private final Board board;
    private final List<Player> players = new ArrayList<>();
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();
    private int current;

    /**
     * Creates the players from the setup. They are sorted clockwise by castle
     * (Red, Green, Yellow, Blue), so the first color in play starts (P-SET-5).
     */
    public Game(GameConfig config, Board board) {
        this.difficulty = config.difficulty();
        this.board = board;
        config.seats().stream()
                .sorted(Comparator.comparingInt(seat -> seat.color().quadrant()))
                .forEach(seat -> players.add(new Player(seat.name(), seat.color())));
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public Board board() {
        return board;
    }

    /** The square a piece stands on, or null while it is in the castle. */
    public Square squareOf(Piece piece) {
        return piece.inCastle() ? null : board.route(piece.color()).get(piece.position());
    }

    /** The players in turn order. */
    public List<Player> players() {
        return Collections.unmodifiableList(players);
    }

    public Player currentPlayer() {
        return players.get(current);
    }

    /** Announces the first player. Called once, when the board screen is ready. */
    public void start() {
        Player first = currentPlayer();
        listeners.forEach(l -> l.turnStarted(first));
    }
}
