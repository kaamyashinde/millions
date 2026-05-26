package model.learning.quiz;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Tracks a player's progress through a single {@link Quiz} during a session.
 *
 * <p>This class is intentionally mutable — it accumulates answers as the player progresses
 * through each question. Call {@link #submitAnswer(String)} after each question, then check
 * {@link #isFinished()} to determine when the quiz is complete.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
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

  /**
   * Returns the current question.
   *
   * @return current question
   */
  public QuizQuestion currentQuestion() {
    if (isFinished()) {
      throw new IllegalStateException("Quiz is already finished");
    }
    return quiz.questions().get(currentIndex);
  }

  /**
   * Returns the zero-based index of the current question.
   *
   * @return current question index
   */
  public int currentIndex() {
    return currentIndex;
  }

  /**
   * Returns the total number of questions in the quiz.
   *
   * @return total question count
   */
  public int totalQuestions() {
    return quiz.questions().size();
  }

  /**
   * Returns whether all questions have been answered.
   *
   * @return {@code true} when the quiz is finished
   */
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
   * @return {@code true} when the submitted answer was correct
   */
  public boolean wasCorrect(int i) {
    return quiz.questions().get(i).correctAnswerId().equals(givenAnswerIds.get(i));
  }

  /**
   * Returns the number of correctly answered questions so far.
   *
   * @return correct answer count
   */
  public int correctCount() {
    return (int) IntStream.range(0, givenAnswerIds.size())
        .filter(this::wasCorrect)
        .count();
  }

  /**
   * Returns an immutable snapshot of the answer IDs given so far.
   *
   * @return submitted answer IDs
   */
  public List<String> givenAnswerIds() {
    return List.copyOf(givenAnswerIds);
  }

  /**
   * Returns the quiz this attempt belongs to.
   *
   * @return attempted quiz
   */
  public Quiz quiz() {
    return quiz;
  }
}
