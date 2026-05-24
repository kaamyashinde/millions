package util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MarkdownLoaderTest {

  @Test
  void stripFrontMatter_removesYamlBlock() {
    String markdown =
        """
        ---
        title: How Stock Prices Move
        slug: how-stock-prices-move
        difficulty: BEGINNER
        ---

        ## Summary
        Stock prices change constantly.
        """;

    String body = MarkdownLoader.stripFrontMatter(markdown);

    assertFalse(body.contains("title: How Stock Prices Move"));
    assertTrue(body.startsWith("## Summary"));
  }

  @Test
  void stripFrontMatter_leavesMarkdownWithoutFrontMatterUnchanged() {
    String markdown = "## Summary\n\nHello.";

    assertTrue(MarkdownLoader.stripFrontMatter(markdown).startsWith("## Summary"));
  }
}
