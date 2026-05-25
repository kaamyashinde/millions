package model.core.player;


import model.core.player.level.PlayerLevels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerTest {

  private Player player;

  @BeforeEach
  void setUp() {
    player = new Player("Alice", new BigDecimal("1000.00"));
  }

  @Test
  void getName() {
    assertEquals("Alice", player.getName());
  }

  @Test
  void getStartingMoney() {
    assertEquals(new BigDecimal("1000.00"), player.getStartingMoney());
  }

  @Test
  void getMoney() {
    assertEquals(new BigDecimal("1000.00"), player.getMoney());
  }

  @Test
  void addMoney() {
    player.addMoney(new BigDecimal("250.50"));
    assertEquals(new BigDecimal("1250.50"), player.getMoney());
  }

  @Test
  void withdrawMoney() {
    player.withdrawMoney(new BigDecimal("300.00"));
    assertEquals(new BigDecimal("700.00"), player.getMoney());
  }

  @Test
  void getPortfolio() {
    assertNotNull(player.getPortfolio());
    assertTrue(player.getPortfolio().getShares().isEmpty());
  }

  @Test
  void getTransactionArchive() {
    assertNotNull(player.getTransactionArchive());
    assertTrue(player.getTransactionArchive().isEmpty());
  }

  @Test
  void getPlayerLevel() {
    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());
  }

  @Test
  void recalculateLevel_updatesLevel() {
    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());
    player.recalculateLevel();
    assertEquals(PlayerLevels.NOVICE, player.getPlayerLevel());
  }

  @Test
  void getNetWorth() {
    assertEquals(new BigDecimal("1000.00"), player.getNetWorth());
    player.withdrawMoney(new BigDecimal("100.00"));
    assertEquals(new BigDecimal("900.00"), player.getNetWorth());
  }

  @Test
  void setName_updatesDisplayName() {
    player.setName("Alicia");
    assertEquals("Alicia", player.getName());
  }

  @Test
  void setName_rejectsBlank() {
    assertThrows(IllegalArgumentException.class, () -> player.setName("   "));
  }

  @Test
  void setName_rejectsNullAndOverlongNames() {
    assertThrows(NullPointerException.class, () -> player.setName(null));
    assertThrows(IllegalArgumentException.class, () -> player.setName("x".repeat(49)));
  }

  @Test
  void regularSavingsPlans_areUnmodifiableAndCanBeRemovedByOneBasedIndex() {
    RegularSavingsPlan plan = new RegularSavingsPlan(
        "AAPL",
        SavingsInstallmentMode.FIXED_SHARES,
        BigDecimal.ONE,
        7,
        1);

    assertThrows(NullPointerException.class, () -> player.addRegularSavingsPlan(null));
    player.addRegularSavingsPlan(plan);

    assertEquals(1, player.getRegularSavingsPlans().size());
    assertThrows(UnsupportedOperationException.class, () -> player.getRegularSavingsPlans().clear());
    assertEquals(false, player.removeRegularSavingsPlanAt(0));
    assertEquals(false, player.removeRegularSavingsPlanAt(2));
    assertEquals(true, player.removeRegularSavingsPlanAt(1));
    assertTrue(player.getRegularSavingsPlans().isEmpty());
  }

  @Test
  void addMoney_notifiesObservers() {
    List<Player> notifications = new ArrayList<>();
    player.addObserver(notifications::add);
    player.addMoney(new BigDecimal("100.00"));
    assertEquals(1, notifications.size());
  }

  @Test
  void withdrawMoney_notifiesObservers() {
    List<Player> notifications = new ArrayList<>();
    player.addObserver(notifications::add);
    player.withdrawMoney(new BigDecimal("50.00"));
    assertEquals(1, notifications.size());
  }

  @Test
  void removeObserver_stopsNotifications() {
    List<Player> notifications = new ArrayList<>();
    PlayerObserver observer = notifications::add;
    player.addObserver(observer);
    player.addMoney(new BigDecimal("10.00"));
    assertEquals(1, notifications.size());

    player.removeObserver(observer);
    player.addMoney(new BigDecimal("10.00"));
    assertEquals(1, notifications.size());
  }
}
