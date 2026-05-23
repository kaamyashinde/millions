package controller;

import java.util.List;
import model.learning.content.LearningCategory;
import model.learning.store.LearningContentStore;
import model.learning.content.LearningItem;
import model.learning.content.LearningResource;

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
}
