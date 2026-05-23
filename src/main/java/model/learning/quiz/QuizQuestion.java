package model.learning.quiz;


import model.learning.content.LearningResource;

import java.util.List;

import model.utils.Validator;

/**
 * A single multiple-choice question in a {@link Quiz}.
 *
 * @param id               unique identifier for the question
 * @param questionText     the question text shown to the player
 * @param answers          the list of answer options (minimum 2)
 * @param correctAnswerId  the {@link QuizAnswer#id()} of the correct answer; must exist in answers
 * @param explanationText  explanation shown after the player answers
 * @param linkedResourceId optional ID of a {@link model.learning.content.LearningResource} to surface
 *                         when the player answers incorrectly; may be {@code null}
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public record QuizQuestion(
    String id,
    String questionText,
    List<QuizAnswer> answers,
    String correctAnswerId,
    String explanationText,
    String linkedResourceId) {

  public QuizQuestion {
    Validator.checkNotNull(id, "id");
    Validator.checkNotNull(questionText, "questionText");
    Validator.checkNotNull(answers, "answers");
    Validator.checkNotNull(correctAnswerId, "correctAnswerId");
    Validator.checkNotNull(explanationText, "explanationText");
    // linkedResourceId is intentionally nullable
    if (answers.size() < 2) {
      throw new IllegalArgumentException("answers must have at least 2 options");
    }
    boolean valid = answers.stream().anyMatch(a -> a.id().equals(correctAnswerId));
    if (!valid) {
      throw new IllegalArgumentException("correctAnswerId '" + correctAnswerId + "' not found in answers list");
    }
    answers = List.copyOf(answers);
  }
}
