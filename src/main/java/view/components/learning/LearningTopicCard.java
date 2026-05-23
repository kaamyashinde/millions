package view.components.learning;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import model.learning.content.Difficulty;
import model.learning.content.LearningItem;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Clickable card for an in-app {@link LearningItem} topic.
 */
public final class LearningTopicCard {

  private LearningTopicCard() {}

  /**
   * Builds a card that invokes {@code onOpen} when clicked.
   *
   * @param item   hub topic to represent
   * @param onOpen action when the user opens the topic
   * @return clickable card node
   */
  public static VBox create(LearningItem item, Runnable onOpen) {
    String badgeColor = difficultyColor(item.difficulty());

    Label badge = new Label(item.difficulty().name());
    badge.setStyle(
        "-fx-background-color: " + badgeColor + "22;"
            + "-fx-text-fill: " + badgeColor + ";"
            + "-fx-background-radius: 4;"
            + "-fx-padding: 2 6 2 6;"
            + "-fx-font-size: 10;");

    Label title = new Label(item.title());
    ThemeStyles.addStyleClasses(title, "learning-card-title");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    ThemeStyles.addStyleClasses(summary, "learning-card-summary");
    summary.setWrapText(true);

    Label cta = new Label("Read in Learning Hub →");
    cta.setStyle("-fx-text-fill: " + ThemePalette.ACCENT + "; -fx-font-size: 11;");

    VBox card = new VBox(6, badge, title, summary, cta);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.addStyleClasses(card, "learning-card-accent");
    card.setOnMouseClicked(_ -> onOpen.run());
    return card;
  }

  private static String difficultyColor(Difficulty difficulty) {
    return switch (difficulty) {
      case BEGINNER -> ThemePalette.DIFFICULTY_BEGINNER;
      case INTERMEDIATE -> ThemePalette.DIFFICULTY_INTERMEDIATE;
      case ADVANCED -> ThemePalette.DIFFICULTY_ADVANCED;
    };
  }
}
