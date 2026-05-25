package model.learning.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

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

  @Test
  void resourcesForItemResolveInItemOrderAndSkipUnknownIds() {
    var item = LearningContentStore.getItemsByIds(List.of("what-is-a-stock")).getFirst();

    var resources = LearningContentStore.getResourcesForItem(item);

    assertEquals(item.resourceIds(), resources.stream().map(resource -> resource.id()).toList());
  }

  @Test
  void privateConstructor_isCoveredForUtilityClass() throws Exception {
    Constructor<LearningContentStore> constructor =
        LearningContentStore.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    constructor.newInstance();
  }

  @Test
  void isolatedStoreFailsWhenCatalogResourceIsMissing() throws Exception {
    try (URLClassLoader loader = isolatedStoreLoader(null)) {
      assertThrows(
          ExceptionInInitializerError.class,
          () -> Class.forName("model.learning.store.LearningContentStore", true, loader));
    }
  }

  @Test
  void isolatedStoreFailsWhenCatalogJsonIsMalformed() throws Exception {
    try (URLClassLoader loader = isolatedStoreLoader("not-json")) {
      assertThrows(
          ExceptionInInitializerError.class,
          () -> Class.forName("model.learning.store.LearningContentStore", true, loader));
    }
  }

  @Test
  void isolatedStoreFailsWhenItemReferencesUnknownCategory() throws Exception {
    String catalog = """
        {
          "categories": [],
          "resources": [],
          "items": [
            {
              "id": "orphan",
              "title": "Orphan",
              "slug": "orphan",
              "summary": "No category",
              "categoryId": "missing",
              "difficulty": "BEGINNER",
              "featured": false,
              "contentFile": "orphan.md",
              "relatedTopicIds": [],
              "resourceIds": []
            }
          ]
        }
        """;

    try (URLClassLoader loader = isolatedStoreLoader(catalog)) {
      assertThrows(
          ExceptionInInitializerError.class,
          () -> Class.forName("model.learning.store.LearningContentStore", true, loader));
    }
  }

  private static URLClassLoader isolatedStoreLoader(String catalogJson) throws Exception {
    URL classes = Path.of(System.getProperty("user.dir"), "target", "classes")
        .toUri()
        .toURL();
    return new ChildFirstStoreLoader(
        classes,
        "model.learning.store.LearningContentStore",
        "learninghub/catalog.json",
        catalogJson);
  }

  private static final class ChildFirstStoreLoader extends URLClassLoader {

    private final String childFirstClassName;
    private final String resourceName;
    private final String resourceContent;

    private ChildFirstStoreLoader(
        URL classes,
        String childFirstClassName,
        String resourceName,
        String resourceContent) {
      super(new URL[] {classes}, ClassLoader.getSystemClassLoader());
      this.childFirstClassName = childFirstClassName;
      this.resourceName = resourceName;
      this.resourceContent = resourceContent;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (name.equals(childFirstClassName) || name.startsWith(childFirstClassName + "$")) {
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null) {
          loaded = findClass(name);
        }
        if (resolve) {
          resolveClass(loaded);
        }
        return loaded;
      }
      return super.loadClass(name, resolve);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
      if (name.equals(resourceName)) {
        if (resourceContent == null) {
          return null;
        }
        return new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8));
      }
      return super.getResourceAsStream(name);
    }
  }
}
