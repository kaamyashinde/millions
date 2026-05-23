package model.learning.quiz;


import model.learning.content.LearningItem;

import java.util.List;

import model.utils.Validator;

/**
 * A quiz linked to a single {@link LearningItem}, containing a fixed set of
 * multiple-choice {@link QuizQuestion questions}.
 *
 * @param id           unique quiz identifier
 * @param linkedItemId the {@link LearningItem#id()} this quiz belongs to
 * @param title        display title shown above the quiz
 * @param questions    ordered list of questions (minimum 1)
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public record Quiz(
    String id,
    String linkedItemId,
    String title,
    List<QuizQuestion> questions) {

  public Quiz {
    Validator.checkNotNull(id, "id");
    Validator.checkNotNull(linkedItemId, "linkedItemId");
    Validator.checkNotNull(title, "title");
    Validator.checkNotNull(questions, "questions");
    if (questions.isEmpty()) {
      throw new IllegalArgumentException("questions must not be empty");
    }
    questions = List.copyOf(questions);
  }
}
