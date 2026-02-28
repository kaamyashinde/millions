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
    // NOVICE always qualifies
    assertTrue(PlayerLevel.NOVICE.qualifies(player));

    // INVESTOR requires >= 10 distinct weeks AND net worth >= 1.2x starting money
    assertFalse(PlayerLevel.INVESTOR.qualifies(player));

    // SPECULATOR requires >= 20 distinct weeks AND net worth >= 2x starting money
    assertFalse(PlayerLevel.SPECULATOR.qualifies(player));

    // Add 10 distinct weeks of transactions
    for (int week = 1; week <= 10; week++) {
      Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("10.00"));
      Purchase purchase = new Purchase(share, week);
      player.getTransactionArchive().addTransaction(purchase);
    }

    // Net worth is still 1000 (starting money), need >= 1200 for INVESTOR
    assertFalse(PlayerLevel.INVESTOR.qualifies(player));

    // Add money to meet 1.2x threshold (need net worth >= 1200)
    player.addMoney(new BigDecimal("200.00"));
    assertTrue(PlayerLevel.INVESTOR.qualifies(player));
    assertFalse(PlayerLevel.SPECULATOR.qualifies(player));

    // Add 10 more distinct weeks (total 20)
    for (int week = 11; week <= 20; week++) {
      Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("10.00"));
      Purchase purchase = new Purchase(share, week);
      player.getTransactionArchive().addTransaction(purchase);
    }

    // Still not speculator - need net worth >= 2000
    assertFalse(PlayerLevel.SPECULATOR.qualifies(player));

    // Add money to meet 2x threshold
    player.addMoney(new BigDecimal("800.00"));
    assertTrue(PlayerLevel.SPECULATOR.qualifies(player));
  }

  @Test
  void values() {
    PlayerLevel[] levels = PlayerLevel.values();
    assertEquals(3, levels.length);
    assertEquals(PlayerLevel.SPECULATOR, levels[0]);
    assertEquals(PlayerLevel.INVESTOR, levels[1]);
    assertEquals(PlayerLevel.NOVICE, levels[2]);
  }

  @Test
  void valueOf() {
    assertEquals(PlayerLevel.SPECULATOR, PlayerLevel.valueOf("SPECULATOR"));
    assertEquals(PlayerLevel.INVESTOR, PlayerLevel.valueOf("INVESTOR"));
    assertEquals(PlayerLevel.NOVICE, PlayerLevel.valueOf("NOVICE"));
  }
}
