package model.learning.store;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.learning.quiz.Quiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Static store providing all quiz content for the Learning Hub.
 *
 * <p>Loads 5 questions for each of the 10 learning topics (50 questions total) from
 * {@code learninghub/quizzes.json}. Each {@link Quiz} is linked to a learning topic via
 * {@link Quiz#linkedItemId()}.
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 04-04-2026
 */
public final class QuizContentStore {

  private static final String QUIZZES_RESOURCE = "learninghub/quizzes.json";

  private static final List<Quiz> QUIZZES = loadQuizzes();

  private QuizContentStore() {
  }

  /**
   * Returns the quiz linked to the given learning item id, or empty if none exists.
   *
   * @param itemId the learning item ID to look up
   * @return the matching quiz, or {@link Optional#empty()}
   */
  public static Optional<Quiz> getQuizForItem(String itemId) {
    return QUIZZES.stream()
        .filter(q -> q.linkedItemId().equals(itemId))
        .findFirst();
  }

  /**
   * Returns all quizzes in the store.
   *
   * @return immutable list of all quizzes
   */
  public static List<Quiz> getAllQuizzesPublic() {
    return QUIZZES;
  }

  private static List<Quiz> loadQuizzes() {
    try (InputStream input = QuizContentStore.class.getClassLoader()
        .getResourceAsStream(QUIZZES_RESOURCE)) {
      if (input == null) {
        throw new IllegalStateException("Missing quiz resource: " + QUIZZES_RESOURCE);
      }
      Map<String, List<Quiz>> document = new ObjectMapper()
          .readValue(input, new TypeReference<>() {});
      List<Quiz> quizzes = document.get("quizzes");
      if (quizzes == null || quizzes.isEmpty()) {
        throw new IllegalStateException("Quiz resource contains no quizzes: " + QUIZZES_RESOURCE);
      }
      return List.copyOf(quizzes);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to load quizzes: " + QUIZZES_RESOURCE, exception);
    }
  }
}
