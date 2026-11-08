package View.ui;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.BorderFactory;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

/** Helpers for labels and scroll panes in the game style. */
public final class Text {
    private Text() { }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        // a few pixels extra on the right: with antialiased text the last letter can be cut off otherwise
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** A label that wraps its text to the given width (in pixels). */
    public static JLabel paragraph(String text, Font font, Color color, int width) {
        JLabel l = new JLabel("<html><div style='width:" + width + "px'>" + escape(text) + "</div></html>");
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /**
     * Text that wraps to the width it really gets from the layout, up to
     * maxWidth. Safer than an HTML label, whose wrap width is drawn a bit
     * differently on Windows and macOS.
     */
    public static JTextArea wrapping(String text, Font font, Color color, int maxWidth) {
        JTextArea area = new JTextArea(text) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(maxWidth, super.getPreferredSize().height);
            }
        };
        area.setFont(font);
        area.setForeground(color);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setHighlighter(null);
        return area;
    }

    public static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(null);
        styleBar(sp.getVerticalScrollBar());
        styleBar(sp.getHorizontalScrollBar());
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    private static void styleBar(JScrollBar bar) {
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(10, 10));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void paintTrack(Graphics g, javax.swing.JComponent c, Rectangle r) { }

            @Override protected void paintThumb(Graphics g0, javax.swing.JComponent c, Rectangle r) {
                Graphics2D g = Theme.quality(g0);
                g.setColor(isThumbRollover() ? Theme.alpha(Theme.CHALK, 0.4f) : Theme.alpha(Theme.CHALK, 0.18f));
                g.fill(new RoundRectangle2D.Double(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6));
                g.dispose();
            }

            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }

            private JButton zero() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }
}
