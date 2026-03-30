package model.savings;

import java.util.ArrayList;
import java.util.List;
import model.Exchange;
import model.Player;
import model.exception.InsufficientFundsException;

/**
 * Applies regular savings plans on trading-day advances: executes due installments via
 * {@link Exchange#buy} or {@link Exchange#buyUpToBudget}, and reschedules each plan after each
 * attempt. Reads current {@link RegularSavingsPlan} field values on each run, so in-place updates
 * to mode, amount, or interval are respected. Depends on {@link Exchange} and {@link Player} only
 * at call sites (no global state).
 *
 * @author kevindmazali
 * @version 1.1.0
 * @since 29-03-2026
 */
public final class RegularSavingsProcessor {

  /** Prevents instantiation. */
  private RegularSavingsProcessor() {
  }

  /**
   * For each trading day in {@code (fromDayExclusive, toDayInclusive]}, executes active plans whose
   * next due day falls on or before that day. On {@link InsufficientFundsException}, skips the
   * installment but still advances that plan's next due day by one interval.
   *
   * @param exchange          exchange used for buys (current day must match {@code toDayInclusive}
   *                            after callers advance it)
   * @param player            player whose plans are run; if null, returns an empty list
   * @param fromDayExclusive  day before the first simulated day (typically previous
   *                            {@link Exchange#getDay()} before {@link Exchange#advance(int)})
   * @param toDayInclusive    last calendar day to process (typically current {@link Exchange#getDay()})
   * @return symbols for which a scheduled installment was skipped due to insufficient funds
   */
  public static List<String> run(Exchange exchange, Player player, int fromDayExclusive,
      int toDayInclusive) {
    List<String> skippedSymbols = new ArrayList<>();
    if (player == null) {
      return skippedSymbols;
    }
    for (int d = fromDayExclusive + 1; d <= toDayInclusive; d++) {
      for (RegularSavingsPlan plan : new ArrayList<>(player.getRegularSavingsPlans())) {
        if (!plan.isActive()) {
          continue;
        }
        while (plan.getNextDueDay() <= d) {
          int due = plan.getNextDueDay();
          try {
            if (plan.getMode() == SavingsInstallmentMode.FIXED_SHARES) {
              exchange.buy(plan.getSymbol(), plan.getAmount(), player);
            } else {
              exchange.buyUpToBudget(plan.getSymbol(), plan.getAmount(), player);
            }
          } catch (InsufficientFundsException ex) {
            skippedSymbols.add(plan.getSymbol());
          }
          plan.setNextDueDay(due + plan.getIntervalDays());
        }
      }
    }
    return skippedSymbols;
  }
}
