package view.theme;

import java.net.URL;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

/**
 * Manages dark/light theme selection and applies the light override stylesheet.
 *
 * <p>AI assistance note: Cursor was used as inspiration when planning the styling organization
 * for this manager; the final implementation was reviewed and adapted by the group.
 */
public final class ThemeManager {

  /** Available UI color themes. */
  public enum Theme {
    /** Dark theme. */
    DARK,
    /** Light theme. */
    LIGHT
  }

  private static final ThemeManager INSTANCE = new ThemeManager();
  private static final String PREFERENCES_NODE = "millions";
  private static final String PREFERENCES_KEY = "theme";
  private static final String LIGHT_STYLESHEET = "/css/millions-light.css";

  private final SimpleObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.DARK);
  private String lightStylesheetUrl;

  private ThemeManager() {}

  /**
   * Returns the shared theme manager instance.
   *
   * @return singleton instance
   */
  public static ThemeManager getInstance() {
    return INSTANCE;
  }

  /**
   * Returns the active theme.
   *
   * @return current theme
   */
  public Theme getTheme() {
    return theme.get();
  }

  /**
   * Exposes the active theme as a read-only property for UI bindings.
   *
   * @return read-only theme property
   */
  public ReadOnlyObjectProperty<Theme> themeProperty() {
    return theme;
  }

  /**
   * Restores the saved theme preference and applies it to the scene.
   *
   * @param scene scene to theme
   */
  public void restorePreference(Scene scene) {
    Theme saved = readSavedTheme();
    setTheme(scene, saved);
  }

  /**
   * Applies the given theme to the scene and updates the active theme property.
   *
   * @param scene scene whose stylesheet stack should be updated
   * @param selected theme to apply
   */
  public void setTheme(Scene scene, Theme selected) {
    if (scene == null || selected == null) {
      return;
    }
    theme.set(selected);
    String lightSheet = lightStylesheetUrl();
    if (selected == Theme.LIGHT) {
      if (!scene.getStylesheets().contains(lightSheet)) {
        scene.getStylesheets().add(lightSheet);
      }
    } else {
      scene.getStylesheets().remove(lightSheet);
    }
  }

  /**
   * Toggles between dark and light themes on the given scene.
   *
   * @param scene scene to update
   */
  public void toggle(Scene scene) {
    Theme next = getTheme() == Theme.DARK ? Theme.LIGHT : Theme.DARK;
    setTheme(scene, next);
    savePreference(next);
  }

  /**
   * Persists the active theme preference.
   *
   * @param selected theme to save
   */
  public void savePreference(Theme selected) {
    preferences().put(PREFERENCES_KEY, selected.name());
  }

  private Theme readSavedTheme() {
    String value = preferences().get(PREFERENCES_KEY, Theme.DARK.name());
    try {
      return Theme.valueOf(value);
    } catch (IllegalArgumentException exception) {
      return Theme.DARK;
    }
  }

  private static Preferences preferences() {
    return Preferences.userRoot().node(PREFERENCES_NODE);
  }

  private String lightStylesheetUrl() {
    if (lightStylesheetUrl == null) {
      URL resource = ThemeManager.class.getResource(LIGHT_STYLESHEET);
      if (resource == null) {
        throw new IllegalStateException("Missing stylesheet resource: " + LIGHT_STYLESHEET);
      }
      lightStylesheetUrl = resource.toExternalForm();
    }
    return lightStylesheetUrl;
  }
}
