package view.theme;

import java.net.URL;
import java.util.Arrays;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import model.learning.content.Difficulty;

/**
 * Reusable JavaFX stylesheet and style-class helpers.
 *
 * <p>AI assistance note: Claude Code was used as inspiration when planning the styling
 * organization for this helper; the final implementation was reviewed and adapted by the group.
 */
public final class ThemeStyles {

  private static final String BASE_STYLESHEET = "/css/millions.css";

  private ThemeStyles() {}

  /**
   * Installs the app's stylesheet on a scene and applies any saved theme preference.
   *
   * @param scene scene that should receive the Millions CSS stack
   */
  public static void install(Scene scene) {
    if (scene == null) {
      return;
    }
    URL resource = ThemeStyles.class.getResource(BASE_STYLESHEET);
    if (resource == null) {
      throw new IllegalStateException("Missing stylesheet resource: " + BASE_STYLESHEET);
    }
    String externalForm = resource.toExternalForm();
    if (!scene.getStylesheets().contains(externalForm)) {
      scene.getStylesheets().add(externalForm);
    }
    ThemeManager.getInstance().restorePreference(scene);
  }

  /**
   * Applies the Millions theme to a modal {@link Dialog} when its scene is created.
   *
   * @param dialog dialog whose pane should receive the stylesheet stack
   */
  public static void installOnDialog(Dialog<?> dialog) {
    if (dialog == null) {
      return;
    }
    DialogPane pane = dialog.getDialogPane();
    addStyleClasses(pane, "dialog-root");
    pane.sceneProperty()
        .addListener(
            (obs, oldScene, scene) -> {
              if (scene != null) {
                install(scene);
              }
            });
    Scene existing = pane.getScene();
    if (existing != null) {
      install(existing);
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
   * Computes a dialog dimension as a fraction of the owner window, clamped to min/max.
   *
   * @param owner parent window; uses {@code min} when null
   * @param fraction fraction of the larger owner dimension
   * @param min minimum size
   * @param max maximum size
   * @return clamped dialog dimension
   */
  public static double dialogDimension(Window owner, double fraction, double min, double max) {
    if (owner == null) {
      return min;
    }
    double base = Math.max(owner.getWidth(), owner.getHeight()) * fraction;
    return Math.max(min, Math.min(max, base));
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
   * Applies the shared destructive-action button classes to a button.
   *
   * @param button button to style
   */
  public static void styleDangerButton(Button button) {
    addStyleClasses(button, "btn", "btn-danger");
  }

  /**
   * Applies the difficulty badge style class for a learning item.
   *
   * @param badge badge label to style
   * @param difficulty item difficulty
   */
  public static void applyDifficultyBadge(Label badge, Difficulty difficulty) {
    addStyleClasses(
        badge,
        switch (difficulty) {
          case BEGINNER -> "learning-badge-beginner";
          case INTERMEDIATE -> "learning-badge-intermediate";
          case ADVANCED -> "learning-badge-advanced";
        });
  }

  /**
   * Returns the CSS class name for a one-based leaderboard rank highlight.
   *
   * @param rank one-based rank
   * @return style class name, or empty string when rank has no highlight
   */
  public static String leaderboardRowClass(int rank) {
    return switch (rank) {
      case 1 -> "leaderboard-top-one";
      case 2 -> "leaderboard-top-two";
      case 3 -> "leaderboard-top-three";
      default -> "";
    };
  }
}
