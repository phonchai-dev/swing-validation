package io.github.phonchai.validation.display;

import javax.swing.JComponent;

/**
 * Strategy interface for displaying and hiding validation error feedback
 * on Swing components.
 *
 * <p>
 * Implementations determine <em>how</em> errors are visually presented —
 * for example, as a floating balloon tooltip, an inline label below the field,
 * or a simple outline color change.
 * </p>
 *
 * <p>
 * The library ships with three built-in implementations:
 * </p>
 * <ul>
 * <li>{@link BalloonTooltipDisplay} — web-style balloon popup with arrow</li>
 * <li>{@link InlineLabelDisplay} — red text label below the component</li>
 * <li>{@link OutlineErrorDisplay} — FlatLaf outline color only</li>
 * </ul>
 *
 * <p>
 * Custom implementations can be created for project-specific error display
 * requirements.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public interface ErrorDisplay {

    /**
     * Shows an error message associated with the given component.
     *
     * <p>
     * If an error is already shown for this component, the implementation
     * should update the message rather than creating a duplicate display.
     * </p>
     *
     * @param component the component that failed validation
     * @param message   the error message to display
     */
    void showError(JComponent component, String message);

    /**
     * Hides any error display previously shown for the given component.
     *
     * <p>
     * If no error is currently shown, this method should be a no-op.
     * </p>
     *
     * @param component the component whose error display should be hidden
     */
    void hideError(JComponent component);

    /**
     * Releases all resources held by this display (e.g., overlay components,
     * listeners, timers). Called when the {@code FormValidator} is disposed.
     *
     * <p>
     * The default implementation is a no-op.
     * </p>
     */
    default void dispose() {
        // No-op by default
    }
}
