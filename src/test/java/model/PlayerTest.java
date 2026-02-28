package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
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
}
