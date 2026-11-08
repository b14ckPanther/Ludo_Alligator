package View.game;

import Model.Game;
import Model.GameListener;
import Model.Player;
import View.MainFrame;
import View.Screen;
import View.ui.Anim;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * The screen of a running game: the board on the left and a side panel on
 * the right. It listens to the game (Observer) and redraws when something
 * changes. It doesn't decide anything about the rules.
 */
public final class GameScreen extends Screen implements GameListener {
    private final Game game;
    private final MainFrame frame;
    private final BoardView boardView;
    private final TurnCard turnCard = new TurnCard();
    private final Players players;
    private final JPanel side = new JPanel();

    public GameScreen(Game game, MainFrame frame) {
        this.game = game;
        this.frame = frame;
        this.boardView = new BoardView(game);
        this.players = new Players();
        game.addListener(this);

        JPanel base = new JPanel(new BorderLayout());
        base.setOpaque(false);
        base.setBorder(BorderFactory.createEmptyBorder(MainFrame.titleBarInset() + 10, 18, 18, 18));
        base.add(boardView, BorderLayout.CENTER);
        base.add(buildSide(), BorderLayout.EAST);
        add(base, BorderLayout.CENTER);
    }

    public BoardView boardView() {
        return boardView;
    }

    private JComponent buildSide() {
        side.setOpaque(false);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(384, 10));
        side.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        addToSide(turnCard);
        addToSide(players);
        addToSide(new BoardKey());
        side.add(Box.createVerticalGlue());
        return side;
    }

    /** Adds a part to the side panel, left aligned, with a gap after it. */
    public void addToSide(JComponent part) {
        part.setAlignmentX(Component.LEFT_ALIGNMENT);
        int at = side.getComponentCount();
        // keep the glue at the bottom if it is already there
        if (at > 0 && side.getComponent(at - 1) instanceof Box.Filler) {
            at--;
        }
        side.add(part, at);
        side.add(Box.createVerticalStrut(12), at + 1);
        side.revalidate();
    }

    @Override
    public void onShow() {
        boardView.playIntro();
        frame.focusBackdrop(game.currentPlayer().color());
    }

    @Override
    public void onHide() {
        game.removeListener(this);
        frame.focusBackdrop(null);
    }

    // ------------------------------------------------------------------ game events

    @Override
    public void turnStarted(Player player) {
        frame.focusBackdrop(player.color());
        turnCard.flash();
        players.repaint();
        boardView.repaint();
    }

    // ------------------------------------------------------------------ side panel parts

    /** Shows whose turn it is, with the player's name and color (P-DIC-1). */
    private final class TurnCard extends JComponent {
        private double flash;
        private String hint = "Roll the dice";

        TurnCard() {
            setPreferredSize(new Dimension(360, 96));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        }

        void flash() {
            Anim.run(700, Anim.Ease.OUT_CUBIC, t -> {
                flash = 1 - t;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            Player p = game.currentPlayer();
            Color c = Theme.player(p.color());
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 24, 24);
            g.setColor(new Color(6, 23, 27, 160));
            g.fill(shape);
            g.setPaint(new GradientPaint(0, 0, Theme.alpha(c, 0.42f + (float) flash * 0.3f), w, 0, Theme.alpha(c, 0.04f)));
            g.fill(shape);
            g.setColor(Theme.alpha(c, 0.7f));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);

            double d = 56;
            double x = 20;
            double y = (h - d) / 2;
            g.setColor(Theme.playerDeep(p.color()));
            g.fill(new Ellipse2D.Double(x, y + 4, d, d));
            g.setPaint(new RadialGradientPaint(new Point2D.Double(x + d * 0.35, y + d * 0.3), (float) d,
                    new float[]{0, 1}, new Color[]{Theme.mix(c, Color.WHITE, 0.45f), c}));
            g.fill(new Ellipse2D.Double(x, y, d, d));
            Icons.draw(g, Icon.USER, x + d * 0.25, y + d * 0.23, d * 0.5, Color.WHITE);

            float tx = (float) (x + d + 16);
            g.setFont(Theme.ui(600, 12.5f));
            g.setColor(Theme.alpha(Theme.CHALK, 0.75f));
            g.drawString(p.color().displayName() + "'s turn", tx, (float) (y + 16));
            g.setFont(Theme.display(900, 21f));
            g.setColor(Theme.CHALK);
            FontMetrics fm = g.getFontMetrics();
            String name = p.name();
            while (fm.stringWidth(name) > w - tx - 20 && name.length() > 3) {
                name = name.substring(0, name.length() - 1);
            }
            g.drawString(name, tx, (float) (y + 40));
            g.setFont(Theme.ui(500, 13.5f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString(hint, tx, (float) (y + 60));
            g.dispose();
        }
    }

    /** Short help text under the current player's name. */
    public void setHint(String text) {
        turnCard.hint = text;
        turnCard.repaint();
    }

    /** List of the players in turn order. The current one is highlighted. */
    private final class Players extends JComponent {
        Players() {
            int rows = game.players().size();
            Dimension d = new Dimension(360, 40 + rows * 40);
            setPreferredSize(d);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            int w = getWidth();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, getHeight() - 1, 22, 22);
            g.setColor(new Color(6, 23, 27, 150));
            g.fill(shape);
            g.setColor(Theme.LINE);
            g.draw(shape);
            g.setFont(Theme.ui(700, 13f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString("Players", 18, 26);
            g.setFont(Theme.ui(500, 12f));
            String right = "Pieces home";
            g.drawString(right, w - 18 - g.getFontMetrics().stringWidth(right), 26);

            int y = 40;
            for (Player p : game.players()) {
                boolean current = p == game.currentPlayer();
                if (current) {
                    g.setColor(Theme.alpha(Theme.player(p.color()), 0.14f));
                    g.fill(new RoundRectangle2D.Double(8, y + 2, w - 16, 34, 14, 14));
                }
                g.setColor(Theme.player(p.color()));
                g.fill(new Ellipse2D.Double(18, y + 12, 12, 12));
                g.setFont(Theme.ui(current ? 700 : 600, 15f));
                g.setColor(p.hasFinished() ? Theme.CHALK_DIM : Theme.CHALK);
                g.drawString(p.name(), 40, y + 24);
                if (p.hasFinished()) {
                    g.setFont(Theme.display(900, 13f));
                    g.setColor(Theme.GOLD);
                    g.drawString("#" + p.rank(), w - 120, y + 24);
                }
                long home = p.pieces().stream().filter(piece -> piece.position() == game.board().finishIndex()).count();
                for (int i = 0; i < 4; i++) {
                    double dotX = w - 26 - (3 - i) * 17;
                    if (i < home) {
                        g.setColor(Theme.player(p.color()));
                        g.fill(new Ellipse2D.Double(dotX - 6, y + 12, 12, 12));
                    } else {
                        g.setColor(Theme.LINE_STRONG);
                        g.setStroke(new BasicStroke(1.4f));
                        g.draw(new Ellipse2D.Double(dotX - 5.5, y + 12.5, 11, 11));
                    }
                }
                y += 40;
            }
            g.dispose();
        }
    }
}
