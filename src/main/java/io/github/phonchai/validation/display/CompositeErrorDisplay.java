package io.github.phonchai.validation.display;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An error display that delegates to multiple other error displays.
 *
 * <p>
 * Useful for combining different error feedback mechanisms, for example:
 * showing a trailing icon AND a balloon tooltip simultaneously.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public class CompositeErrorDisplay implements ErrorDisplay {

    private final List<ErrorDisplay> displays = new ArrayList<>();

    public CompositeErrorDisplay(ErrorDisplay... displays) {
        this.displays.addAll(Arrays.asList(displays));
    }

    public void add(ErrorDisplay display) {
        displays.add(display);
    }

    @Override
    public void showError(JComponent component, String message) {
        for (ErrorDisplay display : displays) {
            display.showError(component, message);
        }
    }

    @Override
    public void hideError(JComponent component) {
        for (ErrorDisplay display : displays) {
            display.hideError(component);
        }
    }

    @Override
    public void dispose() {
        for (ErrorDisplay display : displays) {
            display.dispose();
        }
        displays.clear();
    }
}
