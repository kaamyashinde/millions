package model.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import model.Player;
import model.SaleCalculator;
import model.Share;
import model.ShareNotFoundException;
import model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaleTest {

  private Stock stock;
  private Share share;
  private Player player;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
    player = new Player("Alice", new BigDecimal("10000.00"));
    player.getPortfolio().addShare(share);
  }

  @Test
  void constructorSetsCalculatorAndShare() {
    Sale sale = new Sale(share, 1);
    assertEquals(share, sale.getShare());
    assertEquals(1, sale.getWeek());
    assertNotNull(sale.getCalculator());
    assertInstanceOf(SaleCalculator.class, sale.getCalculator());
  }

  @Test
  void isCommitedInitiallyFalse() {
    Sale sale = new Sale(share, 1);
    assertFalse(sale.isCommited());
  }

  @Test
  void commitAddsMoneyRemovesShareAndMarksCommited() {
    Sale sale = new Sale(share, 1);
    BigDecimal beforeMoney = player.getMoney();
    BigDecimal expectedGross = new BigDecimal("2000.00"); // 10 * 200

    sale.commit(player);

    assertEquals(beforeMoney.add(expectedGross), player.getMoney());
    assertFalse(player.getPortfolio().getShares().contains(share));
    assertTrue(player.getTransactionArchive().getTransactions(1).contains(sale));
    assertTrue(sale.isCommited());
  }

  @Test
  void commitThrowsShareNotFoundWhenShareNotInPortfolio() {
    Player playerWithoutShare = new Player("Bob", new BigDecimal("10000.00"));
    Sale sale = new Sale(share, 1);

    assertThrows(ShareNotFoundException.class, () -> sale.commit(playerWithoutShare));
    assertFalse(sale.isCommited());
  }
}
