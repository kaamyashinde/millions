package model.trading.command.sell;


import model.trading.command.buy.BuyCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.asset.Stock;
import model.exception.trading.InsufficientSharesException;
import model.trading.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SellByQuantityCommandTest {

  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    exchange = new Exchange.Builder("NYSE").stocks(List.of(stock)).build();
    player = new Player("T", new BigDecimal("100000.00"));
    new BuyCommand(exchange, "AAPL", new BigDecimal("3")).execute(player);
    new BuyCommand(exchange, "AAPL", new BigDecimal("2")).execute(player);
  }

  @Test
  void execute_splitsFifoLots() {
    SellByQuantityCommand cmd =
        new SellByQuantityCommand(exchange, "AAPL", new BigDecimal("4"));
    List<Transaction> txs = cmd.execute(player);

    assertEquals(2, txs.size());
    assertTrue(
        player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(new BigDecimal("1")) == 0);
  }

  @Test
  void execute_throwsWhenNotEnoughShares() {
    SellByQuantityCommand cmd =
        new SellByQuantityCommand(exchange, "AAPL", new BigDecimal("50"));
    assertThrows(InsufficientSharesException.class, () -> cmd.execute(player));
  }

  @Test
  void execute_throwsWhenNoSliceCanBeBuilt() {
    Player emptyPlayer = new Player("Empty", new BigDecimal("1000"));
    SellByQuantityCommand cmd =
        new SellByQuantityCommand(exchange, "AAPL", BigDecimal.ONE);

    assertThrows(InsufficientSharesException.class, () -> cmd.execute(emptyPlayer));
  }

  @Test
  void describe_mentionsQuantityAndSymbol() {
    SellByQuantityCommand cmd =
        new SellByQuantityCommand(exchange, "AAPL", new BigDecimal("4"));
    assertTrue(cmd.describe().contains("AAPL"));
    assertTrue(cmd.describe().contains("4"));
  }
}
