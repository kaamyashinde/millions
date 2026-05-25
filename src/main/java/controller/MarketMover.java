package controller;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;

/**
 * Read-only market mover row derived from a stock's latest day-to-day price change.
 *
 * @param symbol stock symbol
 * @param company company name
 * @param currentPrice latest stock price
 * @param absoluteChange latest price minus previous price
 * @param percentChange fractional change, where {@code 0.01} means 1%
 */
public record MarketMover(
    String symbol,
    String company,
    BigDecimal currentPrice,
    BigDecimal absoluteChange,
    BigDecimal percentChange) {

  /**
   * Validates that each display value is present.
   */
  public MarketMover {
    checkNotNull(symbol, "Symbol");
    checkNotNull(company, "Company");
    checkNotNull(currentPrice, "Current price");
    checkNotNull(absoluteChange, "Absolute change");
    checkNotNull(percentChange, "Percent change");
  }
}
