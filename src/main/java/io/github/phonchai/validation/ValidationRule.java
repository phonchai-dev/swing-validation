package io.github.phonchai.validation;

import javax.swing.JComponent;

/**
 * Functional interface representing a single validation rule.
 *
 * <p>
 * A validation rule inspects a {@link JComponent} and returns an error message
 * if the component's state is invalid, or {@code null} if valid.
 * </p>
 *
 * <p>
 * <b>Example usage:</b>
 * </p>
 * 
 * <pre>{@code
 * ValidationRule required = component -> {
 *     if (component instanceof JTextField tf && tf.getText().trim().isEmpty()) {
 *         return "This field is required.";
 *     }
 *     return null;
 * };
 * }</pre>
 *
 * @author phonchai
 * @since 1.0.0
 */
@FunctionalInterface
public interface ValidationRule {

    /**
     * Validates the given component.
     *
     * @param component the Swing component to validate
     * @return an error message if validation fails, or {@code null} if the
     *         component is valid
     */
    String validate(JComponent component);
}
