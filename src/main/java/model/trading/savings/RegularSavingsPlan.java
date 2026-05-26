package model.trading.savings;


import java.math.BigDecimal;
import model.core.asset.Share;
import util.Validator;

/**
 * Recurring purchase plan for a single stock symbol: installment mode, amount, interval in trading
 * days, next due day, and active flag. The {@link #symbol} is fixed for the lifetime of the
 * instance; to invest in a different stock, remove this plan and add a new one. Other fields may be
 * updated via setters; changing {@code mode}, {@code amount}, or {@code intervalDays} does
 * <strong>not</strong> automatically change {@link #nextDueDay} — use {@link #setNextDueDay(int)}
 * if
 * the schedule should be adjusted. {@link RegularSavingsProcessor} reads current values on each run
 * and updates {@link #nextDueDay} after each installment attempt.
 *
 * @author kevindmazali
 * @version 1.1.0
 * @since 2026-03-29
 */
public class RegularSavingsPlan {

  /** Uppercase stock symbol; immutable after construction. */
  private final String symbol;

  /** Fixed share count vs. budget cap per installment; may be updated. */
  private SavingsInstallmentMode mode;

  /** Share quantity (fixed mode) or max spend (budget mode); may be updated. */
  private BigDecimal amount;

  /** Trading days between installments; may be updated. */
  private int intervalDays;

  /** Next trading day on which an installment is due. */
  private int nextDueDay;

  /** When false, {@link RegularSavingsProcessor} skips the plan. */
  private boolean active;

  /**
   * Creates a recurring savings plan.
   *
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
   * Returns the target stock symbol.
   *
   * @return uppercase stock symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the installment sizing mode.
   *
   * @return how each installment is sized
   */
  public SavingsInstallmentMode getMode() {
    return mode;
  }

  /**
   * Sets the installment mode.
   *
   * @param mode fixed shares or budget; must not be null
   * @throws NullPointerException if {@code mode} is null
   */
  public void setMode(SavingsInstallmentMode mode) {
    Validator.checkNotNull(mode, "Mode");
    this.mode = mode;
  }

  /**
   * Returns the per-installment amount.
   *
   * @return share quantity or budget amount depending on {@link #getMode()}
   */
  public BigDecimal getAmount() {
    return amount;
  }

  /**
   * Sets the per-installment amount (share quantity or max spend depending on {@link #getMode()}).
   *
   * @param amount strictly positive
   */
  public void setAmount(BigDecimal amount) {
    Validator.requirePositive(amount, "Amount");
    this.amount = amount;
  }

  /**
   * Returns the installment interval.
   *
   * @return trading days between installments
   */
  public int getIntervalDays() {
    return intervalDays;
  }

  /**
   * Sets the number of trading days between installments. Does not by itself reschedule
   * {@link #nextDueDay}; the processor applies the new interval when advancing the schedule after
   * each run.
   *
   * @param intervalDays strictly positive
   */
  public void setIntervalDays(int intervalDays) {
    Validator.requirePositive(intervalDays, "intervalDays");
    this.intervalDays = intervalDays;
  }

  /**
   * Returns the next scheduled due day.
   *
   * @return next trading day when an installment should run
   */
  public int getNextDueDay() {
    return nextDueDay;
  }

  /**
   * Sets the next scheduled due day.
   *
   * @param nextDueDay next scheduled trading day
   */
  public void setNextDueDay(int nextDueDay) {
    this.nextDueDay = nextDueDay;
  }

  /**
   * Returns whether the plan is active.
   *
   * @return whether installments are still scheduled
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Sets whether installments should run.
   *
   * @param active whether installments should run
   */
  public void setActive(boolean active) {
    this.active = active;
  }
}
