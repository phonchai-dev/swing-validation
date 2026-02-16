package io.github.phonchai.validation;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
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
 * <p>
 * Now updated with Internationalization (i18n) support.
 * Parameterless rules automatically fetch localized messages via
 * {@link Localization}.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public final class Rules {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "^-?\\d+(\\.\\d+)?$");

    private static final Pattern INTEGER_PATTERN = Pattern.compile(
            "^-?\\d+$");

    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile(
            "^\\d+$");

    private Rules() {
    }

    /**
     * Creates a rule with a custom message supplier.
     */
    private static ValidationRule create(Predicate<JComponent> isValid, Supplier<String> messageSupplier) {
        return component -> isValid.test(component) ? null : messageSupplier.get();
    }

    /**
     * Creates a rule with a static error message.
     */
    private static ValidationRule create(Predicate<JComponent> isValid, String message) {
        return create(isValid, () -> message);
    }

    // ──────────────────────────────────────────────────────────────
    // Presence
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule required() {
        return create(Rules::checkRequired, () -> Localization.get("validation.required"));
    }

    public static ValidationRule required(String message) {
        return create(Rules::checkRequired, message);
    }

    private static boolean checkRequired(JComponent component) {
        if (component instanceof JTextComponent tc) {
            return !tc.getText().trim().isEmpty();
        } else if (component instanceof JComboBox<?> cb) {
            return cb.getSelectedIndex() >= 0 && cb.getSelectedItem() != null;
        } else {
            Object value = component.getClientProperty("validation.value");
            if (value == null)
                return false;
            if (value instanceof String s)
                return !s.trim().isEmpty();
            return true;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Length
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule minLength(int min) {
        return create(c -> checkMinLength(c, min), () -> Localization.get("validation.minLength", min));
    }

    public static ValidationRule minLength(int min, String message) {
        return create(c -> checkMinLength(c, min), message);
    }

    private static boolean checkMinLength(JComponent c, int min) {
        String text = extractText(c);
        return text.isEmpty() || text.length() >= min;
    }

    public static ValidationRule maxLength(int max) {
        return create(c -> checkMaxLength(c, max), () -> Localization.get("validation.maxLength", max));
    }

    public static ValidationRule maxLength(int max, String message) {
        return create(c -> checkMaxLength(c, max), message);
    }

    private static boolean checkMaxLength(JComponent c, int max) {
        String text = extractText(c);
        return text.length() <= max;
    }

    // ──────────────────────────────────────────────────────────────
    // Pattern
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule pattern(String regex) {
        Pattern compiled = Pattern.compile(regex);
        return create(c -> checkPattern(c, compiled), () -> Localization.get("validation.pattern"));
    }

    public static ValidationRule pattern(String regex, String message) {
        Pattern compiled = Pattern.compile(regex);
        return create(c -> checkPattern(c, compiled), message);
    }

    private static boolean checkPattern(JComponent c, Pattern p) {
        String text = extractText(c);
        return text.isEmpty() || p.matcher(text).matches();
    }

    // ──────────────────────────────────────────────────────────────
    // Type-specific
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule email() {
        return create(c -> checkPattern(c, EMAIL_PATTERN), () -> Localization.get("validation.email"));
    }

    public static ValidationRule email(String message) {
        return create(c -> checkPattern(c, EMAIL_PATTERN), message);
    }

    public static ValidationRule number() {
        return create(c -> checkPattern(c, NUMBER_PATTERN), () -> Localization.get("validation.number"));
    }

    public static ValidationRule number(String message) {
        return create(c -> checkPattern(c, NUMBER_PATTERN), message);
    }

    public static ValidationRule min(double min) {
        return create(c -> checkMin(c, min), () -> Localization.get("validation.min", min));
    }

    public static ValidationRule min(double min, String message) {
        return create(c -> checkMin(c, min), message);
    }

    private static boolean checkMin(JComponent c, double min) {
        String text = extractText(c);
        if (text.isEmpty())
            return true;
        try {
            return Double.parseDouble(text) >= min;
        } catch (NumberFormatException e) {
            return true; // Let number() rule handle format
        }
    }

    public static ValidationRule max(double max) {
        return create(c -> checkMax(c, max), () -> Localization.get("validation.max", max));
    }

    public static ValidationRule max(double max, String message) {
        return create(c -> checkMax(c, max), message);
    }

    private static boolean checkMax(JComponent c, double max) {
        String text = extractText(c);
        if (text.isEmpty())
            return true;
        try {
            return Double.parseDouble(text) <= max;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // URL
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule url() {
        return create(Rules::checkUrl, () -> Localization.get("validation.url"));
    }

    public static ValidationRule url(String message) {
        return create(Rules::checkUrl, message);
    }

    private static boolean checkUrl(JComponent c) {
        String text = extractText(c);
        if (text.isEmpty())
            return true;
        try {
            URI.create(text).toURL();
            return true;
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Integer
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule integer() {
        return create(c -> checkPattern(c, INTEGER_PATTERN), () -> Localization.get("validation.integer"));
    }

    public static ValidationRule integer(String message) {
        return create(c -> checkPattern(c, INTEGER_PATTERN), message);
    }

    // ──────────────────────────────────────────────────────────────
    // Between (range shortcut)
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule between(double min, double max) {
        return create(c -> checkBetween(c, min, max), () -> Localization.get("validation.between", min, max));
    }

    public static ValidationRule between(double min, double max, String message) {
        return create(c -> checkBetween(c, min, max), message);
    }

    private static boolean checkBetween(JComponent c, double min, double max) {
        String text = extractText(c);
        if (text.isEmpty())
            return true;
        try {
            double val = Double.parseDouble(text);
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Digits (exact count)
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule digits(int count) {
        return create(c -> checkDigits(c, count), () -> Localization.get("validation.digits", count));
    }

    public static ValidationRule digits(int count, String message) {
        return create(c -> checkDigits(c, count), message);
    }

    private static boolean checkDigits(JComponent c, int count) {
        String text = extractText(c);
        if (text.isEmpty())
            return true;
        return DIGITS_ONLY_PATTERN.matcher(text).matches() && text.length() == count;
    }

    // ──────────────────────────────────────────────────────────────
    // Matches (confirm field)
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule matches(Supplier<String> otherValueSupplier) {
        return create(c -> checkMatches(c, otherValueSupplier), () -> Localization.get("validation.matches"));
    }

    public static ValidationRule matches(Supplier<String> otherValueSupplier, String message) {
        return create(c -> checkMatches(c, otherValueSupplier), message);
    }

    // Overload for Component-to-Component match is handled in FieldValidator
    // mostly,
    // but Rules.matches takes a supplier so it's fine.

    private static boolean checkMatches(JComponent c, Supplier<String> other) {
        String text = extractText(c);
        if (text.isEmpty())
            return true;
        String otherVal = other.get();
        return otherVal != null && text.equals(otherVal.trim());
    }

    // ──────────────────────────────────────────────────────────────
    // OneOf (whitelist)
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule oneOf(Collection<String> allowedValues) {
        return create(c -> checkOneOf(c, allowedValues), () -> Localization.get("validation.oneOf"));
    }

    public static ValidationRule oneOf(Collection<String> allowedValues, String message) {
        return create(c -> checkOneOf(c, allowedValues), message);
    }

    private static boolean checkOneOf(JComponent c, Collection<String> allowed) {
        String text = extractText(c);
        return text.isEmpty() || allowed.contains(text);
    }

    // ──────────────────────────────────────────────────────────────
    // Custom & Conditional
    // ──────────────────────────────────────────────────────────────

    public static ValidationRule custom(Function<JComponent, String> validator) {
        return validator::apply;
    }

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

    private static String extractText(JComponent component) {
        if (component instanceof JTextComponent tc) {
            return tc.getText().trim();
        }
        Object value = component.getClientProperty("validation.value");
        if (value instanceof String s) {
            return s.trim();
        }
        return "";
    }
}
