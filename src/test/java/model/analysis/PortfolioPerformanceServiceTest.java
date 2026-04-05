package model.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import model.core.market.Exchange;
import model.core.market.HistoricalAssetPriceService;
import model.core.market.InvestableAsset;
import model.core.market.stock.Stock;
import model.core.player.Player;
import model.core.player.portfolio.MarketBenchmarkService;
import model.core.player.portfolio.PerformanceComparison;
import model.core.player.portfolio.PortfolioPerformanceService;
import model.core.player.portfolio.metrics.MetricStatus;
import model.core.player.portfolio.metrics.calc.PerformanceMetricsCalculator;
import model.core.player.portfolio.metrics.calc.ReturnSeriesCalculator;
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
    exchange.buy("AAPL", BigDecimal.ONE, player);
    exchange.advance(2);

    HistoricalAssetPriceService stubHistory = new HistoricalAssetPriceService() {
      @Override
      public BigDecimal getPriceOnDay(InvestableAsset asset, int day) {
        return switch (day) {
          case 1 -> new BigDecimal("100.00");
          case 2 -> new BigDecimal("110.00");
          case 3 -> new BigDecimal("120.00");
          default -> throw new IllegalArgumentException("Unexpected trading day " + day);
        };
      }
    };
    MarketBenchmarkService benchmarkService = new MarketBenchmarkService(
        stubHistory,
        new PerformanceMetricsCalculator(),
        new ReturnSeriesCalculator());
    PortfolioPerformanceService service = new PortfolioPerformanceService(
        stubHistory,
        new PerformanceMetricsCalculator(),
        benchmarkService);

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
}
