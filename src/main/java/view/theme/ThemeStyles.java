package view.theme;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Reusable inline JavaFX style helpers for the light theme.
 */
public final class ThemeStyles {

  private ThemeStyles() {}

  public static String cardStyle() {
    return "-fx-background-color: " + ThemePalette.SURFACE + ";"
        + "-fx-border-color: " + ThemePalette.BORDER + ";"
        + "-fx-background-radius: 12;"
        + "-fx-border-radius: 12;"
        + "-fx-padding: 14;";
  }

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

  public static void styleField(TextField field) {
    field.setStyle(fieldStyle());
  }

  public static void styleButton(Button button) {
    button.setStyle(
        "-fx-background-color: " + ThemePalette.BACKGROUND + ";"
            + "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + ";"
            + "-fx-border-color: " + ThemePalette.BORDER + ";"
            + "-fx-border-width: 1;"
            + "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;"
            + "-fx-padding: 8 16;");
  }

  public static void styleAccentButton(Button button) {
    button.setStyle(
        "-fx-background-color: " + ThemePalette.ACCENT + ";"
            + "-fx-text-fill: #ffffff;"
            + "-fx-font-size: 14px;"
            + "-fx-font-weight: bold;"
            + "-fx-background-radius: 6;"
            + "-fx-border-radius: 6;"
            + "-fx-cursor: hand;"
            + "-fx-padding: 10 16;");
  }

  public static String workspaceBackground() {
    return "-fx-background-color: " + ThemePalette.BACKGROUND + ";";
  }

  public static String headingText() {
    return "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + "; -fx-font-weight: bold;";
  }

  public static String mutedText() {
    return "-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + ";";
  }
}
