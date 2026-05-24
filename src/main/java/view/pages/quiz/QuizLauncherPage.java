package view.pages.quiz;

import java.util.List;
import java.util.function.Consumer;

import controller.LearningHubController;
import controller.QuizController;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.learning.content.LearningItem;
import model.learning.quiz.Quiz;
import model.learning.quiz.QuizAttempt;
import view.theme.ThemeStyles;

/**
 * Top-level panel for the Quiz tab. Shows a list of all available quizzes and
 * handles the full quiz-play flow (quiz → result → back to launcher).
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 04-04-2026
 */
public class QuizLauncherPage extends BorderPane {

  private final QuizController quiz;
  private final LearningHubController learningHub;
  private final Consumer<LearningItem> onOpenHubTopic;
  private javafx.scene.Node launcherView;

  /**
   * Builds the panel and shows the quiz list.
   *
   * @param quiz           supplies quiz content and session recording
   * @param learningHub    supplies linked topic lookups
   * @param onOpenHubTopic called when the player opens a linked hub topic from quiz feedback
   */
  public QuizLauncherPage(
      QuizController quiz,
      LearningHubController learningHub,
      Consumer<LearningItem> onOpenHubTopic) {
    this.quiz = quiz;
    this.learningHub = learningHub;
    this.onOpenHubTopic = onOpenHubTopic;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "quiz-root");

    Text heading = new Text("Quizzes");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    ThemeStyles.addStyleClasses(heading, "quiz-heading");

    Label subtitle = new Label("Test your investing knowledge across all topics.");
    ThemeStyles.addStyleClasses(subtitle, "quiz-subtitle");

    VBox top = new VBox(4, heading, subtitle);
    top.setPadding(new Insets(0, 0, 12, 0));
    setTop(top);

    VBox list = new VBox(10);
    List<Quiz> quizzes = quiz.getAllQuizzes();
    list.getChildren().addAll(quizzes.stream()
        .map(this::buildQuizCard)
        .toList());

    ScrollPane scroll = new ScrollPane(list);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    this.launcherView = scroll;
    setCenter(launcherView);
  }

  private javafx.scene.Node buildQuizCard(Quiz quizItem) {
    Label title = new Label(quizItem.title());
    ThemeStyles.addStyleClasses(title, "quiz-card-title");
    title.setWrapText(true);

    Label meta = new Label(quizItem.questions().size() + " questions");
    ThemeStyles.addStyleClasses(meta, "quiz-meta");

    Label cta = new Label("Start →");
    ThemeStyles.addStyleClasses(cta, "quiz-cta");

    VBox card = new VBox(4, title, meta, cta);
    card.setPadding(new Insets(14));
    card.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.addStyleClasses(card, "quiz-card");

    card.setOnMouseClicked(_ -> startQuiz(quizItem));
    return card;
  }

  private void startQuiz(Quiz quizItem) {
    QuizAttempt attempt = new QuizAttempt(quizItem);
    Runnable backToLauncher = this::showLauncher;
    setCenter(new QuizView(
        attempt,
        backToLauncher,
        () -> showResult(attempt, backToLauncher),
        learningHub,
        onOpenHubTopic));
  }

  private void showResult(QuizAttempt attempt, Runnable onBackToTopic) {
    quiz.recordAttempt(attempt);
    setCenter(new QuizResultView(
        attempt, onBackToTopic, this::showLauncher, learningHub, onOpenHubTopic));
  }

  private void showLauncher() {
    setCenter(launcherView);
  }
}
