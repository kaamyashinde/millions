package model.learning.store;


import model.learning.quiz.Quiz;
import model.learning.quiz.QuizQuestion;

import java.util.List;
import java.util.Optional;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizContentStoreTest {

  private static final List<String> ALL_TOPIC_IDS = List.of(
      "what-is-a-stock",
      "how-stock-prices-move",
      "what-is-investment-risk",
      "stocks-vs-bonds",
      "compound-interest",
      "diversification-basics",
      "reading-stock-charts",
      "index-funds-explained",
      "dollar-cost-averaging",
      "paper-trading");

  @Test
  void allTenTopicsHaveAQuiz() {
    for (String topicId : ALL_TOPIC_IDS) {
      Optional<Quiz> quiz = QuizContentStore.getQuizForItem(topicId);
      assertTrue(quiz.isPresent(), "Missing quiz for topic: " + topicId);
    }
  }

  @Test
  void eachQuizHasFiveQuestions() {
    for (String topicId : ALL_TOPIC_IDS) {
      Quiz quiz = QuizContentStore.getQuizForItem(topicId).orElseThrow();
      assertEquals(5, quiz.questions().size(),
          "Quiz for " + topicId + " should have 5 questions");
    }
  }

  @Test
  void everyCorrectAnswerIdExistsInAnswerList() {
    for (Quiz quiz : QuizContentStore.getAllQuizzesPublic()) {
      for (QuizQuestion q : quiz.questions()) {
        boolean found = q.answers().stream()
            .anyMatch(a -> a.id().equals(q.correctAnswerId()));
        assertTrue(found,
            "correctAnswerId '" + q.correctAnswerId()
            + "' not found in answers for question " + q.id());
      }
    }
  }

  @Test
  void eachQuizHasFourAnswerOptions() {
    for (Quiz quiz : QuizContentStore.getAllQuizzesPublic()) {
      for (QuizQuestion q : quiz.questions()) {
        assertEquals(4, q.answers().size(),
            "Question " + q.id() + " should have 4 answer options");
      }
    }
  }

  @Test
  void unknownTopicIdReturnsEmpty() {
    Optional<Quiz> result = QuizContentStore.getQuizForItem("non-existent-topic");
    assertTrue(result.isEmpty());
  }

  @Test
  void quizLinkedItemIdMatchesTopicId() {
    for (String topicId : ALL_TOPIC_IDS) {
      Quiz quiz = QuizContentStore.getQuizForItem(topicId).orElseThrow();
      assertEquals(topicId, quiz.linkedItemId());
    }
  }

  @Test
  void allQuizzesReturnsAllTen() {
    assertEquals(10, QuizContentStore.getAllQuizzesPublic().size());
  }

  @Test
  void privateConstructor_isCoveredForUtilityClass() throws Exception {
    Constructor<QuizContentStore> constructor = QuizContentStore.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    constructor.newInstance();
  }

  @Test
  void isolatedStoreFailsWhenQuizResourceIsMissing() throws Exception {
    try (URLClassLoader loader = isolatedStoreLoader(null)) {
      assertThrows(
          ExceptionInInitializerError.class,
          () -> Class.forName("model.learning.store.QuizContentStore", true, loader));
    }
  }

  @Test
  void isolatedStoreFailsWhenQuizResourceIsEmpty() throws Exception {
    try (URLClassLoader loader = isolatedStoreLoader("{\"quizzes\":[]}")) {
      assertThrows(
          ExceptionInInitializerError.class,
          () -> Class.forName("model.learning.store.QuizContentStore", true, loader));
    }
  }

  @Test
  void isolatedStoreFailsWhenQuizJsonIsMalformed() throws Exception {
    try (URLClassLoader loader = isolatedStoreLoader("not-json")) {
      assertThrows(
          ExceptionInInitializerError.class,
          () -> Class.forName("model.learning.store.QuizContentStore", true, loader));
    }
  }

  private static URLClassLoader isolatedStoreLoader(String quizJson) throws Exception {
    URL classes = Path.of(System.getProperty("user.dir"), "target", "classes")
        .toUri()
        .toURL();
    return new ChildFirstStoreLoader(
        classes,
        "model.learning.store.QuizContentStore",
        "learninghub/quizzes.json",
        quizJson);
  }

  private static final class ChildFirstStoreLoader extends URLClassLoader {

    private final String childFirstClassName;
    private final String resourceName;
    private final String resourceContent;

    private ChildFirstStoreLoader(
        URL classes,
        String childFirstClassName,
        String resourceName,
        String resourceContent) {
      super(new URL[] {classes}, ClassLoader.getSystemClassLoader());
      this.childFirstClassName = childFirstClassName;
      this.resourceName = resourceName;
      this.resourceContent = resourceContent;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      if (name.equals(childFirstClassName) || name.startsWith(childFirstClassName + "$")) {
        Class<?> loaded = findLoadedClass(name);
        if (loaded == null) {
          loaded = findClass(name);
        }
        if (resolve) {
          resolveClass(loaded);
        }
        return loaded;
      }
      return super.loadClass(name, resolve);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
      if (name.equals(resourceName)) {
        if (resourceContent == null) {
          return null;
        }
        return new ByteArrayInputStream(resourceContent.getBytes(StandardCharsets.UTF_8));
      }
      return super.getResourceAsStream(name);
    }
  }
}
