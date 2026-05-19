package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import model.Stock;
import model.marketevent.MarketEvent;
import model.stockinfo.StockFinancialInfo;
import model.stockinfo.StockFinancialInfoProvider;
import view.pages.stocks.StockDetailView;
import model.marketevent.SymbolMarketEventTarget;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import recommendation.StockRecommendation;

/**
 * Tests refresh behavior of the stock detail view.
 */
class StockDetailViewTest {

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
  void initialView_showsPlaceholderFundamentals() throws Exception {
    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(null, 0);
              return detailView;
            });

    assertEquals("Revenue: -", view.getRevenueLabelText());
    assertEquals("Profit: -", view.getProfitLabelText());
    assertEquals("Health: -", view.getHealthLabelText());
  }

  @Test
  void showStock_displaysMockFundamentalsMatchingProvider() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    StockFinancialInfoProvider provider = new StockFinancialInfoProvider();
    StockFinancialInfo expected = provider.forStock(stock);

    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(stock, 1);
              return detailView;
            });

    assertEquals("Revenue: " + provider.formatMoney(expected.revenue()), view.getRevenueLabelText());
    assertEquals("Profit: " + provider.formatMoney(expected.profit()), view.getProfitLabelText());
    assertEquals("Health: " + expected.health().displayLabel(), view.getHealthLabelText());
  }

  @Test
  void refreshRecomputesRecommendationFromUpdatedHistory() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("102.00"));

    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(stock, 1);
              return detailView;
            });

    assertEquals(StockRecommendation.BUY, view.getDisplayedRecommendation());
    assertEquals("Latest price: 102.00", view.getLatestPriceText());

    stock.addNewSalesPrice(new BigDecimal("95.00"));
    runOnFxThread(
        () -> {
          view.refresh(2);
          return view;
        });

    assertEquals(StockRecommendation.SELL, view.getDisplayedRecommendation());
    assertEquals("Latest price: 95.00", view.getLatestPriceText());
  }

  @Test
  void showStock_displaysRelevantMarketEventTextForAffectedStock() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    MarketEvent event =
        new MarketEvent(
            2,
            "AAPL: Earnings beat expectations",
            "Apple Inc. reported stronger earnings than expected.",
            new SymbolMarketEventTarget(Set.of("AAPL")),
            new BigDecimal("1.12"));

    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(stock, 2, Optional.of(event));
              return detailView;
            });

    assertEquals(
        "Latest market event: AAPL: Earnings beat expectations - "
            + "Apple Inc. reported stronger earnings than expected.",
        view.getMarketEventText());
    assertEquals(List.of(), view.getDisplayedMarketHistory());
  }

  @Test
  void refresh_replacesMarketEventTextWhenSelectedStockIsUnaffected() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    MarketEvent event =
        new MarketEvent(
            2,
            "MSFT: Regulatory setback",
            "Microsoft faces a regulatory setback.",
            new SymbolMarketEventTarget(Set.of("MSFT")),
            new BigDecimal("0.89"));

    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(stock, 2, Optional.of(event));
              return detailView;
            });

    assertEquals("Latest market event: no active event for AAPL", view.getMarketEventText());

    runOnFxThread(
        () -> {
          view.refresh(3, Optional.empty());
          return view;
        });

    assertEquals("Latest market event: none", view.getMarketEventText());
  }

  @Test
  void showStock_displaysPastEventsNewestFirst() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("104.00"));
    MarketEvent firstEvent =
        new MarketEvent(
            2,
            "AAPL: Earnings beat expectations",
            "Apple Inc. reported stronger earnings than expected.",
            new SymbolMarketEventTarget(Set.of("AAPL")),
            new BigDecimal("1.12"));
    MarketEvent secondEvent =
        new MarketEvent(
            5,
            "AAPL: Product launch gains traction",
            "Apple Inc. announced strong demand for a new release.",
            new SymbolMarketEventTarget(Set.of("AAPL")),
            new BigDecimal("1.08"));

    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(
                  stock,
                  5,
                  Optional.of(secondEvent),
                  List.of(firstEvent, secondEvent));
              return detailView;
            });

    assertEquals(
        List.of(
            "Day 5 - AAPL: Product launch gains traction: "
                + "Apple Inc. announced strong demand for a new release.",
            "Day 2 - AAPL: Earnings beat expectations: "
                + "Apple Inc. reported stronger earnings than expected."),
        view.getDisplayedMarketHistory());
  }

  /**
   * Runs work on the JavaFX thread and returns the result.
   *
   * @param supplier work to run on the FX thread
   * @return supplier result
   * @throws Exception any exception thrown by the supplier
   */
  private static StockDetailView runOnFxThread(ViewSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<StockDetailView> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            ref.set(supplier.get());
          } catch (Exception e) {
            err.set(e);
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

  @FunctionalInterface
  private interface ViewSupplier {
    StockDetailView get() throws Exception;
  }
}
