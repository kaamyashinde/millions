package model.learninghub;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizAttemptTest {

  private Quiz quiz;

  @BeforeEach
  void setUp() {
    List<QuizAnswer> answers = List.of(
        new QuizAnswer("a", "Option A"),
        new QuizAnswer("b", "Option B"));

    quiz = new Quiz("q", "topic", "Test Quiz", List.of(
        new QuizQuestion("q1", "Q1?", answers, "a", "Explanation 1", null),
        new QuizQuestion("q2", "Q2?", answers, "b", "Explanation 2", null)));
  }

  @Test
  void nullQuizThrows() {
    assertThrows(NullPointerException.class, () -> new QuizAttempt(null));
  }

  @Test
  void initialStateIsFirstQuestion() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    assertFalse(attempt.isFinished());
    assertEquals(0, attempt.currentIndex());
    assertEquals(2, attempt.totalQuestions());
    assertEquals("q1", attempt.currentQuestion().id());
  }

  @Test
  void submitAnswerAdvancesIndex() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a");
    assertEquals(1, attempt.currentIndex());
    assertEquals("q2", attempt.currentQuestion().id());
  }

  @Test
  void finishedAfterAllQuestionsAnswered() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a");
    attempt.submitAnswer("b");
    assertTrue(attempt.isFinished());
  }

  @Test
  void currentQuestionThrowsWhenFinished() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a");
    attempt.submitAnswer("b");
    assertThrows(IllegalStateException.class, attempt::currentQuestion);
  }

  @Test
  void submitAnswerThrowsWhenFinished() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a");
    attempt.submitAnswer("b");
    assertThrows(IllegalStateException.class, () -> attempt.submitAnswer("a"));
  }

  @Test
  void wasCorrectReturnsTrueForCorrectAnswer() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a"); // correct for q1
    attempt.submitAnswer("a"); // wrong for q2 (correct is "b")
    assertTrue(attempt.wasCorrect(0));
    assertFalse(attempt.wasCorrect(1));
  }

  @Test
  void correctCountReflectsActualCorrectAnswers() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a"); // correct
    attempt.submitAnswer("b"); // correct
    assertEquals(2, attempt.correctCount());
  }

  @Test
  void givenAnswerIdsIsImmutableSnapshot() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a");
    List<String> snapshot = attempt.givenAnswerIds();
    assertThrows(UnsupportedOperationException.class, () -> snapshot.add("x"));
  }
}
