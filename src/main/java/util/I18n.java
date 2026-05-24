package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Loads UI strings from classpath {@code messages*.properties} using UTF-8. Use {@link #get} for
 * plain strings and {@link #format} for {@link MessageFormat} patterns with {@code {0}},
 * {@code {1}}, etc.
 */
public final class I18n {

  private static final String BASE_NAME = "messages";
  private static Locale locale = Locale.getDefault();
  private static ResourceBundle bundle = loadBundle();

  private I18n() {
  }

  private static ResourceBundle loadBundle() {
    return ResourceBundle.getBundle(
        BASE_NAME, locale, I18n.class.getClassLoader(), new Utf8Control());
  }

  /**
   * Sets the locale for subsequent lookups and reloads the bundle.
   *
   * @param newLocale locale to use, or {@code null} to use {@link Locale#getDefault()}
   */
  public static void setLocale(Locale newLocale) {
    locale = newLocale != null ? newLocale : Locale.getDefault();
    bundle = loadBundle();
  }

  /**
   * Returns the locale used for message bundle lookups.
   *
   * @return the locale currently used for lookups
   */
  public static Locale getLocale() {
    return locale;
  }

  /**
   * Looks up a string by key.
   *
   * @param key bundle key
   * @return the translated string, or the key if missing
   */
  public static String get(String key) {
    try {
      return bundle.getString(key);
    } catch (MissingResourceException _) {
      return key;
    }
  }

  /**
   * Looks up a pattern and formats it with {@link MessageFormat}.
   *
   * @param key  bundle key
   * @param args format arguments
   * @return formatted string, or the key if the key is missing
   */
  public static String format(String key, Object... args) {
    String pattern;
    try {
      pattern = bundle.getString(key);
    } catch (MissingResourceException _) {
      return key;
    }
    return MessageFormat.format(pattern, args);
  }

  private static final class Utf8Control extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(String baseName, Locale loc, String format,
        ClassLoader loader, boolean reload)
        throws IllegalAccessException, InstantiationException, IOException {
      if (!"java.properties".equals(format)) {
        return super.newBundle(baseName, loc, format, loader, reload);
      }
      String bundleName = toBundleName(baseName, loc);
      String resourceName = toResourceName(bundleName, "properties");
      try (InputStream stream = loader.getResourceAsStream(resourceName)) {
        if (stream == null) {
          return null;
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
          return new PropertyResourceBundle(reader);
        }
      }
    }
  }
}
