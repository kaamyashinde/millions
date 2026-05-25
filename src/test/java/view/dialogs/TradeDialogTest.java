package view.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import controller.TradingController;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.Label;
import model.core.asset.Stock;
import model.core.market.Exchange;
import model.core.player.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import view.components.notification.NotificationService;

class TradeDialogTest {

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

  @Test
  void applyBuyEstimateFormatsCommissionSummaryWithTwoDecimals() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    Exchange exchange = new Exchange.Builder("NYSE").stocks(java.util.List.of(stock)).build();
    TradingController controller =
        new TradingController(exchange, new Player("Alice", new BigDecimal("1000.00")),
            new NotificationService());
    TradingController.BuyEstimate estimate =
        controller.estimateBuyByQuantity("AAPL", "2").orElseThrow();

    LabelSnapshot snapshot = runOnFxThread(() -> {
      Label unit = new Label();
      Label quantity = new Label();
      Label gross = new Label();
      Label commission = new Label();
      Label total = new Label();
      TradeDialog.applyBuyEstimate(
          controller,
          Optional.of(estimate),
          unit,
          quantity,
          gross,
          commission,
          total);
      return new LabelSnapshot(
          unit.getText(),
          quantity.getText(),
          gross.getText(),
          commission.getText(),
          total.getText());
    });

    assertEquals("Unit price: 150.00", snapshot.unit());
    assertEquals("Quantity: 2.00", snapshot.quantity());
    assertEquals("Before commission: 300.00", snapshot.gross());
    assertEquals("Commission: 1.50", snapshot.commission());
    assertEquals("After commission: 301.50", snapshot.total());
  }

  private static <T> T runOnFxThread(FxSupplier<T> supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<T> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(supplier.get());
      } catch (Exception exception) {
        err.set(exception);
      } finally {
        latch.countDown();
      }
    });
    latch.await(5, TimeUnit.SECONDS);
    if (err.get() != null) {
      throw err.get();
    }
    return ref.get();
  }

  private record LabelSnapshot(
      String unit,
      String quantity,
      String gross,
      String commission,
      String total) {}

  @FunctionalInterface
  private interface FxSupplier<T> {
    T get() throws Exception;
  }
}
