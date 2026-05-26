package model.learning.store;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import model.learning.content.Difficulty;
import model.learning.content.LearningCategory;
import model.learning.content.LearningItem;
import model.learning.content.LearningResource;
import model.learning.content.LearningResourceType;

/**
 * Central store for all Learning Hub catalog content. Loads categories, items, and resources
 * from {@code learninghub/catalog.json} on the classpath.
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 2026-04-04
 */
public final class LearningContentStore {

  private static final String CATALOG_RESOURCE = "learninghub/catalog.json";

  private static final List<LearningCategory> CATEGORIES = loadCategories();
  private static final List<LearningResource> RESOURCES = loadResources();
  private static final List<LearningItem> ITEMS = loadItems();

  private LearningContentStore() {
  }

  /**
   * Returns all six learning categories in display order.
   *
   * @return immutable list of {@link LearningCategory}
   */
  public static List<LearningCategory> getCategories() {
    return CATEGORIES;
  }

  /**
   * Returns all items belonging to the given category.
   *
   * @param category the category to filter by
   * @return immutable list of matching {@link LearningItem}s
   */
  public static List<LearningItem> getItemsByCategory(LearningCategory category) {
    return ITEMS.stream()
        .filter(item -> item.category().equals(category))
        .toList();
  }

  /**
   * Returns items marked as featured, sorted with {@link Difficulty#BEGINNER} first (by ordinal).
   *
   * @return immutable list of featured {@link LearningItem}s
   */
  public static List<LearningItem> getFeaturedItems() {
    return ITEMS.stream()
        .filter(LearningItem::featured)
        .sorted(Comparator.comparingInt(item -> item.difficulty().ordinal()))
        .toList();
  }

  /**
   * Returns all available learning resources.
   *
   * @return immutable list of {@link LearningResource}
   */
  public static List<LearningResource> getResources() {
    return RESOURCES;
  }

  /**
   * Returns the items whose {@link LearningItem#id()} appears in {@code ids},
   * preserving the order of {@code ids}. Items not found are silently skipped.
   *
   * @param ids list of item IDs to look up
   * @return list of matching {@link LearningItem}s
   */
  public static List<LearningItem> getItemsByIds(List<String> ids) {
    return ids.stream()
        .flatMap(id -> ITEMS.stream().filter(item -> item.id().equals(id)))
        .toList();
  }

  /**
   * Returns the resources linked to the given item via {@link LearningItem#resourceIds()}.
   * Resources not found are silently skipped.
   *
   * @param item the learning item whose linked resources are wanted
   * @return list of matching {@link LearningResource}s
   */
  public static List<LearningResource> getResourcesForItem(LearningItem item) {
    List<String> ids = item.resourceIds();
    return ids.stream()
        .flatMap(id -> RESOURCES.stream().filter(r -> r.id().equals(id)))
        .toList();
  }

  private static List<LearningCategory> loadCategories() {
    return List.copyOf(readCatalog().categories().stream()
        .map(c -> new LearningCategory(c.id(), c.name(), c.description(), c.emoji()))
        .toList());
  }

  private static List<LearningResource> loadResources() {
    return List.copyOf(readCatalog().resources().stream()
        .map(r -> new LearningResource(
            r.id(),
            r.title(),
            r.sourceLabel(),
            LearningResourceType.valueOf(r.type()),
            r.url(),
            r.description()))
        .toList());
  }

  private static List<LearningItem> loadItems() {
    CatalogDocument catalog = readCatalog();
    Map<String, LearningCategory> categoriesById = catalog.categories().stream()
        .map(c -> new LearningCategory(c.id(), c.name(), c.description(), c.emoji()))
        .collect(Collectors.toMap(LearningCategory::id, Function.identity(), (a, b) -> a,
            java.util.LinkedHashMap::new));

    return List.copyOf(catalog.items().stream()
        .map(raw -> toItem(raw, categoriesById))
        .toList());
  }

  private static LearningItem toItem(JsonItem raw, Map<String, LearningCategory> categoriesById) {
    LearningCategory category = categoriesById.get(raw.categoryId());
    if (category == null) {
      throw new IllegalStateException(
          "Unknown categoryId '" + raw.categoryId() + "' for item '" + raw.id() + "'");
    }
    return new LearningItem(
        raw.id(),
        raw.title(),
        raw.slug(),
        raw.summary(),
        category,
        Difficulty.valueOf(raw.difficulty()),
        raw.featured(),
        raw.contentFile(),
        List.copyOf(raw.relatedTopicIds()),
        List.copyOf(raw.resourceIds()));
  }

  private static CatalogDocument readCatalog() {
    try (InputStream input = LearningContentStore.class.getClassLoader()
        .getResourceAsStream(CATALOG_RESOURCE)) {
      if (input == null) {
        throw new IllegalStateException("Missing learning catalog resource: " + CATALOG_RESOURCE);
      }
      return new ObjectMapper().readValue(input, CatalogDocument.class);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to load learning catalog: " + CATALOG_RESOURCE,
          exception);
    }
  }

  private record CatalogDocument(
      List<JsonCategory> categories,
      List<JsonResource> resources,
      List<JsonItem> items) {
  }

  private record JsonCategory(String id, String name, String description, String emoji) {
  }

  private record JsonResource(
      String id,
      String title,
      String sourceLabel,
      String type,
      String url,
      String description) {
  }

  private record JsonItem(
      String id,
      String title,
      String slug,
      String summary,
      String categoryId,
      String difficulty,
      boolean featured,
      String contentFile,
      List<String> relatedTopicIds,
      List<String> resourceIds) {
  }
}
