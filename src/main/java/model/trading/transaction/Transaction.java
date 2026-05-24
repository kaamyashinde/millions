package model.trading.transaction;


import model.core.player.Player;
import model.core.asset.Share;
import model.exception.trading.AlreadyCommittedException;
import model.trading.calculator.TransactionCalculator;

/**
 * An abstract class representing a financial transaction.
 *
 * @author kevindmazali
 * @version 0.0.1
 * @since 02-02-2026
 */

public abstract class Transaction {

  private final Share share;
  private final int day;
  private final TransactionCalculator calculator;
  protected boolean commited;

  /**
   * Constructor for Transaction.
   *
   * @param share      The share involved in the transaction.
   * @param day        The trading day of the transaction.
   * @param calculator The calculator to compute transaction details.
   */

  protected Transaction(Share share, int day, TransactionCalculator calculator) {
    this.share = share;
    this.day = day;
    this.calculator = calculator;
    this.commited = false;
  }

  /**
   * Gets the share involved in the transaction.
   *
   * @return The share involved in the transaction.
   */
  public Share getShare() {
    return share;
  }

  /**
   * Gets the trading day of the transaction.
   *
   * @return The trading day of the transaction.
   */
  public int getDay() {
    return day;
  }

  /**
   * Gets the calculator for the transaction.
   *
   * @return The calculator for the transaction.
   */
  public TransactionCalculator getCalculator() {
    return calculator;
  }

  /**
   * Checks if the transaction is committed.
   *
   * @return True if the transaction is committed, false otherwise.
   */
  public boolean isCommited() {
    return commited;
  }

  /**
   * Returns the human-readable transaction type name (e.g. {@code "Purchase"} or {@code "Sale"}).
   *
   * @return type label for this transaction
   */
  public abstract String getTypeName();

  /**
   * Commits the transaction to a player. This template method enforces the
   * commit lifecycle: guard against double-commit, validate preconditions,
   * execute the domain action, archive the transaction, and mark it committed.
   *
   * @param player the player to commit the transaction against
   * @throws AlreadyCommittedException if the transaction was already committed
   */
  public final void commit(Player player) {
    if (this.isCommited()) {
      throw new AlreadyCommittedException();
    }
    validatePreconditions(player);
    execute(player);
    player.getTransactionArchive().addTransaction(this);
    this.commited = true;
  }

  /**
   * Validates that the player meets all requirements for this transaction.
   * Throws a domain-specific exception if any precondition is violated.
   *
   * @param player the player to validate against
   */
  protected abstract void validatePreconditions(Player player);

  /**
   * Performs the core domain action of the transaction (e.g. transfer money
   * and shares). Called only after preconditions have passed.
   *
   * @param player the player to execute the transaction against
   */
  protected abstract void execute(Player player);
}
