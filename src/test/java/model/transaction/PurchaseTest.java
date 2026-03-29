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
import model.exception.InsufficientFundsException;
import model.transactioncalculator.PurchaseCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PurchaseTest {

  private Share share;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
    player = new Player("Alice", new BigDecimal("10000.00"));
  }

  @Test
  void constructorSetsCalculatorAndShare() {
    Purchase purchase = new Purchase(share, 1);
    assertEquals(share, purchase.getShare());
    assertEquals(1, purchase.getDay());
    assertNotNull(purchase.getCalculator());
    assertInstanceOf(PurchaseCalculator.class, purchase.getCalculator());
  }

  @Test
  void isCommitedInitiallyFalse() {
    Purchase purchase = new Purchase(share, 1);
    assertFalse(purchase.isCommited());
  }

  @Test
  void commitSuccessDeductsMoneyAddsShareAndMarksCommited() {
    Purchase purchase = new Purchase(share, 1);
    BigDecimal beforeMoney = player.getMoney();
    BigDecimal totalCost = purchase.getCalculator().calculateTotal();

    purchase.commit(player);

    assertEquals(beforeMoney.subtract(totalCost), player.getMoney());
    assertTrue(player.getPortfolio().getShares().contains(share));
    assertTrue(player.getTransactionArchive().getTransactions(1).contains(purchase));
    assertTrue(purchase.isCommited());
  }

  @Test
  void commitThrowsInsufficientFundsWhenNotEnoughMoney() {
    Purchase purchase = new Purchase(share, 1);
    Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));

    assertThrows(InsufficientFundsException.class, () -> purchase.commit(poorPlayer));

    // Verify player state is unchanged
    assertEquals(new BigDecimal("1.00"), poorPlayer.getMoney());
    assertFalse(poorPlayer.getPortfolio().getShares().contains(share));
    assertTrue(poorPlayer.getTransactionArchive().isEmpty());
    assertFalse(purchase.isCommited());
  }

  @Test
  void commitThrowsIllegalStateWhenTransactionNotInArchive() {
    Purchase purchase = new Purchase(share, 1);
    // Player whose getTransactionArchive() returns a fresh empty archive each call,
    // so addTransaction writes to one instance while the contains-check reads another.
    Player playerWithFreshArchive = new Player("Charlie", new BigDecimal("50000.00")) {
      @Override
      public TransactionArchive getTransactionArchive() {
        return new TransactionArchive();
      }
    };

    assertThrows(IllegalStateException.class, () -> purchase.commit(playerWithFreshArchive));
    assertFalse(purchase.isCommited());
  }

  @Test
  void commitThrowsAlreadyCommittedWhenCommitCalledTwice() {
    Purchase purchase = new Purchase(share, 1);
    BigDecimal afterFirstCommit = player.getMoney().subtract(purchase.getCalculator().calculateTotal());

    purchase.commit(player);

    assertThrows(AlreadyCommittedException.class, () -> purchase.commit(player));

    // Player state unchanged by second call: same money, share still in portfolio, purchase still committed
    assertEquals(afterFirstCommit, player.getMoney());
    assertTrue(player.getPortfolio().getShares().contains(share));
    assertTrue(purchase.isCommited());
    assertEquals(1, player.getTransactionArchive().getTransactions(1).size());
  }
}
