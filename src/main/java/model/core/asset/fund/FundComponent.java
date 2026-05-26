package model.core.asset.fund;


import java.math.BigDecimal;
import model.core.asset.Stock;
import util.Validator;

/**
 * Immutable stock allocation inside a fund composite.
 *
 * <p>{@link Fund} uses these components to calculate weighted current and historical values from
 * the underlying {@link Stock}.
 *
 * @param stock underlying stock included in the fund
 * @param weight positive portfolio weight for this stock
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
 */
public record FundComponent(Stock stock, BigDecimal weight) {

  /**
   * Creates one weighted stock component for a fund definition.
   *
   * @param stock underlying stock included in the fund
   * @param weight positive portfolio weight for this stock
   */
  public FundComponent {
    Validator.checkNotNull(stock, "Stock");
    Validator.requirePositive(weight, "Weight");
  }

  /**
   * Returns the current price contribution from this component.
   *
   * @return weighted contribution based on the latest stock price
   */
  public BigDecimal currentValueContribution() {
    return stock.getSalesPrice().multiply(weight);
  }

  /**
   * Returns this component's weighted contribution on a historical trading day.
   *
   * @param day trading day number, 1-based
   * @return weighted contribution based on the stock price for that day
   */
  public BigDecimal valueContributionOnDay(int day) {
    return stock.getPriceOnDay(day).multiply(weight);
  }
}
