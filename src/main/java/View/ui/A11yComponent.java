package View.ui;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;

/**
 * Base class for our own painted controls. It gives them an accessible role
 * (button, radio button...) so screen readers know what they are.
 */
public abstract class A11yComponent extends JComponent {
    private final AccessibleRole role;

    protected A11yComponent(AccessibleRole role) {
        this.role = role;
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override public AccessibleRole getAccessibleRole() { return role; }
            };
        }
        return accessibleContext;
    }
}
