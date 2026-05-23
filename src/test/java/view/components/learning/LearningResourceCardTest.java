package view.components.learning;

import javafx.scene.layout.VBox;

import model.learning.content.LearningResource;
import model.learning.content.LearningResourceType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LearningResourceCardTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void create_rendersResourceFieldsAndClickHandler() {
    LearningResource resource = new LearningResource(
        "res-test",
        "Test Article",
        "Test Source",
        LearningResourceType.ARTICLE,
        "https://example.com/article",
        "A short description.");

    VBox card = LearningResourceCard.create(resource);

    assertEquals(4, card.getChildren().size());
    assertNotNull(card.getOnMouseClicked());
  }
}
