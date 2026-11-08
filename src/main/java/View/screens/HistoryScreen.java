package View.screens;

import Model.Difficulty;
import Model.GameRecord;
import View.MainFrame;
import View.Screen;
import View.ui.A11yComponent;
import View.ui.GameButton;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Segmented;
import View.ui.Text;
import View.ui.Theme;

import javax.accessibility.AccessibleRole;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * All saved games, newest first, with the date, difficulty, finishing order
 * and duration of each one (P-HIS-1). Rows that couldn't be read were already
 * skipped by the repository, so the rest is still shown (REL-2).
 */
public final class HistoryScreen extends Screen {
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm");
    // the history file has no player colors (they come with DB-2 in iteration 3), so places use medal colors
    private static final Color[] PLACE_COLORS = {
        new Color(0xFFC94A), new Color(0xC9D3D6), new Color(0xD08A4E), new Color(0x7E9A98)
    };

    private final List<GameRecord> all;
    private final JPanel list = new JPanel();
    private final JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));

    /**
     * @param records saved games, newest first
     * @param onBack  goes back to Home
     */
    public HistoryScreen(List<GameRecord> records, Runnable onBack) {
        this.all = records;
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(MainFrame.titleBarInset() + 22, 48, 28, 48));
        root.add(header(onBack), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(10, 96));
        center.add(stats, BorderLayout.NORTH);
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        JPanel listWrap = new JPanel(new BorderLayout());
        listWrap.setOpaque(false);
        listWrap.add(list, BorderLayout.NORTH);
        center.add(Text.scroll(listWrap), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
        show(Optional.empty());
    }

    private JPanel header(Runnable onBack) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        backWrap.setOpaque(false);
        backWrap.add(new GameButton("Back", Icon.BACK, GameButton.Variant.GHOST).size(40, 14f).onClick(onBack));
        header.add(backWrap, BorderLayout.WEST);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Text.label("Game history", Theme.display(900, 38f), Theme.CHALK));
        titles.add(Text.label("Every finished game, newest first.", Theme.ui(500, 16f), Theme.CHALK_DIM));
        header.add(titles, BorderLayout.CENTER);

        // filter: All, Easy, Medium, Hard
        List<Optional<Difficulty>> options = new ArrayList<>();
        options.add(Optional.empty());
        for (Difficulty d : Difficulty.values()) {
            options.add(Optional.of(d));
        }
        Segmented<Optional<Difficulty>> filter = new Segmented<>(options,
                o -> o.map(Difficulty::displayName).orElse("All"), Optional.empty());
        filter.setPreferredSize(new Dimension(380, 42));
        filter.getAccessibleContext().setAccessibleName("Filter by difficulty");
        filter.onChange(this::show);
        JPanel filterWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        filterWrap.setOpaque(false);
        filterWrap.add(filter);
        header.add(filterWrap, BorderLayout.EAST);
        return header;
    }

    /** Fills the totals and the list for the chosen difficulty (empty = all). */
    private void show(Optional<Difficulty> level) {
        List<GameRecord> shown = all.stream()
                .filter(r -> level.isEmpty() || r.difficulty() == level.get())
                .toList();

        stats.removeAll();
        stats.add(new Stat("Games played", String.valueOf(shown.size())));
        long average = (long) shown.stream().mapToLong(GameRecord::durationSeconds).average().orElse(0);
        stats.add(new Stat("Average length", shown.isEmpty() ? "-" : Theme.formatDuration(average)));
        Map<String, Long> wins = shown.stream()
                .collect(Collectors.groupingBy(r -> r.places().get(0), Collectors.counting()));
        String most = wins.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + ")").orElse("-");
        stats.add(new Stat("Most wins", most));

        list.removeAll();
        if (shown.isEmpty()) {
            String text = all.isEmpty() ? "No finished games yet. Finish a game and it shows up here."
                    : "No " + level.map(d -> d.displayName().toLowerCase()).orElse("") + " games yet.";
            JLabel empty = Text.label(text, Theme.ui(500, 16f), Theme.CHALK_DIM);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 8, 0, 0));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
        }
        for (GameRecord r : shown) {
            Row row = new Row(r);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(row);
            list.add(Box.createVerticalStrut(10));
        }
        revalidate();
        repaint();
    }

    /** A small card with one number (games played, average length, most wins). */
    private static final class Stat extends JComponent {
        private final String label;
        private final String value;

        Stat(String label, String value) {
            this.label = label;
            this.value = value;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 22, 22);
            g.setColor(new Color(6, 23, 27, 150));
            g.fill(shape);
            g.setColor(Theme.LINE);
            g.draw(shape);
            g.setFont(Theme.ui(600, 13f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString(label, 22, 32);
            g.setFont(Theme.display(900, 26f));
            g.setColor(Theme.CHALK);
            FontMetrics fm = g.getFontMetrics();
            String text = value;
            while (fm.stringWidth(text) > getWidth() - 40 && text.length() > 3) {
                text = text.substring(0, text.length() - 1);
            }
            g.drawString(text, 22, 70);
            g.dispose();
        }
    }

    /** One saved game: date and time, difficulty and duration, then the places in order. */
    private static final class Row extends A11yComponent {
        private final GameRecord r;

        Row(GameRecord r) {
            super(AccessibleRole.LABEL);
            this.r = r;
            setPreferredSize(new Dimension(800, 76));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
            getAccessibleContext().setAccessibleName("Game " + r.id() + ", won by " + r.places().get(0));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 20, 20);
            g.setColor(new Color(6, 23, 27, 140));
            g.fill(shape);
            g.setColor(Theme.LINE);
            g.draw(shape);
            g.setColor(levelColor(r.difficulty()));
            g.fill(new RoundRectangle2D.Double(0, 14, 5, h - 28, 4, 4));

            g.setFont(Theme.ui(700, 16f));
            g.setColor(Theme.CHALK);
            g.drawString(WHEN.format(r.endedAt()), 24, 32);
            g.setFont(Theme.ui(500, 13.5f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString("#" + r.id() + "   " + r.difficulty().displayName() + "   "
                    + Theme.formatDuration(r.durationSeconds()), 24, 54);

            double x = 300;
            g.setFont(Theme.ui(600, 14f));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < r.places().size(); i++) {
                String label = (i + 1) + "  " + r.places().get(i);
                double chip = fm.stringWidth(label) + 46 + (i == 0 ? 18 : 0);
                if (x + chip > w - 16) {
                    break;
                }
                Color c = PLACE_COLORS[i];
                g.setColor(Theme.alpha(c, i == 0 ? 0.28f : 0.14f));
                g.fill(new RoundRectangle2D.Double(x, (h - 34) / 2.0, chip, 34, 34, 34));
                g.setColor(c);
                g.fill(new Ellipse2D.Double(x + 12, h / 2.0 - 6, 12, 12));
                g.setColor(Theme.CHALK);
                g.drawString(label, (float) (x + 32), (float) (h / 2.0 + fm.getAscent() / 2.0 - 2));
                if (i == 0) {
                    Icons.draw(g, Icon.CROWN, x + chip - 24, h / 2.0 - 8, 16, Theme.GOLD);
                }
                x += chip + 8;
            }
            g.dispose();
        }

        private static Color levelColor(Difficulty d) {
            return switch (d) {
                case EASY -> Theme.LEVEL_EASY;
                case MEDIUM -> Theme.LEVEL_MEDIUM;
                case HARD -> Theme.LEVEL_HARD;
            };
        }
    }
}
