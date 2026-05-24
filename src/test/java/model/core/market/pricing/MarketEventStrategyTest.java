package model.core.market.pricing;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MarketEventStrategyTest {

  @Test
  void isolatedStrategyRejectsEmptyTemplateResource() throws Exception {
    InvocationTargetException thrown = assertThrows(
        InvocationTargetException.class,
        () -> invokeRandomFromResources("[]"));

    assertInstanceOf(IllegalStateException.class, thrown.getCause());
  }

  @Test
  void isolatedStrategyRejectsMalformedTemplateResource() throws Exception {
    InvocationTargetException thrown = assertThrows(
        InvocationTargetException.class,
        () -> invokeRandomFromResources("not-json"));

    assertInstanceOf(IllegalStateException.class, thrown.getCause());
  }

  private static void invokeRandomFromResources(String templatesJson) throws Exception {
    try (URLClassLoader loader = isolatedStrategyLoader(templatesJson)) {
      Class<?> strategyClass = Class.forName(
          "model.core.market.pricing.MarketEventStrategy",
          true,
          loader);
      Method method = strategyClass.getMethod("randomFromResources", double.class, String.class);
      method.invoke(null, 1.0, MarketEventStrategy.DEFAULT_TEMPLATE_RESOURCE);
    }
  }

  private static URLClassLoader isolatedStrategyLoader(String templatesJson) throws Exception {
    URL classes = Path.of(System.getProperty("user.dir"), "target", "classes")
        .toUri()
        .toURL();
    return new ChildFirstStrategyLoader(
        classes,
        "model.core.market.pricing.MarketEventStrategy",
        MarketEventStrategy.DEFAULT_TEMPLATE_RESOURCE,
        templatesJson);
  }

  private static final class ChildFirstStrategyLoader extends URLClassLoader {

    private final String childFirstClassName;
    private final String resourceName;
    private final String resourceContent;

    private ChildFirstStrategyLoader(
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
        return new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8));
      }
      return super.getResourceAsStream(name);
    }
  }
}
