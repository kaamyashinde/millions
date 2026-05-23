package view.pages.learning;

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

import model.learning.content.Difficulty;
import model.learning.content.LearningCategory;
import model.learning.store.LearningContentStore;
import model.learning.content.LearningItem;
import model.learning.content.LearningResource;
import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizSession;
import view.components.learning.LearningResourceCard;
import view.pages.quiz.QuizResultView;
import view.pages.quiz.QuizView;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Learning Hub landing page. Displays a featured-topics row, a 6-category grid, and a highlighted
 * beginner resource card. All content is sourced from {@link LearningContentStore}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class LearningHubPage extends BorderPane {

  private javafx.scene.Node landingView;

  /**
   * Builds the panel, wires up all sections from {@link LearningContentStore}.
   */
  public LearningHubPage() {
    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "learning-root", "learning-hub-root");

    // ── TOP: heading + subtitle ──────────────────────────────────────────────
    Text heading = new Text("Learning Hub");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    ThemeStyles.addStyleClasses(heading, "learning-heading");

    Label subtitle = new Label("Build your investing knowledge, one topic at a time.");
    ThemeStyles.addStyleClasses(subtitle, "learning-subtitle");

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

    return buildSection("Start Here", LearningResourceCard.create(resource));
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
    ThemeStyles.addStyleClasses(title, "learning-card-title");
    title.setWrapText(true);

    Label summary = new Label(item.summary());
    ThemeStyles.addStyleClasses(summary, "learning-card-summary");
    summary.setWrapText(true);

    Label category = new Label(item.category().emoji() + "  " + item.category().name());
    ThemeStyles.addStyleClasses(category, "learning-card-meta");

    VBox card = new VBox(6, badge, title, summary, category);
    card.setPadding(new Insets(12));
    ThemeStyles.addStyleClasses(card, "learning-card-accent");
    HBox.setHgrow(card, Priority.ALWAYS);

    card.setOnMouseClicked(_ -> onFeaturedCardClicked(item));
    return card;
  }

  private javafx.scene.Node buildCategoryCard(LearningCategory category) {
    Label emoji = new Label(category.emoji());
    ThemeStyles.addStyleClasses(emoji, "learning-card-emoji");

    Label name = new Label(category.name());
    ThemeStyles.addStyleClasses(name, "learning-card-title");
    name.setWrapText(true);

    Label desc = new Label(category.description());
    ThemeStyles.addStyleClasses(desc, "learning-card-summary");
    desc.setWrapText(true);

    VBox card = new VBox(6, emoji, name, desc);
    card.setPadding(new Insets(12));
    ThemeStyles.addStyleClasses(card, "learning-card");

    card.setOnMouseClicked(_ -> onCategoryCardClicked(category));
    return card;
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private static VBox buildSection(String title, javafx.scene.Node content) {
    Label heading = new Label(title);
    ThemeStyles.addStyleClasses(heading, "learning-section-title");

    VBox section = new VBox(10, heading, content);
    return section;
  }

  private static String difficultyColor(Difficulty difficulty) {
    return switch (difficulty) {
      case BEGINNER -> ThemePalette.DIFFICULTY_BEGINNER;
      case INTERMEDIATE -> ThemePalette.DIFFICULTY_INTERMEDIATE;
      case ADVANCED -> ThemePalette.DIFFICULTY_ADVANCED;
    };
  }

  // ── Navigation ───────────────────────────────────────────────────────────────

  private void showLanding() {
    setCenter(landingView);
  }

  private void onItemCardClicked(LearningItem item) {
    setCenter(new ItemDetailView(
        item, this::showLanding, this::onItemCardClicked, this::onTakeQuiz));
  }

  private void onCategoryCardClicked(LearningCategory category) {
    setCenter(new CategoryView(category, this::showLanding, this::onItemCardClicked));
  }

  private void onFeaturedCardClicked(LearningItem item) {
    onItemCardClicked(item);
  }

  private void onTakeQuiz(QuizAttempt attempt) {
    LearningItem item = LearningContentStore
        .getItemsByIds(List.of(attempt.quiz().linkedItemId())).get(0);
    Runnable backToItem = () -> onItemCardClicked(item);
    setCenter(new QuizView(
        attempt,
        backToItem,
        () -> showQuizResult(attempt, backToItem)));
  }

  private void showQuizResult(QuizAttempt attempt, Runnable onBackToTopic) {
    QuizSession.record(attempt);
    setCenter(new QuizResultView(attempt, onBackToTopic, this::showLanding));
  }
}
