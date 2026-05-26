package model.trading.command.sell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import model.core.player.Player;
import model.trading.command.buy.BuyCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SellAllHoldingsCommandTest {

  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock aapl = new Stock("AAPL", "Apple Inc.");
    aapl.addNewSalesPrice(new BigDecimal("150.00"));
    Stock msft = new Stock("MSFT", "Microsoft");
    msft.addNewSalesPrice(new BigDecimal("300.00"));
    Fund fund = new Fund(
        "TECHX",
        "Tech Blend",
        List.of(
            new FundComponent(aapl, new BigDecimal("0.60")),
            new FundComponent(msft, new BigDecimal("0.40"))));
    exchange = new ExchangeBuilder("NYSE")
        .stocks(List.of(aapl, msft))
        .funds(List.of(fund))
        .build();
    player = new Player("T", new BigDecimal("100000.00"));
  }

  @Test
  void constructor_nullExchange_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new SellAllHoldingsCommand(null));
  }

  @Test
  void execute_emptyPortfolio_returnsNoTransactions() {
    List<?> txs = new SellAllHoldingsCommand(exchange).execute(player);
    assertTrue(txs.isEmpty());
    assertTrue(player.getPortfolio().getShares().isEmpty());
  }

  @Test
  void execute_skipsSymbolsWithNonPositiveQuantity() {
    Stock ghost = exchange.listings().getStock("AAPL");
    player.getPortfolio().addShare(new Share(ghost, BigDecimal.ZERO, ghost.getSalesPrice()));

    List<?> txs = new SellAllHoldingsCommand(exchange).execute(player);

    assertTrue(txs.isEmpty());
  }

  @Test
  void execute_sellsAllSymbolsIncludingFunds() {
    new BuyCommand(exchange, "AAPL", new BigDecimal("2")).execute(player);
    new BuyCommand(exchange, "MSFT", new BigDecimal("1")).execute(player);
    new BuyCommand(exchange, "TECHX", new BigDecimal("3")).execute(player);

    List<?> txs = new SellAllHoldingsCommand(exchange).execute(player);

    assertTrue(txs.size() >= 3);
    assertTrue(player.getPortfolio().getShares().isEmpty());
    assertEquals(0, player.getPortfolio().totalQuantityForSymbol("AAPL").signum());
    assertEquals(0, player.getPortfolio().totalQuantityForSymbol("MSFT").signum());
    assertEquals(0, player.getPortfolio().totalQuantityForSymbol("TECHX").signum());
  }

  @Test
  void execute_sellsFifoLotsPerSymbol() {
    new BuyCommand(exchange, "AAPL", new BigDecimal("3")).execute(player);
    new BuyCommand(exchange, "AAPL", new BigDecimal("2")).execute(player);

    List<?> txs = new SellAllHoldingsCommand(exchange).execute(player);

    assertEquals(2, txs.size());
    assertTrue(
        player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(BigDecimal.ZERO) == 0);
  }

  @Test
  void describe_mentionsSellAll() {
    assertTrue(new SellAllHoldingsCommand(exchange).describe().toLowerCase().contains("all"));
  }
}
