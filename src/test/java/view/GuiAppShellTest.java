package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import model.Stock;
import model.fund.Fund;
import model.fund.FundComponent;
import model.persistence.GameStateRepository;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.ProfilePreferencesRepository;
import model.persistence.SavedRunRepository;
import model.persistence.UserAccountRepository;
import model.session.ActiveSession;
import model.session.SessionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.components.toast.ToastMode;

/**
 * Tests the session-aware JavaFX shell for register, logout, and switch-user behavior.
 */
class GuiAppShellTest {

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
  void registerThenLogoutSwapsBetweenAuthAndWorkspace() throws Exception {
    SessionService sessionService = createSessionService();
    GuiAppShell shell = runOnFxThread(() -> new GuiAppShell(sessionService, new SessionWorkspaceFactory(), false));

    assertTrue(shell.isShowingAuthView());

    runOnFxThread(() -> {
      shell.submitRegistration("Alice", "1234", "1000.00");
      return shell;
    });

    assertFalse(shell.isShowingAuthView());
    assertEquals("Alice", shell.getWorkspaceView().getDisplayedUsername());

    runOnFxThread(() -> {
      shell.logoutActiveUser();
      return shell;
    });

    assertTrue(shell.isShowingAuthView());
    assertEquals(1, shell.getAuthPane().getRegisteredUserCount());
  }

  @Test
  void switchingUsersRebuildsWorkspaceAndAvoidsCrossUserLeakage() throws Exception {
    SessionService sessionService = createSessionService();
    GuiAppShell shell = runOnFxThread(() -> new GuiAppShell(sessionService, new SessionWorkspaceFactory(), false));

    runOnFxThread(() -> {
      shell.submitRegistration("Alice", "1234", "1000.00");
      return shell;
    });

    ActiveSession alice = sessionService.getActiveSession().orElseThrow();
    alice.exchange().buy("AAPL", BigDecimal.ONE, alice.player());
    sessionService.saveActiveSession();

    runOnFxThread(() -> {
      shell.getWorkspaceView().refreshAll();
      shell.getWorkspaceView().getNotificationService().show(ToastMode.INFO, "Alice note");
      return shell;
    });

    runOnFxThread(() -> {
      shell.beginSwitchUserFlow();
      return shell;
    });

    assertTrue(shell.isShowingAuthView());
    assertEquals(1, shell.getAuthPane().getRegisteredUserCount());

    runOnFxThread(() -> {
      shell.submitRegistration("Bob", "5678", "2000.00");
      return shell;
    });

    assertFalse(shell.isShowingAuthView());
    assertEquals("Bob", shell.getWorkspaceView().getDisplayedUsername());
    assertEquals(0, shell.getWorkspaceView().getPlayerPanel().getHoldingCount());
    assertEquals(2, shell.getWorkspaceView().getNotificationService().getItems().size());

    runOnFxThread(() -> {
      shell.beginSwitchUserFlow();
      return shell;
    });
    runOnFxThread(() -> {
      shell.submitLogin("Alice", "1234");
      return shell;
    });

    assertEquals("Alice", shell.getWorkspaceView().getDisplayedUsername());
    assertEquals(1, shell.getWorkspaceView().getPlayerPanel().getHoldingCount());
    assertEquals(2, shell.getWorkspaceView().getNotificationService().getItems().size());
  }

  @Test
  void switchUserFlowShowsLiveLeaderboardAndCanReturnToCurrentSession() throws Exception {
    SessionService sessionService = createSessionService();
    GuiAppShell shell = runOnFxThread(() -> new GuiAppShell(sessionService));

    runOnFxThread(() -> {
      shell.submitRegistration("Alice", "1234", "1000.00");
      return shell;
    });

    ActiveSession alice = sessionService.getActiveSession().orElseThrow();
    sessionService.saveActiveSession();
    alice.player().addMoney(new BigDecimal("50.00"));

    runOnFxThread(() -> {
      shell.beginSwitchUserFlow();
      return shell;
    });

    assertTrue(shell.isShowingAuthView());
    assertEquals("1050.00", shell.getAuthPane().getLeaderboardNetWorthForUser("Alice"));

    runOnFxThread(() -> {
      shell.getAuthPane().triggerReturnToSession();
      return shell;
    });

    assertFalse(shell.isShowingAuthView());
    assertEquals("Alice", shell.getWorkspaceView().getDisplayedUsername());
  }

  private SessionService createSessionService() {
    return new SessionService(
        new UserAccountRepository(tempDir),
        new GameStateRepository(tempDir),
        new SavedRunRepository(tempDir),
        new ProfilePreferencesRepository(tempDir),
        new PinHashingService(),
        GuiAppShellTest::sampleMarketData,
        "NYSE",
        tempDir);
  }

  private static MarketData sampleMarketData() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("150.00"));

    Stock microsoft = new Stock("MSFT", "Microsoft Corp.");
    microsoft.addNewSalesPrice(new BigDecimal("300.00"));

    Fund blend = new Fund(
        "BLEND",
        "Blend Fund",
        List.of(
            new FundComponent(apple, new BigDecimal("0.60")),
            new FundComponent(microsoft, new BigDecimal("0.40"))));

    return new MarketData(List.of(apple, microsoft), List.of(blend));
  }

  private static GuiAppShell runOnFxThread(ShellSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<GuiAppShell> ref = new AtomicReference<>();
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
  private interface ShellSupplier {
    GuiAppShell get() throws Exception;
  }
}
