package model.analysis.performance;


import model.core.player.Portfolio;

import static util.Validator.checkNotNull;

/**
 * Holds side-by-side performance metrics for the player portfolio and market benchmark.
 *
 * <p>{@link PortfolioPerformanceService} returns this record after comparing a
 * {@link Portfolio} against the current market data.
 *
 * @param portfolio metrics for the player portfolio
 * @param benchmark metrics for the market benchmark
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
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
