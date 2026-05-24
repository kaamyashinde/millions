package util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Loads markdown content files from the classpath.
 *
 * @author kaamyashinde
 * @version 1.1.0
 * @since 04-04-2026
 */
public final class MarkdownLoader {

  private static final Parser MD_PARSER = Parser.builder().build();
  private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

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

  /**
   * Loads a markdown resource and converts it to an HTML body fragment.
   *
   * @param resourcePath classpath-relative path to the markdown file
   * @return rendered HTML body, or a fallback paragraph when content is missing
   */
  public static String toHtml(String resourcePath) {
    String markdown = load(resourcePath);
    if (markdown.isEmpty()) {
      return "<p style='color:#CBD5E1'>Content not available.</p>";
    }
    return HTML_RENDERER.render(MD_PARSER.parse(stripFrontMatter(markdown)));
  }

  /**
   * Removes a leading YAML front matter block ({@code --- ... ---}) when present.
   *
   * @param markdown raw markdown file content
   * @return markdown body without front matter
   */
  static String stripFrontMatter(String markdown) {
    if (!markdown.startsWith("---")) {
      return markdown;
    }
    int closing = markdown.indexOf("\n---", 3);
    if (closing < 0) {
      return markdown;
    }
    return markdown.substring(closing + 4).stripLeading();
  }
}
