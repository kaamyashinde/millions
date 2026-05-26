package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
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
}
