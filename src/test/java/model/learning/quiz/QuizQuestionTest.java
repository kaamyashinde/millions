package model.learning.quiz;


import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuizQuestionTest {

  private static final List<QuizAnswer> VALID_ANSWERS = List.of(
      new QuizAnswer("a", "Option A"),
      new QuizAnswer("b", "Option B"),
      new QuizAnswer("c", "Option C"),
      new QuizAnswer("d", "Option D"));

  @Test
  void validQuestionCreated() {
    QuizQuestion q = new QuizQuestion("q1", "What?", VALID_ANSWERS, "b", "Explanation.", null);
    assertEquals("q1", q.id());
    assertEquals("b", q.correctAnswerId());
    assertNull(q.linkedResourceId());
  }

  @Test
  void nullIdThrows() {
    assertThrows(NullPointerException.class,
        () -> new QuizQuestion(null, "What?", VALID_ANSWERS, "a", "Exp.", null));
  }

  @Test
  void nullQuestionTextThrows() {
    assertThrows(NullPointerException.class,
        () -> new QuizQuestion("q1", null, VALID_ANSWERS, "a", "Exp.", null));
  }

  @Test
  void nullAnswersThrows() {
    assertThrows(NullPointerException.class,
        () -> new QuizQuestion("q1", "What?", null, "a", "Exp.", null));
  }

  @Test
  void nullCorrectAnswerIdThrows() {
    assertThrows(NullPointerException.class,
        () -> new QuizQuestion("q1", "What?", VALID_ANSWERS, null, "Exp.", null));
  }

  @Test
  void nullExplanationThrows() {
    assertThrows(NullPointerException.class,
        () -> new QuizQuestion("q1", "What?", VALID_ANSWERS, "a", null, null));
  }

  @Test
  void fewerThanTwoAnswersThrows() {
    List<QuizAnswer> single = List.of(new QuizAnswer("a", "Only one"));
    assertThrows(IllegalArgumentException.class,
        () -> new QuizQuestion("q1", "What?", single, "a", "Exp.", null));
  }

  @Test
  void correctAnswerIdNotInListThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> new QuizQuestion("q1", "What?", VALID_ANSWERS, "z", "Exp.", null));
  }

  @Test
  void answersListIsImmutable() {
    QuizQuestion q = new QuizQuestion("q1", "What?", VALID_ANSWERS, "a", "Exp.", null);
    assertThrows(UnsupportedOperationException.class, () -> q.answers().add(new QuizAnswer("e", "Extra")));
  }
}
