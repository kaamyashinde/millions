package model.learning.content;


import util.Validator;

/**
 * A broad topic grouping for learning content.
 *
 * @param id          unique identifier (e.g. {@code "basics"})
 * @param name        display name shown in the UI
 * @param description one-sentence summary shown on the category card
 * @param emoji       single emoji used as a visual icon
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public record LearningCategory(String id, String name, String description, String emoji) {

  /** Validates that no field is null. */
  public LearningCategory {
    Validator.checkNotNull(id, "id");
    Validator.checkNotNull(name, "name");
    Validator.checkNotNull(description, "description");
    Validator.checkNotNull(emoji, "emoji");
  }
}
