package controller;

import java.util.List;
import java.util.Optional;

import model.learning.quiz.Quiz;
import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizSession;
import model.learning.store.QuizContentStore;

/**
 * Manages quiz content access and attempt lifecycle.
 *
 * <p>Quiz definitions come from {@link QuizContentStore}; completed attempts are stored in
 * {@link QuizSession} for the current JVM session.
 *
 * @author kaamyashinde
 * @contributor kevindmazali
 * @version 1.0.0
 * @since 2026-05-03
 */
public class QuizController {

  /**
   * Creates a quiz controller.
   */
  public QuizController() {
  }

  /**
   * Returns all available quizzes.
   *
   * @return all available quizzes
   */
  public List<Quiz> getAllQuizzes() {
    return QuizContentStore.getAllQuizzesPublic();
  }

  /**
   * Finds the quiz linked to a learning item.
   *
   * @param itemId linked learning item identifier
   * @return quiz for that topic, if present
   */
  public Optional<Quiz> getQuizForItem(String itemId) {
    return QuizContentStore.getQuizForItem(itemId);
  }

  /**
   * Records a completed quiz attempt in the session summary.
   *
   * @param attempt completed attempt
   */
  public void recordAttempt(QuizAttempt attempt) {
    QuizSession.record(attempt);
  }

  /**
   * Returns completed attempts from this JVM session.
   *
   * @return all quiz attempts recorded in this JVM session
   */
  public List<QuizAttempt> getSessionAttempts() {
    return QuizSession.getCompletedAttempts();
  }
}
