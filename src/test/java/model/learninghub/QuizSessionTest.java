package model.learninghub;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizSessionTest {

  private Quiz quiz;

  @BeforeEach
  void setUp() {
    QuizSession.clear();

    List<QuizAnswer> answers = List.of(
        new QuizAnswer("a", "Option A"),
        new QuizAnswer("b", "Option B"));

    quiz = new Quiz("q", "topic", "Test Quiz", List.of(
        new QuizQuestion("q1", "Q1?", answers, "a", "Exp.", null),
        new QuizQuestion("q2", "Q2?", answers, "b", "Exp.", null)));
  }

  private QuizAttempt completedAttempt(String... answers) {
    QuizAttempt attempt = new QuizAttempt(quiz);
    for (String answer : answers) {
      attempt.submitAnswer(answer);
    }
    return attempt;
  }

  @Test
  void recordsCompletedAttempt() {
    QuizAttempt attempt = completedAttempt("a", "b");
    QuizSession.record(attempt);
    assertEquals(1, QuizSession.getCompletedAttempts().size());
  }

  @Test
  void nullAttemptThrows() {
    assertThrows(NullPointerException.class, () -> QuizSession.record(null));
  }

  @Test
  void unfinishedAttemptThrows() {
    QuizAttempt attempt = new QuizAttempt(quiz);
    attempt.submitAnswer("a"); // only first answered — not finished
    assertThrows(IllegalArgumentException.class, () -> QuizSession.record(attempt));
  }

  @Test
  void getCompletedAttemptsReturnsDefensiveCopy() {
    List<QuizAttempt> snapshot = QuizSession.getCompletedAttempts();
    assertThrows(UnsupportedOperationException.class,
        () -> snapshot.add(completedAttempt("a", "b")));
  }

  @Test
  void totalCorrectAcrossSession() {
    QuizSession.record(completedAttempt("a", "b")); // both correct
    QuizSession.record(completedAttempt("b", "a")); // both wrong
    assertEquals(2, QuizSession.totalCorrectAcrossSession());
  }

  @Test
  void totalAnsweredAcrossSession() {
    QuizSession.record(completedAttempt("a", "b"));
    QuizSession.record(completedAttempt("a", "b"));
    assertEquals(4, QuizSession.totalAnsweredAcrossSession());
  }

  @Test
  void clearRemovesAllAttempts() {
    QuizSession.record(completedAttempt("a", "b"));
    QuizSession.clear();
    assertEquals(0, QuizSession.getCompletedAttempts().size());
  }
}
