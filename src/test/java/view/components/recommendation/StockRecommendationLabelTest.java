package view.components.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import view.components.recommendation.StockRecommendationLabel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import recommendation.StockRecommendation;

/**
 * Tests for the stock recommendation badge component.
 */
class StockRecommendationLabelTest {

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
  void constructorRendersBuyTextAndStyle() throws Exception {
    StockRecommendationLabel label = runOnFxThread(() -> new StockRecommendationLabel(StockRecommendation.BUY));

    assertEquals(StockRecommendation.BUY, label.getRecommendation());
    assertEquals("BUY", label.getText());
    assertNotNull(label.getStyle());
  }

  @Test
  void setRecommendationUpdatesBadgeText() throws Exception {
    StockRecommendationLabel label = runOnFxThread(() -> new StockRecommendationLabel(StockRecommendation.HOLD));

    runOnFxThread(
        () -> {
          label.setRecommendation(StockRecommendation.SELL);
          return label;
        });

    assertEquals(StockRecommendation.SELL, label.getRecommendation());
    assertEquals("SELL", label.getText());
    assertNotNull(label.getStyle());
  }

  @Test
  void constructorRejectsNullRecommendation() {
    NullPointerException error =
        assertThrows(NullPointerException.class, () -> runOnFxThread(() -> new StockRecommendationLabel(null)));

    assertEquals("Recommendation cannot be null", error.getMessage());
  }

  /**
   * Runs a supplier on the JavaFX thread and returns its result.
   *
   * @param supplier supplier to execute on the FX thread
   * @return supplier result
   * @throws Exception any exception thrown by the supplier
   */
  private static StockRecommendationLabel runOnFxThread(LabelSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<StockRecommendationLabel> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            ref.set(supplier.get());
          } catch (Exception e) {
            err.set(e);
          } finally {
            latch.countDown();
          }
        });
    latch.await(5, TimeUnit.SECONDS);
    if (err.get() != null) {
      throw err.get();
    }
    return ref.get();
  }

  @FunctionalInterface
  private interface LabelSupplier {
    StockRecommendationLabel get() throws Exception;
  }
}
