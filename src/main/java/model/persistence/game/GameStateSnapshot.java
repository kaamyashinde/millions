package model.persistence.game;


import java.math.BigDecimal;
import java.util.List;
import model.trading.savings.SavingsInstallmentMode;

/**
 * Persisted user-specific game state snapshot.
 *
 * @param schemaVersion snapshot schema version for future compatibility
 * @param player persisted player state
 * @param exchange persisted exchange state
 */
public record GameStateSnapshot(
    int schemaVersion,
    PlayerSnapshot player,
    ExchangeSnapshot exchange
) {

  /**
   * Persisted player state.
   *
   * @param name player name shown in the UI
   * @param startingMoney initial balance at registration time
   * @param currentMoney current liquid balance
   * @param holdings portfolio lots
   * @param transactions transaction history
   * @param savingsPlans recurring investment plans
   */
  public record PlayerSnapshot(
      String name,
      BigDecimal startingMoney,
      BigDecimal currentMoney,
      List<ShareSnapshot> holdings,
      List<TransactionSnapshot> transactions,
      List<RegularSavingsPlanSnapshot> savingsPlans
  ) {
  }

  /**
   * Persisted exchange state for one user's private simulation timeline.
   *
   * @param name exchange name
   * @param day current trading day
   * @param stocks stock definitions plus price histories
   * @param marketEventHistory chronological generated events
   * @param lastMarketEvent most recent event for the current day, if any
   */
  public record ExchangeSnapshot(
      String name,
      int day,
      List<StockSnapshot> stocks,
      List<MarketEventSnapshot> marketEventHistory,
      MarketEventSnapshot lastMarketEvent
  ) {
  }

  /**
   * Persisted stock state needed to rebuild price history.
   *
   * @param symbol trading symbol
   * @param company display company name
   * @param historicalPrices oldest-to-newest recorded prices
   */
  public record StockSnapshot(
      String symbol,
      String company,
      List<BigDecimal> historicalPrices
  ) {
  }

  /**
   * Persisted portfolio lot.
   *
   * @param assetSymbol stock or fund symbol
   * @param quantity quantity held in the lot
   * @param purchasePrice original purchase price per unit
   */
  public record ShareSnapshot(
      String assetSymbol,
      BigDecimal quantity,
      BigDecimal purchasePrice
  ) {
  }

  /**
   * Persisted transaction entry.
   *
   * @param type purchase or sale
   * @param assetSymbol traded asset symbol
   * @param quantity traded quantity
   * @param purchasePrice lot purchase price stored on the underlying share slice
   * @param day trading day
   */
  public record TransactionSnapshot(
      String type,
      String assetSymbol,
      BigDecimal quantity,
      BigDecimal purchasePrice,
      int day
  ) {
  }

  /**
   * Persisted recurring savings plan.
   *
   * @param symbol stock or fund symbol
   * @param mode fixed-shares or budget mode
   * @param amount installment amount
   * @param intervalDays schedule frequency in trading days
   * @param nextDueDay next due trading day
   * @param active whether the plan should keep running
   */
  public record RegularSavingsPlanSnapshot(
      String symbol,
      SavingsInstallmentMode mode,
      BigDecimal amount,
      int intervalDays,
      int nextDueDay,
      boolean active
  ) {
  }

  /**
   * Persisted market event.
   *
   * @param day trading day
   * @param title short title
   * @param description user-facing description
   * @param affectedSymbols affected stock symbols
   * @param priceFactor multiplicative price shock
   */
  public record MarketEventSnapshot(
      int day,
      String title,
      String description,
      List<String> affectedSymbols,
      BigDecimal priceFactor
  ) {
  }
}
