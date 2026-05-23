package model.exception.trading;


/**
 * An exception thrown when a player attempts to perform a transaction without sufficient funds.
 *
 * @author kevindmazali
 * @version 0.0.2
 * @since 07-02-2026
 */
public class InsufficientFundsException extends RuntimeException {

  /**
   * Constructs a new InsufficientFundsException with a default error message.
   *
   * @throws RuntimeException if the player does not have enough money to complete the transaction.
   */
  public InsufficientFundsException() {
    super("Insufficient funds to complete the transaction.");
  }
}
