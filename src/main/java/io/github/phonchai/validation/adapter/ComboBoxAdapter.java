package io.github.phonchai.validation.adapter;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import java.awt.event.ItemEvent;

/**
 * Component adapter for {@link JComboBox}.
 *
 * <p>
 * Listens for selection changes via {@link java.awt.event.ItemListener}
 * and triggers validation when a new item is selected.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public final class ComboBoxAdapter implements ComponentAdapter {

    @Override
    public boolean supports(JComponent component) {
        return component instanceof JComboBox<?>;
    }

    @Override
    public String getValue(JComponent component) {
        if (component instanceof JComboBox<?> cb) {
            Object selected = cb.getSelectedItem();
            return selected != null ? selected.toString() : "";
        }
        return "";
    }

    @Override
    public void addChangeListener(JComponent component, Runnable onChange) {
        if (component instanceof JComboBox<?> cb) {
            cb.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED
                        || e.getStateChange() == ItemEvent.DESELECTED) {
                    onChange.run();
                }
            });
        }
    }
}
