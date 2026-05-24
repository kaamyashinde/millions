package model.learning.content;


import util.Validator;

/**
 * An external learning resource (article, video, or download) linked from the Learning Hub.
 *
 * @param id          unique identifier (e.g. {@code "res-aksjer-for-alle"})
 * @param title       display title
 * @param sourceLabel name of the publisher or platform (e.g. {@code "Oslo Børs"})
 * @param type        content format
 * @param url         fully-qualified URL to open in a browser or WebView
 * @param description one-sentence teaser shown on the resource card
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public record LearningResource(
    String id,
    String title,
    String sourceLabel,
    LearningResourceType type,
    String url,
    String description) {

  /** Validates that all fields are non-null. */
  public LearningResource {
    Validator.checkNotNull(id, "id");
    Validator.checkNotNull(title, "title");
    Validator.checkNotNull(sourceLabel, "sourceLabel");
    Validator.checkNotNull(type, "type");
    Validator.checkNotNull(url, "url");
    Validator.checkNotNull(description, "description");
  }
}
