package View.game;

import Model.Board;
import Model.Game;
import Model.Piece;
import Model.Player;
import Model.PlayerColor;
import Model.Square;
import View.ui.Anim;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Theme;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Draws the board and the pieces.
 *
 * <p>Positions are worked out in "grid units" (x = column, y = row, one
 * square = 1) and turned into pixels when painting, so the board grows and
 * shrinks with the window. The board itself doesn't change during a game, so
 * it is drawn once into an image and only the pieces are drawn every time.
 */
public final class BoardView extends JComponent {
    private static final double PIECE_SIZE = 0.74;

    private final Game game;
    private final Board board;
    private final int n;

    private BufferedImage cache;
    private double cacheScale;
    private int cacheW;
    private int cacheH;

    private double cell;
    private double ox;
    private double oy;
    private double intro = 1;

    /**
     * Where each piece is drawn right now. During an animation this is
     * behind the model: the model already has the new position, the screen
     * still walks the piece there square by square.
     */
    private final Map<Piece, Integer> shown = new IdentityHashMap<>();
    /** Piece that is moving right now, with its position between two squares. */
    private Piece walking;
    private Point2D.Double walkAt;
    private double hop;
    private Consumer<Piece> onPick = p -> { };

    public BoardView(Game game) {
        this.game = game;
        this.board = game.board();
        this.n = board.size();
        setOpaque(false);
        for (Player p : game.players()) {
            for (Piece piece : p.pieces()) {
                shown.put(piece, piece.position());
            }
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Piece piece = pieceAt(e.getX(), e.getY());
                if (piece != null) {
                    onPick.accept(piece);
                }
            }
        });
    }

    /** Called with the piece the player clicked on. */
    public void onPick(Consumer<Piece> listener) {
        this.onPick = listener;
    }

    /** The piece drawn at this pixel, or null. */
    private Piece pieceAt(int x, int y) {
        layoutBoard();
        Piece best = null;
        double bestDistance = cell * 0.5;
        for (Piece piece : shown.keySet()) {
            Point2D.Double p = pointOf(piece);
            double d = Math.hypot(px(p.x) - x, py(p.y) - y);
            if (d < bestDistance) {
                bestDistance = d;
                best = piece;
            }
        }
        return best;
    }

    /**
     * Walks a piece from one route index to another, one square at a time,
     * and calls done when it has arrived.
     */
    public void animateMove(Piece piece, int from, int to, Runnable done) {
        if (from == Piece.IN_CASTLE) {
            // out of the castle: one jump onto the start square
            Point2D.Double a = castleSlot(piece.color(), piece.number());
            Point2D.Double b = routePoint(piece, 0);
            jump(piece, a, b, 1.2, 420, () -> land(piece, to, done));
            return;
        }
        int steps = Math.max(1, to - from);
        int perSquare = (int) Math.max(70, Math.min(160, 1800.0 / steps));
        step(piece, from, to, perSquare, done);
    }

    private void step(Piece piece, int at, int to, int ms, Runnable done) {
        if (at >= to) {
            land(piece, to, done);
            return;
        }
        Point2D.Double a = routePoint(piece, at);
        Point2D.Double b = routePoint(piece, at + 1);
        jump(piece, a, b, 0.42, ms, () -> {
            shown.put(piece, at + 1);
            step(piece, at + 1, to, ms, done);
        });
    }

    /** Moves the piece in a small arc from a to b. */
    private void jump(Piece piece, Point2D.Double a, Point2D.Double b, double height, int ms, Runnable done) {
        walking = piece;
        Anim.run(ms, Anim.Ease.LINEAR, t -> {
            double e = Anim.Ease.IN_OUT_CUBIC.apply(t);
            walkAt = new Point2D.Double(Anim.lerp(a.x, b.x, e), Anim.lerp(a.y, b.y, e));
            hop = Math.sin(Math.PI * t) * height;
            repaint();
        }, done);
    }

    private void land(Piece piece, int to, Runnable done) {
        shown.put(piece, to);
        walking = null;
        walkAt = null;
        hop = 0;
        repaint();
        done.run();
    }

    /** Center of a route square (the finish uses the middle of the color's triangle). */
    private Point2D.Double routePoint(Piece piece, int index) {
        if (index == board.finishIndex()) {
            return finishSpot(piece.color(), 2);
        }
        Square sq = board.route(piece.color()).get(index);
        return new Point2D.Double(sq.col() + 0.5, sq.row() + 0.5);
    }

    /** Short opening animation: the board grows in from the middle. */
    public void playIntro() {
        Anim.run(900, Anim.Ease.OUT_CUBIC, t -> {
            intro = t;
            repaint();
        });
    }

    // ------------------------------------------------------------------ positions

    private void layoutBoard() {
        double size = Math.min(getWidth(), getHeight()) * 0.96;
        cell = size / (n + 0.7);
        ox = (getWidth() - cell * n) / 2.0;
        oy = (getHeight() - cell * n) / 2.0;
    }

    private double px(double gx) {
        return ox + gx * cell;
    }

    private double py(double gy) {
        return oy + gy * cell;
    }

    /** Turns a point 90 degrees clockwise around the board center, once per turn. */
    private Point2D.Double rot(double x, double y, int turns) {
        for (int i = 0; i < turns; i++) {
            double nx = n - y;
            double ny = x;
            x = nx;
            y = ny;
        }
        return new Point2D.Double(x, y);
    }

    /** The 4 places for pieces inside a castle. Written for Red and turned for the others. */
    Point2D.Double castleSlot(PlayerColor color, int slot) {
        double[][] slots = {{1.9, 1.9}, {4.1, 1.9}, {1.9, 4.1}, {4.1, 4.1}};
        return rot(slots[slot][0], slots[slot][1], color.quadrant());
    }

    /** Where finished pieces are drawn, inside the color's triangle in the middle. */
    Point2D.Double finishSpot(PlayerColor color, int slot) {
        double[][] spots = {{6.55, 7.05}, {6.55, 7.95}, {7.05, 7.5}, {6.95, 7.5}};
        return rot(spots[slot][0], spots[slot][1], color.quadrant());
    }

    /**
     * Where a piece is drawn. If more than one piece is on the same square
     * they are moved a little apart in a circle so all of them can be seen.
     */
    Point2D.Double pointOf(Piece piece) {
        if (piece == walking && walkAt != null) {
            return walkAt;
        }
        int at = shown.get(piece);
        if (at == Piece.IN_CASTLE) {
            return castleSlot(piece.color(), piece.number());
        }
        if (at == board.finishIndex()) {
            return finishSpot(piece.color(), piece.number());
        }
        Square square = board.route(piece.color()).get(at);
        List<Piece> here = piecesOn(square);
        double cx = square.col() + 0.5;
        double cy = square.row() + 0.5;
        if (here.size() <= 1) {
            return new Point2D.Double(cx, cy);
        }
        int i = here.indexOf(piece);
        double a = -Math.PI / 4 + i * 2 * Math.PI / here.size();
        return new Point2D.Double(cx + Math.cos(a) * 0.2, cy + Math.sin(a) * 0.2);
    }

    /** Pieces on a shared square are drawn smaller, finished ones even smaller. */
    double scaleOf(Piece piece) {
        if (piece == walking) {
            return 1;
        }
        int at = shown.get(piece);
        if (at == Piece.IN_CASTLE) {
            return 1;
        }
        if (at == board.finishIndex()) {
            return 0.55;
        }
        return piecesOn(board.route(piece.color()).get(at)).size() > 1 ? 0.72 : 1;
    }

    /** Pieces drawn on a square right now (not the one that is moving). */
    private List<Piece> piecesOn(Square square) {
        List<Piece> result = new ArrayList<>();
        for (Player p : game.players()) {
            for (Piece piece : p.pieces()) {
                int at = shown.get(piece);
                if (piece != walking && at >= 0 && at < board.finishIndex()
                        && board.route(piece.color()).get(at) == square) {
                    result.add(piece);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = Theme.quality(g0);
        layoutBoard();
        double scale = g.getTransform().getScaleX();
        if (cache == null || cacheW != getWidth() || cacheH != getHeight() || cacheScale != scale) {
            cache = renderBoard(scale);
            cacheW = getWidth();
            cacheH = getHeight();
            cacheScale = scale;
        }

        Graphics2D gb = (Graphics2D) g.create();
        if (intro < 1) {
            // grow from 94% and open a circle from the middle
            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;
            double s = 0.94 + 0.06 * intro;
            gb.translate(cx, cy);
            gb.scale(s, s);
            gb.translate(-cx, -cy);
            double r = Math.hypot(getWidth(), getHeight()) * 0.6 * intro;
            gb.clip(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            gb.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.min(1, intro * 1.5)));
        }
        gb.drawImage(cache, 0, 0, getWidth(), getHeight(), null);
        gb.dispose();

        paintPieces(g);
        g.dispose();
    }

    /** Draws everything that doesn't move into an image (at screen resolution, so it stays sharp). */
    private BufferedImage renderBoard(double scale) {
        int w = (int) Math.ceil(getWidth() * scale);
        int h = (int) Math.ceil(getHeight() * scale);
        BufferedImage img = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = Theme.quality(img.getGraphics());
        g.scale(scale, scale);
        paintSlab(g);
        for (PlayerColor c : PlayerColor.values()) {
            paintCastle(g, c);
        }
        for (Square sq : board.squares()) {
            paintSquare(g, sq);
        }
        paintCenter(g);
        g.dispose();
        return img;
    }

    /** The cream board under everything, with a soft shadow. */
    private void paintSlab(Graphics2D g) {
        double m = cell * 0.32;
        double x = px(0) - m;
        double y = py(0) - m;
        double s = cell * n + m * 2;
        for (int i = 8; i >= 1; i--) {
            g.setColor(new Color(0, 0, 0, 14));
            double spread = i * cell * 0.09;
            g.fill(new RoundRectangle2D.Double(x - spread * 0.6, y - spread * 0.2 + cell * 0.25, s + spread * 1.2,
                    s + spread * 1.2, cell * 1.2 + spread, cell * 1.2 + spread));
        }
        RoundRectangle2D slab = new RoundRectangle2D.Double(x, y, s, s, cell * 1.1, cell * 1.1);
        g.setPaint(new GradientPaint((float) x, (float) y, Theme.mix(Theme.SLAB, Color.WHITE, 0.35f), (float) x,
                (float) (y + s), Theme.mix(Theme.SLAB, Theme.SLAB_EDGE, 0.5f)));
        g.fill(slab);
        g.setColor(Theme.alpha(Color.WHITE, 0.7f));
        g.setStroke(new BasicStroke((float) (cell * 0.04)));
        g.draw(new RoundRectangle2D.Double(x + cell * 0.04, y + cell * 0.04, s - cell * 0.08, s - cell * 0.08, cell, cell));
        g.setColor(Theme.alpha(Theme.SLAB_EDGE, 0.8f));
        g.setStroke(new BasicStroke(1f));
        g.draw(slab);
    }

    /** A 6x6 castle in the player's corner with 4 round places for the pieces. */
    private void paintCastle(Graphics2D g, PlayerColor color) {
        Point2D.Double a = rot(0, 0, color.quadrant());
        Point2D.Double b = rot(6, 6, color.quadrant());
        double inset = 0.1;
        double x = px(Math.min(a.x, b.x) + inset);
        double y = py(Math.min(a.y, b.y) + inset);
        double s = cell * (6 - inset * 2);
        Color base = Theme.player(color);
        Color deep = Theme.playerDeep(color);
        g.setPaint(new GradientPaint((float) x, (float) y, Theme.mix(base, Color.WHITE, 0.12f), (float) (x + s),
                (float) (y + s), deep));
        g.fill(new RoundRectangle2D.Double(x, y, s, s, cell * 0.9, cell * 0.9));
        g.setColor(Theme.alpha(Color.WHITE, 0.35f));
        g.setStroke(new BasicStroke((float) (cell * 0.03)));
        g.draw(new RoundRectangle2D.Double(x + cell * 0.05, y + cell * 0.05, s - cell * 0.1, s - cell * 0.1,
                cell * 0.8, cell * 0.8));

        double yi = cell * 0.9;
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new RoundRectangle2D.Double(x + yi, y + yi + cell * 0.06, s - yi * 2, s - yi * 2, cell * 0.8, cell * 0.8));
        g.setPaint(new GradientPaint((float) x, (float) (y + yi), Theme.TILE, (float) x, (float) (y + s - yi),
                Theme.mix(Theme.TILE, Theme.SLAB, 0.6f)));
        g.fill(new RoundRectangle2D.Double(x + yi, y + yi, s - yi * 2, s - yi * 2, cell * 0.8, cell * 0.8));

        for (int i = 0; i < 4; i++) {
            Point2D.Double p = castleSlot(color, i);
            double r = cell * 0.5;
            g.setColor(Theme.alpha(deep, 0.16f));
            g.fill(new Ellipse2D.Double(px(p.x) - r, py(p.y) - r + cell * 0.04, r * 2, r * 2));
            g.setColor(Theme.playerLight(color));
            g.fill(new Ellipse2D.Double(px(p.x) - r, py(p.y) - r, r * 2, r * 2));
            g.setColor(Theme.alpha(deep, 0.35f));
            g.setStroke(new BasicStroke((float) (cell * 0.04)));
            g.draw(new Ellipse2D.Double(px(p.x) - r, py(p.y) - r, r * 2, r * 2));
        }
    }

    /**
     * One square. Special squares get a symbol and not only a color, so
     * color-blind players can tell them apart (P-BRD-6): # start, star safe,
     * arrow for the turn into the home column.
     */
    private void paintSquare(Graphics2D g, Square sq) {
        double pad = cell * 0.055;
        double x = px(sq.col()) + pad;
        double y = py(sq.row()) + pad;
        double s = cell - pad * 2;
        double arc = cell * 0.26;
        Color fill = switch (sq.type()) {
            case START -> Theme.player(sq.owner());
            case HOME -> Theme.mix(Theme.player(sq.owner()), Color.WHITE, 0.08f);
            case ARROW -> Theme.playerLight(sq.owner());
            default -> Theme.TILE;
        };
        // darker copy a bit lower, so the square looks pressed into the board
        g.setColor(Theme.mix(fill, Color.BLACK, 0.16f));
        g.fill(new RoundRectangle2D.Double(x, y + cell * 0.035, s, s, arc, arc));
        g.setPaint(new GradientPaint((float) x, (float) y, Theme.mix(fill, Color.WHITE, 0.18f), (float) x,
                (float) (y + s), fill));
        RoundRectangle2D tile = new RoundRectangle2D.Double(x, y, s, s, arc, arc);
        g.fill(tile);
        if (sq.owner() == null) {
            g.setColor(Theme.TILE_LINE);
            g.setStroke(new BasicStroke(1f));
            g.draw(tile);
        }

        double is = s * 0.62;
        double ix = x + (s - is) / 2;
        double iy = y + (s - is) / 2;
        switch (sq.type()) {
            case SAFE -> {
                GeneralPath star = Icons.star(x + s / 2, y + s / 2 + s * 0.02, s * 0.36, s * 0.16);
                g.setColor(new Color(0xF6C54B));
                g.fill(star);
                g.setColor(new Color(0xA77A12));
                g.setStroke(new BasicStroke((float) (cell * 0.035), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(star);
            }
            case START -> Icons.draw(g, Icon.HASH, ix, iy, is, Color.WHITE);
            case ARROW -> {
                // the arrow points into the color's home column
                Graphics2D ga = (Graphics2D) g.create();
                ga.rotate(sq.owner().quadrant() * Math.PI / 2, x + s / 2, y + s / 2);
                Icons.draw(ga, Icon.ARROW, ix, iy, is, Theme.playerDeep(sq.owner()));
                ga.dispose();
            }
            case HOME -> {
                g.setColor(Theme.alpha(Color.WHITE, 0.35f));
                g.fill(new Ellipse2D.Double(x + s * 0.4, y + s * 0.4, s * 0.2, s * 0.2));
            }
            default -> { }
        }
    }

    /** The finish in the middle: four triangles in the player colors and a crown. */
    private void paintCenter(Graphics2D g) {
        double mid = 7.5;
        for (PlayerColor color : PlayerColor.values()) {
            Point2D.Double a = rot(6, 6, color.quadrant());
            Point2D.Double b = rot(6, 9, color.quadrant());
            GeneralPath tri = new GeneralPath();
            tri.moveTo(px(a.x), py(a.y));
            tri.lineTo(px(b.x), py(b.y));
            tri.lineTo(px(mid), py(mid));
            tri.closePath();
            Color base = Theme.player(color);
            g.setPaint(new GradientPaint((float) px((a.x + b.x) / 2), (float) py((a.y + b.y) / 2),
                    Theme.mix(base, Color.WHITE, 0.15f), (float) px(mid), (float) py(mid), Theme.playerDeep(color)));
            g.fill(tri);
            g.setColor(Theme.alpha(Color.WHITE, 0.6f));
            g.setStroke(new BasicStroke((float) (cell * 0.04), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(tri);
        }
        double r = cell * 0.42;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(px(mid) - r * 0.3, py(mid) - r * 0.3), (float) (r * 1.4),
                new float[]{0, 1}, new Color[]{Color.WHITE, Theme.SLAB}));
        g.fill(new Ellipse2D.Double(px(mid) - r, py(mid) - r, r * 2, r * 2));
        Icons.draw(g, Icon.CROWN, px(mid) - r * 0.62, py(mid) - r * 0.66, r * 1.24, new Color(0xC8952A));
    }

    private void paintPieces(Graphics2D g) {
        List<Piece> all = new ArrayList<>();
        for (Player p : game.players()) {
            all.addAll(p.pieces());
        }
        // draw from top to bottom so lower pieces overlap the ones above them
        // the moving piece last, so it is on top
        all.sort(Comparator.comparingDouble(p -> p == walking ? Double.MAX_VALUE : pointOf(p).y));
        for (Piece piece : all) {
            Point2D.Double p = pointOf(piece);
            double lift = piece == walking ? hop * cell : 0;
            paintToken(g, piece.color(), px(p.x), py(p.y) - lift, cell * PIECE_SIZE * scaleOf(piece), (float) intro);
        }
    }

    /** A round piece with a shadow, a light spot and a ring, so it looks like a real game token. */
    static void paintToken(Graphics2D g0, PlayerColor color, double cx, double cy, double d, float alpha) {
        if (alpha <= 0 || d <= 0.5) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, alpha)));
        Color base = Theme.player(color);
        Color deep = Theme.playerDeep(color);

        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(cx - d * 0.5, cy - d * 0.18 + d * 0.28, d, d * 0.42));

        double r = d / 2;
        g.translate(cx, cy - d * 0.08);
        g.setColor(deep);
        g.fill(new Ellipse2D.Double(-r, -r + d * 0.1, d, d));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(-r * 0.35, -r * 0.45), (float) (r * 1.6),
                new float[]{0f, 0.55f, 1f}, new Color[]{Theme.mix(base, Color.WHITE, 0.45f), base, deep}));
        g.fill(new Ellipse2D.Double(-r, -r, d, d));
        double ir = r * 0.58;
        g.setColor(Theme.alpha(Color.WHITE, 0.92f));
        g.setStroke(new BasicStroke((float) (d * 0.07)));
        g.draw(new Ellipse2D.Double(-ir, -ir, ir * 2, ir * 2));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(-ir * 0.3, -ir * 0.3), (float) (ir * 1.3),
                new float[]{0f, 1f}, new Color[]{Theme.mix(base, Color.WHITE, 0.3f), Theme.mix(base, deep, 0.3f)}));
        g.fill(new Ellipse2D.Double(-ir * 0.8, -ir * 0.8, ir * 1.6, ir * 1.6));
        g.setColor(Theme.alpha(Color.WHITE, 0.55f));
        g.fill(new Ellipse2D.Double(-r * 0.62, -r * 0.78, r * 0.62, r * 0.36));
        g.dispose();
    }
}
