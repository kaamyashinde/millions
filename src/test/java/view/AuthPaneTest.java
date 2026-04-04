package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import model.session.PlayerLeaderboardEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests auth-screen leaderboard rendering and return flow.
 */
class AuthPaneTest {

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
  void authPaneShowsLeaderboardRowsAndReturnButtonWorks() throws Exception {
    AtomicBoolean returned = new AtomicBoolean(false);

    AuthPane pane = runOnFxThread(() -> new AuthPane(
        List.of("Alice", "Bob"),
        List.of(
            new PlayerLeaderboardEntry("Bob", new BigDecimal("1400.00"), new BigDecimal("0.40000000")),
            new PlayerLeaderboardEntry("Alice", new BigDecimal("1000.00"), BigDecimal.ZERO)),
        true,
        (username, pin) -> {
        },
        (username, pin, startingMoney) -> {
        },
        () -> returned.set(true)));

    assertEquals(List.of("Bob", "Alice"), pane.getLeaderboardDisplayedUsernames());

    runOnFxThread(
        () -> {
          pane.triggerReturnToSession();
          return pane;
        });

    assertTrue(returned.get());
  }

  private static AuthPane runOnFxThread(PaneSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<AuthPane> ref = new AtomicReference<>();
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
  private interface PaneSupplier {
    AuthPane get() throws Exception;
  }
}
