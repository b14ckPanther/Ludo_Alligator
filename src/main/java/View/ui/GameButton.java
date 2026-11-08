package View.ui;

import View.ui.Icons.Icon;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
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

/**
 * The button used on all screens. We paint it ourselves to match the theme.
 * Works with the mouse and with Space / Enter when it has focus, and shows a
 * ring around it when focused.
 */
public class GameButton extends A11yComponent {
    public enum Variant { PRIMARY, SECONDARY, GHOST, DANGER, ACCENT }

    private String text;
    private Icon icon;
    private Variant variant;
    private Color accent = Theme.CHALK;
    private final List<Runnable> actions = new ArrayList<>();
    private float hover;
    private float press;
    private boolean focused;
    private Anim.Tween hoverTween;
    private Font font = Theme.ui(700, 15f);
    private int height = 46;

    public GameButton(String text, Icon icon, Variant variant) {
        super(javax.accessibility.AccessibleRole.PUSH_BUTTON);
        this.text = text;
        this.icon = icon;
        this.variant = variant;
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(text);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    animateHover(1);
                }
            }
            @Override public void mouseExited(MouseEvent e) { animateHover(0); }
            @Override public void mousePressed(MouseEvent e) {
                if (isEnabled()) { press = 1; repaint(); }
            }
            @Override public void mouseReleased(MouseEvent e) {
                boolean inside = contains(e.getPoint());
                Anim.run(180, Anim.Ease.OUT_CUBIC, t -> { press = (float) (1 - t); repaint(); });
                if (inside && isEnabled() && e.getButton() == MouseEvent.BUTTON1) fire();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
            @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
        });
        getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "press");
        getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "press");
        getActionMap().put("press", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (isEnabled()) fire(); }
        });
    }

    public GameButton(String text, Variant variant) { this(text, null, variant); }

    public GameButton onClick(Runnable action) {
        actions.add(action);
        return this;
    }

    public GameButton accent(Color color) {
        this.accent = color;
        repaint();
        return this;
    }

    public GameButton size(int height, float fontSize) {
        this.height = height;
        this.font = Theme.ui(700, fontSize);
        revalidate();
        return this;
    }

    public void setText(String text) {
        this.text = text;
        getAccessibleContext().setAccessibleName(text);
        revalidate();
        repaint();
    }

    public String getText() { return text; }

    public void setIcon(Icon icon) {
        this.icon = icon;
        repaint();
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
        repaint();
    }

    private void fire() {
        for (Runnable r : List.copyOf(actions)) r.run();
    }

    private void animateHover(float target) {
        if (hoverTween != null) hoverTween.cancel();
        float from = hover;
        hoverTween = Anim.run(160, Anim.Ease.OUT_CUBIC, t -> {
            hover = (float) Anim.lerp(from, target, t);
            repaint();
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        if (!enabled) hover = 0;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(font);
        int w = text == null || text.isEmpty() ? 0 : fm.stringWidth(text);
        int iconW = icon == null ? 0 : (int) (height * 0.42) + (w > 0 ? 10 : 0);
        int pad = w == 0 ? (height - (int) (height * 0.42)) : 26;
        return new Dimension(w + iconW + pad * 2 - (w == 0 ? 0 : 4), height);
    }

    @Override
    public Dimension getMaximumSize() {
        if (isMaximumSizeSet()) return super.getMaximumSize();
        return new Dimension(Integer.MAX_VALUE, height);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Theme.quality(g0);
        int w = getWidth();
        int h = getHeight();
        if (!isEnabled()) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
        double scale = 1 - press * 0.035;
        g.translate(w / 2.0, h / 2.0);
        g.scale(scale, scale);
        g.translate(-w / 2.0, -h / 2.0);
        double arc = h;
        RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, arc, arc);

        Color fg;
        switch (variant) {
            case PRIMARY -> {
                g.setPaint(new GradientPaint(0, 0, Theme.mix(Theme.CHALK, Color.WHITE, 0.4f + hover * 0.4f), 0, h,
                        Theme.mix(Theme.CHALK, new Color(0xD8CFBF), 0.6f - hover * 0.3f)));
                g.fill(shape);
                g.setColor(new Color(255, 255, 255, 160));
                g.setStroke(new BasicStroke(1f));
                g.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, arc, arc));
                fg = Theme.INK;
            }
            case ACCENT, DANGER -> {
                Color base = variant == Variant.DANGER ? Theme.DANGER : accent;
                g.setPaint(new GradientPaint(0, 0, Theme.mix(base, Color.WHITE, 0.12f + hover * 0.12f), 0, h,
                        Theme.mix(base, Color.BLACK, 0.12f)));
                g.fill(shape);
                g.setColor(new Color(255, 255, 255, 70));
                g.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, arc, arc));
                fg = luminance(base) > 0.6 ? Theme.INK : Color.WHITE;
            }
            case SECONDARY -> {
                g.setColor(Theme.alpha(Color.WHITE, 0.06f + hover * 0.07f));
                g.fill(shape);
                g.setColor(Theme.alpha(Color.WHITE, 0.16f + hover * 0.14f));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, arc, arc));
                fg = Theme.CHALK;
            }
            default -> {
                g.setColor(Theme.alpha(Color.WHITE, hover * 0.08f));
                g.fill(shape);
                fg = Theme.mix(Theme.CHALK_DIM, Theme.CHALK, hover);
            }
        }

        if (focused && isEnabled()) {
            g.setColor(Theme.alpha(Theme.CHALK, 0.85f));
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(-2, -2, w + 3, h + 3, arc + 4, arc + 4));
        }

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int textW = text == null ? 0 : fm.stringWidth(text);
        double iconSize = h * 0.42;
        double gap = (icon != null && textW > 0) ? 10 : 0;
        double total = textW + (icon != null ? iconSize + gap : 0);
        double x = (w - total) / 2.0;
        if (icon != null) {
            Icons.draw(g, icon, x, (h - iconSize) / 2.0, iconSize, fg);
            x += iconSize + gap;
        }
        if (textW > 0) {
            g.setColor(fg);
            g.drawString(text, (float) x, (float) ((h - fm.getHeight()) / 2.0 + fm.getAscent()));
        }
        g.dispose();
    }

    static double luminance(Color c) {
        return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
    }
}
