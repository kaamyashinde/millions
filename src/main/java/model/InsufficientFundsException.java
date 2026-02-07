package model;

/**
 * An exception thrown when a player attempts to perform a transaction without sufficient funds.
 */
public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException() {
    super("Insufficient funds to complete the transaction.");
  }
}
