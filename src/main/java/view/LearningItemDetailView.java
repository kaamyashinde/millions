package view;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import model.learninghub.Difficulty;
import model.learninghub.LearningContentStore;
import model.learninghub.LearningItem;
import model.learninghub.LearningResource;
import util.MarkdownLoader;

/**
 * Detail view for a single {@link LearningItem}. Renders the item's markdown content file in a
 * {@link WebView} with a dark theme, followed by item-specific resource cards and suggested next
 * topics.
 *
 * @author kaamyashinde
 * @version 1.2.0
 * @since 04-04-2026
 */
public class LearningItemDetailView extends BorderPane {

  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_BORDER_ACCENT = "#2196F3";
  private static final String COLOR_BORDER_RESOURCE = "#4CAF50";
  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";
  private static final String COLOR_DIFFICULTY_BEGINNER = "#4CAF50";
  private static final String COLOR_DIFFICULTY_INTERMEDIATE = "#FFA500";
  private static final String COLOR_DIFFICULTY_ADVANCED = "#FF4444";

  private static final Parser MD_PARSER = Parser.builder().build();
  private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

  /**
   * Builds the detail view for the given item.
   *
   * @param item          the learning item to display
   * @param onBack        called when the back button is clicked
   * @param onItemClicked called when a related topic card is clicked
   */
  public LearningItemDetailView(LearningItem item, Runnable onBack, Consumer<LearningItem> onItemClicked) {
    setPadding(new Insets(16));

    // ── TOP: back button ─────────────────────────────────────────────────────
    Button backBtn = new Button("← Back");
    backBtn.setStyle("-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    backBtn.setOnAction(_ -> onBack.run());

    HBox topBar = new HBox(backBtn);
    topBar.setPadding(new Insets(0, 0, 8, 0));
    setTop(topBar);

    // ── CENTER: WebView for markdown + resource cards + related topics ────────
    VBox content = new VBox(16);
    content.setPadding(new Insets(0, 0, 16, 0));

    content.getChildren().add(buildMarkdownView(item));
    content.getChildren().add(buildResourcesSection(item));
    content.getChildren().add(buildRelatedTopicsSection(item, onItemClicked));

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

  private static javafx.scene.Node buildResourcesSection(LearningItem item) {
    List<LearningResource> resources = LearningContentStore.getResourcesForItem(item);

    VBox section = new VBox(10);
    section.setPadding(new Insets(8, 0, 0, 0));

    Label heading = new Label("Further Reading");
    heading.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");
    section.getChildren().add(heading);

    if (resources.isEmpty()) {
      Label fallback = new Label(
          "Open links in the Further Reading section above to explore more.");
      fallback.setStyle(
          "-fx-text-fill: " + COLOR_SUBTITLE + ";"
          + "-fx-font-size: 11;"
          + "-fx-font-style: italic;");
      fallback.setWrapText(true);
      section.getChildren().add(fallback);
    } else {
      for (LearningResource res : resources) {
        section.getChildren().add(buildResourceCard(res));
      }
    }

    return section;
  }

  private static javafx.scene.Node buildResourceCard(LearningResource resource) {
    Label sourceLabel = new Label(resource.sourceLabel());
    sourceLabel.setStyle(
        "-fx-background-color: " + COLOR_BORDER_RESOURCE + "22;"
        + "-fx-text-fill: " + COLOR_BORDER_RESOURCE + ";"
        + "-fx-background-radius: 4;"
        + "-fx-padding: 2 6 2 6;"
        + "-fx-font-size: 10;");

    Label title = new Label(resource.title());
    title.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");

    Label desc = new Label(resource.description());
    desc.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");
    desc.setWrapText(true);

    Label cta = new Label("Open →");
    cta.setStyle("-fx-text-fill: " + COLOR_BORDER_RESOURCE + "; -fx-font-size: 11;");

    VBox card = new VBox(6, sourceLabel, title, desc, cta);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + COLOR_BORDER_RESOURCE + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;"
        + "-fx-cursor: hand;");
    card.setOnMouseClicked(_ -> openUrl(resource.url()));
    return card;
  }

  private static javafx.scene.Node buildRelatedTopicsSection(
      LearningItem item, Consumer<LearningItem> onItemClicked) {
    List<LearningItem> related = LearningContentStore.getItemsByIds(item.relatedTopicIds());
    if (related.isEmpty()) {
      return new Region();
    }

    VBox section = new VBox(10);
    section.setPadding(new Insets(8, 0, 0, 0));

    Label heading = new Label("Suggested Next Topics");
    heading.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");
    section.getChildren().add(heading);

    for (LearningItem relatedItem : related) {
      section.getChildren().add(buildRelatedTopicCard(relatedItem, onItemClicked));
    }

    return section;
  }

  private static javafx.scene.Node buildRelatedTopicCard(
      LearningItem item, Consumer<LearningItem> onItemClicked) {
    String badgeColor = difficultyColor(item.difficulty());

    Label badge = new Label(item.difficulty().name());
    badge.setStyle(
        "-fx-background-color: " + badgeColor + "22;"
        + "-fx-text-fill: " + badgeColor + ";"
        + "-fx-background-radius: 4;"
        + "-fx-padding: 2 6 2 6;"
        + "-fx-font-size: 10;");

    Label title = new Label(item.title());
    title.setStyle("-fx-text-fill: " + COLOR_HEADING + "; -fx-font-weight: bold;");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    summary.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");
    summary.setWrapText(true);

    Label category = new Label(item.category().emoji() + "  " + item.category().name());
    category.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 10;");

    VBox card = new VBox(6, badge, title, summary, category);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + COLOR_BORDER_ACCENT + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;"
        + "-fx-cursor: hand;");
    card.setOnMouseClicked(_ -> onItemClicked.accept(item));
    return card;
  }

  private static String difficultyColor(Difficulty d) {
    return switch (d) {
      case BEGINNER -> COLOR_DIFFICULTY_BEGINNER;
      case INTERMEDIATE -> COLOR_DIFFICULTY_INTERMEDIATE;
      case ADVANCED -> COLOR_DIFFICULTY_ADVANCED;
    };
  }

  private static void openUrl(String url) {
    try {
      Desktop.getDesktop().browse(URI.create(url));
    } catch (Exception ignored) {
      // Silent fail — desktop browsing may be unavailable in some environments
    }
  }
}
