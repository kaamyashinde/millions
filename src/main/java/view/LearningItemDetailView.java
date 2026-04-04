package view;

import java.awt.Desktop;
import java.net.URI;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import model.learninghub.LearningItem;
import util.MarkdownLoader;

/**
 * Detail view for a single {@link LearningItem}. Renders the item's markdown content file in a
 * {@link WebView} with a dark theme, followed by the item's curated further-reading links.
 *
 * @author kaamyashinde
 * @version 1.1.0
 * @since 04-04-2026
 */
public class LearningItemDetailView extends BorderPane {

  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_BORDER_RESOURCE = "#4CAF50";
  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";

  private static final Parser MD_PARSER = Parser.builder().build();
  private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

  /**
   * Builds the detail view for the given item.
   *
   * @param item   the learning item to display
   * @param onBack called when the back button is clicked
   */
  public LearningItemDetailView(LearningItem item, Runnable onBack) {
    setPadding(new Insets(16));

    // ── TOP: back button ─────────────────────────────────────────────────────
    Button backBtn = new Button("← Back");
    backBtn.setStyle("-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    backBtn.setOnAction(_ -> onBack.run());

    HBox topBar = new HBox(backBtn);
    topBar.setPadding(new Insets(0, 0, 8, 0));
    setTop(topBar);

    // ── CENTER: WebView for markdown + further reading cards ─────────────────
    VBox content = new VBox(16);
    content.setPadding(new Insets(0, 0, 16, 0));

    content.getChildren().add(buildMarkdownView(item));
    content.getChildren().add(buildFurtherReadingSection(item));

    setCenter(content);
  }

  private static javafx.scene.Node buildMarkdownView(LearningItem item) {
    String markdown = MarkdownLoader.load(item.contentFile());
    String bodyHtml = markdown.isEmpty()
        ? "<p style='color:#9e9e9e'>Content not available.</p>"
        : HTML_RENDERER.render(MD_PARSER.parse(markdown));

    String html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
          body {
            background: #121212;
            color: #e0e0e0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            font-size: 14px;
            line-height: 1.7;
            margin: 0;
            padding: 8px 4px;
          }
          h1, h2, h3 { color: #ffffff; margin-top: 1.2em; margin-bottom: 0.4em; }
          h2 { font-size: 1.15em; border-bottom: 1px solid #2a2a2a; padding-bottom: 4px; }
          p { margin: 0.6em 0; }
          ul, ol { padding-left: 1.4em; margin: 0.6em 0; }
          li { margin-bottom: 4px; }
          a { color: #64b5f6; text-decoration: none; }
          a:hover { text-decoration: underline; }
          code { background: #1e1e1e; padding: 1px 5px; border-radius: 3px; font-size: 0.9em; }
          pre { background: #1e1e1e; padding: 10px; border-radius: 6px; overflow-x: auto; }
          hr { border: none; border-top: 1px solid #2a2a2a; margin: 1em 0; }
          strong { color: #ffffff; }
        </style>
        </head>
        <body>
        """ + bodyHtml + """
        </body>
        </html>
        """;

    WebView webView = new WebView();
    webView.getEngine().setUserStyleSheetLocation(null);
    webView.getEngine().loadContent(html, "text/html");
    webView.setPrefHeight(520);
    webView.setMaxWidth(Double.MAX_VALUE);

    // Open clicked links in the system browser instead of inside the WebView
    webView.getEngine().locationProperty().addListener((obs, oldLoc, newLoc) -> {
      if (newLoc != null && !newLoc.isEmpty() && !newLoc.startsWith("about:")) {
        webView.getEngine().loadContent(html, "text/html");
        openUrl(newLoc);
      }
    });

    return webView;
  }

  private static javafx.scene.Node buildFurtherReadingSection(LearningItem item) {
    // Parse the Further Reading links out of the markdown file directly from the rendered HTML.
    // Simpler approach: re-parse the markdown and extract the last section's links.
    // For now, show a generic "Further Reading" heading — the links are rendered inside the WebView.
    // This section is intentionally left minimal; the WebView already contains the Further Reading
    // section from the markdown content.
    Label heading = new Label("Open links above in the Further Reading section to explore more.");
    heading.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11; -fx-font-style: italic;");
    heading.setWrapText(true);
    return heading;
  }

  private static void openUrl(String url) {
    try {
      Desktop.getDesktop().browse(URI.create(url));
    } catch (Exception ignored) {
      // Silent fail — desktop browsing may be unavailable in some environments
    }
  }
}
