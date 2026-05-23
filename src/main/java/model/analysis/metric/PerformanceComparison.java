package model.analysis.metric;


import model.core.player.Portfolio;

import static util.Validator.checkNotNull;

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
