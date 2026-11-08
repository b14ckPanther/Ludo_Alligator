package Controller;

import Model.BoardFactory;
import Model.Game;
import Model.GameConfig;
import Model.Player;
import View.MainFrame;
import View.game.GameScreen;
import View.screens.HomeScreen;
import View.screens.ResultsScreen;
import View.screens.SetupScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Moves between the screens. Each screen gets callbacks for its buttons,
 * so the screens don't know about each other.
 */
public final class AppController {
    private final MainFrame frame;
    private final Random random = new Random();
    private GameConfig lastConfig;

    public AppController(MainFrame frame) {
        this.frame = frame;
    }

    public void showHome() {
        frame.showScreen(new HomeScreen(this::showSetup, this::exit));
    }

    /** Opens the setup screen, filled with the last game's players if there was one. */
    public void showSetup() {
        frame.showScreen(new SetupScreen(lastConfig, this::startGame, this::showHome));
    }

    /** Called by the setup screen with a config that already passed the checks. */
    public void startGame(GameConfig config) {
        lastConfig = config;
        Game game = new Game(config, BoardFactory.create(config.difficulty(), random), random);
        GameController controller = new GameController(game, this);
        GameScreen screen = new GameScreen(game, frame, controller);
        controller.attach(screen);
        frame.showScreen(screen);
        controller.start();
    }

    /** Shows the results of a finished game (P-END-3). */
    public void showResults(Game game) {
        List<ResultsScreen.Place> places = new ArrayList<>();
        for (Player p : game.ranking()) {
            places.add(new ResultsScreen.Place(p.rank(), p.name(), p.color()));
        }
        long seconds = game.clock().elapsedMillis() / 1000;
        frame.showScreen(new ResultsScreen(places, seconds, this::playAgain, this::showHome));
    }

    /** Starts a new game with the same players, colors and difficulty. */
    public void playAgain() {
        if (lastConfig != null) {
            startGame(lastConfig);
        } else {
            showSetup();
        }
    }

    public void exit() {
        frame.dispose();
        System.exit(0);
    }
}
