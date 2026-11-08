package View;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/** Base class for a full-window screen (Home, Setup, Board, Results, History). */
public abstract class Screen extends JPanel {

    protected Screen() {
        super(new BorderLayout());
        setOpaque(false);
    }

    /** Called right after the screen is put in the window. */
    public void onShow() { }

    /** Called right before the screen is replaced by another one. */
    public void onHide() { }
}
