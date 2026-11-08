package View;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;

/**
 * The only window of the game. Screens are swapped inside it with
 * {@link #showScreen(Screen)}, so the window never closes between screens.
 */
public final class MainFrame extends JFrame {
    private static final Color BACKGROUND = new Color(0x06171B);
    private static final Color TEXT = new Color(0xF4EFE6);

    private final JPanel stage = new Stage();
    private Screen current;

    public MainFrame() {
        super("Ludo - Alligator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 700));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1400, screen.width - 80), Math.min(900, screen.height - 80));
        setLocationRelativeTo(null);
        setContentPane(stage);
    }

    /** Replaces the current screen with {@code next}. */
    public void showScreen(Screen next) {
        if (current != null) {
            current.onHide();
            stage.remove(current);
        }
        current = next;
        stage.add(next, BorderLayout.CENTER);
        stage.revalidate();
        stage.repaint();
        next.onShow();
        next.requestFocusInWindow();
    }

    /** Background panel. Shows the game name until the first screen is set. */
    private final class Stage extends JPanel {
        Stage() {
            super(new BorderLayout());
            setBackground(BACKGROUND);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (current != null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(TEXT);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
            FontMetrics fm = g2.getFontMetrics();
            String title = "Ludo";
            g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, getHeight() / 2);
            g2.dispose();
        }
    }
}
