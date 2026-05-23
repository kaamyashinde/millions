package model.learning.quiz;


import java.util.ArrayList;
import java.util.List;

/**
 * Session-level store for completed {@link QuizAttempt quiz attempts}.
 *
 * <p>All state is held in-memory for the duration of the application session.
 * Call {@link #record(QuizAttempt)} exactly once per completed quiz (from the
 * navigation layer), then read aggregate stats from {@link #totalCorrectAcrossSession()}
 * and {@link #totalAnsweredAcrossSession()}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public final class QuizSession {

  private static final List<QuizAttempt> completedAttempts = new ArrayList<>();

  private QuizSession() {
  }

  /**
   * Records a completed quiz attempt.
   *
   * @param attempt the finished attempt to store
   * @throws NullPointerException     if attempt is null
   * @throws IllegalArgumentException if the attempt is not yet finished
   */
  public static void record(QuizAttempt attempt) {
    if (attempt == null) {
      throw new NullPointerException("attempt cannot be null");
    }
    if (!attempt.isFinished()) {
      throw new IllegalArgumentException("Cannot record an unfinished attempt");
    }
    completedAttempts.add(attempt);
  }

  /** Returns an immutable snapshot of all completed attempts this session. */
  public static List<QuizAttempt> getCompletedAttempts() {
    return List.copyOf(completedAttempts);
  }

  /** Total correct answers across all completed quizzes this session. */
  public static int totalCorrectAcrossSession() {
    return completedAttempts.stream().mapToInt(QuizAttempt::correctCount).sum();
  }

  /** Total questions answered across all completed quizzes this session. */
  public static int totalAnsweredAcrossSession() {
    return completedAttempts.stream().mapToInt(QuizAttempt::totalQuestions).sum();
  }

  /**
   * Clears all recorded attempts. Intended for use in unit tests only.
   */
  public static void clear() {
    completedAttempts.clear();
  }
}
