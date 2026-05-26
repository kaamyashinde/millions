package view.theme;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThemeStylesTest {

  @BeforeAll
  static void initJavaFx() throws InterruptedException {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await(5, TimeUnit.SECONDS);
    } catch (IllegalStateException ignored) {
      // toolkit already running
    }
  }

  @Test
  void styleDangerButtonAddsDangerClass() {
    Button button = new Button("Sell");

    ThemeStyles.styleDangerButton(button);

    assertTrue(button.getStyleClass().contains("btn"));
    assertTrue(button.getStyleClass().contains("btn-danger"));
  }
}
