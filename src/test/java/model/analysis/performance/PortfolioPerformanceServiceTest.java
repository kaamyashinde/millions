package model.analysis.performance;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import model.core.asset.Share;
import model.core.market.Exchange;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.pricing.MarketEventStrategy;
import model.core.player.Player;
import model.core.asset.Stock;
import model.trading.calculator.TransactionCalculator;
import model.trading.transaction.Purchase;
import model.trading.transaction.Sale;
import model.trading.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortfolioPerformanceServiceTest {

  private Stock apple;
  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("100.00"));
    exchange = new Exchange.Builder("NYSE").stocks(List.of(apple)).build();
    player = new Player("TestPlayer", new BigDecimal("1000.00"));
  }

  @Test
  void compareAgainstMarket_returnsNoTradesForPlayerWhenNoTransactionsExist() {
    exchange.advance();
    PortfolioPerformanceService service = new PortfolioPerformanceService();

    PerformanceComparison comparison = service.compareAgainstMarket(player, exchange);

    assertEquals(MetricStatus.NO_TRADES, comparison.portfolio().returnPercent().status());
    assertEquals(MetricStatus.NO_TRADES, comparison.portfolio().volatility().status());
    assertEquals(MetricStatus.NO_TRADES, comparison.portfolio().sharpeRatio().status());
    assertEquals(MetricStatus.AVAILABLE, comparison.benchmark().returnPercent().status());
  }

  @Test
  void buildDailyNetWorthSeries_replaysTransactionsUsingHistoricalLiquidationValue() {
    DailyPriceMoveStrategy tenDollarDailyIncrease =
        (stock, random) -> stock.getSalesPrice().add(new BigDecimal("10.00"));
    MarketEventStrategy noMarketEvents =
        (listedStocks, tradingDay, random) -> Optional.empty();
    exchange = new Exchange.Builder("NYSE")
        .stocks(List.of(apple))
        .dailyPriceMoveStrategy(tenDollarDailyIncrease)
        .marketEventStrategy(noMarketEvents)
        .build();

    exchange.buy("AAPL", BigDecimal.ONE, player);
    exchange.advance(2);

    PortfolioPerformanceService service = new PortfolioPerformanceService();

    List<BigDecimal> series = service.buildDailyNetWorthSeries(player, exchange);
    PerformanceComparison comparison = service.compareAgainstMarket(player, exchange);

    assertEquals(3, series.size());
    assertEquals(0, new BigDecimal("998.50").compareTo(series.get(0)));
    assertEquals(0, new BigDecimal("1005.73").compareTo(series.get(1)));
    assertEquals(0, new BigDecimal("1012.66").compareTo(series.get(2)));
    assertEquals(MetricStatus.AVAILABLE, comparison.portfolio().returnPercent().status());
    assertEquals(MetricStatus.AVAILABLE, comparison.portfolio().volatility().status());
    assertEquals(MetricStatus.AVAILABLE, comparison.portfolio().sharpeRatio().status());
    assertEquals(MetricStatus.AVAILABLE, comparison.benchmark().returnPercent().status());
  }

  @Test
  void compareAgainstMarket_returnsInsufficientHistoryWhenBenchmarkHasNoStocks() {
    Exchange emptyExchange = new Exchange.Builder("EMPTY").stocks(List.of()).build();
    PortfolioPerformanceService service = new PortfolioPerformanceService();

    PerformanceComparison comparison = service.compareAgainstMarket(player, emptyExchange);

    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, comparison.benchmark().returnPercent().status());
  }

  @Test
  void buildDailyNetWorthSeries_replaysSalesAndRejectsImpossibleHistory() {
    exchange.buy("AAPL", BigDecimal.ONE, player);
    exchange.advance();
    exchange.sellByQuantity("AAPL", BigDecimal.ONE, player);
    PortfolioPerformanceService service = new PortfolioPerformanceService();

    List<BigDecimal> series = service.buildDailyNetWorthSeries(player, exchange);

    assertEquals(2, series.size());

    Player impossible = new Player("Impossible", new BigDecimal("1000.00"));
    impossible.getTransactionArchive().addTransaction(new Sale(
        new Share(apple, BigDecimal.ONE, new BigDecimal("100.00")),
        1));

    assertThrows(
        IllegalStateException.class,
        () -> service.buildDailyNetWorthSeries(impossible, exchange));
  }

  @Test
  void buildDailyNetWorthSeries_rejectsUnsupportedTransactions() {
    Player unsupported = new Player("Unsupported", new BigDecimal("1000.00"));
    unsupported.getTransactionArchive().addTransaction(new UnsupportedTransaction(
        new Share(apple, BigDecimal.ONE, new BigDecimal("100.00")),
        1));
    PortfolioPerformanceService service = new PortfolioPerformanceService();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.buildDailyNetWorthSeries(unsupported, exchange));
  }

  @Test
  void compareAgainstMarket_rejectsZeroBenchmarkStartingPrice() {
    Stock zero = new Stock("ZERO", "Zero Corp");
    zero.addNewSalesPrice(BigDecimal.ZERO);
    Exchange zeroExchange = new Exchange.Builder("ZERO")
        .stocks(List.of(zero))
        .dailyPriceMoveStrategy((stock, random) -> BigDecimal.ONE)
        .marketEventStrategy((listedStocks, tradingDay, random) -> Optional.empty())
        .build();
    zeroExchange.advance();

    assertThrows(
        IllegalArgumentException.class,
        () -> new PortfolioPerformanceService().compareAgainstMarket(player, zeroExchange));
  }

  private static final class UnsupportedTransaction extends Transaction {

    private UnsupportedTransaction(Share share, int day) {
      super(share, day, zeroCalculator());
    }

    @Override
    public String getTypeName() {
      return "Unsupported";
    }

    @Override
    protected void validatePreconditions(Player player) {
    }

    @Override
    protected void execute(Player player) {
    }
  }

  private static TransactionCalculator zeroCalculator() {
    return new TransactionCalculator() {
      @Override
      public BigDecimal calculateGross() {
        return BigDecimal.ZERO;
      }

      @Override
      public BigDecimal calculateCommission() {
        return BigDecimal.ZERO;
      }

      @Override
      public BigDecimal calculateTax() {
        return BigDecimal.ZERO;
      }

      @Override
      public BigDecimal calculateTotal() {
        return BigDecimal.ZERO;
      }
    };
  }
}
