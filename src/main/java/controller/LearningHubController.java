package controller;

import java.util.List;
import java.util.Optional;
import model.learning.content.LearningCategory;
import model.learning.content.LearningItem;
import model.learning.content.LearningResource;
import model.learning.store.LearningContentStore;
import util.MarkdownLoader;

/**
 * Supplies learning hub content and navigation helpers.
 *
 * <p>The controller reads immutable learning data from {@link LearningContentStore} and renders
 * article markdown through {@link MarkdownLoader} for the JavaFX views.
 *
 * @author kaamyashinde
 * @contributor kevindmazali
 * @version 1.0.0
 * @since 2026-05-03
 */
public class LearningHubController {

  /**
   * Creates a learning hub controller.
   */
  public LearningHubController() {
  }

  /**
   * Returns featured topics for the learning hub landing page.
   *
   * @return featured learning items for the landing page
   */
  public List<LearningItem> getFeaturedItems() {
    return LearningContentStore.getFeaturedItems();
  }

  /**
   * Returns every learning category in display order.
   *
   * @return all learning categories
   */
  public List<LearningCategory> getCategories() {
    return LearningContentStore.getCategories();
  }

  /**
   * Returns the beginner resource highlighted as the starting point.
   *
   * @return the highlighted beginner resource
   */
  public LearningResource getStartHereResource() {
    return LearningContentStore.getResources().stream()
        .filter(r -> "res-aksjer-for-alle".equals(r.id()))
        .findFirst()
        .orElse(LearningContentStore.getResources().get(0));
  }

  /**
   * Returns all curated external resources.
   *
   * @return all external learning resources
   */
  public List<LearningResource> getResources() {
    return LearningContentStore.getResources();
  }

  /**
   * Looks up the items belonging to a category.
   *
   * @param category category to browse
   * @return items in that category
   */
  public List<LearningItem> getItemsForCategory(LearningCategory category) {
    return LearningContentStore.getItemsByCategory(category);
  }

  /**
   * Looks up learning items by identifier.
   *
   * @param itemIds item identifiers
   * @return matching learning items
   */
  public List<LearningItem> getItemsByIds(List<String> itemIds) {
    return LearningContentStore.getItemsByIds(itemIds);
  }

  /**
   * Looks up a single learning item by identifier.
   *
   * @param itemId learning item identifier
   * @return matching item, if present
   */
  public Optional<LearningItem> getItemById(String itemId) {
    return LearningContentStore.getItemsByIds(List.of(itemId)).stream().findFirst();
  }

  /**
   * Looks up a single external resource by identifier.
   *
   * @param resourceId resource identifier
   * @return matching resource, if present
   */
  public Optional<LearningResource> getResourceById(String resourceId) {
    return LearningContentStore.getResources().stream()
        .filter(r -> r.id().equals(resourceId))
        .findFirst();
  }

  /**
   * Returns the resources linked from a learning item.
   *
   * @param item learning item whose linked resources are wanted
   * @return resources linked to the item
   */
  public List<LearningResource> getResourcesForItem(LearningItem item) {
    return LearningContentStore.getResourcesForItem(item);
  }

  /**
   * Renders a learning item's markdown content to HTML.
   *
   * @param item learning item whose markdown article should be rendered
   * @return HTML body fragment for the item content file
   */
  public String getItemBodyHtml(LearningItem item) {
    return MarkdownLoader.toHtml(item.contentFile());
  }
}
