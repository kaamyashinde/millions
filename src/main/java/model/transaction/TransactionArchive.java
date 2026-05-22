package model.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a Transaction Archive in the system.
 *
 * @author kaamyashinde
 * @version 0.0.1
 * @since 02-02-2026
 */

public class TransactionArchive {

  private final List<Transaction> transactions;

  /**
   * Constructor for TransactionArchive.
   */
  public TransactionArchive() {
    this.transactions = new ArrayList<>();
  }

  /**
   * Adds a transaction to the archive.
   *
   * @param transaction the transaction to be added
   * @return true if the transaction was added successfully, false otherwise
   */

  public boolean addTransaction(Transaction transaction) {
    return this.transactions.add(transaction);
  }

  /**
   * Checks if the transaction archive is empty.
   *
   * @return true if the archive is empty, false otherwise
   */
  public boolean isEmpty() {
    return this.transactions.isEmpty();
  }

  /**
   * Gets the list of transactions up to a specified trading day.
   *
   * @param day the day up to which transactions are retrieved
   * @return the list of transactions up to the specified day
   */
  public List<Transaction> getTransactions(int day) {
    return this.transactions.stream()
        .filter(t -> t.getDay() <= day)
        .toList();
  }

  /**
   * Gets the full transaction history in insertion order.
   *
   * @return immutable snapshot of all transactions
   */
  public List<Transaction> getAllTransactions() {
    return List.copyOf(transactions);
  }

  /**
   * Gets the list of purchases up to a specified trading day.
   *
   * @param day the day up to which purchases are retrieved
   * @return the list of purchases up to the specified day
   */
  public List<Purchase> getPurchases(int day) {
    return this.transactions.stream()
        .filter(Purchase.class::isInstance)
        .map(t -> (Purchase) t)
        .filter(p -> p.getDay() <= day)
        .toList();   // or .collect(Collectors.toList()) on Java < 16
  }

  /**
   * Gets the list of sales up to a specified trading day.
   *
   * @param day the day up to which sales are retrieved
   * @return the list of sales up to the specified day
   */
  public List<Sale> getSales(int day) {
    return this.transactions.stream()
        .filter(Sale.class::isInstance)
        .map(t -> (Sale) t)
        .filter(s -> s.getDay() <= day)
        .toList();   // or .collect(Collectors.toList()) on Java < 16
  }

  /**
   * Counts the number of distinct trading days in the transaction archive.
   *
   * @return the number of distinct days
   */

  public int countDistinctDay() {
    return (int) this.transactions.stream()
        .map(Transaction::getDay)
        .distinct()
        .count();
  }


}
