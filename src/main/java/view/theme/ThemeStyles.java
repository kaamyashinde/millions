package view.theme;

import java.net.URL;
import java.util.Arrays;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Reusable JavaFX stylesheet and style-class helpers.
 */
public final class ThemeStyles {

  private static final String[] STYLESHEETS = {
      "/css/base.css",
      "/css/components.css",
      "/css/layouts.css",
      "/css/pages/auth.css",
      "/css/pages/finance.css",
      "/css/pages/learning.css",
      "/css/pages/quiz.css"
  };

  private ThemeStyles() {}

  /**
   * Installs the app's stylesheets on a scene in dependency order.
   *
   * @param scene scene that should receive the Millions CSS stack
   */
  public static void install(Scene scene) {
    if (scene == null) {
      return;
    }
    for (String stylesheet : STYLESHEETS) {
      URL resource = ThemeStyles.class.getResource(stylesheet);
      if (resource == null) {
        throw new IllegalStateException("Missing stylesheet resource: " + stylesheet);
      }
      String externalForm = resource.toExternalForm();
      if (!scene.getStylesheets().contains(externalForm)) {
        scene.getStylesheets().add(externalForm);
      }
    }
  }

  /**
   * Adds style classes to a node if they are not already present.
   *
   * @param node node to classify
   * @param styleClasses style classes to add
   */
  public static void addStyleClasses(Node node, String... styleClasses) {
    if (node == null || styleClasses == null) {
      return;
    }
    Arrays.stream(styleClasses)
        .filter(styleClass -> styleClass != null && !styleClass.isBlank())
        .filter(styleClass -> !node.getStyleClass().contains(styleClass))
        .forEach(styleClass -> node.getStyleClass().add(styleClass));
  }

  /**
   * Returns the legacy inline card style for code that has not moved to CSS classes yet.
   *
   * @return inline JavaFX card style
   */
  public static String cardStyle() {
    return "-fx-background-color: " + ThemePalette.SURFACE + ";"
        + "-fx-border-color: " + ThemePalette.BORDER + ";"
        + "-fx-background-radius: 12;"
        + "-fx-border-radius: 12;"
        + "-fx-padding: 14;";
  }

  /**
   * Returns the legacy inline field style for code that has not moved to CSS classes yet.
   *
   * @return inline JavaFX field style
   */
  public static String fieldStyle() {
    return "-fx-background-color: " + ThemePalette.INPUT_BG + ";"
        + "-fx-background-radius: 6;"
        + "-fx-border-radius: 6;"
        + "-fx-border-color: " + ThemePalette.INPUT_BORDER + ";"
        + "-fx-border-width: 1;"
        + "-fx-padding: 8 12;"
        + "-fx-font-size: 14px;"
        + "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + ";";
  }

  /**
   * Applies the shared field class to a text field.
   *
   * @param field field to style
   */
  public static void styleField(TextField field) {
    addStyleClasses(field, "field");
  }

  /**
   * Applies the shared secondary button classes to a button.
   *
   * @param button button to style
   */
  public static void styleButton(Button button) {
    addStyleClasses(button, "btn", "btn-secondary");
  }

  /**
   * Applies the shared primary button classes to a button.
   *
   * @param button button to style
   */
  public static void styleAccentButton(Button button) {
    addStyleClasses(button, "btn", "btn-primary");
  }

  /**
   * Returns the legacy inline workspace background style.
   *
   * @return inline JavaFX background style
   */
  public static String workspaceBackground() {
    return "-fx-background-color: " + ThemePalette.BACKGROUND + ";";
  }

  /**
   * Returns the legacy inline heading text style.
   *
   * @return inline JavaFX heading style
   */
  public static String headingText() {
    return "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + "; -fx-font-weight: bold;";
  }

  /**
   * Returns the legacy inline muted text style.
   *
   * @return inline JavaFX muted text style
   */
  public static String mutedText() {
    return "-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + ";";
  }

  /**
   * Returns an inline row highlight style for tables that still style rows programmatically.
   *
   * @param rank one-based rank
   * @return inline JavaFX row style, or empty string when rank has no highlight
   */
  public static String leaderboardRowStyle(int rank) {
    return switch (rank) {
      case 1 -> "-fx-background-color: " + ThemePalette.TOP_ONE + ";-fx-font-weight: bold;";
      case 2 -> "-fx-background-color: " + ThemePalette.TOP_TWO + ";-fx-font-weight: bold;";
      case 3 -> "-fx-background-color: " + ThemePalette.TOP_THREE + ";-fx-font-weight: bold;";
      default -> "";
    };
  }
}
