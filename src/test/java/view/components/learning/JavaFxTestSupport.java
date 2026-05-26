package view.components.learning;

import javafx.application.Platform;

/**
 * Initializes JavaFX once for component tests.
 */
final class JavaFxTestSupport {

  private static volatile boolean started;

  private JavaFxTestSupport() {}

  static void ensureStarted() {
    if (started) {
      return;
    }
    synchronized (JavaFxTestSupport.class) {
      if (started) {
        return;
      }
      try {
        Platform.startup(() -> {});
      } catch (IllegalStateException ignored) {
        // Toolkit already started by another test class
      }
      started = true;
    }
  }
}
