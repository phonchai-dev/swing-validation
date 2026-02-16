package io.github.phonchai.validation;

import io.github.phonchai.validation.adapter.ComponentAdapter;
import io.github.phonchai.validation.adapter.ComponentAdapterRegistry;
import io.github.phonchai.validation.display.ErrorDisplay;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Fluent builder for configuring validation rules on a single
 * {@link JComponent}.
 *
 * <p>
 * Instances are created via {@link FormValidator#field(JComponent)}
 * and support method chaining for concise rule definition:
 * </p>
 *
 * <pre>{@code
 * validator.field(txtEmail)
 *         .required("Email is required")
 *         .email("Please enter a valid email")
 *         .add();
 * }</pre>
 *
 * <p>
 * The {@link #add()} call (or any terminal shortcut method like
 * {@link #required(String)}) registers the field and its rules with
 * the parent {@code FormValidator} and installs real-time change
 * listeners on the component.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public final class FieldValidator {

    private final JComponent component;
    private final FormValidator parent;
    private final List<ValidationRule> rules = new ArrayList<>();
    private ErrorDisplay fieldDisplay; // per-field override (null = use parent's)
    private boolean registered = false;

    /**
     * Package-private constructor — instances created by {@link FormValidator}.
     */
    FieldValidator(JComponent component, FormValidator parent) {
        this.component = Objects.requireNonNull(component, "component must not be null");
        this.parent = Objects.requireNonNull(parent, "parent FormValidator must not be null");
    }

    // ──────────────────────────────────────────────────────────────
    // Fluent rule methods (return this for chaining)
    // ──────────────────────────────────────────────────────────────

    /**
     * Adds a "required" rule — field must have a non-empty value.
     * <p>
     * This method both adds the rule AND registers the field.
     * </p>
     *
     * @param message error message to show when empty
     * @return this builder for chaining
     */
    public FieldValidator required(String message) {
        rules.add(Rules.required(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a conditional "required" rule — field is required only
     * when the given condition is true.
     *
     * @param condition when to enforce the requirement
     * @param message   error message
     * @return this builder for chaining
     */
    public FieldValidator requiredWhen(BooleanSupplier condition, String message) {
        rules.add(Rules.when(condition, Rules.required(message)));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a regex pattern validation rule.
     *
     * @param regex   the regular expression
     * @param message error message when pattern doesn't match
     * @return this builder for chaining
     */
    public FieldValidator pattern(String regex, String message) {
        rules.add(Rules.pattern(regex, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a minimum length validation rule.
     *
     * @param min     minimum character count
     * @param message error message
     * @return this builder for chaining
     */
    public FieldValidator minLength(int min, String message) {
        rules.add(Rules.minLength(min, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a maximum length validation rule.
     *
     * @param max     maximum character count
     * @param message error message
     * @return this builder for chaining
     */
    public FieldValidator maxLength(int max, String message) {
        rules.add(Rules.maxLength(max, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds an email validation rule.
     *
     * @param message error message for invalid email
     * @return this builder for chaining
     */
    public FieldValidator email(String message) {
        rules.add(Rules.email(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a numeric validation rule.
     *
     * @param message error message for non-numeric input
     * @return this builder for chaining
     */
    public FieldValidator number(String message) {
        rules.add(Rules.number(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a minimum value validation rule (for numeric fields).
     *
     * @param min     minimum allowed value
     * @param message error message
     * @return this builder for chaining
     */
    public FieldValidator min(double min, String message) {
        rules.add(Rules.min(min, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a maximum value validation rule (for numeric fields).
     *
     * @param max     maximum allowed value
     * @param message error message
     * @return this builder for chaining
     */
    public FieldValidator max(double max, String message) {
        rules.add(Rules.max(max, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a URL validation rule.
     *
     * @param message error message for invalid URL
     * @return this builder for chaining
     */
    public FieldValidator url(String message) {
        rules.add(Rules.url(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds an integer validation rule (whole numbers only).
     *
     * @param message error message for non-integer input
     * @return this builder for chaining
     */
    public FieldValidator integer(String message) {
        rules.add(Rules.integer(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a numeric range validation rule.
     *
     * @param min     minimum value (inclusive)
     * @param max     maximum value (inclusive)
     * @param message error message
     * @return this builder for chaining
     */
    public FieldValidator between(double min, double max, String message) {
        rules.add(Rules.between(min, max, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a digits-only validation rule with exact count.
     * E.g., national ID (13 digits), PIN code (6 digits).
     *
     * @param count   exact number of digits required
     * @param message error message
     * @return this builder for chaining
     */
    public FieldValidator digits(int count, String message) {
        rules.add(Rules.digits(count, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a "matches" rule — this field must equal the value from
     * another field (e.g., confirm password).
     *
     * @param otherValueSupplier supplier for the other field's value
     * @param message            error message
     * @return this builder for chaining
     */
    public FieldValidator matches(Supplier<String> otherValueSupplier, String message) {
        rules.add(Rules.matches(otherValueSupplier, message));
        ensureRegistered();
        return this;
    }

    /**
     * Validates that this field matches another component's value.
     * <p>
     * Automatically adds a listener to the other component to trigger
     * re-validation of this field when the other component changes.
     * </p>
     */
    public FieldValidator matches(JComponent other, String message) {
        // Add rule
        ComponentAdapter otherAdapter = ComponentAdapterRegistry.getInstance().findAdapter(other);
        rules.add(Rules.matches(() -> otherAdapter.getValue(other), message));

        // Add cross-field trigger
        otherAdapter.addChangeListener(other, () -> {
            if (parent.isRealTimeEnabled()) {
                parent.validateSingleField(this);
            }
        });

        ensureRegistered();
        return this;
    }

    /**
     * Adds a whitelist validation rule — value must be one of
     * the allowed values.
     *
     * @param allowedValues collection of allowed values
     * @param message       error message
     * @return this builder for chaining
     */
    public FieldValidator oneOf(Collection<String> allowedValues, String message) {
        rules.add(Rules.oneOf(allowedValues, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a custom validation rule.
     *
     * @param rule the custom rule
     * @return this builder for chaining
     */
    public FieldValidator rule(ValidationRule rule) {
        rules.add(Objects.requireNonNull(rule, "rule must not be null"));
        ensureRegistered();
        return this;
    }

    /**
     * Adds multiple rules at once.
     *
     * @param rules the rules to add
     * @return this builder for chaining
     */
    public FieldValidator rules(ValidationRule... rules) {
        for (ValidationRule r : rules) {
            this.rules.add(Objects.requireNonNull(r, "rule must not be null"));
        }
        ensureRegistered();
        return this;
    }

    /**
     * Sets a per-field error display, overriding the parent's default.
     *
     * @param display the error display for this specific field
     * @return this builder for chaining
     */
    public FieldValidator display(ErrorDisplay display) {
        this.fieldDisplay = display;
        return this;
    }

    /**
     * Explicitly registers this field with the parent validator.
     * <p>
     * This is called automatically by rule methods, but can be
     * called explicitly if you want to register a field with only
     * a per-field display override and no rules.
     * </p>
     */
    public void add() {
        ensureRegistered();
    }

    // ──────────────────────────────────────────────────────────────
    // Package-private validation execution
    // ──────────────────────────────────────────────────────────────

    /**
     * Runs all rules on this field and returns the first error message,
     * or null if all rules pass.
     */
    String validateField() {
        for (ValidationRule rule : rules) {
            String error = rule.validate(component);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    /**
     * Returns the target component.
     */
    JComponent getComponent() {
        return component;
    }

    /**
     * Returns the effective error display for this field.
     */
    ErrorDisplay getEffectiveDisplay(ErrorDisplay parentDefault) {
        return fieldDisplay != null ? fieldDisplay : parentDefault;
    }

    /**
     * Returns an unmodifiable view of the rules.
     */
    List<ValidationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    // ──────────────────────────────────────────────────────────────
    // Registration
    // ──────────────────────────────────────────────────────────────

    /**
     * Ensures this field is registered with the parent validator
     * and change listeners are installed (once only).
     */
    private void ensureRegistered() {
        if (!registered) {
            registered = true;
            parent.registerField(this);
            installChangeListener();
        }
    }

    /**
     * Installs a real-time change listener on the component.
     */
    private void installChangeListener() {
        io.github.phonchai.validation.adapter.ComponentAdapter adapter = ComponentAdapterRegistry.getInstance()
                .findAdapter(component);
        if (adapter != null) {
            adapter.addChangeListener(component, () -> parent.validateSingleField(this));
        }
    }
}
