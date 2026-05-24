package view.pages.quiz;

import java.util.List;
import java.util.function.Consumer;

import controller.LearningHubController;
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
import model.learning.content.LearningItem;
import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizQuestion;
import model.learning.quiz.QuizSession;
import view.components.learning.QuizWrongAnswerLinks;
import view.theme.ThemeStyles;

/**
 * Displays the score summary after a quiz is completed.
 * Shows per-question breakdown and session-level totals.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class QuizResultView extends BorderPane {

  private final LearningHubController learningHub;
  private final Consumer<LearningItem> onOpenHubTopic;

  /**
   * Builds the result view.
   *
   * @param attempt        the completed quiz attempt
   * @param onBackToTopic  called when the player returns to the topic detail view
   * @param onBackToHub    called when the player returns to the Learning Hub landing
   * @param learningHub    supplies linked topic and resource lookups for breakdown links
   * @param onOpenHubTopic called when the player opens a linked hub topic from breakdown links
   */
  public QuizResultView(
      QuizAttempt attempt,
      Runnable onBackToTopic,
      Runnable onBackToHub,
      LearningHubController learningHub,
      Consumer<LearningItem> onOpenHubTopic) {
    this.learningHub = learningHub;
    this.onOpenHubTopic = onOpenHubTopic;

    ThemeStyles.addStyleClasses(this, "quiz-root");
    setPadding(new Insets(16));

    Button backToTopicBtn = new Button("← Back to Topic");
    ThemeStyles.styleButton(backToTopicBtn);
    backToTopicBtn.setOnAction(_ -> onBackToTopic.run());

    Button backToHubBtn = new Button("Learning Hub");
    ThemeStyles.styleButton(backToHubBtn);
    backToHubBtn.setOnAction(_ -> onBackToHub.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(backToTopicBtn, spacer, backToHubBtn);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(0, 0, 12, 0));
    setTop(topBar);

    VBox content = new VBox(20);
    content.setPadding(new Insets(4, 0, 16, 0));

    content.getChildren().add(buildScoreCard(attempt));
    content.getChildren().add(buildBreakdownSection(attempt));

    List<QuizAttempt> allAttempts = QuizSession.getCompletedAttempts();
    if (allAttempts.size() > 1) {
      content.getChildren().add(buildSessionSummary(allAttempts));
    }

    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    ThemeStyles.addStyleClasses(scroll, "scroll-transparent");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);
  }

  private VBox buildBreakdownSection(QuizAttempt attempt) {
    Label heading = new Label("Question Breakdown");
    ThemeStyles.addStyleClasses(heading, "quiz-result-heading");

    VBox section = new VBox(8, heading);
    List<QuizQuestion> questions = attempt.quiz().questions();

    for (int i = 0; i < questions.size(); i++) {
      section.getChildren().add(
          buildBreakdownRow(attempt, questions.get(i), attempt.wasCorrect(i), i + 1));
    }

    return section;
  }

  private VBox buildBreakdownRow(
      QuizAttempt attempt, QuizQuestion q, boolean correct, int number) {
    String badgeText = correct ? "CORRECT" : "WRONG";

    String questionPreview = q.questionText().length() > 65
        ? q.questionText().substring(0, 62) + "..."
        : q.questionText();

    Label numLabel = new Label("#" + number);
    ThemeStyles.addStyleClasses(numLabel, "quiz-result-subtitle");

    Label qText = new Label(questionPreview);
    qText.setWrapText(true);
    ThemeStyles.addStyleClasses(qText, "quiz-result-question");

    Label badge = new Label(badgeText);
    ThemeStyles.addStyleClasses(
        badge, "status-badge", correct ? "status-badge-success" : "status-badge-error");

    VBox row = new VBox(4, new HBox(8, numLabel, badge), qText);

    if (!correct) {
      Label explanation = new Label(q.explanationText());
      explanation.setWrapText(true);
      ThemeStyles.addStyleClasses(explanation, "text-small-secondary-italic");
      row.getChildren().add(explanation);

      VBox linksBox = new VBox(8);
      QuizWrongAnswerLinks.appendTo(
          linksBox, learningHub, q, attempt.quiz().linkedItemId(), onOpenHubTopic);
      row.getChildren().add(linksBox);
    }

    ThemeStyles.addStyleClasses(
        row, "quiz-result-row", correct ? "quiz-result-row-correct" : "quiz-result-row-wrong");
    return row;
  }

  private static VBox buildScoreCard(QuizAttempt attempt) {
    int correct = attempt.correctCount();
    int total = attempt.totalQuestions();
    double pct = total == 0 ? 0 : (double) correct / total;

    String badgeClass;
    String badgeText;
    if (pct >= 0.8) {
      badgeClass = "status-badge-success";
      badgeText = "Excellent!";
    } else if (pct >= 0.6) {
      badgeClass = "status-badge-warning";
      badgeText = "Good effort!";
    } else {
      badgeClass = "status-badge-error";
      badgeText = "Keep practicing!";
    }

    Label quizTitle = new Label(attempt.quiz().title());
    ThemeStyles.addStyleClasses(quizTitle, "quiz-result-subtitle-sm");

    Label scoreLabel = new Label(correct + " / " + total + " Correct");
    scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
    ThemeStyles.addStyleClasses(scoreLabel, "quiz-result-score");

    Label badge = new Label(badgeText);
    ThemeStyles.addStyleClasses(badge, "status-badge-lg", badgeClass);

    VBox card = new VBox(8, quizTitle, scoreLabel, badge);
    card.setAlignment(Pos.CENTER_LEFT);
    ThemeStyles.addStyleClasses(card, "quiz-result-card");
    return card;
  }

  private static VBox buildSessionSummary(List<QuizAttempt> allAttempts) {
    int totalCorrect = QuizSession.totalCorrectAcrossSession();
    int totalAnswered = QuizSession.totalAnsweredAcrossSession();
    int quizCount = allAttempts.size();

    Label heading = new Label("Session Summary");
    ThemeStyles.addStyleClasses(heading, "quiz-result-heading");

    Label stats = new Label(
        totalCorrect + " correct out of " + totalAnswered
            + " questions across " + quizCount + " quizzes");
    stats.setWrapText(true);
    ThemeStyles.addStyleClasses(stats, "quiz-result-subtitle-sm");

    VBox card = new VBox(6, heading, stats);
    ThemeStyles.addStyleClasses(card, "quiz-result-summary-card");
    return card;
  }
}
