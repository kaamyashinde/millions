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

import model.learninghub.LearningContentStore;
import model.learninghub.LearningResource;
import model.learninghub.QuizAnswer;
import model.learninghub.QuizAttempt;
import model.learninghub.QuizQuestion;

/**
 * Interactive quiz view. Presents one question at a time with four answer choices.
 * Gives immediate feedback after each answer and navigates to the result view when done.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class QuizView extends BorderPane {

  private static final String COLOR_BG = "#121212";
  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";
  private static final String COLOR_ACCENT = "#2196F3";
  private static final String COLOR_CORRECT = "#4CAF50";
  private static final String COLOR_WRONG = "#FF4444";
  private static final String COLOR_BORDER_DEFAULT = "#2a2a2a";

  private final QuizAttempt attempt;
  private final Runnable onFinish;

  private Label progressLabel;
  private VBox centerContent;

  /**
   * Builds the quiz view.
   *
   * @param attempt  the quiz attempt tracking progress and answers
   * @param onBack   called when the back button is clicked
   * @param onFinish called when the player has answered all questions
   */
  public QuizView(QuizAttempt attempt, Runnable onBack, Runnable onFinish) {
    this.attempt = attempt;
    this.onFinish = onFinish;

    setStyle("-fx-background-color: " + COLOR_BG + ";");
    setPadding(new Insets(16));

    // ── TOP bar ──────────────────────────────────────────────────────────────
    Button backBtn = new Button("← Back");
    backBtn.setStyle("-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    backBtn.setOnAction(_ -> onBack.run());

    progressLabel = new Label();
    progressLabel.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 12;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox topBar = new HBox(backBtn, spacer, progressLabel);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(0, 0, 12, 0));
    setTop(topBar);

    // ── CENTER (built dynamically per question) ───────────────────────────────
    centerContent = new VBox(16);
    ScrollPane scroll = new ScrollPane(centerContent);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);

    refreshQuestion();
  }

  // ── Question rendering ───────────────────────────────────────────────────────

  private void refreshQuestion() {
    centerContent.getChildren().clear();

    QuizQuestion q = attempt.currentQuestion();
    int current = attempt.currentIndex() + 1;
    int total = attempt.totalQuestions();

    progressLabel.setText("Question " + current + " of " + total);

    // Question card
    Label questionText = new Label(q.questionText());
    questionText.setWrapText(true);
    questionText.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-size: 15;"
        + "-fx-font-weight: bold;");

    VBox questionCard = new VBox(questionText);
    questionCard.setPadding(new Insets(16));
    questionCard.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + COLOR_BORDER_DEFAULT + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;");

    // Answer buttons
    VBox answersBox = new VBox(8);
    List<QuizAnswer> answers = q.answers();
    Button[] answerButtons = new Button[answers.size()];

    // Feedback pane (hidden until answered)
    VBox feedbackPane = new VBox(8);
    feedbackPane.setVisible(false);
    feedbackPane.setManaged(false);
    feedbackPane.setPadding(new Insets(12));
    feedbackPane.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;");

    // Next / finish button (hidden until answered)
    Button nextBtn = new Button(
        (attempt.currentIndex() + 1 >= attempt.totalQuestions()) ? "See Results →" : "Next →");
    nextBtn.setVisible(false);
    nextBtn.setManaged(false);
    nextBtn.setStyle(
        "-fx-background-color: " + COLOR_ACCENT + ";"
        + "-fx-text-fill: white;"
        + "-fx-font-weight: bold;"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;"
        + "-fx-cursor: hand;"
        + "-fx-padding: 10 20 10 20;");
    nextBtn.setOnAction(_ -> {
      String chosenId = (String) nextBtn.getUserData();
      attempt.submitAnswer(chosenId);
      if (attempt.isFinished()) {
        onFinish.run();
      } else {
        refreshQuestion();
      }
    });

    for (int i = 0; i < answers.size(); i++) {
      QuizAnswer answer = answers.get(i);
      Button btn = buildAnswerButton(answer);
      answerButtons[i] = btn;

      btn.setOnAction(_ -> {
        // Disable all buttons after selection
        for (Button b : answerButtons) {
          b.setDisable(true);
        }

        String correctId = q.correctAnswerId();
        boolean correct = answer.id().equals(correctId);

        // Highlight correct answer green
        for (Button b : answerButtons) {
          if (b.getUserData().equals(correctId)) {
            b.setStyle(b.getStyle()
                + "-fx-border-color: " + COLOR_CORRECT + ";"
                + "-fx-background-color: " + COLOR_CORRECT + "22;");
          }
        }

        // Highlight wrong choice red
        if (!correct) {
          btn.setStyle(btn.getStyle()
              + "-fx-border-color: " + COLOR_WRONG + ";"
              + "-fx-background-color: " + COLOR_WRONG + "22;");
        }

        // Build and show feedback
        buildFeedbackContent(feedbackPane, q, correct, answer.id());
        feedbackPane.setVisible(true);
        feedbackPane.setManaged(true);

        // Store chosen answer on the next button for submission
        nextBtn.setUserData(answer.id());
        nextBtn.setVisible(true);
        nextBtn.setManaged(true);
      });

      answersBox.getChildren().add(btn);
    }

    HBox nextRow = new HBox(nextBtn);
    nextRow.setAlignment(Pos.CENTER_RIGHT);

    centerContent.getChildren().addAll(questionCard, answersBox, feedbackPane, nextRow);
  }

  private static Button buildAnswerButton(QuizAnswer answer) {
    Button btn = new Button(answer.text());
    btn.setMaxWidth(Double.MAX_VALUE);
    btn.setWrapText(true);
    btn.setUserData(answer.id());
    btn.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-border-color: " + COLOR_ACCENT + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;"
        + "-fx-cursor: hand;"
        + "-fx-alignment: CENTER_LEFT;"
        + "-fx-padding: 10 14 10 14;");
    return btn;
  }

  private static void buildFeedbackContent(
      VBox feedbackPane, QuizQuestion q, boolean correct, String chosenId) {
    feedbackPane.getChildren().clear();

    String resultColor = correct ? COLOR_CORRECT : COLOR_WRONG;
    String resultText = correct ? "✓  Correct!" : "✗  Not quite.";

    feedbackPane.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + resultColor + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;");

    Label resultLabel = new Label(resultText);
    resultLabel.setStyle(
        "-fx-text-fill: " + resultColor + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");

    Label explanation = new Label(q.explanationText());
    explanation.setWrapText(true);
    explanation.setStyle("-fx-text-fill: " + COLOR_HEADING + "; -fx-font-size: 12;");

    feedbackPane.getChildren().addAll(resultLabel, explanation);

    // On wrong answer with a linked resource — surface the resource card
    if (!correct && q.linkedResourceId() != null) {
      LearningContentStore.getResources().stream()
          .filter(r -> r.id().equals(q.linkedResourceId()))
          .findFirst()
          .ifPresent(resource -> {
            Label learnMore = new Label("Review this resource:");
            learnMore.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");
            feedbackPane.getChildren().addAll(learnMore, buildResourceCard(resource));
          });
    }
  }

  private static VBox buildResourceCard(LearningResource resource) {
    Label sourceLabel = new Label(resource.sourceLabel());
    sourceLabel.setStyle(
        "-fx-background-color: #4CAF5022;"
        + "-fx-text-fill: #4CAF50;"
        + "-fx-background-radius: 4;"
        + "-fx-padding: 2 6 2 6;"
        + "-fx-font-size: 10;");

    Label title = new Label(resource.title());
    title.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 12;");
    title.setWrapText(true);

    Label desc = new Label(resource.description());
    desc.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");
    desc.setWrapText(true);

    VBox card = new VBox(4, sourceLabel, title, desc);
    card.setPadding(new Insets(10));
    card.setStyle(
        "-fx-background-color: #1a1a1a;"
        + "-fx-border-color: #4CAF50;"
        + "-fx-border-radius: 6;"
        + "-fx-background-radius: 6;");
    return card;
  }
}
