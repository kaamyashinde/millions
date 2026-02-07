package model.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import model.Player;
import model.Share;
import model.Stock;
import model.exception.AlreadyCommittedException;
import model.exception.ShareNotFoundException;
import model.transactioncalculator.SaleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaleTest {

  private Share share;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
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
    BigDecimal expectedTotal = sale.getCalculator().calculateTotal();

    sale.commit(player);

    assertEquals(beforeMoney.add(expectedTotal), player.getMoney());
    assertFalse(player.getPortfolio().getShares().contains(share));
    assertTrue(player.getTransactionArchive().getTransactions(1).contains(sale));
    assertTrue(sale.isCommited());
  }

  @Test
  void commitThrowsShareNotFoundWhenShareNotInPortfolio() {
    Player playerWithoutShare = new Player("Bob", new BigDecimal("10000.00"));
    Sale sale = new Sale(share, 1);
    BigDecimal moneyBefore = playerWithoutShare.getMoney();

    assertThrows(ShareNotFoundException.class, () -> sale.commit(playerWithoutShare));

    assertFalse(sale.isCommited());
    assertEquals(moneyBefore, playerWithoutShare.getMoney());
    assertTrue(playerWithoutShare.getPortfolio().getShares().isEmpty());
    assertTrue(playerWithoutShare.getTransactionArchive().isEmpty());
  }

  @Test
  void commitThrowsAlreadyCommittedWhenCommitCalledTwice() {
    Sale sale = new Sale(share, 1);
    BigDecimal expectedTotal = sale.getCalculator().calculateTotal();
    BigDecimal moneyAfterFirstCommit = player.getMoney().add(expectedTotal);

    sale.commit(player);

    assertThrows(AlreadyCommittedException.class, () -> sale.commit(player));

    // Player state unchanged by second call: same money, share still removed, sale still committed
    assertEquals(moneyAfterFirstCommit, player.getMoney());
    assertFalse(player.getPortfolio().getShares().contains(share));
    assertTrue(sale.isCommited());
    assertEquals(1, player.getTransactionArchive().getTransactions(1).size());
  }
}
