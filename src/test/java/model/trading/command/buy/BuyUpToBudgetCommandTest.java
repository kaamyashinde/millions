package model.trading.command.buy;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import model.core.player.Player;
import model.core.asset.Stock;
import model.exception.trading.InsufficientFundsException;
import model.trading.calculator.PurchaseCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuyUpToBudgetCommandTest {

  private Exchange exchange;
  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    exchange = new ExchangeBuilder("NYSE").stocks(List.of(stock)).build();
  }

  @Test
  void execute_spendsAtMostMaxAndCash() {
    Player p = new Player("P", new BigDecimal("1600"));
    BuyUpToBudgetCommand cmd = new BuyUpToBudgetCommand(exchange, "AAPL", new BigDecimal("1600"));
    cmd.execute(p);

    assertTrue(p.getMoney().signum() >= 0);
    var sh = p.getPortfolio().getShares().getFirst();
    BigDecimal total = new PurchaseCalculator(sh).calculateTotal();
    assertTrue(total.compareTo(new BigDecimal("1600")) <= 0);
  }

  @Test
  void execute_throwsWhenCannotAffordAnyShare() {
    Player p = new Player("P", BigDecimal.ZERO);
    BuyUpToBudgetCommand cmd = new BuyUpToBudgetCommand(exchange, "AAPL", new BigDecimal("50000"));
    assertThrows(InsufficientFundsException.class, () -> cmd.execute(p));
  }

  @Test
  void execute_unknownSymbol_throws() {
    Player p = new Player("P", new BigDecimal("1000"));
    BuyUpToBudgetCommand cmd = new BuyUpToBudgetCommand(exchange, "X", new BigDecimal("100"));
    assertThrows(IllegalArgumentException.class, () -> cmd.execute(p));
  }

  @Test
  void describe_mentionsBudgetAndSymbol() {
    BuyUpToBudgetCommand cmd = new BuyUpToBudgetCommand(exchange, "AAPL", new BigDecimal("500"));
    assertTrue(cmd.describe().contains("AAPL"));
    assertTrue(cmd.describe().contains("500"));
  }
}
