package model.learninghub;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks a player's progress through a single {@link Quiz} during a session.
 *
 * <p>This class is intentionally mutable — it accumulates answers as the player progresses
 * through each question. Call {@link #submitAnswer(String)} after each question, then check
 * {@link #isFinished()} to determine when the quiz is complete.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 04-04-2026
 */
public final class QuizAttempt {

  private final Quiz quiz;
  private final List<String> givenAnswerIds;
  private int currentIndex;

  /**
   * Creates a new attempt for the given quiz, starting at the first question.
   *
   * @param quiz the quiz to attempt
   */
  public QuizAttempt(Quiz quiz) {
    if (quiz == null) {
      throw new NullPointerException("quiz cannot be null");
    }
    this.quiz = quiz;
    this.givenAnswerIds = new ArrayList<>();
    this.currentIndex = 0;
  }

  /** Returns the current question, or throws if the quiz is already finished. */
  public QuizQuestion currentQuestion() {
    if (isFinished()) {
      throw new IllegalStateException("Quiz is already finished");
    }
    return quiz.questions().get(currentIndex);
  }

  /** Zero-based index of the current question. */
  public int currentIndex() {
    return currentIndex;
  }

  /** Total number of questions in the quiz. */
  public int totalQuestions() {
    return quiz.questions().size();
  }

  /** Returns {@code true} once all questions have been answered. */
  public boolean isFinished() {
    return currentIndex >= quiz.questions().size();
  }

  /**
   * Records the player's answer for the current question and advances to the next.
   *
   * @param answerId the {@link QuizAnswer#id()} chosen by the player
   * @throws IllegalStateException if the quiz is already finished
   */
  public void submitAnswer(String answerId) {
    if (isFinished()) {
      throw new IllegalStateException("Quiz is already finished");
    }
    givenAnswerIds.add(answerId);
    currentIndex++;
  }

  /**
   * Returns {@code true} if the answer submitted at position {@code i} was correct.
   *
   * @param i zero-based question index
   */
  public boolean wasCorrect(int i) {
    return quiz.questions().get(i).correctAnswerId().equals(givenAnswerIds.get(i));
  }

  /** Returns the number of correctly answered questions so far. */
  public int correctCount() {
    int count = 0;
    for (int i = 0; i < givenAnswerIds.size(); i++) {
      if (wasCorrect(i)) {
        count++;
      }
    }
    return count;
  }

  /** Returns an immutable snapshot of the answer IDs given so far. */
  public List<String> givenAnswerIds() {
    return List.copyOf(givenAnswerIds);
  }

  /** Returns the quiz this attempt belongs to. */
  public Quiz quiz() {
    return quiz;
  }
}
