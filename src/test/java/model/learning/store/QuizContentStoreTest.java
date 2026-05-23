package model.learning.store;


import model.learning.quiz.Quiz;
import model.learning.quiz.QuizQuestion;

import java.util.List;
import java.util.Optional;

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
}
