package View.ui;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/** Text fields in the game style, with a placeholder text and a red border for errors. */
public final class Field {
    private Field() { }

    public static JTextField text(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                paintShell(this, g, placeholder);
                super.paintComponent(g);
            }
        };
        style(f, 16);
        return f;
    }

    public static JPasswordField password(String placeholder) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                paintShell(this, g, getPassword().length == 0 ? placeholder : null);
                super.paintComponent(g);
            }
        };
        style(f, 16);
        return f;
    }

    public static JTextArea area(String placeholder) {
        JTextArea a = new JTextArea() {
            @Override protected void paintComponent(Graphics g) {
                paintShell(this, g, placeholder);
                super.paintComponent(g);
            }
        };
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        style(a, 14);
        return a;
    }

    private static void style(JTextComponent c, int padX) {
        c.setOpaque(false);
        c.setFont(Theme.ui(500, 15f));
        c.setForeground(Theme.CHALK);
        c.setCaretColor(Theme.CHALK);
        c.setSelectionColor(Theme.alpha(Theme.CHALK, 0.25f));
        c.setSelectedTextColor(Theme.CHALK);
        c.setBorder(BorderFactory.createEmptyBorder(11, padX, 11, padX));
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.repaint(); }
            @Override public void focusLost(FocusEvent e) { c.repaint(); }
        });
    }

    /** Shows or hides the red error border (used by the setup screen, P-SET-4). */
    public static void flagError(JTextComponent c, boolean error) {
        c.putClientProperty("field.error", error);
        c.repaint();
    }

    static void paintShell(JTextComponent c, Graphics g0, String placeholder) {
        Graphics2D g = Theme.quality(g0);
        int w = c.getWidth();
        int h = c.getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 14, 14);
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(shape);
        boolean error = Boolean.TRUE.equals(c.getClientProperty("field.error"));
        Color ring = error ? Theme.DANGER : c.hasFocus() ? Theme.alpha(Theme.CHALK, 0.7f) : Theme.LINE_STRONG;
        g.setColor(ring);
        g.setStroke(new BasicStroke(c.hasFocus() || error ? 1.6f : 1f));
        g.draw(shape);
        if (placeholder != null && c.getText().isEmpty() && !(c instanceof JPasswordField pf && pf.getPassword().length > 0)) {
            Insets in = c.getInsets();
            g.setFont(c.getFont());
            g.setColor(Theme.CHALK_FAINT);
            g.drawString(placeholder, in.left, in.top + g.getFontMetrics().getAscent());
        }
        g.dispose();
    }
}
