package model.analysis;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import model.analysis.performance.MetricStatus;
import model.analysis.performance.PerformanceAnalyzer;
import model.analysis.performance.PerformanceMetrics;

class PerformanceAnalyzerTest {

  private static final double RETURN_DELTA = 0.0000001;

  @Test
  void calculateMetrics_returnsExpectedMetricsForVaryingSeries() {
    PerformanceMetrics metrics = PerformanceAnalyzer.calculateMetrics(
        List.of(
            new BigDecimal("100.00"),
            new BigDecimal("110.00"),
            new BigDecimal("105.00")));

    assertEquals(MetricStatus.AVAILABLE, metrics.returnPercent().status());
    assertEquals(0.05, metrics.returnPercent().value().doubleValue(), RETURN_DELTA);
    assertEquals(MetricStatus.AVAILABLE, metrics.volatility().status());
    assertEquals(0.07272727, metrics.volatility().value().doubleValue(), 0.0000002);
    assertEquals(MetricStatus.AVAILABLE, metrics.sharpeRatio().status());
    assertEquals(0.375, metrics.sharpeRatio().value().doubleValue(), 0.000001);
  }

  @Test
  void calculateMetrics_returnsInsufficientHistoryForSingleValue() {
    PerformanceMetrics metrics =
        PerformanceAnalyzer.calculateMetrics(List.of(new BigDecimal("100.00")));

    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, metrics.returnPercent().status());
    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, metrics.volatility().status());
    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, metrics.sharpeRatio().status());
  }

  @Test
  void calculateMetrics_returnsZeroVolatilityForFlatSeries() {
    PerformanceMetrics metrics = PerformanceAnalyzer.calculateMetrics(
        List.of(
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("100.00")));

    assertEquals(MetricStatus.AVAILABLE, metrics.returnPercent().status());
    assertEquals(0, BigDecimal.ZERO.compareTo(metrics.returnPercent().value()));
    assertEquals(MetricStatus.AVAILABLE, metrics.volatility().status());
    assertEquals(0, BigDecimal.ZERO.compareTo(metrics.volatility().value()));
    assertEquals(MetricStatus.ZERO_VOLATILITY, metrics.sharpeRatio().status());
  }
}
