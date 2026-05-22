package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import model.transaction.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerLevelTest {

  private Player player;
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple");
    stock.addNewSalesPrice(new BigDecimal("10.00"));
    player = new Player("TestPlayer", new BigDecimal("1000.00"));
  }

  @Test
  void qualifies() {
    assertTrue(PlayerLevels.NOVICE.qualifies(player));
    assertFalse(PlayerLevels.INVESTOR.qualifies(player));
    assertFalse(PlayerLevels.SPECULATOR.qualifies(player));

    addDistinctTradingDays(1, 70);

    assertFalse(PlayerLevels.INVESTOR.qualifies(player));

    player.addMoney(new BigDecimal("200.00"));
    assertTrue(PlayerLevels.INVESTOR.qualifies(player));
    assertFalse(PlayerLevels.SPECULATOR.qualifies(player));

    addDistinctTradingDays(71, 140);

    assertFalse(PlayerLevels.SPECULATOR.qualifies(player));

    player.addMoney(new BigDecimal("800.00"));
    assertTrue(PlayerLevels.SPECULATOR.qualifies(player));
  }

  @Test
  void levelAutoUpdates_toInvestor_whenThresholdMet() {
    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());

    addDistinctTradingDays(1, 70);
    player.addMoney(new BigDecimal("200.00"));

    assertEquals(PlayerLevels.INVESTOR, player.getPlayerLevel());
  }

  @Test
  void levelAutoUpdates_toSpeculator_whenThresholdMet() {
    addDistinctTradingDays(1, 140);
    player.addMoney(new BigDecimal("1000.00"));

    assertEquals(PlayerLevels.SPECULATOR, player.getPlayerLevel());
  }

  @Test
  void levelRemainsNovice_whenOnlyDaysMetButNotNetWorth() {
    addDistinctTradingDays(1, 70);
    player.recalculateLevel();

    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());
  }

  /**
   * Adds one purchase transaction per day for the given range (inclusive)
   * to build up distinct trading days in the player's archive.
   */
  private void addDistinctTradingDays(int fromDay, int toDay) {
    for (int day = fromDay; day <= toDay; day++) {
      Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("10.00"));
      Purchase purchase = new Purchase(share, day);
      player.getTransactionArchive().addTransaction(purchase);
    }
  }
}
