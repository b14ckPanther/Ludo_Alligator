package Controller;

import Model.Game;
import Model.GameConfig;
import View.MainFrame;
import View.screens.HomeScreen;
import View.screens.SetupScreen;

/**
 * Moves between the screens. Each screen gets callbacks for its buttons,
 * so the screens don't know about each other.
 */
public final class AppController {
    private final MainFrame frame;
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
        Game game = new Game(config);
        // TODO T1.5: open the board screen for this game
        showHome();
    }

    public void exit() {
        frame.dispose();
        System.exit(0);
    }
}
