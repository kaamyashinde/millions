package model.core.market.fund;

import java.math.BigDecimal;
import model.core.market.stock.Stock;
import model.utils.Validator;

/**
 * Immutable stock allocation inside a fund composite.
 */
public record FundComponent(Stock stock, BigDecimal weight) {

  /**
   * Creates one weighted stock component for a fund definition.
   *
   * @param stock  underlying stock included in the fund
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
}
