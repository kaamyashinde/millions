package model;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;

/**
 * Represents a share in the stock market. A purchase results in acquiring certain amount of
 * Shares.
 *
 * @author kaamyashinde
 * @version 0.0.3
 * @since 31-01-2026
 */

public class Share {

  private final Stock stock;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  /**
   * This is the constructor for the Share class.
   *
   * @param stock         The stock associated with the share.
   * @param quantity      The quantity of shares purchased.
   * @param purchasePrice The price at which the shares were purchased.
   * @throws IllegalArgumentException if {@code stock, quantity, purchasePrice} is null.
   */
  public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
    checkNotNull(stock, "Stock");
    checkNotNull(stock, "Quantity");
    checkNotNull(stock, "Purchase Price");
    this.stock = stock;
    this.quantity = quantity;
    this.purchasePrice = purchasePrice;
  }

  /**
   * Gets the stock associated with the share.
   *
   * @return The stock associated with the share.
   */
  public Stock getStock() {
    return stock;
  }

  /**
   * Gets the quantity of shares purchased.
   *
   * @return The quantity of shares purchased.
   */
  public BigDecimal getQuantity() {
    return quantity;
  }

  /**
   * Gets the price at which the shares were purchased.
   *
   * @return The price at which the shares were purchased.
   */
  public BigDecimal getPurchasePrice() {
    return purchasePrice;
  }

}
