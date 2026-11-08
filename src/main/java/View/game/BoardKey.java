package View.game;

import Model.PlayerColor;
import View.ui.Icons;
import View.ui.Icons.Icon;
import View.ui.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

/** Small key in the side panel that explains the special squares of the Easy board. */
final class BoardKey extends JComponent {
    private record Row(Color fill, Icon icon, Color iconColor, boolean star, String title, String text) { }

    private final Row[] rows = {
        new Row(Theme.player(PlayerColor.RED), Icon.HASH, Color.WHITE, false, "Start",
                "Roll a six to bring a piece out here"),
        new Row(Theme.TILE, null, null, true, "Safe", "Pieces on a star can't be captured"),
        new Row(Theme.playerLight(PlayerColor.RED), Icon.ARROW, Theme.playerDeep(PlayerColor.RED), false, "Arrow",
                "Your arrow turns you into your home column"),
        new Row(Theme.player(PlayerColor.RED), null, null, false, "Home column",
                "Safe. You need the exact roll to finish"),
    };

    BoardKey() {
        setPreferredSize(new Dimension(360, 40 + rows.length * 40));
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
        g.drawString("Board key", 18, 26);

        int y = 40;
        for (Row r : rows) {
            double s = 30;
            double x = 18;
            g.setColor(Theme.mix(r.fill(), Color.BLACK, 0.15f));
            g.fill(new RoundRectangle2D.Double(x, y + 2, s, s, 9, 9));
            g.setColor(r.fill());
            g.fill(new RoundRectangle2D.Double(x, y, s, s, 9, 9));
            if (r.star()) {
                GeneralPath star = Icons.star(x + s / 2, y + s / 2 + 1, 10, 4.4);
                g.setColor(new Color(0xF6C54B));
                g.fill(star);
                g.setColor(new Color(0xA77A12));
                g.setStroke(new BasicStroke(1.2f));
                g.draw(star);
            } else if (r.icon() != null) {
                Icons.draw(g, r.icon(), x + 6, y + 6, s - 12, r.iconColor());
            }
            g.setFont(Theme.ui(700, 13.5f));
            g.setColor(Theme.CHALK);
            g.drawString(r.title(), 62, y + 13);
            g.setFont(Theme.ui(500, 12.5f));
            g.setColor(Theme.CHALK_DIM);
            g.drawString(r.text(), 62, y + 29);
            y += 40;
        }
        g.dispose();
    }
}
