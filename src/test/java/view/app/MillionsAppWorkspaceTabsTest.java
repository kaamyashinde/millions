package view.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.WorkspaceController;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import model.session.ActiveSession;
import model.session.SessionService;
import model.session.SessionServiceFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.app.events.WorkspaceEventBus;

class MillionsAppWorkspaceTabsTest {

  @TempDir
  Path tempDir;

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
  void mainWorkspaceTabsIncludeNotifications() throws Exception {
    TabPane tabs = runOnFxThread(() -> {
      SessionService service = SessionServiceFactory.createLocalProfileSessionService(
          tempDir,
          "/data/demo-stocks.csv",
          MillionsAppWorkspaceTabsTest.class,
          "NYSE");
      ActiveSession session =
          service.register("TabUser", "1234".toCharArray(), new BigDecimal("1000.00"));
      WorkspaceController ctrl = new WorkspaceController(session, service);
      MillionsApp app = new MillionsApp();
      setSessionService(app, service);

      Method buildWorkspaceTabs = MillionsApp.class.getDeclaredMethod(
          "buildWorkspaceTabs",
          WorkspaceController.class,
          Runnable.class,
          WorkspaceEventBus.class);
      buildWorkspaceTabs.setAccessible(true);
      return (TabPane) buildWorkspaceTabs.invoke(
          app,
          ctrl,
          (Runnable) () -> {},
          new WorkspaceEventBus());
    });

    List<String> tabTexts = tabs.getTabs().stream().map(tab -> tab.getText()).toList();

    assertTrue(tabTexts.contains("Notifications"));
  }

  private static void setSessionService(MillionsApp app, SessionService service) throws Exception {
    Field sessionService = MillionsApp.class.getDeclaredField("sessionService");
    sessionService.setAccessible(true);
    sessionService.set(app, service);
  }

  private static <T> T runOnFxThread(FxSupplier<T> supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<T> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(supplier.get());
      } catch (Exception exception) {
        err.set(exception);
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
  private interface FxSupplier<T> {
    T get() throws Exception;
  }
}
