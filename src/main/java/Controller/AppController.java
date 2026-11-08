package Controller;

import Model.BoardFactory;
import Model.CsvHistoryRepository;
import Model.DataFiles;
import Model.Game;
import Model.GameConfig;
import Model.GameRecord;
import Model.HistoryRepository;
import Model.Player;
import View.MainFrame;
import View.game.GameScreen;
import View.screens.HistoryScreen;
import View.screens.HomeScreen;
import View.screens.ResultsScreen;
import View.screens.SetupScreen;

import java.time.LocalDateTime;
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
    private final HistoryRepository history = new CsvHistoryRepository(DataFiles.historyFile());
    private GameConfig lastConfig;

    public AppController(MainFrame frame) {
        this.frame = frame;
    }

    public void showHome() {
        frame.showScreen(new HomeScreen(this::showSetup, this::showHistory, this::exit));
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

    /**
     * Saves a finished game to the history (P-END-4) and shows the results
     * (P-END-3). If saving fails the results are still shown, with the error
     * under them (P-END-5).
     */
    public void showResults(Game game) {
        List<ResultsScreen.Place> places = new ArrayList<>();
        for (Player p : game.ranking()) {
            places.add(new ResultsScreen.Place(p.rank(), p.name(), p.color()));
        }
        long seconds = game.clock().elapsedMillis() / 1000;
        ResultsScreen screen = new ResultsScreen(places, seconds, this::playAgain, this::showHome);
        try {
            GameRecord saved = history.save(GameRecord.of(game, LocalDateTime.now()));
            screen.setMessage("Game time " + CsvHistoryRepository.formatDuration(seconds)
                    + ". Saved to the history as game #" + saved.id() + ".", false);
        } catch (RuntimeException e) {
            screen.setMessage("The game couldn't be saved to the history. " + e.getMessage(), true);
        }
        frame.showScreen(screen);
    }

    /** Opens the list of saved games (P-HIS-1). */
    public void showHistory() {
        frame.showScreen(new HistoryScreen(history.findAll(), this::showHome));
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
