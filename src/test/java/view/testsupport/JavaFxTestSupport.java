package view.testsupport;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Shared JavaFX toolkit startup for view-layer unit tests.
 */
public final class JavaFxTestSupport {

  private JavaFxTestSupport() {}

  /**
   * Starts the JavaFX platform once for the current JVM if it is not already running.
   */
  public static void initToolkit() throws InterruptedException {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await(5, TimeUnit.SECONDS);
    } catch (IllegalStateException ignored) {
      // toolkit already running
    }
  }
}
