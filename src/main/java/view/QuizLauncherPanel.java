package view;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import model.learninghub.Quiz;
import model.learninghub.QuizAttempt;
import model.learninghub.QuizContentStore;
import model.learninghub.QuizSession;

/**
 * Top-level panel for the Quiz tab. Shows a list of all available quizzes and
 * handles the full quiz-play flow (quiz → result → back to launcher).
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public class QuizLauncherPanel extends BorderPane {

  private static final String COLOR_BG_CARD = "#1e1e1e";
  private static final String COLOR_BORDER_ACCENT = "#2196F3";
  private static final String COLOR_HEADING = "#e0e0e0";
  private static final String COLOR_SUBTITLE = "#9e9e9e";

  private javafx.scene.Node launcherView;

  /** Builds the panel and shows the quiz list. */
  public QuizLauncherPanel() {
    setPadding(new Insets(16));

    Text heading = new Text("Quizzes");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    heading.setStyle("-fx-fill: " + COLOR_HEADING + ";");

    Label subtitle = new Label("Test your investing knowledge across all topics.");
    subtitle.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + ";");

    VBox top = new VBox(4, heading, subtitle);
    top.setPadding(new Insets(0, 0, 12, 0));
    setTop(top);

    VBox list = new VBox(10);
    List<Quiz> quizzes = QuizContentStore.getAllQuizzesPublic();
    for (Quiz quiz : quizzes) {
      list.getChildren().add(buildQuizCard(quiz));
    }

    ScrollPane scroll = new ScrollPane(list);
    scroll.setFitToWidth(true);
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    this.launcherView = scroll;
    setCenter(launcherView);
  }

  private javafx.scene.Node buildQuizCard(Quiz quiz) {
    Label title = new Label(quiz.title());
    title.setStyle(
        "-fx-text-fill: " + COLOR_HEADING + ";"
        + "-fx-font-weight: bold;"
        + "-fx-font-size: 13;");
    title.setWrapText(true);

    Label meta = new Label(quiz.questions().size() + " questions");
    meta.setStyle("-fx-text-fill: " + COLOR_SUBTITLE + "; -fx-font-size: 11;");

    Label cta = new Label("Start →");
    cta.setStyle(
        "-fx-text-fill: " + COLOR_BORDER_ACCENT + ";"
        + "-fx-font-size: 11;"
        + "-fx-font-weight: bold;");

    VBox card = new VBox(4, title, meta, cta);
    card.setPadding(new Insets(14));
    card.setMaxWidth(Double.MAX_VALUE);
    card.setStyle(
        "-fx-background-color: " + COLOR_BG_CARD + ";"
        + "-fx-border-color: " + COLOR_BORDER_ACCENT + ";"
        + "-fx-border-radius: 8;"
        + "-fx-background-radius: 8;"
        + "-fx-cursor: hand;");

    card.setOnMouseClicked(_ -> startQuiz(quiz));
    return card;
  }

  private void startQuiz(Quiz quiz) {
    QuizAttempt attempt = new QuizAttempt(quiz);
    Runnable backToLauncher = this::showLauncher;
    setCenter(new QuizView(
        attempt,
        backToLauncher,
        () -> showResult(attempt, backToLauncher)));
  }

  private void showResult(QuizAttempt attempt, Runnable onBackToTopic) {
    QuizSession.record(attempt);
    setCenter(new QuizResultView(attempt, onBackToTopic, this::showLauncher));
  }

  private void showLauncher() {
    setCenter(launcherView);
  }
}
