package View.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Icons drawn with Java2D shapes instead of image files, so they look sharp
 * at any size. Each icon is drawn in a size x size box at (x, y).
 */
public final class Icons {
    public enum Icon {
        PLAY, HISTORY, LOCK, UNLOCK, STAR, GIFT, HASH, ARROW, PAUSE, SOUND, SOUND_OFF, MUSIC, MUSIC_OFF,
        CLOSE, CHECK, TRASH, PLUS, BOT, USER, BACK, SEARCH, CROWN, CLOCK, QUESTION, EDIT, LOGOUT, KEY, FLAG, SHIELD, DICE
    }

    private Icons() { }

    public static void draw(Graphics2D g0, Icon icon, double x, double y, double size, Color color) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.scale(size / 24.0, size / 24.0);
        g.setColor(color);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case PLAY -> g.fill(poly(8, 5, 19, 12, 8, 19));
            case HISTORY -> {
                g.draw(new Arc2D.Double(3, 3, 18, 18, 110, 300, Arc2D.OPEN));
                g.draw(poly(3, 6, 4.2, 10.6, 8.6, 9.4));
                g.draw(new Line2D.Double(12, 7.5, 12, 12));
                g.draw(new Line2D.Double(12, 12, 15.5, 14));
            }
            case LOCK, UNLOCK -> {
                g.fill(new RoundRectangle2D.Double(4.5, 10.5, 15, 10.5, 3, 3));
                Path2D shackle = new Path2D.Double();
                shackle.moveTo(8, 10.5);
                shackle.lineTo(8, 7.5);
                shackle.curveTo(8, 2.5, 16, 2.5, 16, 7.5);
                if (icon == Icon.LOCK) shackle.lineTo(16, 10.5);
                g.draw(shackle);
            }
            case STAR -> g.fill(star(12, 12.6, 10.5, 4.6));
            case GIFT -> {
                g.fill(new RoundRectangle2D.Double(3.5, 9, 17, 4.5, 1.5, 1.5));
                g.fill(new RoundRectangle2D.Double(5, 13, 14, 8, 1.5, 1.5));
                Color c = g.getColor();
                g.setColor(new Color(255, 255, 255, 210));
                g.fill(new java.awt.geom.Rectangle2D.Double(11, 9, 2, 12));
                g.setColor(c);
                g.draw(new Arc2D.Double(6.5, 4, 6, 6, 0, 230, Arc2D.OPEN));
                g.draw(new Arc2D.Double(11.5, 4, 6, 6, -50, 230, Arc2D.OPEN));
            }
            case HASH -> {
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(9.5, 4, 8, 20));
                g.draw(new Line2D.Double(16, 4, 14.5, 20));
                g.draw(new Line2D.Double(4.5, 9, 19.5, 9));
                g.draw(new Line2D.Double(4, 15.5, 19, 15.5));
            }
            case ARROW -> {
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(4, 12, 19, 12));
                g.draw(poly(13, 6, 19.5, 12, 13, 18));
            }
            case PAUSE -> {
                g.fill(new RoundRectangle2D.Double(6, 5, 4, 14, 2, 2));
                g.fill(new RoundRectangle2D.Double(14, 5, 4, 14, 2, 2));
            }
            case SOUND, SOUND_OFF -> {
                g.fill(poly(4, 9, 8, 9, 13, 4.5, 13, 19.5, 8, 15, 4, 15));
                if (icon == Icon.SOUND) {
                    g.draw(new Arc2D.Double(11, 8, 7, 8, -55, 110, Arc2D.OPEN));
                    g.draw(new Arc2D.Double(11, 5, 10, 14, -55, 110, Arc2D.OPEN));
                } else {
                    g.draw(new Line2D.Double(16, 9, 21, 15));
                    g.draw(new Line2D.Double(21, 9, 16, 15));
                }
            }
            case MUSIC, MUSIC_OFF -> {
                g.fill(new Ellipse2D.Double(4, 15, 6, 5));
                g.fill(new Ellipse2D.Double(14, 13, 6, 5));
                g.draw(new Line2D.Double(9.5, 17, 9.5, 6));
                g.draw(new Line2D.Double(19.5, 15, 19.5, 4));
                g.draw(new Line2D.Double(9.5, 6, 19.5, 4));
                if (icon == Icon.MUSIC_OFF) {
                    g.setColor(color);
                    g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new Line2D.Double(3, 3, 21, 21));
                }
            }
            case CLOSE -> {
                g.draw(new Line2D.Double(6, 6, 18, 18));
                g.draw(new Line2D.Double(18, 6, 6, 18));
            }
            case CHECK -> {
                g.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(poly(5, 12.5, 10, 17.5, 19, 7));
            }
            case TRASH -> {
                g.draw(new Line2D.Double(4, 7, 20, 7));
                g.draw(poly(9, 7, 9.5, 4, 14.5, 4, 15, 7));
                g.draw(poly(6, 7, 7, 20, 17, 20, 18, 7));
                g.draw(new Line2D.Double(10, 11, 10, 16));
                g.draw(new Line2D.Double(14, 11, 14, 16));
            }
            case PLUS -> {
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
            }
            case BOT -> {
                g.draw(new RoundRectangle2D.Double(4.5, 8, 15, 11.5, 4, 4));
                g.draw(new Line2D.Double(12, 8, 12, 4.5));
                g.fill(new Ellipse2D.Double(10.5, 2.5, 3, 3));
                g.fill(new Ellipse2D.Double(8, 12, 2.6, 2.6));
                g.fill(new Ellipse2D.Double(13.4, 12, 2.6, 2.6));
                g.draw(new Line2D.Double(2.5, 12, 2.5, 15.5));
                g.draw(new Line2D.Double(21.5, 12, 21.5, 15.5));
            }
            case USER -> {
                g.fill(new Ellipse2D.Double(8, 3.5, 8, 8));
                Path2D body = new Path2D.Double();
                body.moveTo(4, 21);
                body.curveTo(4, 14, 20, 14, 20, 21);
                body.closePath();
                g.fill(body);
            }
            case BACK -> {
                g.draw(new Line2D.Double(20, 12, 5, 12));
                g.draw(poly(11, 6, 5, 12, 11, 18));
            }
            case SEARCH -> {
                g.draw(new Ellipse2D.Double(4, 4, 12, 12));
                g.draw(new Line2D.Double(14.5, 14.5, 20, 20));
            }
            case CROWN -> {
                g.fill(poly(3, 8, 8, 12, 12, 5, 16, 12, 21, 8, 19, 18, 5, 18));
                g.fill(new RoundRectangle2D.Double(5, 19, 14, 2.4, 1, 1));
            }
            case CLOCK -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
                g.draw(new Line2D.Double(12, 7.5, 12, 12));
                g.draw(new Line2D.Double(12, 12, 15, 14.5));
            }
            case QUESTION -> {
                g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D q = new Path2D.Double();
                q.moveTo(7.5, 8.5);
                q.curveTo(7.5, 3, 16.5, 3, 16.5, 8.5);
                q.curveTo(16.5, 12, 12, 11.5, 12, 15);
                g.draw(q);
                g.fill(new Ellipse2D.Double(10.4, 18, 3.2, 3.2));
            }
            case EDIT -> {
                g.draw(poly(4, 20, 5, 15, 16, 4, 20, 8, 9, 19));
                g.draw(new Line2D.Double(13.5, 6.5, 17.5, 10.5));
            }
            case LOGOUT -> {
                g.draw(poly(10, 4, 4, 4, 4, 20, 10, 20));
                g.draw(new Line2D.Double(9, 12, 20, 12));
                g.draw(poly(16, 8, 20, 12, 16, 16));
            }
            case KEY -> {
                g.draw(new Ellipse2D.Double(3, 9, 8, 8));
                g.draw(new Line2D.Double(11, 13, 21, 13));
                g.draw(new Line2D.Double(17.5, 13, 17.5, 16.5));
                g.draw(new Line2D.Double(20.5, 13, 20.5, 15.5));
            }
            case FLAG -> {
                g.draw(new Line2D.Double(5, 21, 5, 3.5));
                g.fill(poly(5, 4, 19, 4, 15.5, 8.5, 19, 13, 5, 13));
            }
            case SHIELD -> {
                Path2D s = new Path2D.Double();
                s.moveTo(12, 2.5);
                s.lineTo(20, 5.5);
                s.curveTo(20, 13, 17, 18.5, 12, 21.5);
                s.curveTo(7, 18.5, 4, 13, 4, 5.5);
                s.closePath();
                g.fill(s);
            }
            case DICE -> {
                g.draw(new RoundRectangle2D.Double(3.5, 3.5, 17, 17, 5, 5));
                g.fill(new Ellipse2D.Double(6.6, 6.6, 3.2, 3.2));
                g.fill(new Ellipse2D.Double(10.4, 10.4, 3.2, 3.2));
                g.fill(new Ellipse2D.Double(14.2, 14.2, 3.2, 3.2));
            }
        }
        g.dispose();
    }

    public static GeneralPath poly(double... pts) {
        GeneralPath p = new GeneralPath();
        p.moveTo(pts[0], pts[1]);
        for (int i = 2; i < pts.length; i += 2) p.lineTo(pts[i], pts[i + 1]);
        if (pts.length > 4 && !(pts[0] == pts[pts.length - 2] && pts[1] == pts[pts.length - 1])) {
            // Polylines used for strokes stay open; filled shapes close implicitly.
        }
        return p;
    }

    /** Five-point star centered on (cx, cy). */
    public static GeneralPath star(double cx, double cy, double outer, double inner) {
        GeneralPath p = new GeneralPath();
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? outer : inner;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double px = cx + Math.cos(a) * r;
            double py = cy + Math.sin(a) * r;
            if (i == 0) p.moveTo(px, py);
            else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }
}
