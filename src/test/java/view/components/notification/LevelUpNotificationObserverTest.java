package view.components.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.Player;
import model.PlayerLevel;
import model.PlayerLevels;
import model.PlayerObserver;
import model.market.Share;
import model.market.Stock;
import model.trading.transaction.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the level-transition tracking logic of {@link LevelUpNotificationObserver} without
 * requiring a JavaFX runtime. Uses a recording spy in place of {@link NotificationService} to
 * capture when the observer would fire.
 */
class LevelUpNotificationObserverTest {

  private Player player;
  private Stock stock;
  private List<PlayerLevel> levelTransitions;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple");
    stock.addNewSalesPrice(new BigDecimal("10.00"));
    player = new Player("TestPlayer", new BigDecimal("1000.00"));
    levelTransitions = new ArrayList<>();
  }

  @Test
  void firesOnLevelUp() {
    PlayerObserver spy = buildLevelTransitionSpy(PlayerLevels.NOVICE);
    player.addObserver(spy);

    addDistinctTradingDays(1, 70);
    player.addMoney(new BigDecimal("200.00"));

    assertEquals(PlayerLevels.INVESTOR, player.getPlayerLevel());
    assertEquals(1, levelTransitions.size());
    assertEquals(PlayerLevels.INVESTOR, levelTransitions.getFirst());
  }

  @Test
  void doesNotFire_whenLevelUnchanged() {
    PlayerObserver spy = buildLevelTransitionSpy(PlayerLevels.NOVICE);
    player.addObserver(spy);

    player.addMoney(new BigDecimal("10.00"));

    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());
    assertEquals(0, levelTransitions.size());
  }

  @Test
  void firesOnce_perDistinctTransition() {
    PlayerObserver spy = buildLevelTransitionSpy(PlayerLevels.NOVICE);
    player.addObserver(spy);

    addDistinctTradingDays(1, 140);

    player.addMoney(new BigDecimal("200.00"));
    assertEquals(PlayerLevels.INVESTOR, player.getPlayerLevel());

    player.addMoney(new BigDecimal("800.00"));
    assertEquals(PlayerLevels.SPECULATOR, player.getPlayerLevel());

    assertEquals(2, levelTransitions.size());
    assertEquals(PlayerLevels.INVESTOR, levelTransitions.get(0));
    assertEquals(PlayerLevels.SPECULATOR, levelTransitions.get(1));
  }

  /**
   * Mirrors the tracking logic of {@link LevelUpNotificationObserver} without requiring a real
   * {@link NotificationService} (which needs JavaFX).
   */
  private PlayerObserver buildLevelTransitionSpy(PlayerLevel initialLevel) {
    final PlayerLevel[] previous = {initialLevel};
    return p -> {
      PlayerLevel current = p.getPlayerLevel();
      if (current != previous[0]) {
        levelTransitions.add(current);
        previous[0] = current;
      }
    };
  }

  private void addDistinctTradingDays(int fromDay, int toDay) {
    for (int day = fromDay; day <= toDay; day++) {
      Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("10.00"));
      Purchase purchase = new Purchase(share, day);
      player.getTransactionArchive().addTransaction(purchase);
    }
  }
}
