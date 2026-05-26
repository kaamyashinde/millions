package model.learning.quiz;


import util.Validator;

/**
 * An answer option for a {@link QuizQuestion}.
 *
 * @param id   short identifier, e.g. {@code "a"}, {@code "b"}, {@code "c"}, {@code "d"}
 * @param text the display label shown on the answer button
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public record QuizAnswer(String id, String text) {

  /**
   * Validates answer fields.
   */
  public QuizAnswer {
    Validator.checkNotNull(id, "id");
    Validator.checkNotNull(text, "text");
  }
}
