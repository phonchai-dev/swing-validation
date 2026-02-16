package io.github.phonchai.validation;

import io.github.phonchai.validation.display.BalloonTooltipDisplay;
import io.github.phonchai.validation.display.ErrorDisplay;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * The main entry point for form validation.
 *
 * <p>
 * {@code FormValidator} manages a collection of
 * {@link FieldValidator FieldValidators}, coordinates error display,
 * and provides form-level operations (validate all, clear all, etc.).
 * </p>
 *
 * <h2>Quick Start (i18n Supported)</h2>
 * 
 * <pre>{@code
 * // Set global locale (Optional - defaults to English)
 * FormValidator.setLocale(new Locale("th", "TH"));
 * 
 * FormValidator validator = new FormValidator();
 *
 * // Add validation rules (messages auto-fetched from bundle)
 * validator.field(txtUsername).required();
 * validator.field(txtEmail).required().email();
 * validator.field(txtAge).number().min(1).max(150);
 * 
 * // Validate all
 * if (validator.validate()) {
 *     // Proceed...
 * }
 * }</pre>
 *
 * @author phonchai
 * @since 1.0.0
 */
public class FormValidator {

    // ── Global Configuration ────────────────────────────────────

    /**
     * Sets the global locale for all validation messages.
     * <p>
     * This affects all validators in the application, as message
     * bundles are loaded statically.
     * </p>
     *
     * @param locale the locale to set (e.g., Locale.ENGLISH or new Locale("th",
     *               "TH"))
     */
    public static void setLocale(Locale locale) {
        Localization.setLocale(locale);
    }

    // ── State ───────────────────────────────────────────────────

    /**
     * Ordered list of registered field validators.
     * Insertion order is preserved for consistent validation order.
     */
    private final List<FieldValidator> fields = new ArrayList<>();

    /**
     * Fast lookup by component reference.
     */
    private final Map<JComponent, FieldValidator> fieldMap = new LinkedHashMap<>();

    /**
     * Tracks which fields currently have errors.
     */
    private final Set<JComponent> erroneousFields = new LinkedHashSet<>();

    /**
     * Listeners for validation state changes.
     */
    private final List<ValidationListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * The default error display strategy.
     */
    private ErrorDisplay defaultDisplay;

    /**
     * Whether real-time validation (on each keystroke / change) is enabled.
     */
    private boolean realTimeEnabled = true;

    /**
     * Whether this validator has been triggered at least once
     * (via {@link #validate()}). Before first trigger, real-time
     * errors are suppressed to avoid showing errors on empty forms.
     */
    private boolean hasBeenTriggered = false;

    // ── Constructor ─────────────────────────────────────────────

    /**
     * Creates a new FormValidator with the default error display
     * (dark balloon tooltip).
     */
    public FormValidator() {
        this.defaultDisplay = BalloonTooltipDisplay.dark();
    }

    /**
     * Creates a new FormValidator with the specified error display.
     *
     * @param errorDisplay the error display strategy to use
     */
    public FormValidator(ErrorDisplay errorDisplay) {
        this.defaultDisplay = Objects.requireNonNull(errorDisplay,
                "errorDisplay must not be null");
    }

    // ── Configuration ───────────────────────────────────────────

    /**
     * Sets the default error display strategy for all fields
     * (unless overridden per-field).
     *
     * @param display the error display strategy
     */
    public void setErrorDisplay(ErrorDisplay display) {
        // Dispose old display
        if (this.defaultDisplay != null) {
            this.defaultDisplay.dispose();
        }
        this.defaultDisplay = Objects.requireNonNull(display);
    }

    /**
     * Returns the current default error display.
     */
    public ErrorDisplay getErrorDisplay() {
        return defaultDisplay;
    }

    /**
     * Enables or disables real-time validation.
     *
     * <p>
     * When enabled (default), validation runs on every keystroke
     * or selection change. When disabled, validation only runs
     * when {@link #validate()} is called explicitly.
     * </p>
     *
     * @param enabled true to enable real-time validation
     */
    public void setRealTimeEnabled(boolean enabled) {
        this.realTimeEnabled = enabled;
    }

    /**
     * Returns whether real-time validation is enabled.
     */
    public boolean isRealTimeEnabled() {
        return realTimeEnabled;
    }

    // ── Field registration ──────────────────────────────────────

    /**
     * Starts configuring validation for a component.
     *
     * <p>
     * Returns a fluent {@link FieldValidator} builder. If this
     * component already has a validator registered, the existing
     * one is returned (allowing rules to be appended).
     * </p>
     *
     * @param component the Swing component to validate
     * @return the field validator builder
     */
    public FieldValidator field(JComponent component) {
        Objects.requireNonNull(component, "component must not be null");

        // Return existing if already registered
        FieldValidator existing = fieldMap.get(component);
        if (existing != null) {
            return existing;
        }

        return new FieldValidator(component, this);
    }

    /**
     * Convenience method to add a raw validator function.
     * <p>
     * This is the legacy-compatible API for migration from
     * the old {@code FormValidator}.
     * </p>
     *
     * @param component the component to validate
     * @param validator a function returning an error message (or null)
     */
    public void addValidator(JComponent component, Function<JComponent, String> validator) {
        field(component).rule(Rules.custom(validator));
    }

    /**
     * Removes a field from validation.
     *
     * <p>
     * Clears any existing errors for the field and removes it from
     * the internal tracking. Call this when a component is removed
     * from the UI to prevent memory leaks and ghost errors.
     * </p>
     *
     * @param component the component to remove
     */
    public void removeField(JComponent component) {
        FieldValidator fv = fieldMap.remove(component);
        if (fv != null) {
            fields.remove(fv);

            // Clear error if present
            if (erroneousFields.contains(component)) {
                fv.getEffectiveDisplay(defaultDisplay).hideError(component);
                erroneousFields.remove(component);
                notifyListeners(erroneousFields.isEmpty());
            }
        }
    }

    // ── Validation ──────────────────────────────────────────────

    /**
     * Validates all registered fields.
     *
     * <p>
     * Updates the error display for every field and notifies
     * listeners of the overall validation state.
     * </p>
     *
     * <p>
     * After calling this method, real-time validation becomes
     * active — subsequent changes to fields will trigger immediate
     * re-validation.
     * </p>
     *
     * @return {@code true} if all fields are valid, {@code false} otherwise
     */
    public boolean validate() {
        hasBeenTriggered = true;
        boolean allValid = true;

        for (FieldValidator fv : fields) {
            String error = fv.validateField();
            ErrorDisplay display = fv.getEffectiveDisplay(defaultDisplay);

            if (error != null) {
                display.showError(fv.getComponent(), error);
                erroneousFields.add(fv.getComponent());
                allValid = false;
            } else {
                display.hideError(fv.getComponent());
                erroneousFields.remove(fv.getComponent());
            }
        }

        notifyListeners(allValid);

        // Scroll to first error
        if (!allValid) {
            scrollToFirstError();
        }

        return allValid;
    }

    /**
     * Checks whether all fields are currently valid, without
     * triggering any UI updates.
     *
     * @return {@code true} if all fields pass validation
     */
    public boolean isValid() {
        for (FieldValidator fv : fields) {
            if (fv.validateField() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears all validation error displays and resets state.
     */
    public void clearValidation() {
        for (FieldValidator fv : fields) {
            ErrorDisplay display = fv.getEffectiveDisplay(defaultDisplay);
            display.hideError(fv.getComponent());
        }
        erroneousFields.clear();
        hasBeenTriggered = false;
        notifyListeners(true);
    }

    // ── Listeners ───────────────────────────────────────────────

    /**
     * Adds a listener that is notified when the overall validation
     * state changes.
     *
     * @param listener the listener
     */
    public void onValidationChanged(ValidationListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    /**
     * Removes a validation listener.
     *
     * @param listener the listener to remove
     */
    public void removeValidationListener(ValidationListener listener) {
        listeners.remove(listener);
    }

    // ── Resource management ─────────────────────────────────────

    /**
     * Disposes all resources held by this validator and its
     * error displays.
     *
     * <p>
     * Call this when the form panel is removed or the window
     * is closed to prevent memory leaks.
     * </p>
     */
    public void dispose() {
        clearValidation();
        if (defaultDisplay != null) {
            defaultDisplay.dispose();
        }
        fields.clear();
        fieldMap.clear();
        erroneousFields.clear();
        listeners.clear();
    }

    /**
     * Returns the number of registered fields.
     */
    public int getFieldCount() {
        return fields.size();
    }

    /**
     * Returns an unmodifiable list of components with current errors.
     */
    public Set<JComponent> getErroneousFields() {
        return Collections.unmodifiableSet(erroneousFields);
    }

    // ── Package-private (used by FieldValidator) ────────────────

    /**
     * Registers a field validator. Called by {@link FieldValidator}
     * during its builder chain.
     */
    void registerField(FieldValidator fieldValidator) {
        JComponent comp = fieldValidator.getComponent();
        if (!fieldMap.containsKey(comp)) {
            fields.add(fieldValidator);
            fieldMap.put(comp, fieldValidator);
        }
    }

    /**
     * Validates a single field (called by real-time change listeners).
     */
    void validateSingleField(FieldValidator fieldValidator) {
        // Don't show errors until first explicit validate() call
        if (!hasBeenTriggered && !realTimeEnabled) {
            return;
        }

        // If real-time is enabled but hasn't been triggered yet,
        // only clear existing errors (don't show new ones)
        if (!hasBeenTriggered) {
            String error = fieldValidator.validateField();
            if (error == null && erroneousFields.contains(fieldValidator.getComponent())) {
                // Field was fixed — hide error
                ErrorDisplay display = fieldValidator.getEffectiveDisplay(defaultDisplay);
                display.hideError(fieldValidator.getComponent());
                erroneousFields.remove(fieldValidator.getComponent());
                notifyListeners(erroneousFields.isEmpty());
            }
            return;
        }

        String error = fieldValidator.validateField();
        ErrorDisplay display = fieldValidator.getEffectiveDisplay(defaultDisplay);

        if (error != null) {
            display.showError(fieldValidator.getComponent(), error);
            erroneousFields.add(fieldValidator.getComponent());
        } else {
            display.hideError(fieldValidator.getComponent());
            erroneousFields.remove(fieldValidator.getComponent());
        }

        notifyListeners(erroneousFields.isEmpty());
    }

    // ── Private helpers ─────────────────────────────────────────

    private void notifyListeners(boolean allValid) {
        for (ValidationListener listener : listeners) {
            try {
                listener.onValidationChanged(allValid);
            } catch (Exception e) {
                // Don't let listener errors break the validation flow
                System.err.println("[FormValidator] Listener error: " + e.getMessage());
            }
        }
    }

    /**
     * Scrolls to the first component with a validation error.
     */
    private void scrollToFirstError() {
        for (FieldValidator fv : fields) {
            if (erroneousFields.contains(fv.getComponent())) {
                JComponent comp = fv.getComponent();
                // Find enclosing JScrollPane and scroll to component
                Container parent = comp.getParent();
                while (parent != null) {
                    if (parent instanceof JScrollPane sp) {
                        // Scroll to make the component visible
                        Rectangle bounds = SwingUtilities.convertRectangle(
                                comp.getParent(), comp.getBounds(),
                                sp.getViewport().getView());
                        if (sp.getViewport().getView() instanceof JComponent view) {
                            view.scrollRectToVisible(bounds);
                        }
                        break;
                    }
                    parent = parent.getParent();
                }

                // Request focus on the first error field
                comp.requestFocusInWindow();
                break;
            }
        }
    }
}
