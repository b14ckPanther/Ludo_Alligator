package View.game;

import Model.DiceRoll;
import Model.Game;
import Model.GameListener;
import Model.Piece;
import Model.Player;
import Model.TurnPhase;
import View.MainFrame;
import View.Screen;
import View.ui.Anim;
import View.ui.GameButton;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Surface;
import View.ui.Text;
import View.ui.Theme;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * The screen of a running game: the board on the left and a side panel on
 * the right. It listens to the game (Observer) and redraws when something
 * changes. It doesn't decide anything about the rules.
 */
public final class GameScreen extends Screen implements GameListener {

    /** What the screen asks the controller to do. */
    public interface Actions {
        void roll();

        void pick(Piece piece);
    }

    private final Game game;
    private final MainFrame frame;
    private final Actions actions;
    private final Sequencer sequencer = new Sequencer();
    private final BoardView boardView;
    private final DiceView dice = new DiceView();
    private final GameButton rollButton = new GameButton("Roll the dice", Icon.DICE, GameButton.Variant.ACCENT);
    private final JLabel rollResult = Text.label(" ", Theme.ui(600, 14f), Theme.CHALK);
    private final JLabel clockLabel = Text.label("00:00", Theme.mono(600, 20f), Theme.CHALK);
    private final Timer clockTimer;
    private final TurnCard turnCard = new TurnCard();
    private final Players players;
    private final Feed feed = new Feed();
    private final JPanel side = new JPanel();
    private final CardLayout infoCards = new CardLayout();
    private final JPanel info = new JPanel(infoCards);
    private boolean showingKey;
    /** The player shown on the turn card. It changes only after the animations before it are done. */
    private Player shown;

    public GameScreen(Game game, MainFrame frame, Actions actions) {
        this.game = game;
        this.frame = frame;
        this.actions = actions;
        this.boardView = new BoardView(game);
        this.players = new Players();
        this.shown = game.currentPlayer();
        game.addListener(this);

        JPanel base = new JPanel(new BorderLayout());
        base.setOpaque(false);
        base.setBorder(BorderFactory.createEmptyBorder(MainFrame.titleBarInset() + 10, 18, 18, 18));
        base.add(boardView, BorderLayout.CENTER);
        base.add(buildSide(), BorderLayout.EAST);
        add(base, BorderLayout.CENTER);

        dice.onRoll(this::onRollPressed);
        boardView.onPick(this::onPiecePicked);
        rollButton.onClick(this::onRollPressed);
        clockTimer = new Timer(250, e -> clockLabel.setText(Theme.formatDuration(game.clock().elapsedMillis() / 1000)));
        bindKeys();
    }

    public BoardView boardView() {
        return boardView;
    }

    public Sequencer sequencer() {
        return sequencer;
    }

    private JComponent buildSide() {
        side.setOpaque(false);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        // top row: game time on the left, a button on the right that swaps the log and the board key
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel clockBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        clockBox.setOpaque(false);
        clockBox.add(new IconLabel(Icon.CLOCK));
        clockBox.add(clockLabel);
        top.add(clockBox, BorderLayout.WEST);
        GameButton keyButton = new GameButton("Board key", Icon.QUESTION, GameButton.Variant.SECONDARY).size(34, 13f);
        keyButton.setFocusable(false); // Space must roll the dice, not press this button
        keyButton.onClick(() -> {
            showingKey = !showingKey;
            infoCards.show(info, showingKey ? "key" : "log");
            keyButton.setText(showingKey ? "Game log" : "Board key");
        });
        JPanel keyBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        keyBox.setOpaque(false);
        keyBox.add(keyButton);
        top.add(keyBox, BorderLayout.EAST);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addToSide(top);
        addToSide(turnCard);
        addToSide(diceTray());
        addToSide(players);

        info.setOpaque(false);
        info.add(feed, "log");
        info.add(new BoardKey(), "key");
        info.setPreferredSize(new Dimension(360, 200));
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        addToSide(info);
        side.add(Box.createVerticalGlue());

        // on small windows the side panel can scroll
        JScrollPane scroll = Text.scroll(side);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(398, 10));
        return scroll;
    }

    private JComponent diceTray() {
        Surface tray = new Surface(new BorderLayout(), 24);
        tray.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));
        tray.add(Text.label("Easy: one step die", Theme.ui(600, 12.5f), Theme.CHALK_FAINT), BorderLayout.NORTH);
        dice.setPreferredSize(new Dimension(300, 100));
        tray.add(dice, BorderLayout.CENTER);
        JPanel under = new JPanel();
        under.setOpaque(false);
        under.setLayout(new BoxLayout(under, BoxLayout.Y_AXIS));
        under.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        rollResult.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollResult.setHorizontalAlignment(JLabel.CENTER);
        rollResult.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        under.add(rollResult);
        under.add(Box.createVerticalStrut(10));
        rollButton.size(50, 16f);
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        under.add(rollButton);
        tray.add(under, BorderLayout.SOUTH);
        tray.setMaximumSize(new Dimension(Integer.MAX_VALUE, tray.getPreferredSize().height));
        return tray;
    }

    /** Space or Enter rolls the dice (SRS 3.1.1). */
    private void bindKeys() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "roll");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "roll");
        getActionMap().put("roll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRollPressed();
            }
        });
        // keys 1-4 pick the current player's piece with that number (SRS 3.1.1)
        for (int i = 1; i <= 4; i++) {
            int number = i - 1;
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(String.valueOf(i)), "piece" + i);
            getActionMap().put("piece" + i, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onPiecePicked(game.currentPlayer().pieces().get(number));
                }
            });
        }
    }

    private void onPiecePicked(Piece piece) {
        if (game.phase() == TurnPhase.CHOOSE_MOVE && sequencer.isIdle() && piece.color() == game.currentPlayer().color()) {
            actions.pick(piece);
        }
    }

    private void onRollPressed() {
        if (game.phase() == TurnPhase.ROLL && sequencer.isIdle()) {
            setRollEnabled(false);
            actions.roll();
        }
    }

    /**
     * Called by the controller when the animations are done and the player
     * can act. Turns on the controls that fit the current phase.
     */
    public void showPhase() {
        boolean canRoll = game.phase() == TurnPhase.ROLL;
        setRollEnabled(canRoll);
        setHint(switch (game.phase()) {
            case ROLL -> "Roll the dice (Space)";
            case CHOOSE_MOVE -> "Pick a piece to move";
            case GAME_OVER -> "Game over";
        });
        turnCard.repaint();
        players.repaint();
    }

    private void setRollEnabled(boolean enabled) {
        rollButton.setEnabled(enabled);
        rollButton.accent(Theme.player(shown.color()));
        dice.arm(enabled, Theme.player(shown.color()));
    }

    /** Adds a part to the side panel, left aligned, with a gap after it. */
    public void addToSide(JComponent part) {
        part.setAlignmentX(Component.LEFT_ALIGNMENT);
        int at = side.getComponentCount();
        // keep the glue at the bottom if it is already there
        if (at > 0 && side.getComponent(at - 1) instanceof Box.Filler) {
            at--;
        }
        side.add(part, at);
        side.add(Box.createVerticalStrut(12), at + 1);
        side.revalidate();
    }

    @Override
    public void onShow() {
        boardView.playIntro();
        clockTimer.start();
        frame.focusBackdrop(game.currentPlayer().color());
    }

    @Override
    public void onHide() {
        clockTimer.stop();
        dice.arm(false, Theme.CHALK);
        game.removeListener(this);
        sequencer.clear();
        frame.focusBackdrop(null);
    }

    // ------------------------------------------------------------------ game events (Observer)
    // Each event is queued, so the player sees them in the order they happened.

    @Override
    public void turnStarted(Player player) {
        sequencer.add(done -> {
            shown = player;
            rollResult.setText(" ");
            frame.focusBackdrop(player.color());
            turnCard.flash();
            setRollEnabled(false);
            players.repaint();
            boardView.repaint();
            Anim.delay(260, done);
        });
    }

    @Override
    public void rolled(Player player, DiceRoll roll) {
        sequencer.add(done -> {
            setRollEnabled(false);
            rollResult.setText(" ");
            dice.play(roll, () -> {
                rollResult.setText(roll.isSix() ? "Six! Bring a piece out or move, then roll again"
                        : "Move " + roll.step() + (roll.step() == 1 ? " square" : " squares"));
                rollResult.setForeground(roll.isSix() ? Theme.GOLD : Theme.CHALK);
                Anim.delay(roll.isSix() ? 380 : 220, done);
            });
        });
    }

    @Override
    public void pieceMoved(Piece piece, int from, int to) {
        sequencer.add(done -> boardView.animateMove(piece, from, to, () -> {
            players.repaint();
            done.run();
        }));
    }

    @Override
    public void log(String message) {
        sequencer.add(done -> {
            feed.add(message);
            Anim.delay(350, done);
        });
    }

    // ------------------------------------------------------------------ side panel parts

    /** Shows whose turn it is, with the player's name and color (P-DIC-1). */
    private final class TurnCard extends JComponent {
        private double flash;
        private String hint = "Roll the dice";

        TurnCard() {
            setPreferredSize(new Dimension(360, 96));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        }

        void flash() {
            Anim.run(700, Anim.Ease.OUT_CUBIC, t -> {
                flash = 1 - t;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            Player p = shown;
            Color c = Theme.player(p.color());
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 24, 24);
            g.setColor(new Color(6, 23, 27, 160));
            g.fill(shape);
            g.setPaint(new GradientPaint(0, 0, Theme.alpha(c, 0.42f + (float) flash * 0.3f), w, 0, Theme.alpha(c, 0.04f)));
            g.fill(shape);
            g.setColor(Theme.alpha(c, 0.7f));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(shape);

            double d = 56;
            double x = 20;
            double y = (h - d) / 2;
            g.setColor(Theme.playerDeep(p.color()));
            g.fill(new Ellipse2D.Double(x, y + 4, d, d));
            g.setPaint(new RadialGradientPaint(new Point2D.Double(x + d * 0.35, y + d * 0.3), (float) d,
                    new float[]{0, 1}, new Color[]{Theme.mix(c, Color.WHITE, 0.45f), c}));
            g.fill(new Ellipse2D.Double(x, y, d, d));
            Icons.draw(g, Icon.USER, x + d * 0.25, y + d * 0.23, d * 0.5, Color.WHITE);

            float tx = (float) (x + d + 16);
            g.setFont(Theme.ui(600, 12.5f));
            g.setColor(Theme.alpha(Theme.CHALK, 0.75f));
            g.drawString(p.color().displayName() + "'s turn", tx, (float) (y + 16));
            g.setFont(Theme.display(900, 21f));
            g.setColor(Theme.CHALK);
            FontMetrics fm = g.getFontMetrics();
            String name = p.name();
            while (fm.stringWidth(name) > w - tx - 20 && name.length() > 3) {
                name = name.substring(0, name.length() - 1);
            }
            g.drawString(name, tx, (float) (y + 40));
            g.setFont(Theme.ui(500, 13.5f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString(hint, tx, (float) (y + 60));
            g.dispose();
        }
    }

    /** Short help text under the current player's name. */
    public void setHint(String text) {
        turnCard.hint = text;
        turnCard.repaint();
    }

    /** List of the players in turn order. The current one is highlighted. */
    private final class Players extends JComponent {
        Players() {
            int rows = game.players().size();
            Dimension d = new Dimension(360, 40 + rows * 40);
            setPreferredSize(d);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            int w = getWidth();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, getHeight() - 1, 22, 22);
            g.setColor(new Color(6, 23, 27, 150));
            g.fill(shape);
            g.setColor(Theme.LINE);
            g.draw(shape);
            g.setFont(Theme.ui(700, 13f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString("Players", 18, 26);
            g.setFont(Theme.ui(500, 12f));
            String right = "Pieces home";
            g.drawString(right, w - 18 - g.getFontMetrics().stringWidth(right), 26);

            int y = 40;
            for (Player p : game.players()) {
                boolean current = p == shown;
                if (current) {
                    g.setColor(Theme.alpha(Theme.player(p.color()), 0.14f));
                    g.fill(new RoundRectangle2D.Double(8, y + 2, w - 16, 34, 14, 14));
                }
                g.setColor(Theme.player(p.color()));
                g.fill(new Ellipse2D.Double(18, y + 12, 12, 12));
                g.setFont(Theme.ui(current ? 700 : 600, 15f));
                g.setColor(p.hasFinished() ? Theme.CHALK_DIM : Theme.CHALK);
                g.drawString(p.name(), 40, y + 24);
                if (p.hasFinished()) {
                    g.setFont(Theme.display(900, 13f));
                    g.setColor(Theme.GOLD);
                    g.drawString("#" + p.rank(), w - 120, y + 24);
                }
                long home = game.finishedCount(p);
                for (int i = 0; i < 4; i++) {
                    double dotX = w - 26 - (3 - i) * 17;
                    if (i < home) {
                        g.setColor(Theme.player(p.color()));
                        g.fill(new Ellipse2D.Double(dotX - 6, y + 12, 12, 12));
                    } else {
                        g.setColor(Theme.LINE_STRONG);
                        g.setStroke(new BasicStroke(1.4f));
                        g.draw(new Ellipse2D.Double(dotX - 5.5, y + 12.5, 11, 11));
                    }
                }
                y += 40;
            }
            g.dispose();
        }
    }

    /** The last game events, newest on top (SRS 3.1.1: log of recent events). */
    private static final class Feed extends JComponent {
        private final Deque<String> lines = new ArrayDeque<>();

        Feed() {
            setPreferredSize(new Dimension(360, 200));
        }

        void add(String text) {
            lines.addFirst(text);
            while (lines.size() > 8) {
                lines.removeLast();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 22, 22);
            g.setColor(new Color(6, 23, 27, 120));
            g.fill(shape);
            g.setColor(Theme.LINE);
            g.draw(shape);
            g.setFont(Theme.ui(700, 13f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString("Game log", 18, 26);
            g.setFont(Theme.ui(500, 13.5f));
            FontMetrics fm = g.getFontMetrics();
            int y = 50;
            int i = 0;
            for (String line : new ArrayList<>(lines)) {
                if (y > h - 8) {
                    break;
                }
                // older lines fade out
                float fade = Math.max(0.25f, 1f - i * 0.16f);
                g.setColor(Theme.alpha(Theme.CHALK_DIM, fade));
                g.fill(new Ellipse2D.Double(18, y - 8, 6, 6));
                String text = line;
                while (fm.stringWidth(text) > w - 52 && text.length() > 4) {
                    text = text.substring(0, text.length() - 2);
                }
                if (!text.equals(line)) {
                    text = text + "...";
                }
                g.setColor(Theme.alpha(Theme.CHALK, fade));
                g.drawString(text, 32, y);
                y += fm.getHeight() + 4;
                i++;
            }
            g.dispose();
        }
    }

    /** A small icon next to a label (the clock). */
    private static final class IconLabel extends JComponent {
        private final Icon icon;

        IconLabel(Icon icon) {
            this.icon = icon;
            setPreferredSize(new Dimension(20, 26));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Theme.quality(g0);
            Icons.draw(g, icon, 0, 3, 20, Theme.CHALK_DIM);
            g.dispose();
        }
    }
}
