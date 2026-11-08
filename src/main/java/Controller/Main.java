package Controller;

import View.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the game. This is the main class of the JAR. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Ludo");
        System.setProperty("awt.useSystemAAFontSettings", "on");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // keep the default look and feel
            }
            MainFrame frame = new MainFrame();
            new AppController(frame).showHome();
            frame.setVisible(true);
        });
    }
}
