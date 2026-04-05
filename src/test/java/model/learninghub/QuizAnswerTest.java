package model.learninghub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import model.learninghub.quiz.QuizAnswer;
import org.junit.jupiter.api.Test;

class QuizAnswerTest {

  @Test
  void validAnswerCreated() {
    QuizAnswer answer = new QuizAnswer("a", "Some text");
    assertEquals("a", answer.id());
    assertEquals("Some text", answer.text());
  }

  @Test
  void nullIdThrows() {
    assertThrows(NullPointerException.class, () -> new QuizAnswer(null, "text"));
  }

  @Test
  void nullTextThrows() {
    assertThrows(NullPointerException.class, () -> new QuizAnswer("a", null));
  }
}
