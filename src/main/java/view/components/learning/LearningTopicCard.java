package view.components.learning;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.learning.content.LearningItem;
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
    Label badge = new Label(item.difficulty().name());
    ThemeStyles.applyDifficultyBadge(badge, item.difficulty());

    Label title = new Label(item.title());
    ThemeStyles.addStyleClasses(title, "learning-card-title");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    ThemeStyles.addStyleClasses(summary, "learning-card-summary");
    summary.setWrapText(true);

    Label cta = new Label("Read in Learning Hub →");
    ThemeStyles.addStyleClasses(cta, "quiz-cta");

    VBox card = new VBox(6, badge, title, summary, cta);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.addStyleClasses(card, "learning-card-accent");
    card.setOnMouseClicked(unused -> onOpen.run());
    return card;
  }
}
