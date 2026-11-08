package View.ui;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A row of options where exactly one is selected (like "2 players | 3 | 4").
 * Left and right arrow keys also change the selection.
 *
 * @param <T> type of the options
 */
public class Segmented<T> extends JComponent {
    private final List<T> options;
    private final Function<T, String> label;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private int selected;
    private double thumb;
    private boolean focused;
    private Function<T, Color> accent = t -> Theme.CHALK;

    public Segmented(List<T> options, Function<T, String> label, T initial) {
        this.options = List.copyOf(options);
        this.label = label;
        this.selected = Math.max(0, options.indexOf(initial));
        this.thumb = selected;
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int i = (int) (e.getX() / (getWidth() / (double) options.size()));
                select(Math.max(0, Math.min(options.size() - 1, i)), true);
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
            @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
        });
        getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "prev");
        getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "next");
        getActionMap().put("prev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { select(Math.max(0, selected - 1), true); }
        });
        getActionMap().put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { select(Math.min(options.size() - 1, selected + 1), true); }
        });
    }

    public Segmented<T> accent(Function<T, Color> accent) {
        this.accent = accent;
        return this;
    }

    public void onChange(Consumer<T> listener) { listeners.add(listener); }

    public T value() { return options.get(selected); }

    public void setValue(T value) { select(options.indexOf(value), false); }

    private void select(int index, boolean user) {
        if (index < 0 || index == selected) return;
        double from = thumb;
        selected = index;
        Anim.run(240, Anim.Ease.OUT_BACK, t -> { thumb = Anim.lerp(from, index, t); repaint(); });
        for (Consumer<T> l : listeners) l.accept(options.get(index));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.ui(600, 14f));
        int w = 0;
        for (T o : options) w = Math.max(w, fm.stringWidth(label.apply(o)));
        return new Dimension((w + 36) * options.size(), 42);
    }

    @Override
    public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 42); }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Theme.quality(g0);
        int w = getWidth();
        int h = getHeight();
        double seg = w / (double) options.size();
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
        g.setColor(Theme.LINE);
        g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, h, h));
        Color acc = accent.apply(options.get(selected));
        g.setColor(acc);
        g.fill(new RoundRectangle2D.Double(thumb * seg + 3, 3, seg - 6, h - 6, h - 6, h - 6));
        if (focused) {
            g.setColor(Theme.alpha(Theme.CHALK, 0.8f));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(-1.5, -1.5, w + 2, h + 2, h + 3, h + 3));
        }
        g.setFont(Theme.ui(600, 14f));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < options.size(); i++) {
            String s = label.apply(options.get(i));
            boolean on = i == selected;
            g.setColor(on ? (GameButton.luminance(acc) > 0.6 ? Theme.INK : Color.WHITE) : Theme.CHALK_DIM);
            g.drawString(s, (float) (i * seg + (seg - fm.stringWidth(s)) / 2), (float) ((h - fm.getHeight()) / 2.0 + fm.getAscent()));
        }
        g.dispose();
    }
}
