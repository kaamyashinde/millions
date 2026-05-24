package view.pages.learning;

import java.util.List;
import java.util.function.Consumer;

import controller.LearningHubController;
import controller.QuizController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import model.learning.content.Difficulty;
import model.learning.content.LearningItem;
import model.learning.content.LearningResource;
import model.learning.quiz.Quiz;
import model.learning.quiz.QuizAttempt;
import util.ExternalLinkOpener;
import view.components.learning.LearningResourceCard;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Detail view for a single {@link LearningItem}. Renders the item's markdown content file in a
 * {@link WebView} with a dark theme, followed by item-specific resource cards and suggested next
 * topics.
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 04-04-2026
 */
public class ItemDetailView extends BorderPane {

  private final LearningHubController learningHub;
  private final QuizController quiz;

  /**
   * Builds the detail view for the given item.
   *
   * @param learningHub   supplies catalog and rendered article content
   * @param quiz          supplies linked quiz content
   * @param item          the learning item to display
   * @param onBack        called when the back button is clicked
   * @param onItemClicked called when a related topic card is clicked
   * @param onTakeQuiz    called with a fresh {@link QuizAttempt} when the player starts the quiz
   */
  public ItemDetailView(
      LearningHubController learningHub,
      QuizController quiz,
      LearningItem item,
      Runnable onBack,
      Consumer<LearningItem> onItemClicked,
      Consumer<QuizAttempt> onTakeQuiz) {
    this.learningHub = learningHub;
    this.quiz = quiz;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "learning-root");

    Button backBtn = new Button("← Back");
    ThemeStyles.styleButton(backBtn);
    backBtn.setOnAction(_ -> onBack.run());

    VBox topBar = new VBox(12, backBtn, buildArticleHeader(item));
    topBar.setPadding(new Insets(0, 0, 8, 0));
    setTop(topBar);

    VBox content = new VBox(16);
    content.setPadding(new Insets(0, 0, 16, 0));

    content.getChildren().add(buildMarkdownView(item));
    content.getChildren().add(buildResourcesSection(item));
    content.getChildren().add(buildRelatedTopicsSection(item, onItemClicked));
    content.getChildren().add(buildQuizSection(item, onTakeQuiz));

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);
  }

  private static VBox buildArticleHeader(LearningItem item) {
    String difficultyColor = difficultyColor(item.difficulty());

    Label difficultyBadge = new Label(item.difficulty().name());
    difficultyBadge.setStyle(
        "-fx-background-color: " + difficultyColor + "22;"
            + "-fx-text-fill: " + difficultyColor + ";");
    ThemeStyles.addStyleClasses(difficultyBadge, "learning-article-badge-difficulty");

    Label categoryBadge =
        new Label(item.category().emoji() + "  " + item.category().name());
    ThemeStyles.addStyleClasses(categoryBadge, "learning-article-badge-category");

    HBox badges = new HBox(8, difficultyBadge, categoryBadge);
    badges.setAlignment(Pos.CENTER_LEFT);
    ThemeStyles.addStyleClasses(badges, "learning-article-badges");

    Label title = new Label(item.title());
    title.setFont(Font.font("System", FontWeight.BOLD, 26));
    title.setWrapText(true);
    ThemeStyles.addStyleClasses(title, "learning-article-title");

    VBox header = new VBox(10, badges, title);
    ThemeStyles.addStyleClasses(header, "learning-article-header");
    return header;
  }

  private javafx.scene.Node buildMarkdownView(LearningItem item) {
    String bodyHtml = wrapExampleCallout(learningHub.getItemBodyHtml(item));

    String html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
          body {
            background: #0B1220;
            color: #CBD5E1;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            font-size: 14px;
            line-height: 1.75;
            margin: 0;
            padding: 4px 4px 8px;
          }
          h1 { display: none; }
          h2 {
            color: #F8FAFC;
            font-size: 1.05em;
            font-weight: 700;
            margin: 1.5em 0 0.5em;
            border: none;
            padding: 0;
          }
          h2:first-of-type { margin-top: 0; }
          h3 { color: #F8FAFC; margin-top: 1.2em; margin-bottom: 0.4em; }
          p { margin: 0.65em 0; color: #CBD5E1; }
          ul, ol { padding-left: 1.4em; margin: 0.6em 0; }
          li { margin-bottom: 6px; color: #CBD5E1; }
          li strong { color: #F8FAFC; }
          a { color: #0EA5A4; text-decoration: none; }
          a:hover { text-decoration: underline; }
          code { background: #111827; padding: 1px 5px; border-radius: 3px; font-size: 0.9em; }
          pre { background: #111827; padding: 10px; border-radius: 6px; overflow-x: auto; }
          hr { border: none; border-top: 1px solid #334155; margin: 1em 0; }
          strong { color: #F8FAFC; font-weight: 600; }
          .callout {
            background: #111827;
            border: 1px solid #334155;
            border-radius: 8px;
            padding: 14px 16px;
            margin: 1.25em 0;
          }
          .callout h2 {
            margin-top: 0;
            font-size: 1em;
            margin-bottom: 0.5em;
          }
          .callout p { margin: 0; }
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

    webView.getEngine().locationProperty().addListener((obs, oldLoc, newLoc) -> {
      if (newLoc != null && !newLoc.isEmpty() && !newLoc.startsWith("about:")) {
        webView.getEngine().loadContent(html, "text/html");
        ExternalLinkOpener.open(newLoc);
      }
    });

    return webView;
  }

  private javafx.scene.Node buildResourcesSection(LearningItem item) {
    List<LearningResource> resources = learningHub.getResourcesForItem(item);

    VBox section = new VBox(10);
    section.setPadding(new Insets(8, 0, 0, 0));

    Label heading = new Label("Further Reading");
    heading.setStyle(
        "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + ";"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 13;");
    section.getChildren().add(heading);

    if (resources.isEmpty()) {
      Label fallback = new Label(
          "Open links in the Further Reading section above to explore more.");
      fallback.setStyle(
          "-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + ";"
              + "-fx-font-size: 11;"
              + "-fx-font-style: italic;");
      fallback.setWrapText(true);
      section.getChildren().add(fallback);
    } else {
      for (LearningResource res : resources) {
        section.getChildren().add(LearningResourceCard.create(res));
      }
    }

    return section;
  }

  private javafx.scene.Node buildRelatedTopicsSection(
      LearningItem item, Consumer<LearningItem> onItemClicked) {
    List<LearningItem> related = learningHub.getItemsByIds(item.relatedTopicIds());
    if (related.isEmpty()) {
      return new Region();
    }

    VBox section = new VBox(10);
    section.setPadding(new Insets(8, 0, 0, 0));

    Label heading = new Label("Suggested Next Topics");
    heading.setStyle(
        "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + ";"
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
    title.setStyle("-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + "; -fx-font-weight: bold;");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    summary.setStyle("-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + "; -fx-font-size: 11;");
    summary.setWrapText(true);

    Label category = new Label(item.category().emoji() + "  " + item.category().name());
    category.setStyle("-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + "; -fx-font-size: 10;");

    VBox card = new VBox(6, badge, title, summary, category);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    card.setStyle(
        "-fx-background-color: " + ThemePalette.SURFACE + ";"
            + "-fx-border-color: " + ThemePalette.ACCENT + ";"
            + "-fx-border-radius: 8;"
            + "-fx-background-radius: 8;"
            + "-fx-cursor: hand;");
    card.setOnMouseClicked(_ -> onItemClicked.accept(item));
    return card;
  }

  private javafx.scene.Node buildQuizSection(
      LearningItem item, Consumer<QuizAttempt> onTakeQuiz) {
    return quiz.getQuizForItem(item.id())
        .map(found -> buildQuizSectionContent(found, onTakeQuiz))
        .orElseGet(Region::new);
  }

  private static javafx.scene.Node buildQuizSectionContent(
      Quiz quiz, Consumer<QuizAttempt> onTakeQuiz) {
    Button quizBtn = new Button("Test your knowledge →");
    quizBtn.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(quizBtn, Priority.ALWAYS);
    ThemeStyles.addStyleClasses(quizBtn, "learning-article-quiz-btn");
    quizBtn.setOnAction(_ -> onTakeQuiz.accept(new QuizAttempt(quiz)));

    VBox section = new VBox(quizBtn);
    section.setPadding(new Insets(16, 0, 0, 0));
    return section;
  }

  /**
   * Wraps the Example section in a styled callout container for card-like presentation.
   *
   * @param html rendered markdown body HTML
   * @return HTML with the Example block wrapped in {@code div.callout}
   */
  static String wrapExampleCallout(String html) {
    if (html == null) {
      return null;
    }
    String marker = "<h2>Example</h2>";
    int start = html.indexOf(marker);
    if (start < 0) {
      return html;
    }
    int nextHeading = html.indexOf("<h2>", start + marker.length());
    String exampleBlock =
        nextHeading >= 0 ? html.substring(start, nextHeading) : html.substring(start);
    String wrapped = "<div class=\"callout\">" + exampleBlock + "</div>";
    return html.substring(0, start) + wrapped + (nextHeading >= 0 ? html.substring(nextHeading) : "");
  }

  private static String difficultyColor(Difficulty d) {
    return switch (d) {
      case BEGINNER -> ThemePalette.DIFFICULTY_BEGINNER;
      case INTERMEDIATE -> ThemePalette.DIFFICULTY_INTERMEDIATE;
      case ADVANCED -> ThemePalette.DIFFICULTY_ADVANCED;
    };
  }
}
