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
 */
public class LearningHubController {

  /**
   * @return featured learning items for the landing page
   */
  public List<LearningItem> getFeaturedItems() {
    return LearningContentStore.getFeaturedItems();
  }

  /**
   * @return all learning categories
   */
  public List<LearningCategory> getCategories() {
    return LearningContentStore.getCategories();
  }

  /**
   * @return the highlighted beginner resource
   */
  public LearningResource getStartHereResource() {
    return LearningContentStore.getResources().stream()
        .filter(r -> "res-aksjer-for-alle".equals(r.id()))
        .findFirst()
        .orElse(LearningContentStore.getResources().get(0));
  }

  /**
   * @return all external learning resources
   */
  public List<LearningResource> getResources() {
    return LearningContentStore.getResources();
  }

  /**
   * @param category category to browse
   * @return items in that category
   */
  public List<LearningItem> getItemsForCategory(LearningCategory category) {
    return LearningContentStore.getItemsByCategory(category);
  }

  /**
   * @param itemIds item identifiers
   * @return matching learning items
   */
  public List<LearningItem> getItemsByIds(List<String> itemIds) {
    return LearningContentStore.getItemsByIds(itemIds);
  }

  /**
   * @param itemId learning item identifier
   * @return matching item, if present
   */
  public Optional<LearningItem> getItemById(String itemId) {
    return LearningContentStore.getItemsByIds(List.of(itemId)).stream().findFirst();
  }

  /**
   * @param resourceId resource identifier
   * @return matching resource, if present
   */
  public Optional<LearningResource> getResourceById(String resourceId) {
    return LearningContentStore.getResources().stream()
        .filter(r -> r.id().equals(resourceId))
        .findFirst();
  }

  /**
   * @param item learning item whose linked resources are wanted
   * @return resources linked to the item
   */
  public List<LearningResource> getResourcesForItem(LearningItem item) {
    return LearningContentStore.getResourcesForItem(item);
  }

  /**
   * @param item learning item whose markdown article should be rendered
   * @return HTML body fragment for the item content file
   */
  public String getItemBodyHtml(LearningItem item) {
    return MarkdownLoader.toHtml(item.contentFile());
  }
}
