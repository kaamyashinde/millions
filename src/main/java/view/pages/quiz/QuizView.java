package view.pages.quiz;

import java.util.List;
import java.util.function.Consumer;

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

import model.learning.content.LearningItem;
import model.learning.quiz.QuizAnswer;
import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizQuestion;
import view.components.learning.QuizWrongAnswerLinks;
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
  private final Consumer<LearningItem> onOpenHubTopic;

  private Label progressLabel;
  private VBox centerContent;

  /**
   * Builds the quiz view.
   *
   * @param attempt         the quiz attempt tracking progress and answers
   * @param onBack          called when the back button is clicked
   * @param onFinish        called when the player has answered all questions
   * @param onOpenHubTopic  called when the player opens the linked hub topic from feedback
   */
  public QuizView(
      QuizAttempt attempt,
      Runnable onBack,
      Runnable onFinish,
      Consumer<LearningItem> onOpenHubTopic) {
    this.attempt = attempt;
    this.onFinish = onFinish;
    this.onOpenHubTopic = onOpenHubTopic;

    ThemeStyles.addStyleClasses(this, "quiz-root");
    setPadding(new Insets(16));

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

    centerContent = new VBox(16);
    ScrollPane scroll = new ScrollPane(centerContent);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setCenter(scroll);

    refreshQuestion();
  }

  private void refreshQuestion() {
    centerContent.getChildren().clear();

    QuizQuestion q = attempt.currentQuestion();
    int current = attempt.currentIndex() + 1;
    int total = attempt.totalQuestions();

    progressLabel.setText("Question " + current + " of " + total);

    Label questionText = new Label(q.questionText());
    questionText.setWrapText(true);
    ThemeStyles.addStyleClasses(questionText, "quiz-question-text");

    VBox questionCard = new VBox(questionText);
    questionCard.setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(questionCard, "quiz-question-card");

    VBox answersBox = new VBox(8);
    List<QuizAnswer> answers = q.answers();
    Button[] answerButtons = new Button[answers.size()];

    VBox feedbackPane = new VBox(8);
    feedbackPane.setVisible(false);
    feedbackPane.setManaged(false);
    feedbackPane.setPadding(new Insets(12));
    ThemeStyles.addStyleClasses(feedbackPane, "quiz-feedback");

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
        for (Button b : answerButtons) {
          b.setDisable(true);
        }

        String correctId = q.correctAnswerId();
        boolean correct = answer.id().equals(correctId);

        for (Button b : answerButtons) {
          if (b.getUserData().equals(correctId)) {
            ThemeStyles.addStyleClasses(b, "quiz-answer-correct");
          }
        }

        if (!correct) {
          ThemeStyles.addStyleClasses(btn, "quiz-answer-wrong");
        }

        buildFeedbackContent(feedbackPane, q, correct);
        feedbackPane.setVisible(true);
        feedbackPane.setManaged(true);

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

  private void buildFeedbackContent(VBox feedbackPane, QuizQuestion q, boolean correct) {
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

    if (!correct) {
      QuizWrongAnswerLinks.appendTo(
          feedbackPane, q, attempt.quiz().linkedItemId(), onOpenHubTopic);
    }
  }
}
