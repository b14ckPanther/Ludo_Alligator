package Controller;

import View.MainFrame;
import View.screens.HomeScreen;

/**
 * Moves between the screens. Each screen gets callbacks for its buttons,
 * so the screens don't know about each other.
 */
public final class AppController {
    private final MainFrame frame;

    public AppController(MainFrame frame) {
        this.frame = frame;
    }

    public void showHome() {
        frame.showScreen(new HomeScreen(this::showSetup, this::exit));
    }

    public void showSetup() {
        // setup screen comes in the next commit
    }

    public void exit() {
        frame.dispose();
        System.exit(0);
    }
}
