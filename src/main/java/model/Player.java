package model;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.savings.RegularSavingsPlan;
import model.transaction.Transaction;
import model.transaction.TransactionArchive;

/**
 * A class representing a Player in the system.
 *
 * @author kevindmazali
 * @version 1.5.0
 */
public class Player {

  private String name;
  private final BigDecimal startingMoney;
  private final Portfolio portfolio;
  private final TransactionArchive transactionArchive;
  private PlayerLevel playerLevel;
  private final List<PlayerObserver> observers;

  /** Recurring purchase plans; list is modified only via add/remove helpers. */
  private final List<RegularSavingsPlan> regularSavingsPlans;
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
    this.regularSavingsPlans = new ArrayList<>();
    this.observers = new ArrayList<>();
    this.observers.add(new PlayerLevelObserver());
    this.playerLevel = PlayerLevels.NOVICE.checkTransition(this);
  }

  /**
   * Rebuilds a player from previously persisted state.
   *
   * @param name player name to restore
   * @param startingMoney initial balance recorded when the profile was created
   * @param currentMoney current liquid balance
   * @param shares portfolio lots to restore
   * @param transactions transaction history to restore
   * @param savingsPlans recurring savings plans to restore
   * @return restored player instance containing the supplied state
   */
  public static Player restore(
      String name,
      BigDecimal startingMoney,
      BigDecimal currentMoney,
      List<Share> shares,
      List<Transaction> transactions,
      List<RegularSavingsPlan> savingsPlans) {
    checkNotNull(name, "name");
    checkNotNull(startingMoney, "startingMoney");
    checkNotNull(currentMoney, "currentMoney");
    checkNotNull(shares, "shares");
    checkNotNull(transactions, "transactions");
    checkNotNull(savingsPlans, "savingsPlans");
    Player player = new Player(name, startingMoney);
    player.money = currentMoney;
    shares.forEach(player.portfolio::addShare);
    transactions.forEach(player.transactionArchive::addTransaction);
    savingsPlans.forEach(player.regularSavingsPlans::add);
    player.recalculateLevel();
    return player;
  }

  /**
   * Re-evaluates and updates the player's level based on current state
   * (net worth and trading history). Called automatically by the
   * {@link PlayerLevelObserver} whenever player state changes.
   */
  public void recalculateLevel() {
    this.playerLevel = PlayerLevels.NOVICE.checkTransition(this);
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
   * Updates the display name shown in the UI. Persists via game-state save.
   *
   * @param name non-blank trimmed name, at most 48 characters
   * @throws IllegalArgumentException if the name is invalid
   */
  public void setName(String name) {
    checkNotNull(name, "name");
    String trimmed = name.trim();
    if (trimmed.isEmpty() || trimmed.length() > 48) {
      throw new IllegalArgumentException("Display name must be 1-48 characters.");
    }
    this.name = trimmed;
  }

  /**
   * Gets the starting money of the player.
   *
   * @return the starting money of the player
   */
  public BigDecimal getStartingMoney() {
    return startingMoney;
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
   * Gets the player's current level.
   *
   * @return the player's current level as a PlayerLevel
   */
  public PlayerLevel getPlayerLevel() {
    return playerLevel;
  }

  /**
   * Adds money to the player's current money.
   *
   * @param amount the amount to be added
   */

  public void addMoney(BigDecimal amount) {
    this.money = this.money.add(amount);
    notifyObservers();
  }

  /**
   * Withdraws money from the player's current money.
   *
   * @param amount the amount to be withdrawn
   */
  public void withdrawMoney(BigDecimal amount) {
    this.money = this.money.subtract(amount);
    notifyObservers();
  }

  /**
   * Gets the player's stock holdings and FIFO lot list.
   *
   * @return the portfolio
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

  /**
   * Active and inactive regular savings plans for this player.
   *
   * @return an unmodifiable view of the plan list
   */
  public List<RegularSavingsPlan> getRegularSavingsPlans() {
    return Collections.unmodifiableList(regularSavingsPlans);
  }

  /**
   * Registers a new plan; mutates internal state only through this method and removal.
   *
   * @param plan non-null plan to append
   * @throws NullPointerException if {@code plan} is null
   */
  public void addRegularSavingsPlan(RegularSavingsPlan plan) {
    checkNotNull(plan, "plan");
    regularSavingsPlans.add(plan);
  }

  /**
   * Removes a plan by index (1-based for CLI) or returns false if out of range.
   *
   * @param oneBasedIndex 1-based index into {@link #getRegularSavingsPlans()}
   * @return {@code true} if a plan was removed
   */
  public boolean removeRegularSavingsPlanAt(int oneBasedIndex) {
    if (oneBasedIndex < 1 || oneBasedIndex > regularSavingsPlans.size()) {
      return false;
    }
    regularSavingsPlans.remove(oneBasedIndex - 1);
    return true;
  }

  /**
   * Calculates the net worth of the player by adding the current money and the net worth of the
   * portfolio.
   *
   * @return the net worth of the player
   */
  public BigDecimal getNetWorth() {
    return this.money.add(this.portfolio.getNetWorth());
  }

  /**
   * Registers an observer to be notified whenever this player's state changes.
   *
   * @param observer the observer to register
   */
  public void addObserver(PlayerObserver observer) {
    checkNotNull(observer, "observer");
    observers.add(observer);
  }

  /**
   * Removes a previously registered observer.
   *
   * @param observer the observer to remove
   */
  public void removeObserver(PlayerObserver observer) {
    observers.remove(observer);
  }

  /**
   * Notifies all registered observers that this player's state has changed.
   */
  private void notifyObservers() {
    for (PlayerObserver observer : observers) {
      observer.onPlayerStateChanged(this);
    }
  }
}
