package model.analysis.performance;


import model.analysis.metric.MetricStatus;
import model.analysis.metric.PerformanceComparison;
import model.analysis.series.ReturnSeriesCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import model.core.market.Exchange;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.pricing.MarketEventStrategy;
import model.core.player.Player;
import model.core.asset.Stock;
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

    MarketBenchmarkService benchmarkService = new MarketBenchmarkService(
        new PerformanceMetricsCalculator(),
        new ReturnSeriesCalculator());
    PortfolioPerformanceService service = new PortfolioPerformanceService(
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
