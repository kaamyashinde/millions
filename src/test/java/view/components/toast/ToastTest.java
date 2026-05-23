package view.components.toast;

import model.core.player.Portfolio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.geometry.Pos;
import view.components.toast.Toast;
import view.components.toast.ToastMode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Toast component. Requires JavaFX toolkit initialization.
 */
class ToastTest {

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

  private Toast runOnFxThread(ToastSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Toast> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(() -> {
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
  interface ToastSupplier {
    Toast get() throws Exception;
  }

  @Test
  void titleOnlyConstructorError() throws Exception {
    Toast toast = runOnFxThread(() -> new Toast(ToastMode.ERROR, "Error title"));
    assertNotNull(toast);
  }

  @Test
  void titleOnlyConstructorWarning() throws Exception {
    Toast toast = runOnFxThread(() -> new Toast(ToastMode.WARNING, "Warning title"));
    assertNotNull(toast);
  }

  @Test
  void titleOnlyConstructorInfo() throws Exception {
    Toast toast = runOnFxThread(() -> new Toast(ToastMode.INFO, "Info title"));
    assertNotNull(toast);
  }

  @Test
  void titleOnlyConstructorSuccess() throws Exception {
    Toast toast = runOnFxThread(() -> new Toast(ToastMode.SUCCESS, "Success title"));
    assertNotNull(toast);
  }

  @Test
  void titleAndDescriptionConstructor() throws Exception {
    Toast toast = runOnFxThread(
        () -> new Toast(ToastMode.INFO, "Title", "Some description text"));
    assertNotNull(toast);
  }

  @Test
  void fullConstructorWithActionCallback() throws Exception {
    AtomicReference<Boolean> called = new AtomicReference<>(false);
    Toast toast = runOnFxThread(
        () -> new Toast(ToastMode.SUCCESS, "Done", "All good", "Dismiss", () -> called.set(true)));
    assertNotNull(toast);
  }

  @Test
  void fullConstructorWithNullOptionals() throws Exception {
    Toast toast = runOnFxThread(
        () -> new Toast(ToastMode.WARNING, "Watch out", null, null, null));
    assertNotNull(toast);
  }

  @Test
  void nullModeThrows() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> err = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        new Toast(null, "Title");
      } catch (NullPointerException e) {
        err.set(e);
      } finally {
        latch.countDown();
      }
    });
    latch.await(5, TimeUnit.SECONDS);
    assertNotNull(err.get(), "Expected NullPointerException for null mode");
  }

  @Test
  void nullTitleThrows() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> err = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        new Toast(ToastMode.INFO, null);
      } catch (NullPointerException e) {
        err.set(e);
      } finally {
        latch.countDown();
      }
    });
    latch.await(5, TimeUnit.SECONDS);
    assertNotNull(err.get(), "Expected NullPointerException for null title");
  }

  @Test
  void nullOnActionWithLabelDoesNotThrow() throws Exception {
    Toast toast = runOnFxThread(
        () -> new Toast(ToastMode.ERROR, "Error", "desc", "Retry", null));
    assertNotNull(toast);
  }

  @Test
  void toastUsesWideHorizontalLayout() throws Exception {
    Toast toast = runOnFxThread(
        () -> new Toast(ToastMode.INFO, "Saved", "Portfolio updated successfully."));

    assertEquals(360.0, toast.getPrefWidth());
    assertEquals(360.0, toast.getMinWidth());
    assertEquals(360.0, toast.getMaxWidth());
    assertEquals(Pos.CENTER_LEFT, toast.getAlignment());
  }
}
