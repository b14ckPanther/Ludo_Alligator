package View.ui;

import Model.PlayerColor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Colors, fonts and a few drawing helpers used by all the screens.
 * Keeping them here means every screen looks the same, and a color is
 * changed in one place only.
 *
 * <p>Look: dark teal background, light "chalk" text, a cream board, and the
 * four player colors as the only strong colors on screen.
 */
public final class Theme {
    private Theme() { }

    // background and text
    public static final Color INK = new Color(0x06171B);
    public static final Color INK_2 = new Color(0x0B2228);
    public static final Color INK_3 = new Color(0x123139);
    public static final Color LINE = new Color(255, 255, 255, 26);
    public static final Color LINE_STRONG = new Color(255, 255, 255, 52);
    public static final Color CHALK = new Color(0xF4EFE6);
    public static final Color CHALK_DIM = new Color(0xA8BDBA);
    public static final Color CHALK_FAINT = new Color(0x6E8A88);
    public static final Color DANGER = new Color(0xFF6B6B);
    public static final Color SUCCESS = new Color(0x4ADE80);
    public static final Color GOLD = new Color(0xFFC94A);

    // board
    public static final Color SLAB = new Color(0xEDE6D8);
    public static final Color SLAB_EDGE = new Color(0xC9BFAA);
    public static final Color TILE = new Color(0xFFFCF6);
    public static final Color TILE_LINE = new Color(0xDCD3C2);
    public static final Color TILE_INK = new Color(0x2B3A3D);

    // difficulty colors (green / yellow / red, like the question levels in the brief)
    public static final Color LEVEL_EASY = new Color(0x22B573);
    public static final Color LEVEL_MEDIUM = new Color(0xF2B21B);
    public static final Color LEVEL_HARD = new Color(0xE5484D);

    public static Color player(PlayerColor color) {
        return switch (color) {
            case RED -> new Color(0xEF4B4F);
            case GREEN -> new Color(0x1DB36B);
            case YELLOW -> new Color(0xFFBF1F);
            case BLUE -> new Color(0x3A78F2);
        };
    }

    /** Darker version of the player color, for shadows and edges. */
    public static Color playerDeep(PlayerColor color) {
        return switch (color) {
            case RED -> new Color(0xA81D2A);
            case GREEN -> new Color(0x0B6E40);
            case YELLOW -> new Color(0xB57C00);
            case BLUE -> new Color(0x1C44A6);
        };
    }

    public static Color playerLight(PlayerColor color) {
        return mix(player(color), Color.WHITE, 0.72f);
    }

    /** Mixes two colors. t = 0 gives a, t = 1 gives b. */
    public static Color mix(Color a, Color b, float t) {
        float u = 1 - t;
        return new Color(
                Math.round(a.getRed() * u + b.getRed() * t),
                Math.round(a.getGreen() * u + b.getGreen() * t),
                Math.round(a.getBlue() * u + b.getBlue() * t),
                Math.round(a.getAlpha() * u + b.getAlpha() * t));
    }

    public static Color alpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, Math.round(alpha * 255))));
    }

    // ---------------------------------------------------------------- fonts
    // The .ttf files are inside the jar (resources/fonts, OFL license), so the
    // game looks the same on Windows and macOS. If a file is missing we fall
    // back to the system font.

    private static final Map<String, Font> BASE = new HashMap<>();
    private static final Map<String, Font> CACHE = new HashMap<>();

    /** Unbounded, used for big titles. Weights 500, 700, 900. */
    public static Font display(int weight, float size) {
        return font("Unbounded-" + weight, size, Font.SANS_SERIF, Font.BOLD);
    }

    /** Bricolage Grotesque, used for normal text and buttons. Weights 400 to 800. */
    public static Font ui(int weight, float size) {
        return font("Bricolage-" + weight, size, Font.SANS_SERIF, weight >= 600 ? Font.BOLD : Font.PLAIN);
    }

    /** JetBrains Mono, used for numbers that should line up (times, dates). */
    public static Font mono(int weight, float size) {
        return font("JetBrainsMono-" + weight, size, Font.MONOSPACED, Font.PLAIN);
    }

    private static synchronized Font font(String file, float size, String fallback, int style) {
        String key = file + "@" + size;
        Font cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Font base = BASE.computeIfAbsent(file, Theme::load);
        Font result = base != null ? base.deriveFont(size) : new Font(fallback, style, Math.round(size));
        CACHE.put(key, result);
        return result;
    }

    private static Font load(String file) {
        try (InputStream in = Theme.class.getResourceAsStream("/fonts/" + file + ".ttf")) {
            if (in == null) {
                return null;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (IOException | FontFormatException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------- helpers

    /** Copy of the graphics with antialiasing turned on. Remember to dispose() it. */
    public static Graphics2D quality(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        return g2;
    }

    /** Elapsed time as MM:SS, or H:MM:SS from one hour (SRS 3.1.1). */
    public static String formatDuration(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }
}
