package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import model.session.PlayerLeaderboardEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests leaderboard sorting, ranks, and highlight behavior.
 */
class PlayerLeaderboardPanelTest {

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
  void defaultSortIsNetWorthDescending() throws Exception {
    PlayerLeaderboardPanel panel = runOnFxThread(() -> new PlayerLeaderboardPanel(List.of(
        entry("Alice", "1000.00", "0.00"),
        entry("Cara", "1300.00", "0.30"),
        entry("Bob", "1400.00", "0.40"))));

    assertEquals(List.of("Bob", "Cara", "Alice"), panel.getDisplayedUsernames());
    assertEquals(List.of(1, 2, 3), panel.getDisplayedRanks());
  }

  @Test
  void returnAscendingKeepsBestRanksAndHighlightsTopThree() throws Exception {
    PlayerLeaderboardPanel panel = runOnFxThread(() -> new PlayerLeaderboardPanel(List.of(
        entry("Alice", "900.00", "-0.10"),
        entry("Bob", "1400.00", "0.40"),
        entry("Cara", "1300.00", "0.30"),
        entry("Dan", "1100.00", "0.10"))));

    runOnFxThread(
        () -> {
          panel.sortByReturnAscending();
          return panel;
        });

    assertEquals(List.of("Alice", "Dan", "Cara", "Bob"), panel.getDisplayedUsernames());
    assertEquals(List.of(4, 3, 2, 1), panel.getDisplayedRanks());
    assertEquals(List.of("Bob", "Cara", "Dan"), panel.getHighlightedUsernames());
  }

  @Test
  void netWorthAscendingSortsLowestFirst() throws Exception {
    PlayerLeaderboardPanel panel = runOnFxThread(() -> new PlayerLeaderboardPanel(List.of(
        entry("Bob", "1400.00", "0.40"),
        entry("Alice", "1000.00", "0.00"),
        entry("Cara", "1300.00", "0.30"))));

    runOnFxThread(
        () -> {
          panel.sortByNetWorthAscending();
          return panel;
        });

    assertEquals(List.of("Alice", "Cara", "Bob"), panel.getDisplayedUsernames());
  }

  private static PlayerLeaderboardEntry entry(String username, String netWorth, String returnPercent) {
    return new PlayerLeaderboardEntry(
        username,
        new BigDecimal(netWorth),
        new BigDecimal(returnPercent));
  }

  private static PlayerLeaderboardPanel runOnFxThread(PanelSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerLeaderboardPanel> ref = new AtomicReference<>();
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
  private interface PanelSupplier {
    PlayerLeaderboardPanel get() throws Exception;
  }
}
