package controller;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.core.market.Exchange;
import model.core.asset.InvestableAsset;
import model.core.player.Player;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;

/**
 * Manages regular savings plans for the savings tab.
 */
public class SavingsController {

  private final Exchange exchange;
  private final Player player;
  private final RegularSavingsPanelController assetListing;
  private final ObservableList<RegularSavingsPlan> plans = FXCollections.observableArrayList();

  /**
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

  public Exchange getExchange() {
    return exchange;
  }

  public Player getPlayer() {
    return player;
  }

  public ObservableList<InvestableAsset> getListedAssets() {
    return assetListing.getListedAssets();
  }

  public ObservableList<RegularSavingsPlan> getPlans() {
    return plans;
  }

  public int getTradingDay() {
    return exchange.getDay();
  }

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
