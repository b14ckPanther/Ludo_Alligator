package View.ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.RoundRectangle2D;

/** Rounded see-through panel with a thin border. Used as a card behind content. */
public class Surface extends JPanel {
    private final int radius;
    private Color tint;

    public Surface(LayoutManager layout, int radius) {
        super(layout);
        this.radius = radius;
        setOpaque(false);
    }

    /** Optional color on top, for example the current player's color. */
    public void setTint(Color tint) {
        this.tint = tint;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Theme.quality(g0);
        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, radius, radius);
        g.setColor(new Color(6, 23, 27, 150));
        g.fill(shape);
        g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 20), 0, h, new Color(255, 255, 255, 6)));
        g.fill(shape);
        if (tint != null) {
            g.setPaint(new GradientPaint(0, 0, Theme.alpha(tint, 0.22f), 0, h * 0.8f, Theme.alpha(tint, 0f)));
            g.fill(shape);
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(tint != null ? Theme.alpha(tint, 0.45f) : Theme.LINE);
        g.draw(shape);
        g.dispose();
    }
}
