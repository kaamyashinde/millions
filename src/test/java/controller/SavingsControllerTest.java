package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import model.core.player.Player;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SavingsControllerTest {

  private Exchange exchange;
  private Player player;
  private SavingsController controller;
  private Stock apple;

  @BeforeEach
  void setUp() {
    apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("150.00"));
    exchange = new ExchangeBuilder("NYSE").stocks(List.of(apple)).build();
    player = new Player("Alice", new BigDecimal("10000.00"));
    controller = new SavingsController(exchange, player);
  }

  @Test
  void addPlan_success_addsPlan() {
    controller.addPlan(apple, SavingsInstallmentMode.FIXED_SHARES, "1", "20");

    assertEquals(1, player.getRegularSavingsPlans().size());
    assertEquals("AAPL", player.getRegularSavingsPlans().getFirst().getSymbol());
  }

  @Test
  void addPlan_duplicateActiveSymbol_throwsIllegalArgumentException() {
    controller.addPlan(apple, SavingsInstallmentMode.FIXED_SHARES, "1", "20");

    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                controller.addPlan(
                    apple, SavingsInstallmentMode.BUDGET, "500", "10"));

    assertEquals("An active savings plan already exists for AAPL.", thrown.getMessage());
    assertEquals(1, player.getRegularSavingsPlans().size());
  }

  @Test
  void addPlan_inactivePlanForSameSymbol_allowsSecondPlan() {
    player.addRegularSavingsPlan(
        new RegularSavingsPlan(
            "AAPL",
            SavingsInstallmentMode.FIXED_SHARES,
            BigDecimal.ONE,
            20,
            exchange.getDay()));
    player.getRegularSavingsPlans().getFirst().setActive(false);
    controller.refreshPlans();

    controller.addPlan(apple, SavingsInstallmentMode.BUDGET, "500", "10");

    assertEquals(2, player.getRegularSavingsPlans().size());
  }
}
