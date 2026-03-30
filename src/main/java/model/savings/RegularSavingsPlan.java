package model.savings;

import java.math.BigDecimal;
import model.utils.Validator;

/**
 * Recurring purchase plan: symbol, installment mode and amount, interval in trading days, next due
 * day, and active flag. Mutable fields ({@link #nextDueDay}, {@link #active}) are updated by
 * {@link RegularSavingsProcessor} and UI.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 29-03-2026
 */
public class RegularSavingsPlan {

  /** Uppercase stock symbol. */
  private final String symbol;

  /** Fixed share count vs. budget cap per installment. */
  private final SavingsInstallmentMode mode;

  /** Share quantity (fixed mode) or max spend (budget mode). */
  private final BigDecimal amount;

  /** Trading days between installments. */
  private final int intervalDays;

  /** Next trading day on which an installment is due. */
  private int nextDueDay;

  /** When false, {@link RegularSavingsProcessor} skips the plan. */
  private boolean active;

  /**
   * @param symbol        stock symbol
   * @param mode          fixed shares or budget
   * @param amount        share quantity or max spend per period
   * @param intervalDays  positive trading days between installments
   * @param currentDay    exchange day when the plan is created
   */
  public RegularSavingsPlan(String symbol, SavingsInstallmentMode mode, BigDecimal amount,
      int intervalDays, int currentDay) {
    Validator.checkNotNull(symbol, "Symbol");
    Validator.checkNotNull(mode, "Mode");
    Validator.requirePositive(amount, "Amount");
    Validator.requirePositive(intervalDays, "intervalDays");
    this.symbol = symbol.toUpperCase();
    this.mode = mode;
    this.amount = amount;
    this.intervalDays = intervalDays;
    this.nextDueDay = currentDay + intervalDays;
    this.active = true;
  }

  /**
   * @return uppercase stock symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * @return how each installment is sized
   */
  public SavingsInstallmentMode getMode() {
    return mode;
  }

  /**
   * @return share quantity or budget amount depending on {@link #getMode()}
   */
  public BigDecimal getAmount() {
    return amount;
  }

  /**
   * @return trading days between installments
   */
  public int getIntervalDays() {
    return intervalDays;
  }

  /**
   * @return next trading day when an installment should run
   */
  public int getNextDueDay() {
    return nextDueDay;
  }

  /**
   * @param nextDueDay next scheduled trading day
   */
  public void setNextDueDay(int nextDueDay) {
    this.nextDueDay = nextDueDay;
  }

  /**
   * @return whether installments are still scheduled
   */
  public boolean isActive() {
    return active;
  }

  /**
   * @param active whether installments should run
   */
  public void setActive(boolean active) {
    this.active = active;
  }
}
