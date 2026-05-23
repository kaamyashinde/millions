package model.trading.command.buy;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.asset.Stock;
import model.trading.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuyCommandTest {

  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    exchange = new Exchange.Builder("NYSE").stocks(List.of(stock)).build();
    player = new Player("Alice", new BigDecimal("100000.00"));
  }

  @Test
  void execute_deductsMoneyAddsShareAndArchives() {
    BigDecimal before = player.getMoney();
    BuyCommand cmd = new BuyCommand(exchange, "AAPL", new BigDecimal("2"));
    List<Transaction> txs = cmd.execute(player);

    assertEquals(1, txs.size());
    assertNotNull(txs.getFirst());
    assertTrue(player.getMoney().compareTo(before) < 0);
    assertFalse(player.getPortfolio().getShares().isEmpty());
    assertTrue(player.getTransactionArchive().getAllTransactions().contains(txs.getFirst()));
  }

  @Test
  void execute_unknownSymbol_throws() {
    BuyCommand cmd = new BuyCommand(exchange, "UNKNOWN", BigDecimal.ONE);
    assertThrows(IllegalArgumentException.class, () -> cmd.execute(player));
  }

  @Test
  void describe_includesSymbolAndQuantity() {
    BuyCommand cmd = new BuyCommand(exchange, "AAPL", new BigDecimal("10"));
    assertEquals("Buy 10 of AAPL", cmd.describe());
  }
}
