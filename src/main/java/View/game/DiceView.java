package View.game;

import Model.DiceRoll;
import View.ui.Anim;
import View.ui.Theme;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * The die on the side panel. When rolling it spins and shows random faces
 * for a moment, then stops on the real result that came from the model.
 * It can also be clicked to roll.
 */
public final class DiceView extends JComponent {
    private final Random shuffle = new Random();
    private final Timer glowTimer;
    private int face = 6;
    private double tumble;
    private double settle = 1;
    private double angle;
    private boolean armed;
    private Color armColor = Theme.CHALK;
    private boolean hover;
    private Runnable onRoll = () -> { };

    public DiceView() {
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (armed) {
                    onRoll.run();
                }
            }
        });
        glowTimer = new Timer(1000 / 30, e -> repaint());
    }

    public void onRoll(Runnable action) {
        this.onRoll = action;
    }

    /** When armed the die glows in the player's color and can be clicked. */
    public void arm(boolean armed, Color color) {
        this.armed = armed;
        this.armColor = color;
        if (armed) {
            glowTimer.start();
        } else {
            glowTimer.stop();
        }
        setCursor(Cursor.getPredefinedCursor(armed ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    /** Plays the roll animation, ends on the given result, then calls done. */
    public void play(DiceRoll roll, Runnable done) {
        double spin = (shuffle.nextBoolean() ? 1 : -1) * (Math.PI * 2 + shuffle.nextDouble() * Math.PI);
        int[] lastSwap = {-1};
        Anim.run(620, Anim.Ease.OUT_CUBIC, t -> {
            tumble = 1 - t;
            angle = spin * (1 - t);
            int bucket = (int) (t * 9);
            if (bucket != lastSwap[0] && t < 0.92) {
                lastSwap[0] = bucket;
                face = 1 + shuffle.nextInt(6);
            }
            repaint();
        }, () -> {
            face = roll.step();
            settle = 0;
            Anim.run(260, Anim.Ease.OUT_BACK, t -> {
                settle = t;
                repaint();
            }, done);
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(260, 120);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Theme.quality(g0);
        double size = Math.min(getHeight() * 0.72, getWidth() * 0.42);
        double x = (getWidth() - size) / 2;
        double y = (getHeight() - size) / 2;

        if (armed) {
            double pulse = 0.5 + 0.5 * Math.sin(System.nanoTime() / 1e9 * 4);
            g.setColor(Theme.alpha(armColor, (float) (0.10 + 0.12 * pulse + (hover ? 0.1 : 0))));
            g.fill(new RoundRectangle2D.Double(x - 14, y - 14, size + 28, size + 28, size * 0.5, size * 0.5));
        }
        double bounce = (1 - settle) * 0.12;
        paintDie(g, x, y - tumble * size * 0.18, size, 1 + bounce);
        g.dispose();
    }

    private void paintDie(Graphics2D g0, double x, double y, double s, double scale) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x + s / 2, y + s / 2);
        g.rotate(angle);
        g.scale(scale, scale);
        double r = s * 0.24;
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new RoundRectangle2D.Double(-s / 2 + 3, -s / 2 + 7, s, s, r, r));
        g.setPaint(new GradientPaint((float) (-s / 2), (float) (-s / 2), new Color(0xFFFDF8), (float) (s / 2),
                (float) (s / 2), new Color(0xE3D9C6)));
        g.fill(new RoundRectangle2D.Double(-s / 2, -s / 2, s, s, r, r));
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(-s / 2 + 2, -s / 2 + 2, s - 4, s - 4, r * 0.9, r * 0.9));

        double pr = s * 0.085;
        double o = s * 0.25;
        double[][] dots = switch (face) {
            case 1 -> new double[][]{{0, 0}};
            case 2 -> new double[][]{{-o, -o}, {o, o}};
            case 3 -> new double[][]{{-o, -o}, {0, 0}, {o, o}};
            case 4 -> new double[][]{{-o, -o}, {o, -o}, {-o, o}, {o, o}};
            case 5 -> new double[][]{{-o, -o}, {o, -o}, {0, 0}, {-o, o}, {o, o}};
            default -> new double[][]{{-o, -o}, {o, -o}, {-o, 0}, {o, 0}, {-o, o}, {o, o}};
        };
        for (double[] p : dots) {
            // a six gets red dots once the die has stopped
            g.setColor(face == 6 && settle >= 1 ? new Color(0xC2263A) : Theme.INK_3);
            g.fill(new Ellipse2D.Double(p[0] - pr, p[1] - pr, pr * 2, pr * 2));
            g.setColor(new Color(255, 255, 255, 60));
            g.fill(new Ellipse2D.Double(p[0] - pr * 0.4, p[1] - pr * 0.6, pr * 0.7, pr * 0.5));
        }
        g.dispose();
    }
}
