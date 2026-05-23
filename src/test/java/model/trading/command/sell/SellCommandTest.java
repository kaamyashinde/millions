package model.trading.command.sell;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.exception.trading.ShareNotFoundException;
import model.trading.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SellCommandTest {

  private Exchange exchange;
  private Player player;
  private Share share;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    exchange = new Exchange.Builder("NYSE").stocks(List.of(stock)).build();
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
    player = new Player("Alice", new BigDecimal("10000.00"));
    player.getPortfolio().addShare(share);
  }

  @Test
  void execute_creditsMoneyRemovesShareArchives() {
    BigDecimal before = player.getMoney();
    SellCommand cmd = new SellCommand(exchange, share);
    List<Transaction> txs = cmd.execute(player);

    assertEquals(1, txs.size());
    assertTrue(player.getMoney().compareTo(before) > 0);
    assertFalse(player.getPortfolio().getShares().contains(share));
    assertTrue(player.getTransactionArchive().getAllTransactions().contains(txs.getFirst()));
  }

  @Test
  void execute_shareNotInPortfolio_throws() {
    Player other = new Player("Bob", new BigDecimal("1000"));
    SellCommand cmd = new SellCommand(exchange, share);
    assertThrows(ShareNotFoundException.class, () -> cmd.execute(other));
  }

  @Test
  void describe_includesQuantityAndSymbol() {
    SellCommand cmd = new SellCommand(exchange, share);
    assertTrue(cmd.describe().contains("AAPL"));
    assertTrue(cmd.describe().contains("10"));
  }
}
