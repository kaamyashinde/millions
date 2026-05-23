package model.trading.calculator;



import java.math.BigDecimal;
import model.core.asset.Share;
import model.utils.Validator;

/**
 * A class that implements TransactionCalculator to calculate purchase transactions.
 *
 * @author kaamyashinde
 * @version 0.0.2
 * @since 31-01-2026
 */
public class PurchaseCalculator implements TransactionCalculator {

  private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005"); // 0.5%
  private final BigDecimal purchasePrice;
  private final BigDecimal quantity;


  /**
   * Constructor for PurchaseCalculator.
   *
   * @param share The share being purchased.
   * @throws NullPointerException if {@code share} is null.
   */
  public PurchaseCalculator(Share share) {
    Validator.checkNotNull(share, "Share");
    this.purchasePrice = share.getPurchasePrice();
    this.quantity = share.getQuantity();
  }

  /**
   * Calculates the gross amount of the purchase.
   *
   * @return the gross amount as a BigDecimal.
   */
  @Override
  public BigDecimal calculateGross() {
    return purchasePrice.multiply(quantity);
  }

  /**
   * Calculates the commission for the purchase.
   *
   * @return the commission as a BigDecimal.
   */
  @Override
  public BigDecimal calculateCommission() {
    return this.calculateGross().multiply(COMMISSION_RATE);
  }

  /**
   * Calculates the tax for the purchase.
   *
   * @return the tax as a BigDecimal.
   */
  @Override
  public BigDecimal calculateTax() {
    return BigDecimal.ZERO;
  }

  /**
   * Calculates the total amount of the purchase.
   *
   * @return the total amount as a BigDecimal.
   */
  @Override
  public BigDecimal calculateTotal() {
    BigDecimal gross = this.calculateGross();
    BigDecimal commission = this.calculateCommission();
    BigDecimal tax = this.calculateTax();
    return gross.add(commission).add(tax);
  }
}
