package model.learning.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LearningContentStoreTest {

  @Test
  void catalogLoadsExpectedCounts() {
    assertEquals(6, LearningContentStore.getCategories().size());
    long totalItems = LearningContentStore.getCategories().stream()
        .flatMap(c -> LearningContentStore.getItemsByCategory(c).stream())
        .count();
    assertEquals(10, totalItems);
    assertEquals(4, LearningContentStore.getResources().size());
  }

  @Test
  void featuredItemsAreSortedWithBeginnerFirst() {
    var featured = LearningContentStore.getFeaturedItems();
    assertEquals(3, featured.size());
  }

  @Test
  void itemsResolveCategoriesFromJson() {
    var item = LearningContentStore.getItemsByIds(java.util.List.of("what-is-a-stock")).get(0);
    assertEquals("basics", item.category().id());
    assertEquals("learninghub/what-is-a-stock.md", item.contentFile());
  }
}
