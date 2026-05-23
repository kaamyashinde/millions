package controller;

import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizSession;

/**
 * Manages quiz attempt lifecycle and session recording.
 */
public class QuizController {

  /**
   * Records a completed quiz attempt in the session summary.
   *
   * @param attempt completed attempt
   */
  public void recordAttempt(QuizAttempt attempt) {
    QuizSession.record(attempt);
  }

  /**
   * @return all quiz attempts recorded in this JVM session
   */
  public java.util.List<QuizAttempt> getSessionAttempts() {
    return QuizSession.getCompletedAttempts();
  }
}
