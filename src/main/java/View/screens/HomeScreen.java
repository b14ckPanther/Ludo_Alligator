package View.screens;

import Model.PlayerColor;
import View.MainFrame;
import View.Screen;
import View.ui.Anim;
import View.ui.GameButton;
import View.ui.Icons.Icon;
import View.ui.Text;
import View.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

/**
 * First screen of the game. From here the player starts a new game (P-SET-1)
 * or closes the game.
 */
public final class HomeScreen extends Screen {
    private final Wordmark wordmark = new Wordmark();
    private final JPanel right = new JPanel(new BorderLayout());
    private final JPanel buttons = new JPanel();
    private final GameButton play;

    public HomeScreen(Runnable onPlay, Runnable onExit) {
        JPanel grid = new JPanel(new GridLayout(1, 2));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(MainFrame.titleBarInset() + 30, 72, 48, 40));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Box.createVerticalGlue());

        wordmark.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(wordmark);
        left.add(Box.createVerticalStrut(4));
        JLabel kicker = Text.label("Group Alligator edition", Theme.ui(600, 17f), Theme.CHALK_DIM);
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(kicker);
        left.add(Box.createVerticalStrut(18));
        JLabel blurb = Text.paragraph("The classic board game for 2 to 4 players on one computer. "
                + "Roll a six to leave your castle, capture the other pieces and be the first "
                + "to bring all four home.", Theme.ui(400, 17f), Theme.CHALK, 430);
        blurb.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(blurb);
        left.add(Box.createVerticalStrut(34));

        play = new GameButton("Play", Icon.PLAY, GameButton.Variant.PRIMARY).size(58, 18f).onClick(onPlay);
        GameButton exit = new GameButton("Exit", Icon.CLOSE, GameButton.Variant.SECONDARY).size(50, 15f).onClick(onExit);
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton(play);
        addButton(exit);
        left.add(buttons);
        left.add(Box.createVerticalGlue());

        JLabel foot = Text.label("University of Haifa, Department of Information Systems", Theme.ui(500, 12.5f),
                Theme.CHALK_FAINT);
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(foot);

        right.setOpaque(false);
        grid.add(left);
        grid.add(right);
        add(grid, BorderLayout.CENTER);
    }

    private void addButton(JComponent button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension size = new Dimension(300, button.getPreferredSize().height);
        button.setMaximumSize(size);
        button.setPreferredSize(size);
        buttons.add(button);
        buttons.add(Box.createVerticalStrut(10));
    }

    @Override
    public void onShow() {
        wordmark.reveal();
        play.requestFocusInWindow();
    }

    /**
     * The big "LUDO" title. Each letter has one player color and a 3D shadow,
     * and the letters drop in one after the other when the screen opens.
     */
    private static final class Wordmark extends JComponent {
        private final double[] drop = {0, 0, 0, 0};

        Wordmark() {
            setPreferredSize(new Dimension(560, 150));
            setMaximumSize(new Dimension(620, 150));
        }

        void reveal() {
            for (int i = 0; i < 4; i++) {
                int k = i;
                drop[k] = 0;
                Anim.delay(120 + i * 110, () -> Anim.run(620, Anim.Ease.OUT_BACK, t -> {
                    drop[k] = t;
                    repaint();
                }));
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            Font f = Theme.display(900, 118f);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            String word = "LUDO";
            PlayerColor[] colors = PlayerColor.values();
            double x = 0;
            double base = fm.getAscent() - 6;
            for (int i = 0; i < word.length(); i++) {
                String ch = String.valueOf(word.charAt(i));
                double y = base - (1 - drop[i]) * 60;
                Graphics2D gl = (Graphics2D) g.create();
                gl.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Anim.clamp01(drop[i])));
                // shadow: the same letter drawn a few times, a bit lower and darker each time
                for (int k = 9; k >= 1; k--) {
                    gl.setColor(Theme.mix(Theme.playerDeep(colors[i]), Theme.INK, 0.25f + k * 0.04f));
                    gl.drawString(ch, (float) (x + k * 0.9), (float) (y + k * 1.1));
                }
                gl.setColor(Theme.player(colors[i]));
                gl.drawString(ch, (float) x, (float) y);
                gl.dispose();
                x += fm.stringWidth(ch) - 4;
            }
            g.dispose();
        }
    }
}
