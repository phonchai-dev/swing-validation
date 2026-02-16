package io.github.phonchai.validation.adapter;

import javax.swing.JComponent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for {@link ComponentAdapter} instances.
 *
 * <p>
 * Maintains an ordered list of adapters; the first adapter whose
 * {@link ComponentAdapter#supports(JComponent)} returns {@code true}
 * for a given component will be used.
 * </p>
 *
 * <p>
 * Built-in adapters for {@code JTextComponent} and {@code JComboBox}
 * are pre-registered. Custom adapters registered via
 * {@link #register(ComponentAdapter)}
 * are checked <em>before</em> built-in ones, allowing overrides.
 * </p>
 *
 * <p>
 * <b>Thread safety:</b> This class is thread-safe; adapters are stored
 * in a {@link CopyOnWriteArrayList}.
 * </p>
 *
 * @author phonchai
 * @since 1.0.0
 */
public final class ComponentAdapterRegistry {

    /** Singleton instance. */
    private static final ComponentAdapterRegistry INSTANCE = new ComponentAdapterRegistry();

    /**
     * Custom adapters registered by the user (checked first).
     */
    private final List<ComponentAdapter> customAdapters = new CopyOnWriteArrayList<>();

    /**
     * Built-in adapters (checked after custom ones).
     */
    private final List<ComponentAdapter> builtInAdapters;

    private ComponentAdapterRegistry() {
        builtInAdapters = List.of(
                new TextComponentAdapter(),
                new ComboBoxAdapter());
    }

    /**
     * Returns the global singleton registry.
     *
     * @return the registry instance
     */
    public static ComponentAdapterRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a custom component adapter.
     *
     * <p>
     * Custom adapters are checked <em>before</em> built-in adapters,
     * so they can override default behavior for specific component types.
     * </p>
     *
     * @param adapter the adapter to register
     * @throws NullPointerException if adapter is null
     */
    public void register(ComponentAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter must not be null");
        customAdapters.add(adapter);
    }

    /**
     * Finds the first adapter that supports the given component.
     *
     * <p>
     * Custom adapters are checked first, then built-in adapters.
     * </p>
     *
     * @param component the component to find an adapter for
     * @return the matching adapter, or {@code null} if none found
     */
    public ComponentAdapter findAdapter(JComponent component) {
        // Check custom adapters first (user overrides)
        for (ComponentAdapter adapter : customAdapters) {
            if (adapter.supports(component)) {
                return adapter;
            }
        }
        // Then built-in adapters
        for (ComponentAdapter adapter : builtInAdapters) {
            if (adapter.supports(component)) {
                return adapter;
            }
        }
        return null;
    }

    /**
     * Clears all custom-registered adapters.
     * Built-in adapters are not affected.
     */
    public void clearCustomAdapters() {
        customAdapters.clear();
    }
}
