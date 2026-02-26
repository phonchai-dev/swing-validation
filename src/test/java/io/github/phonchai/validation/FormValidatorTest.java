package io.github.phonchai.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.*;

class FormValidatorTest {

    private FormValidator validator;
    private JTextField textField;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            validator = new FormValidator();
            textField = new JTextField();
            validator.field(textField).required().minLength(3);
        });
    }

    @Test
    void testValidateOnInput_Disabled_DoesNotShowErrorInitially() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            validator.setValidateOnInput(false);

            // Trigger an event (typing or programmatic text change)
            textField.setText("a");

            // Since validate() was never called and validateOnInput is false,
            // the error should be suppressed before first submit to hide warnings on empty
            // forms.
            assertFalse(validator.getErroneousFields().contains(textField),
                    "Error should be suppressed before first submit when validateOnInput=false");
        });
    }

    @Test
    void testValidateOnInput_Enabled_ShowsErrorInitially() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            validator.setValidateOnInput(true);

            // Trigger an event (typing or programmatic text change)
            textField.setText("a");

            // Since validateOnInput is true, it should validate immediately and show the
            // error for minLength(3)
            assertTrue(validator.getErroneousFields().contains(textField),
                    "Error should be shown immediately with validateOnInput=true");

            // But if we fix it, it should go away
            textField.setText("abcd");
            assertFalse(validator.getErroneousFields().contains(textField),
                    "Error should be cleared when input becomes valid");
        });
    }

    @Test
    void testValidateSingleField_ShowsErrorAfterExplicitValidate() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Default behavior is disabled
            validator.setValidateOnInput(false);

            // explicit validate triggers the hasBeenTriggered state
            validator.validate();

            assertTrue(validator.getErroneousFields().contains(textField),
                    "Error should be shown after explicit validate()");

            // Modifying text
            textField.setText("a");
            assertTrue(validator.getErroneousFields().contains(textField),
                    "Error should remain if input is still invalid");

            // Fix it completely
            textField.setText("abc");
            assertFalse(validator.getErroneousFields().contains(textField),
                    "Error should be removed");
        });
    }

    @Test
    void testValidateSingleField_ShowsErrorAfterBlur() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            validator.setValidateOnInput(false);

            // 1. Type something invalid (doesn't show error because not touched and not
            // submitted)
            textField.setText("a");
            assertFalse(validator.getErroneousFields().contains(textField),
                    "Error should not be shown while typing for the first time");

            // 2. Simulate Blur event manually on the component
            for (java.awt.event.FocusListener fl : textField.getFocusListeners()) {
                fl.focusLost(new java.awt.event.FocusEvent(textField, java.awt.event.FocusEvent.FOCUS_LOST));
            }

            // 3. Now it should be marked as touched and show the error automatically
            assertTrue(validator.getErroneousFields().contains(textField),
                    "Error should be shown after field loses focus (onBlur)");

            // 4. Fixing it should clear the error immediately
            textField.setText("abc");
            assertFalse(validator.getErroneousFields().contains(textField),
                    "Error should clear immediately after fixing a touched field");
        });
    }
}
