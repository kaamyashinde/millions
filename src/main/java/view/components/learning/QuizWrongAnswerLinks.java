package view.components.learning;

import java.util.function.Consumer;

import controller.LearningHubController;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.learning.content.LearningItem;
import model.learning.quiz.QuizQuestion;
import view.theme.ThemeStyles;

/**
 * Builds in-app topic and external resource links shown after a wrong quiz answer.
 */
public final class QuizWrongAnswerLinks {

  private QuizWrongAnswerLinks() {}

  /**
   * Appends hub topic and external resource cards to {@code parent} when applicable.
   *
   * @param parent          container to append to
   * @param learningHub     supplies linked topic and resource lookups
   * @param question        answered question
   * @param linkedItemId    parent quiz topic ID ({@link model.learning.quiz.Quiz#linkedItemId()})
   * @param onOpenHubTopic  called when the user opens the in-app topic; may be null to omit hub card
   */
  public static void appendTo(
      VBox parent,
      LearningHubController learningHub,
      QuizQuestion question,
      String linkedItemId,
      Consumer<LearningItem> onOpenHubTopic) {
    if (onOpenHubTopic != null && linkedItemId != null) {
      learningHub.getItemById(linkedItemId).ifPresent(item -> {
        parent.getChildren().add(sectionLabel("Review this topic:"));
        parent.getChildren().add(
            LearningTopicCard.create(item, () -> onOpenHubTopic.accept(item)));
      });
    }

    if (question.linkedResourceId() != null) {
      learningHub.getResourceById(question.linkedResourceId()).ifPresent(resource -> {
        parent.getChildren().add(sectionLabel("Review this resource:"));
        parent.getChildren().add(LearningResourceCard.create(resource));
      });
    }
  }

  private static Label sectionLabel(String text) {
    Label label = new Label(text);
    ThemeStyles.addStyleClasses(label, "text-small-secondary");
    return label;
  }
}
