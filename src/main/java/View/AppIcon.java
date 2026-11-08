package View;

import Model.PlayerColor;
import View.ui.Theme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Window and dock icon, drawn in code: four colored triangles with a piece in the middle. */
public final class AppIcon {
    private AppIcon() { }

    public static BufferedImage render(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = Theme.quality(img.getGraphics());
        double s = size;
        double r = s * 0.22;
        g.setPaint(new GradientPaint(0, 0, Theme.INK_3, 0, (float) s, Theme.INK));
        g.fill(new RoundRectangle2D.Double(0, 0, s, s, r, r));
        double c = s / 2;
        double m = s * 0.16;
        PlayerColor[] order = {PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE};
        double[][] corners = {{m, m}, {s - m, m}, {s - m, s - m}, {m, s - m}};
        for (int i = 0; i < 4; i++) {
            double[] a = corners[(i + 3) % 4];
            double[] b = corners[i];
            GeneralPath tri = new GeneralPath();
            tri.moveTo(a[0], a[1]);
            tri.lineTo(b[0], b[1]);
            tri.lineTo(c, c);
            tri.closePath();
            g.setColor(Theme.player(order[i]));
            g.fill(tri);
        }
        double tr = s * 0.17;
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Ellipse2D.Double(c - tr, c - tr + s * 0.03, tr * 2, tr * 2));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(c - tr * 0.4, c - tr * 0.4), (float) (tr * 1.8),
                new float[]{0, 1}, new Color[]{Color.WHITE, Theme.SLAB}));
        g.fill(new Ellipse2D.Double(c - tr, c - tr, tr * 2, tr * 2));
        g.dispose();
        return img;
    }
}
