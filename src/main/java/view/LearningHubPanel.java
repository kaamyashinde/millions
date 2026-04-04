package view;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

import javafx.geometry.Insets;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import model.learninghub.Difficulty;
import model.learninghub.LearningCategory;
import model.learninghub.LearningContentStore;
import model.learninghub.LearningItem;
import model.learninghub.LearningResource;

/**
 * Learning Hub landing page. Displays a featured-topics row, a 6-category grid, and a highlighted
 * beginner resource card. All content is sourced from {@link LearningContentStore}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class LearningHubPanel extends BorderPane {

  private javafx.scene.Node landingView;

  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_BORDER_DEFAULT = "#2a2a2a";
  private static final String COLOR_BORDER_ACCENT = "#2196F3";
  private static final String COLOR_BORDER_RESOURCE = "#4CAF50";

  private static final String COLOR_DIFFICULTY_BEGINNER = "#4CAF50";
  private static final String COLOR_DIFFICULTY_INTERMEDIATE = "#FFA500";
  private static final String COLOR_DIFFICULTY_ADVANCED = "#FF4444";

  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";

  /**
   * Builds the panel, wires up all sections from {@link LearningContentStore}.
   */
  public LearningHubPanel() {
    setPadding(new Insets(16));

    // ── TOP: heading + subtitle ──────────────────────────────────────────────
    Text heading = new Text("Learning Hub");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    heading.setStyle("-fx-fill: " + COLOR_HEADING + ";");

    Label subtitle = new Label("Build your investing knowledge, one topic at a time.");
    subtitle.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + ";");

    VBox top = new VBox(4, heading, subtitle);
    top.setPadding(new Insets(0, 0, 12, 0));
    setTop(top);

    // ── CENTER: scrollable content ───────────────────────────────────────────
    VBox content = new VBox(28);
    content.setPadding(new Insets(4, 0, 16, 0));

    content.getChildren().addAll(
        buildFeaturedSection(),
        buildCategorySection(),
        buildStartHereSection()
    );

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    this.landingView = scroll;
    setCenter(landingView);
  }

  // ── Section builders ────────────────────────────────────────────────────────

  private VBox buildFeaturedSection() {
    List<LearningItem> featured = LearningContentStore.getFeaturedItems();

    HBox cards = new HBox(12);
    for (LearningItem item : featured) {
      cards.getChildren().add(buildFeaturedCard(item));
    }

    return buildSection("Featured Topics", cards);
  }

  private VBox buildCategorySection() {
    List<LearningCategory> categories = LearningContentStore.getCategories();

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(12);

    // 3 equal columns
    for (int i = 0; i < 3; i++) {
      ColumnConstraints col = new ColumnConstraints();
      col.setHgrow(Priority.ALWAYS);
      col.setPercentWidth(33.33);
      grid.getColumnConstraints().add(col);
    }

    for (int i = 0; i < categories.size(); i++) {
      grid.add(buildCategoryCard(categories.get(i)), i % 3, i / 3);
    }

    return buildSection("Browse by Category", grid);
  }

  private VBox buildStartHereSection() {
    LearningResource resource = LearningContentStore.getResources().stream()
        .filter(r -> "res-aksjer-for-alle".equals(r.id()))
        .findFirst()
        .orElse(LearningContentStore.getResources().get(0));

    return buildSection("Start Here", buildResourceCard(resource));
  }

  // ── Card builders ────────────────────────────────────────────────────────────

  private javafx.scene.Node buildFeaturedCard(LearningItem item) {
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
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
            + "-fx-border-color: " + COLOR_BORDER_ACCENT + ";"
            + "-fx-border-radius: 8;"
            + "-fx-background-radius: 8;"
            + "-fx-cursor: hand;");
    HBox.setHgrow(card, Priority.ALWAYS);

    card.setOnMouseClicked(_ -> onFeaturedCardClicked(item));
    return card;
  }

  private javafx.scene.Node buildCategoryCard(LearningCategory category) {
    Label emoji = new Label(category.emoji());
    emoji.setStyle("-fx-font-size: 22;");

    Label name = new Label(category.name());
    name.setStyle("-fx-text-fill: " + COLOR_HEADING + "; -fx-font-weight: bold;");
    name.setWrapText(true);

    Label desc = new Label(category.description());
    desc.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");
    desc.setWrapText(true);

    VBox card = new VBox(6, emoji, name, desc);
    card.setPadding(new Insets(12));
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
            + "-fx-border-color: " + COLOR_BORDER_DEFAULT + ";"
            + "-fx-border-radius: 8;"
            + "-fx-background-radius: 8;"
            + "-fx-cursor: hand;");

    card.setOnMouseClicked(_ -> onCategoryCardClicked(category));
    return card;
  }

  private javafx.scene.Node buildResourceCard(LearningResource resource) {
    Label sourceLabel = new Label(resource.sourceLabel());
    sourceLabel.setStyle(
        "-fx-background-color: " + COLOR_BORDER_RESOURCE + "22;"
            + "-fx-text-fill: " + COLOR_BORDER_RESOURCE + ";"
            + "-fx-background-radius: 4;"
            + "-fx-padding: 2 6 2 6;"
            + "-fx-font-size: 10;");

    Label title = new Label(resource.title());
    title.setStyle("-fx-text-fill: " + COLOR_HEADING + "; -fx-font-weight: bold; -fx-font-size: 14;");

    Label desc = new Label(resource.description());
    desc.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + ";");
    desc.setWrapText(true);

    Label cta = new Label("Open article →");
    cta.setStyle("-fx-text-fill: " + COLOR_BORDER_RESOURCE + "; -fx-font-size: 11;");

    VBox card = new VBox(6, sourceLabel, title, desc, cta);
    card.setPadding(new Insets(14));
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

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private static VBox buildSection(String title, javafx.scene.Node content) {
    Label heading = new Label(title);
    heading.setStyle(
        "-fx-text-fill: " + "#e0e0e0" + ";"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 14;");

    VBox section = new VBox(10, heading, content);
    return section;
  }

  private static String difficultyColor(Difficulty difficulty) {
    return switch (difficulty) {
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

  // ── Navigation ───────────────────────────────────────────────────────────────

  private void showLanding() {
    setCenter(landingView);
  }

  private void onItemCardClicked(LearningItem item) {
    setCenter(new LearningItemDetailView(item, this::showLanding, this::onItemCardClicked));
  }

  private void onCategoryCardClicked(LearningCategory category) {
    setCenter(new LearningCategoryView(category, this::showLanding, this::onItemCardClicked));
  }

  private void onFeaturedCardClicked(LearningItem item) {
    onItemCardClicked(item);
  }
}
