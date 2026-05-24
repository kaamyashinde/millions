package view.pages.learning;

import java.util.List;

import controller.LearningHubController;
import controller.QuizController;
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
import model.learning.content.LearningCategory;
import model.learning.content.LearningItem;
import model.learning.content.LearningResource;
import model.learning.quiz.QuizAttempt;
import view.components.learning.LearningResourceCard;
import view.layout.ResponsiveLayout;
import view.pages.quiz.QuizResultView;
import view.pages.quiz.QuizView;
import view.theme.ThemeStyles;

/**
 * Learning Hub landing page. Displays a featured-topics row, a 6-category grid, and a highlighted
 * beginner resource card. All content is supplied by {@link LearningHubController}.
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 2026-04-04
 */
public class LearningHubPage extends BorderPane implements ResponsiveLayout {

  private final LearningHubController learningHub;
  private final QuizController quiz;
  private final List<LearningCategory> categories;
  private final VBox categorySection;
  private javafx.scene.Node landingView;

  /**
   * Builds the panel and wires up all sections from the learning hub controller.
   *
   * @param learningHub supplies catalog content
   * @param quiz        supplies quiz content and session recording
   */
  public LearningHubPage(LearningHubController learningHub, QuizController quiz) {
    this.learningHub = learningHub;
    this.quiz = quiz;
    this.categories = learningHub.getCategories();

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "learning-root", "learning-hub-root");

    Text heading = new Text("Learning Hub");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    ThemeStyles.addStyleClasses(heading, "learning-heading");

    Label subtitle = new Label("Build your investing knowledge, one topic at a time.");
    ThemeStyles.addStyleClasses(subtitle, "learning-subtitle");

    VBox top = new VBox(4, heading, subtitle);
    top.setPadding(new Insets(0, 0, 12, 0));
    setTop(top);

    VBox content = new VBox(28);
    content.setPadding(new Insets(4, 0, 16, 0));
    categorySection = buildCategorySection();
    content.getChildren().addAll(
        buildFeaturedSection(),
        categorySection,
        buildStartHereSection());

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    this.landingView = scroll;
    setCenter(landingView);
  }

  private VBox buildFeaturedSection() {
    List<LearningItem> featured = learningHub.getFeaturedItems();

    HBox cards = new HBox(12);
    for (LearningItem item : featured) {
      cards.getChildren().add(buildFeaturedCard(item));
    }

    return buildSection("Featured Topics", cards);
  }

  private VBox buildCategorySection() {
    return buildSection("Browse by Category", buildCategoryGrid(columnCountForWidth(getWidth())));
  }

  private GridPane buildCategoryGrid(int columns) {
    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(12);

    double percentWidth = 100.0 / columns;
    for (int i = 0; i < columns; i++) {
      ColumnConstraints col = new ColumnConstraints();
      col.setHgrow(Priority.ALWAYS);
      col.setPercentWidth(percentWidth);
      grid.getColumnConstraints().add(col);
    }

    for (int i = 0; i < categories.size(); i++) {
      grid.add(buildCategoryCard(categories.get(i)), i % columns, i / columns);
    }

    return grid;
  }

  private static int columnCountForWidth(double width) {
    if (width < 700) {
      return 1;
    }
    if (width < 1000) {
      return 2;
    }
    return 3;
  }

  private VBox buildStartHereSection() {
    LearningResource resource = learningHub.getStartHereResource();
    return buildSection("Start Here", LearningResourceCard.create(resource));
  }

  private javafx.scene.Node buildFeaturedCard(LearningItem item) {
    Label badge = new Label(item.difficulty().name());
    ThemeStyles.applyDifficultyBadge(badge, item.difficulty());

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

  private static VBox buildSection(String title, javafx.scene.Node content) {
    Label heading = new Label(title);
    ThemeStyles.addStyleClasses(heading, "learning-section-title");
    return new VBox(10, heading, content);
  }

  /**
   * Opens the topic detail view for the given learning item ID.
   *
   * @param itemId {@link LearningItem#id()}
   */
  public void openTopic(String itemId) {
    learningHub.getItemById(itemId).ifPresent(this::onItemCardClicked);
  }

  @Override
  public void onWindowResized(double width, double height) {
    if (getCenter() != landingView) {
      return;
    }
    int columns = columnCountForWidth(width);
    categorySection.getChildren().set(1, buildCategoryGrid(columns));
  }

  private void showLanding() {
    setCenter(landingView);
  }

  private void onItemCardClicked(LearningItem item) {
    setCenter(new ItemDetailView(
        learningHub,
        quiz,
        item,
        this::showLanding,
        this::onItemCardClicked,
        this::onTakeQuiz));
  }

  private void onCategoryCardClicked(LearningCategory category) {
    setCenter(new CategoryView(
        learningHub, category, this::showLanding, this::onItemCardClicked));
  }

  private void onFeaturedCardClicked(LearningItem item) {
    onItemCardClicked(item);
  }

  private void onTakeQuiz(QuizAttempt attempt) {
    LearningItem item = learningHub.getItemById(attempt.quiz().linkedItemId()).orElseThrow();
    Runnable backToItem = () -> onItemCardClicked(item);
    setCenter(new QuizView(
        attempt,
        backToItem,
        () -> showQuizResult(attempt, backToItem),
        learningHub,
        this::onItemCardClicked));
  }

  private void showQuizResult(QuizAttempt attempt, Runnable onBackToTopic) {
    quiz.recordAttempt(attempt);
    setCenter(new QuizResultView(
        attempt, onBackToTopic, this::showLanding, learningHub, this::onItemCardClicked));
  }
}
