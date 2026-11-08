package View.ui;

import Model.PlayerColor;

import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Background painted behind every screen: dark teal with four soft lights in
 * the player colors that move slowly, darker edges and a bit of noise.
 * During a game the light of the current player can be made stronger.
 */
public final class Backdrop {
    private static final BufferedImage GRAIN = makeGrain(256);
    private final long born = System.nanoTime();
    private PlayerColor focus;
    private final float[] weight = new float[PlayerColor.values().length];
    private final Timer timer;

    public Backdrop(Runnable repaint) {
        timer = new Timer(1000 / 30, e -> repaint.run());
    }

    public void start() {
        if (!Anim.reducedMotion()) timer.start();
    }

    public void stop() { timer.stop(); }

    /** Makes one player's light stronger (null = all the same). */
    public void focus(PlayerColor color) { focus = color; }

    public void paint(Graphics2D g, int w, int h) {
        double t = (System.nanoTime() - born) / 1e9;
        g.setPaint(new GradientPaint(0, 0, Theme.INK_2, 0, h, Theme.INK));
        g.fill(new Rectangle2D.Double(0, 0, w, h));

        double r = Math.max(w, h) * 0.55;
        PlayerColor[] colors = PlayerColor.values();
        for (int i = 0; i < colors.length; i++) {
            double phase = t * 0.09 + i * Math.PI / 2;
            int quadrant = colors[i].quadrant();
            double baseX = (quadrant == 0 || quadrant == 3) ? 0.12 : 0.88;
            double baseY = (quadrant == 0 || quadrant == 1) ? 0.1 : 0.9;
            double cx = w * (baseX + Math.sin(phase) * 0.06);
            double cy = h * (baseY + Math.cos(phase * 1.3) * 0.06);
            float target = focus == null ? 0f : (colors[i] == focus ? 1f : -1f);
            weight[i] += (target - weight[i]) * 0.06f; // move a bit each frame so it changes smoothly
            float strength = 0.13f + (weight[i] > 0 ? 0.19f : 0.07f) * weight[i];
            glow(g, cx, cy, r, Theme.player(colors[i]), strength);
        }
        // light in the middle, behind the board
        glow(g, w * 0.42, h * 0.48, Math.min(w, h) * 0.6, new Color(0x2A6A70), 0.22f);

        // darker edges
        g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h / 2.0), (float) (Math.hypot(w, h) * 0.62),
                new float[]{0.45f, 1f}, new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 150)},
                MultipleGradientPaint.CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(0, 0, w, h));

        // noise texture
        Graphics2D gg = (Graphics2D) g.create();
        gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
        for (int y = 0; y < h; y += GRAIN.getHeight()) {
            for (int x = 0; x < w; x += GRAIN.getWidth()) gg.drawImage(GRAIN, x, y, null);
        }
        gg.dispose();
    }

    private static void glow(Graphics2D g, double cx, double cy, double r, Color c, float alpha) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) r,
                new float[]{0f, 0.5f, 1f},
                new Color[]{Theme.alpha(c, alpha), Theme.alpha(c, alpha * 0.3f), Theme.alpha(c, 0)}));
        g.fill(new Rectangle2D.Double(cx - r, cy - r, r * 2, r * 2));
    }

    private static BufferedImage makeGrain(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Random rnd = new Random(11);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int v = rnd.nextInt(256);
                img.setRGB(x, y, (255 << 24) | (v << 16) | (v << 8) | v);
            }
        }
        return img;
    }
}
