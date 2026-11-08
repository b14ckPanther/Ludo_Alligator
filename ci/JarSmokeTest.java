import Controller.AppController;
import Model.BoardFactory;
import Model.CsvHistoryRepository;
import Model.DataFiles;
import Model.Difficulty;
import Model.Game;
import Model.GameConfig;
import Model.PlayerColor;
import Model.TurnPhase;
import View.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * T1.14: checks that the jar works on the computer it runs on.
 * Used by the GitHub workflow on Windows and macOS, and can be run by hand:
 *
 *   javac -cp Ludo_Alligator.jar -d classes JarSmokeTest.java
 *   java -cp Ludo_Alligator.jar;classes JarSmokeTest      (Windows, use : on macOS)
 *
 * It plays games with the rules from the jar, saves one through the real
 * screens into data/history.csv next to the jar, checks the file and takes
 * screenshots of Home, Results and History.
 */
public class JarSmokeTest {
    private static int failures;

    public static void main(String[] args) throws Exception {
        System.out.println("OS:   " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Java: " + System.getProperty("java.version"));
        Path history = DataFiles.historyFile();
        System.out.println("History file: " + history.toAbsolutePath());
        Files.deleteIfExists(history);

        // 1. the rules: 50 games with 2, 3 and 4 players must all end with a full ranking
        int ended = 0;
        for (int i = 0; i < 50; i++) {
            Game g = playToTheEnd(2 + i % 3, i);
            if (g.phase() == TurnPhase.GAME_OVER && g.ranking().size() == g.players().size()) {
                ended++;
            }
        }
        check("50 games end with a full ranking (" + ended + "/50)", ended == 50);

        // 2. the screens and saving
        MainFrame[] frame = new MainFrame[1];
        AppController[] app = new AppController[1];
        SwingUtilities.invokeAndWait(() -> {
            frame[0] = new MainFrame();
            app[0] = new AppController(frame[0]);
            app[0].showHome();
            frame[0].setVisible(true);
        });
        Thread.sleep(2500);
        screenshot(frame[0], "1-home.png");

        Game finished = playToTheEnd(4, 99);
        SwingUtilities.invokeAndWait(() -> app[0].showResults(finished));
        Thread.sleep(2500);
        screenshot(frame[0], "2-results.png");

        SwingUtilities.invokeAndWait(() -> app[0].showHistory());
        Thread.sleep(1500);
        screenshot(frame[0], "3-history.png");

        // 3. the file next to the jar
        check("history.csv was made next to the jar", Files.exists(history));
        if (Files.exists(history)) {
            String text = Files.readString(history, StandardCharsets.UTF_8);
            String[] lines = text.split("\r\n");
            System.out.print(text);
            check("header is the course header", lines[0].equals(CsvHistoryRepository.HEADER));
            check("one game row, starting with game number 1", lines.length == 2 && lines[1].startsWith("1,easy,"));
            check("lines end with CRLF", text.endsWith("\r\n") && text.replace("\r\n", "").indexOf('\n') < 0);
        }

        SwingUtilities.invokeAndWait(() -> frame[0].dispose());
        System.out.println(failures == 0 ? "ALL CHECKS PASSED" : failures + " CHECK(S) FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static Game playToTheEnd(int players, long seed) {
        GameConfig.Builder setup = GameConfig.builder().difficulty(Difficulty.EASY);
        String[] names = {"Nour", "Lama", "Aya", "Sleeman"};
        for (int i = 0; i < players; i++) {
            setup.seat(names[i], PlayerColor.values()[i]);
        }
        Random random = new Random(seed);
        Game game = new Game(setup.build(), BoardFactory.create(Difficulty.EASY, random), random);
        game.start();
        for (int i = 0; i < 50_000 && game.phase() != TurnPhase.GAME_OVER; i++) {
            if (game.phase() == TurnPhase.ROLL) {
                game.roll();
            } else {
                game.play(game.legalMoves().get(random.nextInt(game.legalMoves().size())));
            }
        }
        return game;
    }

    private static void screenshot(MainFrame frame, String name) {
        try {
            Rectangle area = frame.getBounds();
            BufferedImage image = new Robot().createScreenCapture(area);
            ImageIO.write(image, "png", new File(name));
            System.out.println("Screenshot " + name + " " + area.width + "x" + area.height);
        } catch (Exception e) {
            System.out.println("Screenshot " + name + " not taken: " + e);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "ok    " : "FAIL  ") + name);
        if (!ok) {
            failures++;
        }
    }
}
