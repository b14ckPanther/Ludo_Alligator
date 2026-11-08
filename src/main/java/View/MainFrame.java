package View;

import Model.PlayerColor;
import View.ui.Anim;
import View.ui.Backdrop;
import View.ui.Theme;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The only window of the game. Screens are swapped inside it with
 * {@link #showScreen(Screen)}. The old screen fades out while the new one
 * fades in, and the animated background stays behind all of them.
 */
public final class MainFrame extends JFrame {
    private final Stage stage = new Stage();
    private Screen current;

    public MainFrame() {
        super("Ludo - Alligator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 700));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1400, screen.width - 80), Math.min(900, screen.height - 80));
        setLocationRelativeTo(null);

        // on macOS the content goes under a transparent title bar
        JRootPane root = getRootPane();
        root.putClientProperty("apple.awt.fullWindowContent", true);
        root.putClientProperty("apple.awt.transparentTitleBar", true);
        root.putClientProperty("apple.awt.windowTitleVisible", false);
        setContentPane(stage);

        List<Image> icons = List.of(AppIcon.render(16), AppIcon.render(32), AppIcon.render(64), AppIcon.render(256));
        setIconImages(icons);
        try {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(AppIcon.render(512));
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            // no dock / taskbar icon on this system
        }
        stage.backdrop.start();
    }

    /** Space to leave at the top on macOS, so content is not under the window buttons. */
    public static int titleBarInset() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac") ? 28 : 0;
    }

    /** Replaces the current screen with {@code next}. */
    public void showScreen(Screen next) {
        Screen previous = current;
        BufferedImage snapshot = null;
        if (previous != null && previous.getWidth() > 0) {
            // take a picture of the old screen so it can fade out
            snapshot = new BufferedImage(previous.getWidth(), previous.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = snapshot.createGraphics();
            previous.paint(g);
            g.dispose();
            previous.onHide();
            stage.remove(previous);
        }
        current = next;
        stage.add(next, BorderLayout.CENTER);
        stage.revalidate();
        stage.snapshot = snapshot;
        if (snapshot != null) {
            Anim.run(420, Anim.Ease.OUT_CUBIC, t -> {
                stage.fade = t;
                stage.repaint();
            }, () -> {
                stage.snapshot = null;
                stage.repaint();
            });
        }
        next.onShow();
        stage.repaint();
        next.requestFocusInWindow();
    }

    /** Lights up the background on one player's side (null for none). */
    public void focusBackdrop(PlayerColor color) {
        stage.backdrop.focus(color);
    }

    /** Content pane: paints the background, then the screen (fading during a switch). */
    private static final class Stage extends JPanel {
        final Backdrop backdrop = new Backdrop(this::repaint);
        BufferedImage snapshot;
        double fade = 1;

        Stage() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(Theme.INK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            backdrop.paint(g2, getWidth(), getHeight());
            g2.dispose();
        }

        @Override
        protected void paintChildren(Graphics g) {
            if (snapshot == null) {
                super.paintChildren(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fade));
            g2.translate(0, (1 - fade) * 24);
            super.paintChildren(g2);
            g2.dispose();
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (1 - fade)));
            g3.drawImage(snapshot, 0, 0, null);
            g3.dispose();
        }
    }
}
