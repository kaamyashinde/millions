package model.core.trading.transactioncalculator;

import java.math.BigDecimal;
import model.core.market.Share;
import model.utils.Validator;

/**
 * A class that implements TransactionCalculator to calculate sale transactions.
 *
 * @author kaamyashinde
 * @version 0.0.4
 * @since 02-02-2026
 */

public class SaleCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01"); // 1%
  private static final BigDecimal TAX_RATE = new BigDecimal("0.3"); // 30%
  private final BigDecimal salePrice;
  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;


  /**
   * Constructor for SaleCalculator.
   *
   * @param share The share being sold.
   * @throws NullPointerException if {@code share} is null.
   */
  public SaleCalculator(Share share) {
    Validator.checkNotNull(share, "Share");
    this.salePrice = share.getAsset().getSalesPrice();
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  /**
   * The gross amount of the sale is calculated by multiplying the sale price by the quantity sold.
   *
   * @return the gross amount as a BigDecimal.
   */
  @Override
  public BigDecimal calculateGross() {
    return salePrice.multiply(quantity);
  }

  /**
   * The commission for the sale is calculated as a percentage of the gross amount.
   *
   * @return the commission as a BigDecimal.
   */
  @Override
  public BigDecimal calculateCommission() {
    return this.calculateGross().multiply(COMMISSION_RATE);
  }

  /**
   * The tax for the sale is calculated based on the profit made from the sale. If no profit is
   * made, no tax is applied.
   *
   * @return the tax as a BigDecimal.
   */
  @Override
  public BigDecimal calculateTax() {
    BigDecimal purchaseCosts = purchasePrice.multiply(quantity);
    BigDecimal profit =
        calculateGross().subtract(calculateCommission()).subtract(purchaseCosts);

    if (profit.signum() <= 0) {
      return BigDecimal.ZERO;
    }

    return profit.multiply(TAX_RATE);
  }


  /**
   * The total amount of the sale is calculated by subtracting the commission and adding the tax to
   * the gross amount.
   *
   * @return the total amount as a BigDecimal.
   */
  @Override
  public BigDecimal calculateTotal() {
    return this.calculateGross().subtract(this.calculateCommission()).subtract(this.calculateTax());
  }
}
