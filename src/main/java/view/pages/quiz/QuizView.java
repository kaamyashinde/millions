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

import model.learning.store.LearningContentStore;
import model.learning.content.LearningResource;
import model.learning.quiz.QuizAnswer;
import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizQuestion;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Interactive quiz view. Presents one question at a time with four answer choices.
 * Gives immediate feedback after each answer and navigates to the result view when done.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class QuizView extends BorderPane {

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

    ThemeStyles.addStyleClasses(this, "quiz-root");
    setPadding(new Insets(16));

    // ── TOP bar ──────────────────────────────────────────────────────────────
    Button backBtn = new Button("← Back");
    ThemeStyles.styleButton(backBtn);
    backBtn.setOnAction(_ -> onBack.run());

    progressLabel = new Label();
    ThemeStyles.addStyleClasses(progressLabel, "quiz-meta");

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
    ThemeStyles.addStyleClasses(questionText, "quiz-question-text");

    VBox questionCard = new VBox(questionText);
    questionCard.setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(questionCard, "quiz-question-card");

    // Answer buttons
    VBox answersBox = new VBox(8);
    List<QuizAnswer> answers = q.answers();
    Button[] answerButtons = new Button[answers.size()];

    // Feedback pane (hidden until answered)
    VBox feedbackPane = new VBox(8);
    feedbackPane.setVisible(false);
    feedbackPane.setManaged(false);
    feedbackPane.setPadding(new Insets(12));
    ThemeStyles.addStyleClasses(feedbackPane, "quiz-feedback");

    // Next / finish button (hidden until answered)
    Button nextBtn = new Button(
        (attempt.currentIndex() + 1 >= attempt.totalQuestions()) ? "See Results →" : "Next →");
    nextBtn.setVisible(false);
    nextBtn.setManaged(false);
    ThemeStyles.styleAccentButton(nextBtn);
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
            ThemeStyles.addStyleClasses(b, "quiz-answer-correct");
          }
        }

        // Highlight wrong choice red
        if (!correct) {
          ThemeStyles.addStyleClasses(btn, "quiz-answer-wrong");
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
    ThemeStyles.addStyleClasses(btn, "quiz-answer-button");
    return btn;
  }

  private static void buildFeedbackContent(
      VBox feedbackPane, QuizQuestion q, boolean correct, String chosenId) {
    feedbackPane.getChildren().clear();

    String resultColor = correct ? ThemePalette.SUCCESS : ThemePalette.ERROR;
    String resultText = correct ? "✓  Correct!" : "✗  Not quite.";

    feedbackPane.getStyleClass().removeAll("quiz-feedback-correct", "quiz-feedback-wrong");
    ThemeStyles.addStyleClasses(
        feedbackPane, correct ? "quiz-feedback-correct" : "quiz-feedback-wrong");

    Label resultLabel = new Label(resultText);
    resultLabel.setStyle(
        "-fx-text-fill: " + resultColor + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");

    Label explanation = new Label(q.explanationText());
    explanation.setWrapText(true);
    explanation.setStyle("-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + "; -fx-font-size: 12;");

    feedbackPane.getChildren().addAll(resultLabel, explanation);

    // On wrong answer with a linked resource — surface the resource card
    if (!correct && q.linkedResourceId() != null) {
      LearningContentStore.getResources().stream()
          .filter(r -> r.id().equals(q.linkedResourceId()))
          .findFirst()
          .ifPresent(resource -> {
            Label learnMore = new Label("Review this resource:");
            learnMore.setStyle("-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + "; -fx-font-size: 11;");
            feedbackPane.getChildren().addAll(learnMore, buildResourceCard(resource));
          });
    }
  }

  private static VBox buildResourceCard(LearningResource resource) {
    Label sourceLabel = new Label(resource.sourceLabel());
    sourceLabel.setStyle(
        "-fx-background-color: " + ThemePalette.SUCCESS + "22;"
        + "-fx-text-fill: " + ThemePalette.SUCCESS + ";"
        + "-fx-background-radius: 4;"
        + "-fx-padding: 2 6 2 6;"
        + "-fx-font-size: 10;");

    Label title = new Label(resource.title());
    title.setStyle(
        "-fx-text-fill: " + ThemePalette.TEXT_PRIMARY + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 12;");
    title.setWrapText(true);

    Label desc = new Label(resource.description());
    desc.setStyle("-fx-text-fill: " + ThemePalette.TEXT_SECONDARY + "; -fx-font-size: 11;");
    desc.setWrapText(true);

    VBox card = new VBox(4, sourceLabel, title, desc);
    card.setPadding(new Insets(10));
    card.setStyle(
        "-fx-background-color: " + ThemePalette.SURFACE + ";"
        + "-fx-border-color: " + ThemePalette.SUCCESS + ";"
        + "-fx-border-radius: 6;"
        + "-fx-background-radius: 6;");
    return card;
  }
}
