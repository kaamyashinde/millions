package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import model.transaction.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerLevelObserverTest {

  private Player player;
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple");
    stock.addNewSalesPrice(new BigDecimal("10.00"));
    player = new Player("TestPlayer", new BigDecimal("1000.00"));
  }

  @Test
  void observerRecalculatesLevel_afterAddMoney() {
    addDistinctTradingDays(1, 70);
    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());

    player.addMoney(new BigDecimal("200.00"));

    assertEquals(PlayerLevels.INVESTOR, player.getPlayerLevel());
  }

  @Test
  void levelStaysNovice_whenOnlyMoneyChanges() {
    player.addMoney(new BigDecimal("5000.00"));

    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());
  }

  @Test
  void levelUpdatesFromRestore() {
    addDistinctTradingDays(1, 70);
    Player restored = Player.restore(
        "Restored",
        new BigDecimal("1000.00"),
        new BigDecimal("1200.00"),
        java.util.List.of(),
        new java.util.ArrayList<>(player.getTransactionArchive().getAllTransactions()),
        java.util.List.of());

    assertEquals(PlayerLevels.INVESTOR, restored.getPlayerLevel());
  }

  private void addDistinctTradingDays(int fromDay, int toDay) {
    for (int day = fromDay; day <= toDay; day++) {
      Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("10.00"));
      Purchase purchase = new Purchase(share, day);
      player.getTransactionArchive().addTransaction(purchase);
    }
  }
}
