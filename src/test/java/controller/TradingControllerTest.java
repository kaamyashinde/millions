package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import model.core.player.Player;
import model.core.asset.Stock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.components.notification.NotificationService;

class TradingControllerTest {

  private Exchange exchange;
  private Player player;
  private TradingController controller;

  @BeforeAll
  static void initJavaFx() throws InterruptedException {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await(5, TimeUnit.SECONDS);
    } catch (IllegalStateException ignored) {
      // toolkit already running
    }
  }

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    exchange = new ExchangeBuilder("NYSE").stocks(List.of(stock)).build();
    player = new Player("Alice", new BigDecimal("100000.00"));
    controller = new TradingController(exchange, player, new NotificationService());
  }

  @Test
  void buyByQuantity_success_updatesPortfolio() {
    TradeResult result = controller.buyByQuantity("AAPL", "2");

    assertInstanceOf(TradeResult.Success.class, result);
    assertTrue(player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(new BigDecimal("2")) == 0);
    assertTrue(player.getMoney().compareTo(new BigDecimal("100000.00")) < 0);
  }

  @Test
  void buyUpToBudget_success_updatesPortfolio() {
    TradeResult result = controller.buyUpToBudget("AAPL", "500");

    assertInstanceOf(TradeResult.Success.class, result);
    assertTrue(player.getPortfolio().totalQuantityForSymbol("AAPL").signum() > 0);
  }

  @Test
  void estimateBuyByQuantity_returnsGrossCommissionAndTotal() {
    Optional<TradingController.BuyEstimate> estimate =
        controller.estimateBuyByQuantity("AAPL", "2");

    assertTrue(estimate.isPresent());
    assertEquals(0, estimate.get().unitPrice().compareTo(new BigDecimal("150.00")));
    assertEquals(0, estimate.get().quantity().compareTo(new BigDecimal("2")));
    assertEquals(0, estimate.get().gross().compareTo(new BigDecimal("300.00")));
    assertEquals(0, estimate.get().commission().compareTo(new BigDecimal("1.50000")));
    assertEquals(0, estimate.get().total().compareTo(new BigDecimal("301.50000")));
  }

  @Test
  void estimateBuyForBudget_returnsQuantityWithinTotalBudget() {
    Optional<TradingController.BuyEstimate> estimate =
        controller.estimateBuyForBudget("AAPL", "500");

    assertTrue(estimate.isPresent());
    assertTrue(estimate.get().quantity().signum() > 0);
    assertTrue(estimate.get().commission().signum() > 0);
    assertTrue(estimate.get().total().compareTo(new BigDecimal("500")) <= 0);
  }

  @Test
  void sellByQuantity_success_reducesHoldings() throws Exception {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    TradeResult result = controller.sellByQuantity("AAPL", "2");

    assertInstanceOf(TradeResult.Success.class, result);
    assertEquals(
        0,
        player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(new BigDecimal("3")));
  }

  @Test
  void buyByQuantity_unknownSymbol_returnsFailure() {
    TradeResult result = controller.buyByQuantity("UNKNOWN", "1");

    assertInstanceOf(TradeResult.Failure.class, result);
    assertTrue(((TradeResult.Failure) result).message().contains("UNKNOWN"));
  }

  @Test
  void buyByQuantity_invalidQuantity_returnsFailure() {
    TradeResult result = controller.buyByQuantity("AAPL", "not-a-number");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void buyByQuantity_zeroQuantity_returnsFailure() {
    TradeResult result = controller.buyByQuantity("AAPL", "0");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void buyByQuantity_insufficientFunds_returnsFailure() {
    player.withdrawMoney(new BigDecimal("99999.99"));
    TradeResult result = controller.buyByQuantity("AAPL", "10");

    assertInstanceOf(TradeResult.Failure.class, result);
    assertTrue(((TradeResult.Failure) result).message().toLowerCase().contains("insufficient"));
  }

  @Test
  void sellByQuantity_insufficientShares_returnsFailure() {
    TradeResult result = controller.sellByQuantity("AAPL", "1");

    assertInstanceOf(TradeResult.Failure.class, result);
    assertTrue(((TradeResult.Failure) result).message().contains("AAPL"));
  }

  @Test
  void getOwnedQuantity_reflectsPortfolio() {
    exchange.buy("AAPL", new BigDecimal("3"), player);
    assertEquals(0, controller.getOwnedQuantity("AAPL").compareTo(new BigDecimal("3")));
  }

  @Test
  void getLatestPrice_returnsListedPrice() {
    assertTrue(controller.getLatestPrice("AAPL").isPresent());
    assertEquals(0, controller.getLatestPrice("AAPL").get().compareTo(new BigDecimal("150.00")));
  }

  @Test
  void constructor_nullExchange_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new TradingController(null, player, new NotificationService()));
  }

  @Test
  void buyUpToBudget_unknownSymbol_returnsFailure() {
    TradeResult result = controller.buyUpToBudget("UNKNOWN", "500");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void buyUpToBudget_invalidBudget_returnsFailure() {
    TradeResult result = controller.buyUpToBudget("AAPL", "not-a-number");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void buyUpToBudget_zeroBudget_returnsFailure() {
    TradeResult result = controller.buyUpToBudget("AAPL", "0");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void buyUpToBudget_insufficientFunds_returnsFailure() {
    player.withdrawMoney(player.getMoney());
    TradeResult result = controller.buyUpToBudget("AAPL", "500");

    assertInstanceOf(TradeResult.Failure.class, result);
    assertTrue(((TradeResult.Failure) result).message().toLowerCase().contains("insufficient"));
  }

  @Test
  void sellByQuantity_unknownSymbol_returnsFailure() {
    TradeResult result = controller.sellByQuantity("UNKNOWN", "1");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellByQuantity_invalidQuantity_returnsFailure() {
    TradeResult result = controller.sellByQuantity("AAPL", "not-a-number");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellByQuantity_zeroQuantity_returnsFailure() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    TradeResult result = controller.sellByQuantity("AAPL", "0");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellUpToTargetNet_success_reducesHoldings() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    BigDecimal before = player.getPortfolio().totalQuantityForSymbol("AAPL");

    TradeResult result = controller.sellUpToTargetNet("AAPL", "500");

    assertInstanceOf(TradeResult.Success.class, result);
    assertTrue(player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(before) < 0);
  }

  @Test
  void sellUpToTargetNet_unknownSymbol_returnsFailure() {
    TradeResult result = controller.sellUpToTargetNet("UNKNOWN", "500");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellUpToTargetNet_invalidAmount_returnsFailure() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    TradeResult result = controller.sellUpToTargetNet("AAPL", "not-a-number");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellUpToTargetNet_zeroAmount_returnsFailure() {
    exchange.buy("AAPL", new BigDecimal("5"), player);
    TradeResult result = controller.sellUpToTargetNet("AAPL", "0");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellAllForSymbol_success_clearsHoldings() {
    exchange.buy("AAPL", new BigDecimal("5"), player);

    TradeResult result = controller.sellAllForSymbol("AAPL");

    assertInstanceOf(TradeResult.Success.class, result);
    assertEquals(0, player.getPortfolio().totalQuantityForSymbol("AAPL").signum());
  }

  @Test
  void sellAllForSymbol_noHoldings_returnsFailure() {
    TradeResult result = controller.sellAllForSymbol("AAPL");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellAllForSymbol_unknownSymbol_returnsFailure() {
    TradeResult result = controller.sellAllForSymbol("UNKNOWN");

    assertInstanceOf(TradeResult.Failure.class, result);
  }

  @Test
  void sellAllForSymbol_afterBudgetBuy_sellsExactFractionalQuantity() {
    controller.buyUpToBudget("AAPL", "1000");
    BigDecimal owned = player.getPortfolio().totalQuantityForSymbol("AAPL");
    assertTrue(owned.signum() > 0);

    TradeResult result = controller.sellAllForSymbol("AAPL");

    assertInstanceOf(TradeResult.Success.class, result);
    assertEquals(0, player.getPortfolio().totalQuantityForSymbol("AAPL").signum());
  }

  @Test
  void estimateSellByQuantity_returnsEstimateWithProceedsBreakdown() {
    exchange.buy("AAPL", new BigDecimal("5"), player);

    Optional<TradingController.SellEstimate> estimate =
        controller.estimateSellByQuantity("AAPL", "3");

    assertTrue(estimate.isPresent());
    TradingController.SellEstimate value = estimate.get();
    assertEquals(0, value.unitPrice().compareTo(new BigDecimal("150.00")));
    assertEquals(0, value.quantity().compareTo(new BigDecimal("3")));
    assertTrue(value.gross().signum() > 0);
    assertTrue(value.commission().signum() > 0);
    assertTrue(value.netProceeds().signum() > 0);
    assertTrue(value.netProceeds().compareTo(value.gross()) < 0);
  }

  @Test
  void estimateSellByQuantity_insufficientShares_returnsEmpty() {
    exchange.buy("AAPL", new BigDecimal("2"), player);

    Optional<TradingController.SellEstimate> estimate =
        controller.estimateSellByQuantity("AAPL", "5");

    assertTrue(estimate.isEmpty());
  }

  @Test
  void estimateSellAll_returnsEstimateForEntireHolding() {
    exchange.buy("AAPL", new BigDecimal("5"), player);

    Optional<TradingController.SellEstimate> estimate =
        controller.estimateSellAll("AAPL");

    assertTrue(estimate.isPresent());
    assertEquals(0, estimate.get().quantity().compareTo(new BigDecimal("5")));
    assertTrue(estimate.get().netProceeds().signum() > 0);
  }

  @Test
  void estimateSellAll_noHoldings_returnsEmpty() {
    Optional<TradingController.SellEstimate> estimate =
        controller.estimateSellAll("AAPL");

    assertTrue(estimate.isEmpty());
  }
}
