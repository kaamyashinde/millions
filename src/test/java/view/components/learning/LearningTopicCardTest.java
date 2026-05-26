package view.components.learning;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import view.JavaFxTestSupport;
import javafx.scene.layout.VBox;

import model.learning.store.LearningContentStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LearningTopicCardTest {

  @BeforeAll
  static void initJavaFx() throws InterruptedException {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void create_rendersTopicFieldsAndClickHandler() {
    var item = LearningContentStore.getFeaturedItems().get(0);

    VBox card = LearningTopicCard.create(item, () -> {});

    assertEquals(4, card.getChildren().size());
    assertNotNull(card.getOnMouseClicked());
  }

  @Test
  void click_invokesOnOpen() throws Exception {
    var item = LearningContentStore.getFeaturedItems().get(0);
    AtomicBoolean opened = new AtomicBoolean();
    CountDownLatch latch = new CountDownLatch(1);

    VBox card = LearningTopicCard.create(item, () -> {
      opened.set(true);
      latch.countDown();
    });

    Platform.runLater(() -> card.getOnMouseClicked().handle(null));
    assertTrue(latch.await(3, TimeUnit.SECONDS));
    assertTrue(opened.get());
  }
}
