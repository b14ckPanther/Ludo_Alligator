package View.screens;

import Model.Difficulty;
import Model.GameConfig;
import Model.PlayerColor;
import Model.SetupException;
import View.MainFrame;
import View.Screen;
import View.ui.A11yComponent;
import View.ui.Anim;
import View.ui.Field;
import View.ui.GameButton;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Segmented;
import View.ui.Surface;
import View.ui.Text;
import View.ui.Theme;

import javax.accessibility.AccessibleRole;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * New game setup (P-SET-1 to P-SET-4).
 * Left side: number of players and difficulty. Right side: a row per player
 * with a name and a color. The values are checked by GameConfig in the model;
 * this screen only shows the error and marks the field.
 */
public final class SetupScreen extends Screen {
    private final Segmented<Integer> count;
    private final List<DifficultyCard> cards = new ArrayList<>();
    private final List<Seat> seats = new ArrayList<>();
    private final JPanel seatList = new JPanel();
    private final JLabel error = Text.label(" ", Theme.ui(600, 14f), Theme.DANGER);
    private Difficulty difficulty = Difficulty.EASY;

    /**
     * @param previous last game's setup to fill in again, or null
     * @param onStart  called with a valid config when the player presses Start
     * @param onBack   called when the player goes back to Home
     */
    public SetupScreen(GameConfig previous, Consumer<GameConfig> onStart, Runnable onBack) {
        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(MainFrame.titleBarInset() + 22, 48, 32, 48));
        root.add(header(onBack), BorderLayout.NORTH);

        if (previous != null) {
            difficulty = previous.difficulty();
        }

        // left card: number of players and difficulty
        Surface left = new Surface(new BorderLayout(), 28);
        left.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.add(section("How many players?"));
        leftCol.add(Box.createVerticalStrut(10));
        int initialCount = previous == null ? 4 : previous.seats().size();
        count = new Segmented<>(List.of(2, 3, 4), n -> n + " players", initialCount);
        count.setAlignmentX(Component.LEFT_ALIGNMENT);
        count.getAccessibleContext().setAccessibleName("Number of players");
        count.onChange(n -> rebuildSeats());
        leftCol.add(count);
        leftCol.add(Box.createVerticalStrut(26));
        leftCol.add(section("Difficulty"));
        leftCol.add(Box.createVerticalStrut(10));
        for (Difficulty d : Difficulty.values()) {
            DifficultyCard card = new DifficultyCard(d);
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            cards.add(card);
            leftCol.add(card);
            leftCol.add(Box.createVerticalStrut(10));
        }
        left.add(leftCol, BorderLayout.NORTH);

        // right card: one row per player
        Surface right = new Surface(new BorderLayout(), 28);
        right.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        right.add(section("Players"), BorderLayout.NORTH);
        seatList.setOpaque(false);
        seatList.setLayout(new BoxLayout(seatList, BoxLayout.Y_AXIS));
        seatList.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        right.add(seatList, BorderLayout.CENTER);

        for (int i = 0; i < GameConfig.MAX_PLAYERS; i++) {
            String name = "Player " + (i + 1);
            PlayerColor color = PlayerColor.values()[i];
            if (previous != null && i < previous.seats().size()) {
                name = previous.seats().get(i).name();
                color = previous.seats().get(i).color();
            }
            seats.add(new Seat(i, name, color));
        }

        JPanel cols = new JPanel(new GridLayout(1, 2, 22, 0));
        cols.setOpaque(false);
        cols.add(left);
        cols.add(right);
        root.add(cols, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(error, BorderLayout.WEST);
        GameButton start = new GameButton("Start game", Icon.PLAY, GameButton.Variant.PRIMARY).size(56, 17f);
        start.onClick(() -> tryStart(onStart));
        footer.add(start, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
        rebuildSeats();
    }

    private JPanel header(Runnable onBack) {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        GameButton back = new GameButton("Back", Icon.BACK, GameButton.Variant.GHOST).size(40, 14f).onClick(onBack);
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        backWrap.setOpaque(false);
        backWrap.add(back);
        header.add(backWrap, BorderLayout.WEST);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(Text.label("New game", Theme.display(900, 38f), Theme.CHALK));
        titleBox.add(Text.label("Choose who plays and how hard the board is.", Theme.ui(500, 16f), Theme.CHALK_DIM));
        header.add(titleBox, BorderLayout.CENTER);
        return header;
    }

    private static JLabel section(String text) {
        JLabel label = Text.label(text, Theme.ui(700, 17f), Theme.CHALK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Sends the values to the model. If something is wrong, shows the message and marks the field. */
    private void tryStart(Consumer<GameConfig> onStart) {
        clearErrors();
        GameConfig.Builder builder = GameConfig.builder().difficulty(difficulty);
        for (int i = 0; i < count.value(); i++) {
            Seat seat = seats.get(i);
            builder.seat(seat.name.getText(), seat.color);
        }
        try {
            onStart.accept(builder.build());
        } catch (SetupException e) {
            error.setText(e.getMessage());
            if (e.seat() >= 0) {
                Seat seat = seats.get(e.seat());
                if (e.field() == SetupException.Field.NAME) {
                    Field.flagError(seat.name, true);
                    seat.name.requestFocusInWindow();
                } else if (e.field() == SetupException.Field.COLOR) {
                    seat.swatches.flagError(true);
                }
            }
        }
    }

    private void clearErrors() {
        error.setText(" ");
        for (Seat seat : seats) {
            Field.flagError(seat.name, false);
            seat.swatches.flagError(false);
        }
    }

    /** Shows the rows for the chosen number of players. */
    private void rebuildSeats() {
        // a hidden row could keep a color that a visible row took meanwhile,
        // so give every visible row a free color before showing it
        Set<PlayerColor> used = EnumSet.noneOf(PlayerColor.class);
        for (int i = 0; i < count.value(); i++) {
            Seat seat = seats.get(i);
            if (!used.add(seat.color)) {
                for (PlayerColor c : PlayerColor.values()) {
                    if (!used.contains(c)) {
                        seat.color = c;
                        used.add(c);
                        break;
                    }
                }
            }
        }
        seatList.removeAll();
        for (int i = 0; i < count.value(); i++) {
            seatList.add(seats.get(i));
            seatList.add(Box.createVerticalStrut(12));
        }
        seatList.revalidate();
        seatList.repaint();
        clearErrors();
    }

    /** True if another visible row already has this color. */
    private boolean takenByOther(Seat seat, PlayerColor color) {
        for (int i = 0; i < count.value(); i++) {
            Seat other = seats.get(i);
            if (other != seat && other.color == color) {
                return true;
            }
        }
        return false;
    }

    private void repaintSeats() {
        for (Seat seat : seats) {
            seat.repaint();
        }
    }

    // ------------------------------------------------------------------ difficulty card

    /** One difficulty option. Medium and Hard are shown but can't be chosen yet. */
    private final class DifficultyCard extends A11yComponent {
        private final Difficulty level;
        private final String details;
        private float hover;
        private float on;

        DifficultyCard(Difficulty level) {
            super(AccessibleRole.RADIO_BUTTON);
            this.level = level;
            this.details = switch (level) {
                case EASY -> "52-square track   4 safe squares   Step die";
                case MEDIUM -> "Outer 64-square ring   Boost die   Questions";
                case HARD -> "Outer 72-square ring   UltraBoost die   Surprises";
            };
            this.on = level == difficulty ? 1 : 0;
            setPreferredSize(new Dimension(420, 86));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
            setFocusable(level.isAvailable());
            setEnabled(level.isAvailable());
            setCursor(Cursor.getPredefinedCursor(level.isAvailable() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            getAccessibleContext().setAccessibleName(level.displayName() + " difficulty"
                    + (level.isAvailable() ? "" : ", not available yet"));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        Anim.run(140, Anim.Ease.OUT_CUBIC, t -> { hover = (float) t; repaint(); });
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (isEnabled()) {
                        Anim.run(140, Anim.Ease.OUT_CUBIC, t -> { hover = (float) (1 - t); repaint(); });
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    choose();
                }
            });
            getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "choose");
            getActionMap().put("choose", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    choose();
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { repaint(); }

                @Override
                public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        void choose() {
            if (!isEnabled() || difficulty == level) {
                return;
            }
            difficulty = level;
            error.setText(" ");
            for (DifficultyCard card : cards) {
                float from = card.on;
                float target = card.level == level ? 1 : 0;
                Anim.run(220, Anim.Ease.OUT_CUBIC, t -> {
                    card.on = (float) Anim.lerp(from, target, t);
                    card.repaint();
                });
            }
        }

        private Color accent() {
            return switch (level) {
                case EASY -> Theme.LEVEL_EASY;
                case MEDIUM -> Theme.LEVEL_MEDIUM;
                case HARD -> Theme.LEVEL_HARD;
            };
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            int w = getWidth();
            int h = getHeight();
            boolean available = level.isAvailable();
            Color accent = available ? accent() : Theme.alpha(accent(), 0.35f);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 20, 20);
            g.setColor(Theme.alpha(Color.WHITE, 0.03f + hover * 0.04f));
            g.fill(shape);
            g.setColor(Theme.alpha(accent, 0.16f * on));
            g.fill(shape);
            g.setColor(on > 0.5 ? accent : hasFocus() ? Theme.alpha(Theme.CHALK, 0.7f) : Theme.LINE_STRONG);
            g.setStroke(new BasicStroke(on > 0.5 ? 2f : 1.2f));
            g.draw(shape);

            // 1, 2 or 3 bars for the level
            int bars = level.ordinal() + 1;
            for (int i = 0; i < 3; i++) {
                g.setColor(i < bars ? accent : Theme.LINE_STRONG);
                g.fill(new RoundRectangle2D.Double(22 + i * 12, 24, 8, 26, 4, 4));
            }
            g.setFont(Theme.display(900, 19f));
            g.setColor(available ? Theme.CHALK : Theme.CHALK_FAINT);
            g.drawString(level.displayName(), 74, 36);
            g.setFont(Theme.ui(500, 13.5f));
            g.setColor(available ? Theme.CHALK_DIM : Theme.CHALK_FAINT);
            g.drawString(available ? "Classic board with one die." : "Not available yet, coming in a later version.",
                    74, 56);
            g.setFont(Theme.ui(600, 12f));
            g.setColor(Theme.CHALK_FAINT);
            g.drawString(details, 74, 74);

            if (on > 0.5) {
                double size = 24;
                g.setColor(accent);
                g.fill(new Ellipse2D.Double(w - size - 18, (h - size) / 2, size, size));
                Icons.draw(g, Icon.CHECK, w - size - 18 + 4, (h - size) / 2 + 4, size - 8, Theme.INK);
            } else if (!available) {
                Icons.draw(g, Icon.LOCK, w - 38, (h - 18) / 2.0, 18, Theme.CHALK_FAINT);
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ player row

    /** A player row: colored token, name field and the four color dots. */
    private final class Seat extends JPanel {
        final JTextField name;
        final Swatches swatches = new Swatches();
        PlayerColor color;

        Seat(int index, String initialName, PlayerColor color) {
            super(new BorderLayout(14, 0));
            this.color = color;
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            name = Field.text("Player name");
            name.setText(initialName);
            name.getAccessibleContext().setAccessibleName("Name of player " + (index + 1));
            name.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { Field.flagError(name, false); }

                @Override
                public void removeUpdate(DocumentEvent e) { Field.flagError(name, false); }

                @Override
                public void changedUpdate(DocumentEvent e) { }
            });

            JPanel middle = new JPanel(new BorderLayout(0, 8));
            middle.setOpaque(false);
            middle.add(name, BorderLayout.CENTER);
            middle.add(swatches, BorderLayout.SOUTH);
            add(new Token(), BorderLayout.WEST);
            add(middle, BorderLayout.CENTER);
        }

        void pick(PlayerColor c) {
            // P-SET-3: a color that another player has is not offered
            if (takenByOther(this, c)) {
                return;
            }
            color = c;
            swatches.flagError(false);
            repaintSeats();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 20, 20);
            g.setPaint(new GradientPaint(0, 0, Theme.alpha(Theme.player(color), 0.2f), getWidth(), 0,
                    Theme.alpha(Theme.player(color), 0.02f)));
            g.fill(shape);
            g.setColor(Theme.alpha(Theme.player(color), 0.45f));
            g.draw(shape);
            g.dispose();
        }

        /** Round token in the player's color with the player number. */
        private final class Token extends JComponent {
            Token() {
                setPreferredSize(new Dimension(58, 58));
            }

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = Theme.quality(g0);
                double d = 54;
                double y = (getHeight() - d) / 2;
                g.setColor(Theme.playerDeep(color));
                g.fill(new Ellipse2D.Double(2, y + 3, d, d));
                g.setPaint(new RadialGradientPaint(new Point2D.Double(2 + d * 0.35, y + d * 0.3), (float) d,
                        new float[]{0, 1}, new Color[]{Theme.mix(Theme.player(color), Color.WHITE, 0.45f),
                        Theme.player(color)}));
                g.fill(new Ellipse2D.Double(2, y, d, d));
                Icons.draw(g, Icon.USER, 2 + d * 0.26, y + d * 0.24, d * 0.48, Color.WHITE);
                g.dispose();
            }
        }

        /** The four color dots. Taken colors are faded and crossed out. */
        private final class Swatches extends JComponent {
            private boolean error;

            Swatches() {
                setPreferredSize(new Dimension(200, 22));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        int i = (e.getX() - 2) / 30;
                        if (i >= 0 && i < PlayerColor.values().length) {
                            pick(PlayerColor.values()[i]);
                        }
                    }
                });
            }

            void flagError(boolean value) {
                error = value;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = Theme.quality(g0);
                for (int i = 0; i < PlayerColor.values().length; i++) {
                    PlayerColor c = PlayerColor.values()[i];
                    double x = 4 + i * 30;
                    boolean taken = takenByOther(Seat.this, c);
                    g.setColor(Theme.alpha(Theme.player(c), taken ? 0.3f : 1f));
                    g.fill(new Ellipse2D.Double(x, 3, 16, 16));
                    if (taken) {
                        g.setColor(Theme.alpha(Theme.CHALK, 0.5f));
                        g.setStroke(new BasicStroke(1.6f));
                        g.draw(new Line2D.Double(x + 3, 6, x + 13, 16));
                    }
                    if (c == color) {
                        g.setColor(error ? Theme.DANGER : Theme.CHALK);
                        g.setStroke(new BasicStroke(2f));
                        g.draw(new Ellipse2D.Double(x - 3, 0, 22, 22));
                    }
                }
                g.setFont(Theme.ui(600, 12f));
                g.setColor(Theme.CHALK_FAINT);
                g.drawString(color.displayName(), 130, 16);
                g.dispose();
            }
        }
    }
}
