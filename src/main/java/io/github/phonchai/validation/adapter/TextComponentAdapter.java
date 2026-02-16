package io.github.phonchai.validation.adapter;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Component adapter for {@link JTextComponent} and its subclasses
 * ({@code JTextField}, {@code JTextArea}, {@code JEditorPane}, etc.).
 *
 * <p>
 * Listens for text changes via {@link DocumentListener} and triggers
 * real-time validation on every insert, remove, or attribute change.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public final class TextComponentAdapter implements ComponentAdapter {

    @Override
    public boolean supports(JComponent component) {
        return component instanceof JTextComponent;
    }

    @Override
    public String getValue(JComponent component) {
        if (component instanceof JTextComponent tc) {
            return tc.getText().trim();
        }
        return "";
    }

    @Override
    public void addChangeListener(JComponent component, Runnable onChange) {
        if (component instanceof JTextComponent tc) {
            tc.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    onChange.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    onChange.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    onChange.run();
                }
            });
        }
    }
}
