package view.components.learning;

import java.util.List;
import java.util.function.Consumer;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import model.learning.content.LearningItem;
import model.learning.quiz.QuizQuestion;
import model.learning.store.LearningContentStore;
import view.theme.ThemePalette;

/**
 * Builds in-app topic and external resource links shown after a wrong quiz answer.
 */
public final class QuizWrongAnswerLinks {

  private QuizWrongAnswerLinks() {}

  /**
   * Appends hub topic and external resource cards to {@code parent} when applicable.
   *
   * @param parent          container to append to
   * @param question        answered question
   * @param linkedItemId    parent quiz topic ID ({@link model.learning.quiz.Quiz#linkedItemId()})
   * @param onOpenHubTopic  called when the user opens the in-app topic; may be null to omit hub card
   */
  public static void appendTo(
      VBox parent,
      QuizQuestion question,
      String linkedItemId,
      Consumer<LearningItem> onOpenHubTopic) {
    if (onOpenHubTopic != null && linkedItemId != null) {
      LearningContentStore.getItemsByIds(List.of(linkedItemId)).stream()
          .findFirst()
          .ifPresent(item -> {
            parent.getChildren().add(sectionLabel("Review this topic:"));
            parent.getChildren().add(
                LearningTopicCard.create(item, () -> onOpenHubTopic.accept(item)));
          });
    }

    if (question.linkedResourceId() != null) {
      LearningContentStore.getResources().stream()
          .filter(r -> r.id().equals(question.linkedResourceId()))
          .findFirst()
          .ifPresent(resource -> {
            parent.getChildren().add(sectionLabel("Review this resource:"));
            parent.getChildren().add(LearningResourceCard.create(resource));
          });
    }
  }

  private static Label sectionLabel(String text) {
    Label label = new Label(text);
    label.setStyle("-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + "; -fx-font-size: 11;");
    return label;
  }
}
