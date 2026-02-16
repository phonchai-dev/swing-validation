package io.github.phonchai.validation.adapter;

import javax.swing.JComponent;

/**
 * SPI interface for adapting Swing components to the validation framework.
 *
 * <p>
 * Each adapter knows how to:
 * </p>
 * <ul>
 * <li>Determine if it supports a given component type</li>
 * <li>Extract the current value from the component (as a String for rule
 * evaluation)</li>
 * <li>Install a change listener for real-time validation</li>
 * </ul>
 *
 * <p>
 * The library provides built-in adapters for common Swing components
 * ({@code JTextComponent}, {@code JComboBox}). Custom adapters can be
 * registered
 * via {@link ComponentAdapterRegistry#register(ComponentAdapter)} for
 * project-specific components (e.g., {@code DatePickerField}).
 * </p>
 *
 * <p>
 * <b>Implementation guidelines:</b>
 * </p>
 * <ul>
 * <li>{@link #supports(JComponent)} should use {@code instanceof} checks</li>
 * <li>{@link #getValue(JComponent)} should return a trimmed string
 * representation</li>
 * <li>{@link #addChangeListener(JComponent, Runnable)} must not add duplicate
 * listeners</li>
 * </ul>
 *
 * @author phonchai
 * @since 1.0.0
 */
public interface ComponentAdapter {

    /**
     * Tests whether this adapter can handle the given component.
     *
     * @param component the component to check
     * @return {@code true} if this adapter supports the component type
     */
    boolean supports(JComponent component);

    /**
     * Extracts the current value from the component as a string.
     *
     * <p>
     * For text components, this is the trimmed text content.
     * For combo boxes, this is the string representation of the selected item
     * (or empty string if nothing is selected).
     * </p>
     *
     * @param component the component to extract the value from
     * @return the current value as a string, never {@code null}
     */
    String getValue(JComponent component);

    /**
     * Installs a change listener on the component that fires the given
     * callback whenever the component's value changes.
     *
     * <p>
     * The callback should be invoked on the EDT.
     * </p>
     *
     * @param component the component to monitor
     * @param onChange  the callback to invoke on value change
     */
    void addChangeListener(JComponent component, Runnable onChange);
}
