package model.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PerformanceMetricsCalculatorTest {

  private PerformanceMetricsCalculator calculator;

  @BeforeEach
  void setUp() {
    calculator = new PerformanceMetricsCalculator();
  }

  @Test
  void calculateFromDailyValues_returnsExpectedMetricsForVaryingSeries() {
    PerformanceMetrics metrics = calculator.calculateFromDailyValues(
        List.of(
            new BigDecimal("100.00"),
            new BigDecimal("110.00"),
            new BigDecimal("105.00")));

    assertEquals(MetricStatus.AVAILABLE, metrics.returnPercent().status());
    assertEquals(0.05, metrics.returnPercent().value().doubleValue(), 0.0000001);
    assertEquals(MetricStatus.AVAILABLE, metrics.volatility().status());
    assertEquals(0.07272727, metrics.volatility().value().doubleValue(), 0.0000002);
    assertEquals(MetricStatus.AVAILABLE, metrics.sharpeRatio().status());
    assertEquals(0.375, metrics.sharpeRatio().value().doubleValue(), 0.000001);
  }

  @Test
  void calculateFromDailyValues_returnsInsufficientHistoryForSingleValue() {
    PerformanceMetrics metrics =
        calculator.calculateFromDailyValues(List.of(new BigDecimal("100.00")));

    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, metrics.returnPercent().status());
    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, metrics.volatility().status());
    assertEquals(MetricStatus.INSUFFICIENT_HISTORY, metrics.sharpeRatio().status());
  }

  @Test
  void calculateFromDailyValues_returnsZeroVolatilityForFlatSeries() {
    PerformanceMetrics metrics = calculator.calculateFromDailyValues(
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
