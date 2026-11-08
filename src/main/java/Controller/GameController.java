package Controller;

import Model.Game;
import Model.GameListener;
import Model.Move;
import Model.Piece;
import Model.Player;
import Model.TurnPhase;
import View.game.GameScreen;
import View.ui.Anim;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Controller for one running game. It passes the player's actions from the
 * screen to the model, and after the model changed it waits until the screen
 * has finished its animations before letting the player act again.
 */
public final class GameController implements GameScreen.Actions, GameListener {
    private final Game game;
    private final AppController app;
    private GameScreen screen;
    private boolean showScheduled;

    public GameController(Game game, AppController app) {
        this.game = game;
        this.app = app;
    }

    public void attach(GameScreen screen) {
        this.screen = screen;
        game.addListener(this);
    }

    public void start() {
        game.start();
    }

    // ------------------------------------------------------------------ from the screen

    @Override
    public void roll() {
        // the model checks the phase too, this just avoids the exception
        if (game.phase() == TurnPhase.ROLL) {
            game.roll();
        }
    }

    /**
     * The player clicked one of their pieces. If it has a legal move, play it.
     * Castle pieces are all the same, so any of them takes the enter move.
     */
    @Override
    public void pick(Piece piece) {
        if (game.phase() != TurnPhase.CHOOSE_MOVE) {
            return;
        }
        for (Move move : game.legalMoves()) {
            if (move.piece() == piece) {
                game.play(move);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ from the model

    @Override
    public void phaseChanged(TurnPhase phase) {
        scheduleShow();
    }

    /** When the game is over, let the last move finish on screen, wait a moment, then show the results. */
    @Override
    public void gameOver(List<Player> ranking) {
        game.removeListener(this);
        screen.sequencer().whenIdle(() -> Anim.delay(900, () -> app.showResults(game)));
    }

    /**
     * One roll can change the phase several times in a row (roll, no move,
     * next player). We wait until the model is done and the screen has played
     * everything, then show the controls once.
     */
    private void scheduleShow() {
        if (showScheduled || screen == null) {
            return;
        }
        showScheduled = true;
        SwingUtilities.invokeLater(() -> screen.sequencer().whenIdle(() -> {
            showScheduled = false;
            screen.showPhase();
        }));
    }
}
