package model.transaction;

import model.Player;
import model.Share;
import model.transactioncalculator.TransactionCalculator;

/**
 * An abstract class representing a financial transaction.
 *
 * @author kevindmazali
 * @version 0.0.1
 * @since 02-02-2026
 */

public abstract class Transaction {

  private final Share share;
  private final int week;
  private final TransactionCalculator calculator;
  protected boolean commited;

  /**
   * Constructor for Transaction.
   *
   * @param share      The share involved in the transaction.
   * @param week       The week of the transaction.
   * @param calculator The calculator to compute transaction details.
   */

  protected Transaction(Share share, int week, TransactionCalculator calculator) {
    this.share = share;
    this.week = week;
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
   * Gets the week of the transaction.
   *
   * @return The week of the transaction.
   */
  public int getWeek() {
    return week;
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
   * Commits the transaction to a player.
   */
  public abstract void commit(Player player);
}
