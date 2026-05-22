package view.pages.quiz;

import java.util.List;

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

import model.learninghub.QuizAttempt;
import model.learninghub.QuizQuestion;
import model.learninghub.QuizSession;

/**
 * Displays the score summary after a quiz is completed.
 * Shows per-question breakdown and session-level totals.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class QuizResultView extends BorderPane {

  private static final String COLOR_BG = "#121212";
  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";
  private static final String COLOR_CORRECT = "#4CAF50";
  private static final String COLOR_WRONG = "#FF4444";
  private static final String COLOR_ORANGE = "#FFA500";
  private static final String COLOR_BORDER_DEFAULT = "#2a2a2a";

  /**
   * Builds the result view.
   *
   * @param attempt        the completed quiz attempt
   * @param onBackToTopic  called when the player returns to the topic detail view
   * @param onBackToHub    called when the player returns to the Learning Hub landing
   */
  public QuizResultView(QuizAttempt attempt, Runnable onBackToTopic, Runnable onBackToHub) {
    setStyle("-fx-background-color: " + COLOR_BG + ";");
    setPadding(new Insets(16));

    // ── TOP bar ──────────────────────────────────────────────────────────────
    Button backToTopicBtn = new Button("← Back to Topic");
    backToTopicBtn.setStyle("-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    backToTopicBtn.setOnAction(_ -> onBackToTopic.run());

    Button backToHubBtn = new Button("Learning Hub");
    backToHubBtn.setStyle("-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    backToHubBtn.setOnAction(_ -> onBackToHub.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(backToTopicBtn, spacer, backToHubBtn);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(0, 0, 12, 0));
    setTop(topBar);

    // ── CENTER: score card + breakdown + session summary ─────────────────────
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
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);
  }

  // ── Score card ───────────────────────────────────────────────────────────────

  private static VBox buildScoreCard(QuizAttempt attempt) {
    int correct = attempt.correctCount();
    int total = attempt.totalQuestions();
    double pct = total == 0 ? 0 : (double) correct / total;

    String badgeColor;
    String badgeText;
    if (pct >= 0.8) {
      badgeColor = COLOR_CORRECT;
      badgeText = "Excellent!";
    } else if (pct >= 0.6) {
      badgeColor = COLOR_ORANGE;
      badgeText = "Good effort!";
    } else {
      badgeColor = COLOR_WRONG;
      badgeText = "Keep practicing!";
    }

    Label quizTitle = new Label(attempt.quiz().title());
    quizTitle.setStyle("-fx-text-fill: " + "#9e9e9e" + "; -fx-font-size: 12;");

    Label scoreLabel = new Label(correct + " / " + total + " Correct");
    scoreLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
    scoreLabel.setStyle("-fx-text-fill: " + COLOR_HEADING + ";");

    Label badge = new Label(badgeText);
    badge.setStyle(
        "-fx-background-color: " + badgeColor + "22;"
        + "-fx-text-fill: " + badgeColor + ";"
        + "-fx-background-radius: 6;"
        + "-fx-padding: 4 12 4 12;"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");

    VBox card = new VBox(8, quizTitle, scoreLabel, badge);
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPadding(new Insets(20));
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + COLOR_BORDER_DEFAULT + ";"
        + "-fx-border-radius: 10;"
        + "-fx-background-radius: 10;");
    return card;
  }

  // ── Per-question breakdown ───────────────────────────────────────────────────

  private static VBox buildBreakdownSection(QuizAttempt attempt) {
    Label heading = new Label("Question Breakdown");
    heading.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");

    VBox section = new VBox(8, heading);
    List<QuizQuestion> questions = attempt.quiz().questions();

    for (int i = 0; i < questions.size(); i++) {
      section.getChildren().add(buildBreakdownRow(questions.get(i), attempt.wasCorrect(i), i + 1));
    }

    return section;
  }

  private static VBox buildBreakdownRow(QuizQuestion q, boolean correct, int number) {
    String badgeColor = correct ? COLOR_CORRECT : COLOR_WRONG;
    String badgeText = correct ? "CORRECT" : "WRONG";

    String questionPreview = q.questionText().length() > 65
        ? q.questionText().substring(0, 62) + "..."
        : q.questionText();

    Label numLabel = new Label("#" + number);
    numLabel.setStyle("-fx-text-fill: " + "#9e9e9e" + "; -fx-font-size: 11;");

    Label qText = new Label(questionPreview);
    qText.setWrapText(true);
    qText.setStyle("-fx-text-fill: " + COLOR_HEADING + "; -fx-font-size: 12;");

    Label badge = new Label(badgeText);
    badge.setStyle(
        "-fx-background-color: " + badgeColor + "22;"
        + "-fx-text-fill: " + badgeColor + ";"
        + "-fx-background-radius: 4;"
        + "-fx-padding: 2 6 2 6;"
        + "-fx-font-size: 10;"
        + "-fx-font-weight: bold;");

    VBox row = new VBox(4, new HBox(8, numLabel, badge), qText);

    if (!correct) {
      Label explanation = new Label(q.explanationText());
      explanation.setWrapText(true);
      explanation.setStyle("-fx-text-fill: " + "#9e9e9e" + "; -fx-font-size: 11; -fx-font-style: italic;");
      row.getChildren().add(explanation);
    }

    row.setPadding(new Insets(10));
    row.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + (correct ? COLOR_CORRECT : COLOR_WRONG) + "33;"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;");
    return row;
  }

  // ── Session summary ──────────────────────────────────────────────────────────

  private static VBox buildSessionSummary(List<QuizAttempt> allAttempts) {
    int totalCorrect = QuizSession.totalCorrectAcrossSession();
    int totalAnswered = QuizSession.totalAnsweredAcrossSession();
    int quizCount = allAttempts.size();

    Label heading = new Label("Session Summary");
    heading.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");

    Label stats = new Label(
        totalCorrect + " correct out of " + totalAnswered
        + " questions across " + quizCount + " quizzes");
    stats.setWrapText(true);
    stats.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 12;");

    VBox card = new VBox(6, heading, stats);
    card.setPadding(new Insets(14));
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + COLOR_BORDER_DEFAULT + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;");
    return card;
  }
}
