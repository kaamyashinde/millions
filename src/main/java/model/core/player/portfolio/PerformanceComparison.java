package model.core.player.portfolio;

import static model.utils.Validator.checkNotNull;
import model.core.player.portfolio.metrics.PerformanceMetrics;

/**
 * Holds side-by-side performance metrics for the player portfolio and market benchmark.
 */
public record PerformanceComparison(PerformanceMetrics portfolio, PerformanceMetrics benchmark) {

  /**
   * Creates one side-by-side comparison bundle.
   *
   * @param portfolio metrics for the player portfolio
   * @param benchmark metrics for the market benchmark
   */
  public PerformanceComparison {
    checkNotNull(portfolio, "Portfolio metrics");
    checkNotNull(benchmark, "Benchmark metrics");
  }
}
