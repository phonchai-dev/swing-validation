package io.github.phonchai.validation;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Factory class providing built-in {@link ValidationRule} implementations.
 *
 * <p>
 * Rules are designed to mirror HTML5 / web form validation attributes,
 * making the API intuitive for developers familiar with web development.
 * </p>
 *
 * <h2>Quick Reference</h2>
 * <table>
 * <tr>
 * <th>Method</th>
 * <th>Web Equivalent</th>
 * </tr>
 * <tr>
 * <td>{@link #required(String)}</td>
 * <td>{@code required}</td>
 * </tr>
 * <tr>
 * <td>{@link #minLength(int, String)}</td>
 * <td>{@code minlength}</td>
 * </tr>
 * <tr>
 * <td>{@link #maxLength(int, String)}</td>
 * <td>{@code maxlength}</td>
 * </tr>
 * <tr>
 * <td>{@link #pattern(String, String)}</td>
 * <td>{@code pattern}</td>
 * </tr>
 * <tr>
 * <td>{@link #email(String)}</td>
 * <td>{@code type="email"}</td>
 * </tr>
 * <tr>
 * <td>{@link #number(String)}</td>
 * <td>{@code type="number"}</td>
 * </tr>
 * <tr>
 * <td>{@link #min(double, String)}</td>
 * <td>{@code min}</td>
 * </tr>
 * <tr>
 * <td>{@link #max(double, String)}</td>
 * <td>{@code max}</td>
 * </tr>
 * <tr>
 * <td>{@link #url(String)}</td>
 * <td>{@code type="url"}</td>
 * </tr>
 * <tr>
 * <td>{@link #integer(String)}</td>
 * <td>{@code type="number" step="1"}</td>
 * </tr>
 * <tr>
 * <td>{@link #between(double, double, String)}</td>
 * <td>{@code min + max}</td>
 * </tr>
 * <tr>
 * <td>{@link #digits(int, String)}</td>
 * <td>exact digit count</td>
 * </tr>
 * <tr>
 * <td>{@link #matches(Supplier, String)}</td>
 * <td>confirm password</td>
 * </tr>
 * <tr>
 * <td>{@link #oneOf(Collection, String)}</td>
 * <td>whitelist values</td>
 * </tr>
 * <tr>
 * <td>{@link #custom(Function)}</td>
 * <td>JS custom validation</td>
 * </tr>
 * <tr>
 * <td>{@link #when(BooleanSupplier, ValidationRule)}</td>
 * <td>conditional</td>
 * </tr>
 * </table>
 *
 * @author phonchai
 * @since 1.0.0
 */
public final class Rules {

    // RFC 5322 simplified email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "^-?\\d+(\\.\\d+)?$");

    private static final Pattern INTEGER_PATTERN = Pattern.compile(
            "^-?\\d+$");

    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile(
            "^\\d+$");

    private Rules() {
        // Utility class — no instantiation
    }

    // ──────────────────────────────────────────────────────────────
    // Presence
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the component has a non-empty value.
     *
     * <p>
     * Supports:
     * </p>
     * <ul>
     * <li>{@code JTextComponent} — text must not be blank</li>
     * <li>{@code JComboBox} — an item must be selected (index ≥ 0)</li>
     * <li>Other components — always passes (use {@link #custom(Function)}
     * instead)</li>
     * </ul>
     *
     * @param message the error message to show if validation fails
     * @return the validation rule
     */
    public static ValidationRule required(String message) {
        return component -> {
            if (component instanceof JTextComponent tc) {
                if (tc.getText().trim().isEmpty()) {
                    return message;
                }
            } else if (component instanceof JComboBox<?> cb) {
                if (cb.getSelectedIndex() < 0 || cb.getSelectedItem() == null) {
                    return message;
                }
            } else {
                // For unknown component types, try client property "validation.value"
                Object value = component.getClientProperty("validation.value");
                if (value == null) {
                    return message;
                }
                if (value instanceof String s && s.trim().isEmpty()) {
                    return message;
                }
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Length
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value has at least {@code min} characters.
     *
     * <p>
     * Empty values pass this rule — combine with {@link #required(String)}
     * if the field must also be non-empty.
     * </p>
     *
     * @param min     minimum character count (inclusive)
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule minLength(int min, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty() && text.length() < min) {
                return message;
            }
            return null;
        };
    }

    /**
     * Validates that the text value has at most {@code max} characters.
     *
     * @param max     maximum character count (inclusive)
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule maxLength(int max, String message) {
        return component -> {
            String text = extractText(component);
            if (text.length() > max) {
                return message;
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Pattern
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value matches the given regular expression.
     *
     * <p>
     * Empty values pass this rule — combine with {@link #required(String)}
     * if the field must also be non-empty.
     * </p>
     *
     * @param regex   the regular expression pattern
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule pattern(String regex, String message) {
        Pattern compiled = Pattern.compile(regex);
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty() && !compiled.matcher(text).matches()) {
                return message;
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Type-specific
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value is a valid email address.
     *
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule email(String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty() && !EMAIL_PATTERN.matcher(text).matches()) {
                return message;
            }
            return null;
        };
    }

    /**
     * Validates that the text value is a valid number.
     *
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule number(String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty() && !NUMBER_PATTERN.matcher(text).matches()) {
                return message;
            }
            return null;
        };
    }

    /**
     * Validates that the numeric value is at least {@code min}.
     *
     * <p>
     * Non-numeric and empty values pass this rule.
     * </p>
     *
     * @param min     minimum value (inclusive)
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule min(double min, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty()) {
                try {
                    double value = Double.parseDouble(text);
                    if (value < min) {
                        return message;
                    }
                } catch (NumberFormatException ignored) {
                    // Not a number — let the number() rule handle it
                }
            }
            return null;
        };
    }

    /**
     * Validates that the numeric value is at most {@code max}.
     *
     * <p>
     * Non-numeric and empty values pass this rule.
     * </p>
     *
     * @param max     maximum value (inclusive)
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule max(double max, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty()) {
                try {
                    double value = Double.parseDouble(text);
                    if (value > max) {
                        return message;
                    }
                } catch (NumberFormatException ignored) {
                    // Not a number — let the number() rule handle it
                }
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // URL
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value is a valid URL.
     *
     * <p>
     * Empty values pass this rule.
     * </p>
     *
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule url(String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty()) {
                try {
                    URI.create(text).toURL();
                } catch (MalformedURLException | IllegalArgumentException e) {
                    return message;
                }
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Integer
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value is a whole integer (no decimals).
     *
     * <p>
     * Empty values pass this rule.
     * </p>
     *
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule integer(String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty() && !INTEGER_PATTERN.matcher(text).matches()) {
                return message;
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Between (range shortcut)
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the numeric value is between {@code min} and {@code max}
     * (inclusive). Shortcut for combining {@link #min} and {@link #max}.
     *
     * <p>
     * Non-numeric and empty values pass this rule.
     * </p>
     *
     * @param min     minimum value (inclusive)
     * @param max     maximum value (inclusive)
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule between(double min, double max, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty()) {
                try {
                    double value = Double.parseDouble(text);
                    if (value < min || value > max) {
                        return message;
                    }
                } catch (NumberFormatException ignored) {
                    // Not a number — let the number() rule handle it
                }
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Digits (exact count)
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value consists of exactly {@code count}
     * digits (0-9 only, no signs or decimals).
     *
     * <p>
     * Useful for national ID numbers, PIN codes, etc.
     * </p>
     *
     * <p>
     * Empty values pass this rule.
     * </p>
     *
     * @param count   exact number of digits required
     * @param message the error message
     * @return the validation rule
     */
    public static ValidationRule digits(int count, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty()) {
                if (!DIGITS_ONLY_PATTERN.matcher(text).matches()
                        || text.length() != count) {
                    return message;
                }
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Matches (confirm field)
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that this field's value matches the value from another
     * field (e.g., confirm password).
     *
     * <p>
     * The {@code otherValueSupplier} is evaluated at validation time
     * to get the current value of the other field.
     * </p>
     *
     * <p>
     * Empty values pass this rule.
     * </p>
     *
     * @param otherValueSupplier supplier that returns the other field's current
     *                           value
     * @param message            the error message
     * @return the validation rule
     */
    public static ValidationRule matches(Supplier<String> otherValueSupplier, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty()) {
                String otherValue = otherValueSupplier.get();
                if (otherValue == null || !text.equals(otherValue.trim())) {
                    return message;
                }
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // OneOf (whitelist)
    // ──────────────────────────────────────────────────────────────

    /**
     * Validates that the text value is one of the allowed values.
     *
     * <p>
     * Comparison is case-sensitive. Empty values pass this rule.
     * </p>
     *
     * @param allowedValues the collection of allowed values
     * @param message       the error message
     * @return the validation rule
     */
    public static ValidationRule oneOf(Collection<String> allowedValues, String message) {
        return component -> {
            String text = extractText(component);
            if (!text.isEmpty() && !allowedValues.contains(text)) {
                return message;
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Custom & Conditional
    // ──────────────────────────────────────────────────────────────

    /**
     * Creates a validation rule from a custom function.
     *
     * <p>
     * The function receives the component and returns an error message
     * (or {@code null} if valid).
     * </p>
     *
     * @param validator the custom validation function
     * @return the validation rule
     */
    public static ValidationRule custom(Function<JComponent, String> validator) {
        return validator::apply;
    }

    /**
     * Creates a conditional validation rule that only applies when the
     * given condition is {@code true}.
     *
     * <p>
     * Useful for fields that are conditionally required based on
     * other form state (e.g., a detail field required only when a
     * checkbox is selected).
     * </p>
     *
     * @param condition supplier that returns {@code true} when the rule should
     *                  apply
     * @param rule      the rule to evaluate when the condition is met
     * @return the conditional validation rule
     */
    public static ValidationRule when(BooleanSupplier condition, ValidationRule rule) {
        return component -> {
            if (condition.getAsBoolean()) {
                return rule.validate(component);
            }
            return null;
        };
    }

    // ──────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────

    /**
     * Extracts text content from a component, trimmed.
     */
    private static String extractText(JComponent component) {
        if (component instanceof JTextComponent tc) {
            return tc.getText().trim();
        }
        // Check for client property (used by custom component adapters)
        Object value = component.getClientProperty("validation.value");
        if (value instanceof String s) {
            return s.trim();
        }
        return "";
    }
}
