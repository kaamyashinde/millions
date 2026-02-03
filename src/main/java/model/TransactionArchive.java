package model;

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
   * Gets the list of transactions up to a specified week.
   *
   * @param week the week up to which transactions are retrieved
   * @return the list of transactions up to the specified week
   */
  public List<Transaction> getTransactions(int week) {
    return this.transactions.stream()
        .filter(t -> t.getWeek() <= week)
        .toList();
  }

  /**
   * Gets the list of purchases up to a specified week.
   *
   * @param week the week up to which purchases are retrieved
   * @return the list of purchases up to the specified week
   */
  public List<Purchase> getPurchases(int week) {
    return this.transactions.stream()
        .filter(t -> t instanceof Purchase)
        .map(t -> (Purchase) t)
        .filter(p -> p.getWeek() <= week)
        .toList();   // or .collect(Collectors.toList()) on Java < 16
  }

  /**
   * Gets the list of sales up to a specified week.
   *
   * @param week the week up to which sales are retrieved
   * @return the list of sales up to the specified week
   */
  public List<Sale> getSales(int week) {
    return this.transactions.stream()
        .filter(t -> t instanceof Sale)
        .map(t -> (Sale) t)
        .filter(s -> s.getWeek() <= week)
        .toList();   // or .collect(Collectors.toList()) on Java < 16
  }

  /**
   * Counts the number of distinct weeks in the transaction archive.
   *
   * @return the number of distinct weeks
   */

  public int countDistinctWeek() {
    return (int) this.transactions.stream()
        .map(Transaction::getWeek)
        .distinct()
        .count();
  }


}
