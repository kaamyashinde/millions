package view.components.learning;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import controller.LearningHubController;
import javafx.scene.layout.VBox;
import model.learning.quiz.QuizAnswer;
import model.learning.quiz.QuizQuestion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizWrongAnswerLinksTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  private static final List<QuizAnswer> ANSWERS = List.of(
      new QuizAnswer("a", "A"),
      new QuizAnswer("b", "B"));

  @Test
  void appendTo_addsTopicAndResourceCards() {
    QuizQuestion question = new QuizQuestion(
        "q1",
        "What?",
        ANSWERS,
        "a",
        "Explanation.",
        "res-investopedia-stocks");

    VBox parent = new VBox();
    AtomicInteger openCount = new AtomicInteger();
    LearningHubController learningHub = new LearningHubController();

    QuizWrongAnswerLinks.appendTo(
        parent,
        learningHub,
        question,
        "what-is-a-stock",
        item -> openCount.incrementAndGet());

    assertTrue(parent.getChildren().size() >= 4);
  }

  @Test
  void appendTo_omitsHubCardWhenCallbackNull() {
    QuizQuestion question = new QuizQuestion(
        "q1",
        "What?",
        ANSWERS,
        "a",
        "Explanation.",
        "res-investopedia-stocks");

    VBox parent = new VBox();
    LearningHubController learningHub = new LearningHubController();

    QuizWrongAnswerLinks.appendTo(
        parent, learningHub, question, "what-is-a-stock", null);

    assertTrue(parent.getChildren().size() >= 2);
    assertTrue(parent.getChildren().size() < 4);
  }
}
