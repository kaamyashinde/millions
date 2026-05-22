package util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads markdown content files from the classpath.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public final class MarkdownLoader {

  private MarkdownLoader() {
  }

  /**
   * Reads a classpath resource and returns its content as a UTF-8 string.
   *
   * @param resourcePath classpath-relative path (e.g. {@code "learninghub/what-is-a-stock.md"})
   * @return file content, or an empty string if the resource cannot be found or read
   */
  public static String load(String resourcePath) {
    try (InputStream in = MarkdownLoader.class.getClassLoader()
        .getResourceAsStream(resourcePath)) {
      if (in == null) {
        return "";
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      return "";
    }
  }
}
