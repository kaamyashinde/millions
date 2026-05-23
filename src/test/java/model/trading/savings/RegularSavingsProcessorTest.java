package model.trading.savings;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegularSavingsProcessorTest {

  private Exchange exchange;
  private Player player;
  private Fund techFund;

  @BeforeEach
  void setUp() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("100.00"));
    techFund = new Fund(
        "TECHX",
        "Tech Titans Blend Fund",
        List.of(new FundComponent(apple, BigDecimal.ONE)));
    exchange = new Exchange.Builder("NYSE")
        .stocks(List.of(apple))
        .funds(List.of(techFund))
        .build();
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

  @Test
  void run_afterMidLifeAmountChange_usesUpdatedAmount() {
    RegularSavingsPlan plan =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            2, exchange.getDay());
    player.addRegularSavingsPlan(plan);
    plan.setAmount(new BigDecimal("3"));
    int before = exchange.getDay();
    exchange.advance(2);
    RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
    assertEquals(new BigDecimal("3"), player.getPortfolio().totalQuantityForSymbol("AAPL"));
  }

  @Test
  void run_afterMidLifeIntervalChange_usesUpdatedIntervalForReschedule() {
    RegularSavingsPlan plan =
        new RegularSavingsPlan("AAPL", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("1"),
            1, exchange.getDay());
    player.addRegularSavingsPlan(plan);
    plan.setIntervalDays(5);
    int before = exchange.getDay();
    exchange.advance(1);
    RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
    assertEquals(7, plan.getNextDueDay());
  }

  @Test
  void run_executesFundBuyOnDueDay() {
    player.addRegularSavingsPlan(
        new RegularSavingsPlan("TECHX", SavingsInstallmentMode.FIXED_SHARES, new BigDecimal("2"),
            1, exchange.getDay()));
    int before = exchange.getDay();
    exchange.advance(1);

    List<String> skipped = RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());

    assertTrue(skipped.isEmpty());
    assertEquals(new BigDecimal("2"), player.getPortfolio().totalQuantityForSymbol("TECHX"));
  }
}
