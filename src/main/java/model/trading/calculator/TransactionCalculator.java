package model.trading.calculator;


import java.math.BigDecimal;

/**
 * An interface for calculating transactions.
 *
 * @author kaamyashinde
 * @version 0.0.1
 * @since 2026-01-31
 */
public interface TransactionCalculator {

  /**
   * Calculates the gross amount of the transaction.
   *
   * @return the gross amount as a BigDecimal.
   */
  BigDecimal calculateGross();

  /**
   * Calculates the commission for the transaction.
   *
   * @return the commission as a BigDecimal.
   */
  BigDecimal calculateCommission();

  /**
   * Calculates the tax for the transaction.
   *
   * @return the tax as a BigDecimal.
   */
  BigDecimal calculateTax();

  /**
   * Calculates the total amount of the transaction.
   *
   * @return the total amount as a BigDecimal.
   */
  BigDecimal calculateTotal();
}
