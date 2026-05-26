package controller;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.core.asset.InvestableAsset;
import model.core.market.Exchange;
import model.core.player.Player;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;

/**
 * Manages regular savings plans for the savings tab.
 *
 * <p>The controller combines {@link RegularSavingsPanelController} asset listings with the active
 * {@link Player}'s {@link RegularSavingsPlan} collection, parsing form text before creating or
 * editing plans.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-05-01
 */
public class SavingsController {

  private final Exchange exchange;
  private final Player player;
  private final RegularSavingsPanelController assetListing;
  private final ObservableList<RegularSavingsPlan> plans = FXCollections.observableArrayList();

  /**
   * Creates a savings controller and loads the player's current plans.
   *
   * @param exchange exchange supplying trading-day state for new plans
   * @param player player owning savings plans
   */
  public SavingsController(Exchange exchange, Player player) {
    checkNotNull(exchange, "Exchange");
    checkNotNull(player, "Player");
    this.exchange = exchange;
    this.player = player;
    this.assetListing = new RegularSavingsPanelController(exchange);
    refreshPlans();
  }

  /**
   * Exposes the exchange used by the savings page.
   *
   * @return exchange used for asset listings and current trading day
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Exposes the player who owns the savings plans.
   *
   * @return player who owns the displayed savings plans
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Exposes investable assets for new plan creation.
   *
   * @return observable investable assets available for new plans
   */
  public ObservableList<InvestableAsset> getListedAssets() {
    return assetListing.getListedAssets();
  }

  /**
   * Exposes the player's savings plans.
   *
   * @return observable savings plans owned by the player
   */
  public ObservableList<RegularSavingsPlan> getPlans() {
    return plans;
  }

  /**
   * Returns the current exchange trading day.
   *
   * @return current exchange trading day
   */
  public int getTradingDay() {
    return exchange.getDay();
  }

  /**
   * Refreshes listed assets and savings plans from the current model state.
   */
  public void refreshPlans() {
    assetListing.refreshListedAssets();
    plans.setAll(player.getRegularSavingsPlans());
  }

  /**
   * Adds a new savings plan for the given asset.
   *
   * @param asset investable asset
   * @param mode installment mode
   * @param amountText amount text
   * @param intervalText interval in days text
   * @throws RuntimeException when values are invalid
   */
  public void addPlan(
      InvestableAsset asset,
      SavingsInstallmentMode mode,
      String amountText,
      String intervalText) {
    if (asset == null || mode == null) {
      throw new IllegalArgumentException("Asset and mode are required.");
    }
    String symbol = asset.getSymbol();
    boolean duplicateActive =
        player.getRegularSavingsPlans().stream()
            .anyMatch(
                plan ->
                    plan.isActive()
                        && plan.getSymbol().equalsIgnoreCase(symbol));
    if (duplicateActive) {
      throw new IllegalArgumentException(
          "An active savings plan already exists for " + symbol + ".");
    }
    BigDecimal amount = new BigDecimal(amountText.trim());
    int interval = Integer.parseInt(intervalText.trim());
    RegularSavingsPlan plan =
        new RegularSavingsPlan(asset.getSymbol(), mode, amount, interval, exchange.getDay());
    player.addRegularSavingsPlan(plan);
    refreshPlans();
  }

  /**
   * Applies edits to the selected plan.
   *
   * @param plan plan to edit
   * @param mode installment mode
   * @param amountText amount text
   * @param intervalText interval text
   * @param nextDueText next due day text
   * @param active whether the plan is active
   */
  public void applyEdit(
      RegularSavingsPlan plan,
      SavingsInstallmentMode mode,
      String amountText,
      String intervalText,
      String nextDueText,
      boolean active) {
    if (plan == null || mode == null) {
      throw new IllegalArgumentException("Plan and mode are required.");
    }
    plan.setMode(mode);
    plan.setAmount(new BigDecimal(amountText.trim()));
    plan.setIntervalDays(Integer.parseInt(intervalText.trim()));
    plan.setNextDueDay(Integer.parseInt(nextDueText.trim()));
    plan.setActive(active);
    refreshPlans();
  }

  /**
   * Removes a plan at the given table index (1-based in the player API).
   *
   * @param tableIndex zero-based table index
   * @return {@code true} when removed
   */
  public boolean removePlanAt(int tableIndex) {
    if (tableIndex < 0) {
      return false;
    }
    boolean removed = player.removeRegularSavingsPlanAt(tableIndex + 1);
    if (removed) {
      refreshPlans();
    }
    return removed;
  }
}
