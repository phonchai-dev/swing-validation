package io.github.phonchai.validation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for managing localization and fetching error messages.
 * Uses ResourceBundle to load messages from .properties files.
 */
public class Localization {

    private static final String BUNDLE_NAME = "io.github.phonchai.validation.messages";
    private static ResourceBundle bundle;

    static {
        // Default to English initially
        setLocale(Locale.ENGLISH);
    }

    /**
     * Sets the global locale for validation messages.
     *
     * @param locale the locale to set (e.g., Locale.ENGLISH, or new Locale("th",
     *               "TH"))
     */
    public static void setLocale(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        } catch (Exception e) {
            // Fallback to English if specified locale is missing
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
        }
    }

    /**
     * Gets a localized message by key.
     *
     * @param key the message key
     * @return the localized string
     */
    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "???" + key + "???";
        }
    }

    /**
     * Gets a localized message by key and formats it with arguments.
     *
     * @param key  the message key
     * @param args arguments for MessageFormat
     * @return the formatted localized string
     */
    public static String get(String key, Object... args) {
        try {
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return "???" + key + "???";
        }
    }
}
