package model;

import java.math.BigDecimal;
import model.transaction.TransactionArchive;

/**
 * A class representing a Player in the system.
 */
public class Player {

  private final String name;
  private final BigDecimal startingMoney;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;
  private BigDecimal money;

  /**
   * Constructor for Player.
   *
   * @param name          the name of the player
   * @param startingMoney the starting money of the player
   */
  public Player(String name, BigDecimal startingMoney) {
    this.name = name;
    this.startingMoney = startingMoney;
    this.money = startingMoney;
    this.portfolio = new Portfolio();
    this.transactionArchive = new TransactionArchive();
  }

  /**
   * Gets the name of the player.
   *
   * @return the name of the player
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the current money of the player.
   *
   * @return the current money of the player
   */

  public BigDecimal getMoney() {
    return money;
  }

  /**
   * Adds money to the player's current money.
   *
   * @param amount the amount to be added
   */

  public void addMoney(BigDecimal amount) {
    this.money = this.money.add(amount);
  }

  /**
   * Withdraws money from the player's current money.
   *
   * @param amount the amount to be withdrawn
   */
  public void withdrawMoney(BigDecimal amount) {
    this.money = this.money.subtract(amount);
  }

  /**
   * Gets the starting money of the player.
   *
   * @return the starting money of the player
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }

  /**
   * Gets the transaction archive of the player.
   *
   * @return the transaction archive of the player
   */
  public TransactionArchive getTransactionArchive() {
    return transactionArchive;
  }

}
