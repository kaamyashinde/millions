package model.savings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import model.Exchange;
import model.Player;
import model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegularSavingsProcessorTest {

  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("100.00"));
    exchange = new Exchange("NYSE", List.of(apple));
    player = new Player("T", new BigDecimal("50000"));
  }

  @Test
  void run_executesBuyOnDueDay() {
    player.addRegularSavingsPlan(
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            2, exchange.getDay()));
    int before = exchange.getDay();
    exchange.advance(2);
    List<String> skipped = RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
    assertTrue(skipped.isEmpty());
    assertTrue(player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(BigDecimal.ZERO) > 0);
  }

  @Test
  void run_skipsWhenBrokeAndAdvancesSchedule() {
    Player broke = new Player("B", BigDecimal.ZERO);
    broke.addRegularSavingsPlan(
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            1, exchange.getDay()));
    int before = exchange.getDay();
    exchange.advance(1);
    List<String> skipped = RegularSavingsProcessor.run(exchange, broke, before, exchange.getDay());
    assertEquals(1, skipped.size());
    assertEquals("AAPL", skipped.getFirst());
    RegularSavingsPlan p = broke.getRegularSavingsPlans().getFirst();
    assertTrue(p.getNextDueDay() > exchange.getDay());
  }

  @Test
  void run_budgetModeUsesBuyUpToBudget() {
    player.addRegularSavingsPlan(
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.BUDGET, new BigDecimal("500"),
            1, exchange.getDay()));
    int before = exchange.getDay();
    exchange.advance(1);
    RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
    assertTrue(player.getPortfolio().totalQuantityForSymbol("AAPL").signum() > 0);
  }
}
