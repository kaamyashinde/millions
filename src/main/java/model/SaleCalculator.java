package model;

import java.math.BigDecimal;

/**
 * A class that implements TransactionCalculator to calculate sale transactions.
 *
 * @author kaamyashinde
 * @version 0.0.1
 * @since 02-02-2026
 */

public class SaleCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01"); // 0.5%
  private final BigDecimal salePrice;
  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;


  /**
   * Constructor for SaleCalculator.
   *
   * @param share The share being sold.
   */
  public SaleCalculator(Share share) {
    this.salePrice = share.getStock().getSalesPrice();
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
   * The tax for the sale is calculated based on the profit made from the sale.
   *
   * @return the tax as a BigDecimal.
   */
  @Override
  public BigDecimal calculateTax() {
    BigDecimal purchaseCosts = salePrice.multiply(quantity);
    return this.calculateGross().subtract(this.calculateCommission().subtract(purchaseCosts));
  }

  /**
   * The total amount of the sale is calculated by subtracting the commission and adding the tax to
   * the gross amount.
   *
   * @return the total amount as a BigDecimal.
   */
  @Override
  public BigDecimal calculateTotal() {
    return this.calculateGross().subtract(this.calculateCommission().subtract(this.calculateTax()));
  }
}
