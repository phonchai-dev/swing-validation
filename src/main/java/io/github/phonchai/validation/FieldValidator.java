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
 * and support method chaining for concise rule definition.
 * </p>
 *
 * <p>
 * Now supports parameterless methods for i18n (Internationalization).
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
     * Adds a "required" rule with default localized message.
     */
    public FieldValidator required() {
        rules.add(Rules.required());
        ensureRegistered();
        return this;
    }

    /**
     * Adds a "required" rule with custom message.
     */
    public FieldValidator required(String message) {
        rules.add(Rules.required(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a conditional "required" rule.
     */
    public FieldValidator requiredWhen(BooleanSupplier condition, String message) {
        rules.add(Rules.when(condition, Rules.required(message)));
        ensureRegistered();
        return this;
    }

    // New parameterless override using i18n message
    public FieldValidator requiredWhen(BooleanSupplier condition) {
        rules.add(Rules.when(condition, Rules.required()));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a regex pattern validation rule.
     */
    public FieldValidator pattern(String regex) {
        rules.add(Rules.pattern(regex));
        ensureRegistered();
        return this;
    }

    public FieldValidator pattern(String regex, String message) {
        rules.add(Rules.pattern(regex, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a minimum length validation rule.
     */
    public FieldValidator minLength(int min) {
        rules.add(Rules.minLength(min));
        ensureRegistered();
        return this;
    }

    public FieldValidator minLength(int min, String message) {
        rules.add(Rules.minLength(min, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a maximum length validation rule.
     */
    public FieldValidator maxLength(int max) {
        rules.add(Rules.maxLength(max));
        ensureRegistered();
        return this;
    }

    public FieldValidator maxLength(int max, String message) {
        rules.add(Rules.maxLength(max, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds an email validation rule.
     */
    public FieldValidator email() {
        rules.add(Rules.email());
        ensureRegistered();
        return this;
    }

    public FieldValidator email(String message) {
        rules.add(Rules.email(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a numeric validation rule.
     */
    public FieldValidator number() {
        rules.add(Rules.number());
        ensureRegistered();
        return this;
    }

    public FieldValidator number(String message) {
        rules.add(Rules.number(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a minimum value validation rule (for numeric fields).
     */
    public FieldValidator min(double min) {
        rules.add(Rules.min(min));
        ensureRegistered();
        return this;
    }

    public FieldValidator min(double min, String message) {
        rules.add(Rules.min(min, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a maximum value validation rule (for numeric fields).
     */
    public FieldValidator max(double max) {
        rules.add(Rules.max(max));
        ensureRegistered();
        return this;
    }

    public FieldValidator max(double max, String message) {
        rules.add(Rules.max(max, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a URL validation rule.
     */
    public FieldValidator url() {
        rules.add(Rules.url());
        ensureRegistered();
        return this;
    }

    public FieldValidator url(String message) {
        rules.add(Rules.url(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds an integer validation rule (whole numbers only).
     */
    public FieldValidator integer() {
        rules.add(Rules.integer());
        ensureRegistered();
        return this;
    }

    public FieldValidator integer(String message) {
        rules.add(Rules.integer(message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a numeric range validation rule.
     */
    public FieldValidator between(double min, double max) {
        rules.add(Rules.between(min, max));
        ensureRegistered();
        return this;
    }

    public FieldValidator between(double min, double max, String message) {
        rules.add(Rules.between(min, max, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a digits-only validation rule with exact count.
     */
    public FieldValidator digits(int count) {
        rules.add(Rules.digits(count));
        ensureRegistered();
        return this;
    }

    public FieldValidator digits(int count, String message) {
        rules.add(Rules.digits(count, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a "matches" rule — this field must equal the value from
     * another field (e.g., confirm password).
     */
    public FieldValidator matches(Supplier<String> otherValueSupplier) {
        rules.add(Rules.matches(otherValueSupplier));
        ensureRegistered();
        return this;
    }

    public FieldValidator matches(Supplier<String> otherValueSupplier, String message) {
        rules.add(Rules.matches(otherValueSupplier, message));
        ensureRegistered();
        return this;
    }

    /**
     * Validates that this field matches another component's value.
     */
    public FieldValidator matches(JComponent other) {
        // Add rule with default message
        ComponentAdapter otherAdapter = ComponentAdapterRegistry.getInstance().findAdapter(other);
        rules.add(Rules.matches(() -> otherAdapter.getValue(other)));

        // Add cross-field trigger
        addMatchesListener(otherAdapter, other);

        ensureRegistered();
        return this;
    }

    public FieldValidator matches(JComponent other, String message) {
        // Add rule
        ComponentAdapter otherAdapter = ComponentAdapterRegistry.getInstance().findAdapter(other);
        rules.add(Rules.matches(() -> otherAdapter.getValue(other), message));

        // Add cross-field trigger
        addMatchesListener(otherAdapter, other);

        ensureRegistered();
        return this;
    }

    private void addMatchesListener(ComponentAdapter adapter, JComponent other) {
        adapter.addChangeListener(other, () -> {
            if (parent.isRealTimeEnabled()) {
                parent.validateSingleField(this);
            }
        });
    }

    /**
     * Adds a whitelist validation rule.
     */
    public FieldValidator oneOf(Collection<String> allowedValues) {
        rules.add(Rules.oneOf(allowedValues));
        ensureRegistered();
        return this;
    }

    public FieldValidator oneOf(Collection<String> allowedValues, String message) {
        rules.add(Rules.oneOf(allowedValues, message));
        ensureRegistered();
        return this;
    }

    /**
     * Adds a custom validation rule.
     */
    public FieldValidator rule(ValidationRule rule) {
        rules.add(Objects.requireNonNull(rule, "rule must not be null"));
        ensureRegistered();
        return this;
    }

    /**
     * Adds multiple rules at once.
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
     */
    public FieldValidator display(ErrorDisplay display) {
        this.fieldDisplay = display;
        return this;
    }

    /**
     * Explicitly registers this field with the parent validator.
     */
    public void add() {
        ensureRegistered();
    }

    // ──────────────────────────────────────────────────────────────
    // Package-private validation execution
    // ──────────────────────────────────────────────────────────────

    String validateField() {
        for (ValidationRule rule : rules) {
            String error = rule.validate(component);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    JComponent getComponent() {
        return component;
    }

    ErrorDisplay getEffectiveDisplay(ErrorDisplay parentDefault) {
        return fieldDisplay != null ? fieldDisplay : parentDefault;
    }

    List<ValidationRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    // ──────────────────────────────────────────────────────────────
    // Registration
    // ──────────────────────────────────────────────────────────────

    private void ensureRegistered() {
        if (!registered) {
            registered = true;
            parent.registerField(this);
            installChangeListener();
        }
    }

    private void installChangeListener() {
        io.github.phonchai.validation.adapter.ComponentAdapter adapter = ComponentAdapterRegistry.getInstance()
                .findAdapter(component);
        if (adapter != null) {
            adapter.addChangeListener(component, () -> parent.validateSingleField(this));
        }
    }
}
