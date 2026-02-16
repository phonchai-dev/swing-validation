package io.github.phonchai.validation.display;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;

/**
 * Minimal error display that only sets the FlatLaf outline
 * property on invalid components.
 *
 * <p>
 * This display has no visual overlay — it relies solely on
 * FlatLaf's built-in error outline (red border) and the
 * component's tooltip text for error details.
 * </p>
 *
 * <p>
 * Use this when you want the lightest-weight error indication
 * or when combining with your own custom error display logic.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public class OutlineErrorDisplay implements ErrorDisplay {

    @Override
    public void showError(JComponent component, String message) {
        component.putClientProperty(FlatClientProperties.OUTLINE, "error");
        component.setToolTipText(message);
    }

    @Override
    public void hideError(JComponent component) {
        component.putClientProperty(FlatClientProperties.OUTLINE, null);
        component.setToolTipText(null);
    }
}
