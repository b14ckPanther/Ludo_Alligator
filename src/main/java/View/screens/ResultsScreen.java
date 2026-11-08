package View.screens;

import Model.PlayerColor;
import View.MainFrame;
import View.Screen;
import View.ui.Anim;
import View.ui.GameButton;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Text;
import View.ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Shown when a game ends (P-END-3): every place with the player's name and
 * color on a podium, and how long the game took.
 */
public final class ResultsScreen extends Screen {

    /** One line of the result: place, name and color. */
    public record Place(int rank, String name, PlayerColor color) { }

    private final Podium podium;
    private final JLabel message;

    /**
     * @param places          players in finishing order
     * @param durationSeconds game time without pauses
     */
    public ResultsScreen(List<Place> places, long durationSeconds, Runnable onPlayAgain, Runnable onHome) {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(MainFrame.titleBarInset() + 26, 48, 36, 48));
        podium = new Podium(places);
        root.add(podium, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        message = Text.label("Game time " + Theme.formatDuration(durationSeconds), Theme.ui(500, 15f), Theme.CHALK_DIM);
        // the label takes the whole width, so a longer message is never cut off
        message.setHorizontalAlignment(JLabel.CENTER);
        message.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        south.add(message, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttons.setOpaque(false);
        buttons.add(new GameButton("Play again", Icon.PLAY, GameButton.Variant.PRIMARY).size(52, 16f).onClick(onPlayAgain));
        buttons.add(new GameButton("Main menu", Icon.BACK, GameButton.Variant.SECONDARY).size(52, 16f).onClick(onHome));
        south.add(buttons, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        add(root, BorderLayout.CENTER);
    }

    /** Text under the podium, for example whether the game was saved. */
    public void setMessage(String text, boolean error) {
        message.setText(text);
        message.setForeground(error ? Theme.DANGER : Theme.CHALK_DIM);
    }

    @Override
    public void onShow() {
        podium.play();
    }

    @Override
    public void onHide() {
        podium.timer.stop();
    }

    /** The winner's name on top, the podium columns and some confetti. */
    private static final class Podium extends JComponent {
        private final List<Place> places;
        private final Timer timer = new Timer(1000 / 60, e -> step());
        // each piece of confetti: x, y, speed x, speed y, color, angle, size
        private final List<double[]> confetti = new ArrayList<>();
        private final Random random = new Random();
        private double rise;

        Podium(List<Place> places) {
            this.places = places;
            setOpaque(false);
        }

        void play() {
            timer.start();
            for (int i = 0; i < 160; i++) {
                spawn(true);
            }
            Anim.run(1100, Anim.Ease.OUT_BACK, t -> rise = t);
        }

        private void spawn(boolean anywhere) {
            confetti.add(new double[]{random.nextDouble(), anywhere ? -random.nextDouble() * 0.8 : -0.05,
                    (random.nextDouble() - 0.5) * 0.002, 0.002 + random.nextDouble() * 0.004, random.nextInt(4),
                    random.nextDouble() * 6, 6 + random.nextDouble() * 8});
        }

        private void step() {
            for (double[] c : confetti) {
                c[0] += c[2] + Math.sin(c[5]) * 0.0008;
                c[1] += c[3];
                c[5] += 0.08;
            }
            confetti.removeIf(c -> c[1] > 1.1);
            if (confetti.size() < 90) {
                spawn(false);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            int w = getWidth();
            int h = getHeight();
            Place winner = places.get(0);
            Color wc = Theme.player(winner.color());

            g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h * 0.45), (float) (Math.min(w, h) * 0.6),
                    new float[]{0, 1}, new Color[]{Theme.alpha(wc, 0.35f), Theme.alpha(wc, 0f)}));
            g.fillRect(0, 0, w, h);

            g.setFont(Theme.ui(700, 16f));
            FontMetrics fm = g.getFontMetrics();
            String kicker = "Winner";
            g.setColor(Theme.CHALK_DIM);
            g.drawString(kicker, (w - fm.stringWidth(kicker)) / 2f, 30);
            g.setFont(Theme.display(900, 56f));
            fm = g.getFontMetrics();
            g.setColor(Theme.playerDeep(winner.color()));
            g.drawString(winner.name(), (w - fm.stringWidth(winner.name())) / 2f + 3, 96);
            g.setColor(wc);
            g.drawString(winner.name(), (w - fm.stringWidth(winner.name())) / 2f, 92);

            // columns from left to right: 2nd, 1st, 3rd, 4th (with 2 players: 1st, 2nd)
            int[] order = places.size() >= 3 ? new int[]{1, 0, 2, 3} : new int[]{0, 1};
            int cols = Math.min(order.length, places.size());
            double colW = Math.min(170, w / 5.0);
            double startX = (w - cols * colW - (cols - 1) * 16) / 2.0;
            double baseY = h - 20;
            double maxH = Math.min(260, h - 230);
            for (int k = 0; k < order.length; k++) {
                int index = order[k];
                if (index >= places.size()) {
                    continue;
                }
                paintColumn(g, places.get(index), startX + k * (colW + 16), colW, baseY, maxH);
            }

            PlayerColor[] colors = PlayerColor.values();
            for (double[] c : confetti) {
                Graphics2D gc = (Graphics2D) g.create();
                gc.translate(c[0] * w, c[1] * h);
                gc.rotate(c[5]);
                gc.setColor(Theme.player(colors[(int) c[4]]));
                gc.fill(new Rectangle2D.Double(-c[6] / 2, -c[6] / 4, c[6], c[6] / 2 * Math.abs(Math.cos(c[5]))));
                gc.dispose();
            }
            g.dispose();
        }

        private void paintColumn(Graphics2D g, Place p, double x, double colW, double baseY, double maxH) {
            double share = switch (p.rank()) {
                case 1 -> 1.0;
                case 2 -> 0.72;
                case 3 -> 0.5;
                default -> 0.32;
            };
            double colH = maxH * share * Anim.clamp01(rise) + 10;
            double y = baseY - colH;
            Color c = Theme.player(p.color());
            g.setPaint(new GradientPaint((float) x, (float) y, Theme.mix(c, Color.WHITE, 0.1f), (float) x,
                    (float) baseY, Theme.playerDeep(p.color())));
            g.fill(new RoundRectangle2D.Double(x, y, colW, colH + 20, 22, 22));

            g.setFont(Theme.display(900, 40f));
            FontMetrics fm = g.getFontMetrics();
            String rank = String.valueOf(p.rank());
            g.setColor(Theme.alpha(Color.WHITE, 0.9f));
            if (colH > 60) {
                g.drawString(rank, (float) (x + (colW - fm.stringWidth(rank)) / 2), (float) (y + 54));
            }

            double d = 58;
            double ty = y - d - 34;
            g.setColor(Theme.playerDeep(p.color()));
            g.fill(new Ellipse2D.Double(x + (colW - d) / 2, ty + 4, d, d));
            g.setPaint(new RadialGradientPaint(new Point2D.Double(x + colW / 2 - d * 0.15, ty + d * 0.3), (float) d,
                    new float[]{0, 1}, new Color[]{Theme.mix(c, Color.WHITE, 0.5f), c}));
            g.fill(new Ellipse2D.Double(x + (colW - d) / 2, ty, d, d));
            if (p.rank() == 1) {
                Icons.draw(g, Icon.CROWN, x + colW / 2 - 18, ty - 34, 36, Theme.GOLD);
            }
            Icons.draw(g, Icon.USER, x + colW / 2 - 13, ty + 15, 26, Color.WHITE);

            g.setFont(Theme.ui(700, 16f));
            fm = g.getFontMetrics();
            g.setColor(Theme.CHALK);
            g.drawString(p.name(), (float) (x + (colW - fm.stringWidth(p.name())) / 2), (float) (y - 12));
            g.setFont(Theme.ui(500, 12.5f));
            fm = g.getFontMetrics();
            String color = p.color().displayName();
            g.setColor(Theme.CHALK_DIM);
            g.drawString(color, (float) (x + (colW - fm.stringWidth(color)) / 2), (float) (y + colH - 4));
        }
    }
}
