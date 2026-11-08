package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A running game. It keeps the players, whose turn it is and what the game
 * is waiting for, and tells the listeners (the screens) when something
 * changes. No Swing code here.
 *
 * <p>Every action first checks the phase, so clicking twice or pressing
 * Space while a move is being chosen can't break the game.
 */
public final class Game {
    private final Difficulty difficulty;
    private final Board board;
    private final DiceStrategy dice;
    private final Random random;
    private final GameClock clock = new GameClock();
    private final List<Player> players = new ArrayList<>();
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();

    private int current;
    private TurnPhase phase = TurnPhase.ROLL;
    private DiceRoll lastRoll;
    private List<Move> legalMoves = List.of();
    private boolean extraRoll;
    private boolean started;

    /**
     * Creates the players from the setup. They are sorted clockwise by castle
     * (Red, Green, Yellow, Blue), so the first color in play starts (P-SET-5).
     */
    public Game(GameConfig config, Board board, Random random) {
        this.difficulty = config.difficulty();
        this.board = board;
        this.random = random;
        this.dice = difficulty.diceStrategy();
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

    // ------------------------------------------------------------------ state

    public Difficulty difficulty() {
        return difficulty;
    }

    public Board board() {
        return board;
    }

    public GameClock clock() {
        return clock;
    }

    /** The players in turn order. */
    public List<Player> players() {
        return Collections.unmodifiableList(players);
    }

    public Player currentPlayer() {
        return players.get(current);
    }

    public TurnPhase phase() {
        return phase;
    }

    /** The last roll of the current turn, or null before the first roll. */
    public DiceRoll lastRoll() {
        return lastRoll;
    }

    /** The moves the current player can choose from (only in CHOOSE_MOVE). */
    public List<Move> legalMoves() {
        return legalMoves;
    }

    /** The square a piece stands on, or null while it is in the castle. */
    public Square squareOf(Piece piece) {
        return piece.inCastle() ? null : board.route(piece.color()).get(piece.position());
    }

    /** True if the piece reached the finish square. */
    public boolean isFinished(Piece piece) {
        return piece.position() == board.finishIndex();
    }

    /** True if the piece is in its home column (between the arrow and the finish). */
    public boolean inHomeColumn(Piece piece) {
        return piece.position() >= board.homeColumnStart() && !isFinished(piece);
    }

    /** How many of the player's pieces are at the finish. */
    public long finishedCount(Player player) {
        return player.pieces().stream().filter(this::isFinished).count();
    }

    // ------------------------------------------------------------------ actions

    /** Starts the clock and announces the first player. Called once. */
    public void start() {
        if (started) {
            return;
        }
        started = true;
        clock.start();
        fire(l -> l.turnStarted(currentPlayer()));
        setPhase(TurnPhase.ROLL);
    }

    /**
     * Rolls the dice for the current player (P-DIC-2). A six gives another
     * roll after the move (P-DIC-5). If no move is possible the reason is
     * logged and the turn passes on its own (P-DIC-7).
     *
     * @throws IllegalStateException if the game isn't waiting for a roll
     */
    public DiceRoll roll() {
        requirePhase(TurnPhase.ROLL);
        Player player = currentPlayer();
        DiceRoll roll = dice.roll(random);
        lastRoll = roll;
        extraRoll = roll.isSix();
        fire(l -> l.rolled(player, roll));

        legalMoves = findLegalMoves(player, roll);
        if (legalMoves.isEmpty()) {
            log(noMoveReason(player, roll));
            endAction();
        } else {
            setPhase(TurnPhase.CHOOSE_MOVE);
        }
        return roll;
    }

    // ------------------------------------------------------------------ rules

    /**
     * All moves the player can make with this roll.
     * <ul>
     *   <li>P-MOV-1: a piece leaves the castle only on a six, onto its start square.
     *       The player may also use the six for a piece that is already out.</li>
     *   <li>P-MOV-2: a piece on the track moves exactly the movement value.</li>
     *   <li>P-MOV-3: a piece in its home column moves by the step die only.
     *       On Easy both are the same, but this keeps the rule right for Medium and Hard.</li>
     *   <li>P-MOV-4: a move that would go past the finish is not allowed.</li>
     * </ul>
     */
    List<Move> findLegalMoves(Player player, DiceRoll roll) {
        List<Move> moves = new ArrayList<>();
        for (Piece piece : player.pieces()) {
            if (piece.inCastle()) {
                if (roll.isSix()) {
                    moves.add(new Move(piece, Piece.IN_CASTLE, 0));
                }
            } else if (!isFinished(piece)) {
                int steps = inHomeColumn(piece) ? roll.step() : roll.moveSteps();
                int target = piece.position() + steps;
                if (steps > 0 && target <= board.finishIndex()) {
                    moves.add(new Move(piece, piece.position(), target));
                }
            }
        }
        return moves;
    }

    private String noMoveReason(Player player, DiceRoll roll) {
        boolean noneOnBoard = player.pieces().stream().allMatch(p -> p.inCastle() || isFinished(p));
        if (noneOnBoard && !roll.isSix()) {
            return player.name() + " needs a six to leave the castle";
        }
        // the only pieces out are in the home column and the roll is too big
        return player.name() + " can't move " + roll.step() + ", it would go past the finish";
    }

    /** Called after a roll or a move is finished: roll again after a six, else next player. */
    void endAction() {
        if (phase == TurnPhase.GAME_OVER) {
            return;
        }
        if (extraRoll) {
            extraRoll = false;
            log(currentPlayer().name() + " rolled a six and rolls again");
            setPhase(TurnPhase.ROLL);
        } else {
            nextPlayer();
        }
    }

    /** Gives the turn to the next player clockwise who is still playing. */
    void nextPlayer() {
        extraRoll = false;
        legalMoves = List.of();
        lastRoll = null;
        for (int i = 0; i < players.size(); i++) {
            current = (current + 1) % players.size();
            if (!currentPlayer().hasFinished()) {
                break;
            }
        }
        fire(l -> l.turnStarted(currentPlayer()));
        setPhase(TurnPhase.ROLL);
    }

    void setPhase(TurnPhase next) {
        phase = next;
        fire(l -> l.phaseChanged(next));
    }

    private void requirePhase(TurnPhase expected) {
        if (phase != expected) {
            throw new IllegalStateException("The game is waiting for " + phase + ", not " + expected);
        }
    }

    void log(String message) {
        fire(l -> l.log(message));
    }

    void fire(Consumer<GameListener> event) {
        for (GameListener listener : listeners) {
            event.accept(listener);
        }
    }
}
