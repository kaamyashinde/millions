package model.learninghub.learn;

import java.util.List;
import model.learninghub.Difficulty;
import model.utils.Validator;

/**
 * A single educational topic that belongs to a {@link LearningCategory}.
 *
 * @param id              unique identifier (e.g. {@code "what-is-a-stock"})
 * @param title           display title
 * @param slug            URL-friendly identifier for future navigation
 * @param summary         short description shown on a card
 * @param category        the category this item belongs to
 * @param difficulty      difficulty level
 * @param featured        {@code true} if the item should appear in the "Featured Topics" row
 * @param contentFile     classpath resource path to the markdown content file (e.g.
 *                        {@code "learninghub/what-is-a-stock.md"})
 * @param relatedTopicIds IDs of related {@link LearningItem}s to suggest after reading
 * @param resourceIds     IDs of {@link LearningResource}s specifically relevant to this item
 * @author kaamyashinde
 * @version 1.2.0
 * @since 04-04-2026
 */
public record LearningItem(
    String id,
    String title,
    String slug,
    String summary,
    LearningCategory category,
    Difficulty difficulty,
    boolean featured,
    String contentFile,
    List<String> relatedTopicIds,
    List<String> resourceIds) {

  /**
   * Validates that all non-primitive fields are non-null.
   */
  public LearningItem {
    Validator.checkNotNull(id, "id");
    Validator.checkNotNull(title, "title");
    Validator.checkNotNull(slug, "slug");
    Validator.checkNotNull(summary, "summary");
    Validator.checkNotNull(category, "category");
    Validator.checkNotNull(difficulty, "difficulty");
    Validator.checkNotNull(contentFile, "contentFile");
    Validator.checkNotNull(relatedTopicIds, "relatedTopicIds");
    Validator.checkNotNull(resourceIds, "resourceIds");
  }
}
