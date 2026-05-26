package model.trading.transaction;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import model.exception.trading.AlreadyCommittedException;
import model.exception.trading.InsufficientFundsException;
import java.math.BigDecimal;
import model.core.player.Player;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.trading.calculator.PurchaseCalculator;
import model.trading.calculator.SaleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for abstract Transaction via concrete subclasses (Sale, Purchase).
 */
class TransactionTest {

  private Share share;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
  }

  @Test
  void getShareReturnsShareFromTransaction() {
    Sale sale = new Sale(share, 2);
    assertEquals(share, sale.getShare());

    Purchase purchase = new Purchase(share, 3);
    assertEquals(share, purchase.getShare());
  }

  @Test
  void getDayReturnsDayFromTransaction() {
    Sale sale = new Sale(share, 5);
    assertEquals(5, sale.getDay());

    Purchase purchase = new Purchase(share, 7);
    assertEquals(7, purchase.getDay());
  }

  @Test
  void getCalculatorReturnsCorrectCalculator() {
    Sale sale = new Sale(share, 1);
    assertNotNull(sale.getCalculator());
    assertInstanceOf(SaleCalculator.class, sale.getCalculator());

    Purchase purchase = new Purchase(share, 1);
    assertNotNull(purchase.getCalculator());
    assertInstanceOf(PurchaseCalculator.class, purchase.getCalculator());
  }

  @Test
  void commit_purchaseWithoutFunds_throwsInsufficientFundsException() {
    Purchase purchase = new Purchase(share, 1);
    Player broke = new Player("Broke", new BigDecimal("1.00"));

    assertThrows(InsufficientFundsException.class, () -> purchase.commit(broke));
  }

  @Test
  void commit_twice_throwsAlreadyCommittedException() {
    Sale sale = new Sale(share, 1);
    Player player = new Player("Alice", new BigDecimal("10000.00"));
    player.getPortfolio().addShare(share);
    sale.commit(player);

    assertThrows(AlreadyCommittedException.class, () -> sale.commit(player));
  }

  @Test
  void isCommitedFalseBeforeCommitTrueAfterCommit() {
    Sale sale = new Sale(share, 1);
    Player player = new Player("Alice", new BigDecimal("10000.00"));
    player.getPortfolio().addShare(share);

    assertFalse(sale.isCommited());
    sale.commit(player);
    assertTrue(sale.isCommited());
  }
}
