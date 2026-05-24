package view.pages.learning;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ItemDetailViewTest {

  @Test
  void wrapExampleCallout_wrapsExampleSectionUntilNextHeading() {
    String html =
        """
        <h2>Explanation</h2>
        <p>Body text.</p>
        <h2>Example</h2>
        <p>Example paragraph.</p>
        <h2>Further Reading</h2>
        <ul><li>Link</li></ul>
        """;

    String wrapped = ItemDetailView.wrapExampleCallout(html);

    assertTrue(wrapped.contains("<div class=\"callout\"><h2>Example</h2>"));
    assertTrue(wrapped.contains("<p>Example paragraph.</p>"));
    assertTrue(wrapped.contains("</div>"));
    assertTrue(wrapped.indexOf("callout") < wrapped.indexOf("Further Reading"));
    assertTrue(wrapped.contains("<h2>Further Reading</h2>"));
  }
}
