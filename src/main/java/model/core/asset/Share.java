package model.core.asset;


import model.trading.transaction.Purchase;

import static util.Validator.checkNotNull;
import java.math.BigDecimal;

/**
 * Represents a share in the stock market. A purchase results in acquiring certain amount of
 * Shares.
 *
 * @author kaamyashinde
 * @version 0.0.3
 * @since 2026-01-31
 */

public class Share {

  private final InvestableAsset asset;
  private final BigDecimal quantity;
  private final BigDecimal purchasePrice;

  /**
   * This is the constructor for the Share class.
   *
   * @param asset         The asset associated with the share.
   * @param quantity      The quantity of shares purchased.
   * @param purchasePrice The price at which the shares were purchased.
   * @throws NullPointerException if {@code asset}, {@code quantity} or {@code purchasePrice} is
   *                              null.
   */
  public Share(InvestableAsset asset, BigDecimal quantity, BigDecimal purchasePrice) {
    checkNotNull(asset, "Asset");
    checkNotNull(quantity, "Quantity");
    checkNotNull(purchasePrice, "Purchase Price");
    this.asset = asset;
    this.quantity = quantity;
    this.purchasePrice = purchasePrice;
  }

  /**
   * Gets the asset associated with the share.
   *
   * @return The asset associated with the share.
   */
  public InvestableAsset getAsset() {
    return asset;
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
