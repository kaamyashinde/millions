package controller;

import java.util.List;
import java.util.Optional;

import model.learning.quiz.Quiz;
import model.learning.quiz.QuizAttempt;
import model.learning.quiz.QuizSession;
import model.learning.store.QuizContentStore;

/**
 * Manages quiz content access and attempt lifecycle.
 */
public class QuizController {

  /**
   * @return all available quizzes
   */
  public List<Quiz> getAllQuizzes() {
    return QuizContentStore.getAllQuizzesPublic();
  }

  /**
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
   * @return all quiz attempts recorded in this JVM session
   */
  public List<QuizAttempt> getSessionAttempts() {
    return QuizSession.getCompletedAttempts();
  }
}
