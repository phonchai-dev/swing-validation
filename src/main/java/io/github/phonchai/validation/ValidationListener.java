package io.github.phonchai.validation;

/**
 * Listener interface for receiving notifications when the overall
 * form validation state changes.
 *
 * <p>
 * Typical usage is to enable/disable a submit button based on
 * whether all fields are valid:
 * </p>
 * 
 * <pre>{@code
 * validator.onValidationChanged(allValid -> btnSubmit.setEnabled(allValid));
 * }</pre>
 *
 * @author phonchai
 * @since 1.0.0
 */
@FunctionalInterface
public interface ValidationListener {

    /**
     * Called when the overall validation state of the form changes.
     *
     * @param allValid {@code true} if all registered fields are currently valid,
     *                 {@code false} if any field has a validation error
     */
    void onValidationChanged(boolean allValid);
}
