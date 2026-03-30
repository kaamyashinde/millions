package model.transaction;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Function;
import model.Share;
import model.Stock;
import model.transactioncalculator.PurchaseCalculator;
import model.transactioncalculator.SaleCalculator;
import model.utils.Validator;

/**
 * Computes share quantities for purchases (budget cap) and sales (net proceeds cap) using shared
 * binary search and quantum refinement. Metrics follow {@link PurchaseCalculator} and
 * {@link SaleCalculator} respectively.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 29-03-2026
 */
public final class TransactionSizing {

  private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
  private static final BigDecimal QUANTUM = new BigDecimal("0.00000001");
  private static final int BINARY_SEARCH_ITERATIONS = 64;
  private static final BigDecimal HALF = new BigDecimal("0.5");

  private TransactionSizing() {
  }

  /**
   * Largest quantity such that the total purchase cost (gross + commission) does not exceed
   * {@code maxSpend}. Returns zero if {@code maxSpend} is null, non-positive, or too small for any
   * positive quantity.
   *
   * @param stock    the stock (current sales price is used)
   * @param maxSpend maximum total cash to spend
   * @return non-negative quantity
   */
  public static BigDecimal maxQuantityForBudget(Stock stock, BigDecimal maxSpend) {
    Validator.checkNotNull(stock, "Stock");
    if (maxSpend == null || maxSpend.signum() <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal price = stock.getSalesPrice();
    if (price.signum() <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal high = maxSpend.divide(price, MC).add(BigDecimal.ONE);
    BigDecimal low = BigDecimal.ZERO;
    Function<BigDecimal, BigDecimal> metric =
        q -> new PurchaseCalculator(new Share(stock, q, price)).calculateTotal();
    low = binarySearchMaxFeasible(low, high, maxSpend, metric);
    BigDecimal q = refineDown(low, maxSpend, metric);
    return q.signum() <= 0 ? BigDecimal.ZERO : q.stripTrailingZeros();
  }

  /**
   * Maximum quantity {@code q} with {@code 0 < q <= lot.getQuantity()} such that the net sale
   * proceeds for a slice of that size (same stock and purchase price as {@code lot}) do not exceed
   * {@code targetNet}. Returns zero if none.
   *
   * @param lot       FIFO lot to size a slice from
   * @param targetNet maximum net cash to raise from this slice
   * @return largest qualifying quantity, or zero
   */
  public static BigDecimal maxQuantityForTargetNet(Share lot, BigDecimal targetNet) {
    Validator.checkNotNull(lot, "Share lot");
    if (targetNet == null || targetNet.signum() <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal maxQ = lot.getQuantity();
    if (maxQ.signum() <= 0) {
      return BigDecimal.ZERO;
    }
    Function<BigDecimal, BigDecimal> metric =
        q -> new SaleCalculator(new Share(lot.getStock(), q, lot.getPurchasePrice()))
            .calculateTotal();
    Share full = new Share(lot.getStock(), maxQ, lot.getPurchasePrice());
    BigDecimal fullNet = new SaleCalculator(full).calculateTotal();
    if (fullNet.compareTo(targetNet) <= 0) {
      return maxQ;
    }
    BigDecimal low = BigDecimal.ZERO;
    BigDecimal high = maxQ;
    low = binarySearchMaxFeasible(low, high, targetNet, metric);
    return refineDown(low, targetNet, metric);
  }

  /**
   * Largest {@code low} in {@code [low, high)} such that {@code metric(low) <= target}, via binary
   * search (metric monotone in quantity).
   */
  private static BigDecimal binarySearchMaxFeasible(
      BigDecimal low,
      BigDecimal high,
      BigDecimal target,
      Function<BigDecimal, BigDecimal> metric) {
    for (int i = 0; i < BINARY_SEARCH_ITERATIONS; i++) {
      BigDecimal mid = low.add(high).multiply(HALF, MC);
      if (mid.compareTo(low) <= 0 || mid.compareTo(high) >= 0) {
        break;
      }
      if (metric.apply(mid).compareTo(target) <= 0) {
        low = mid;
      } else {
        high = mid;
      }
    }
    return low;
  }

  /**
   * From a quantity at or under the cap, steps down by {@link #QUANTUM} until the metric fits.
   */
  private static BigDecimal refineDown(
      BigDecimal quantity,
      BigDecimal target,
      Function<BigDecimal, BigDecimal> metric) {
    BigDecimal q = quantity;
    while (q.signum() > 0) {
      if (metric.apply(q).compareTo(target) <= 0) {
        return q;
      }
      q = q.subtract(QUANTUM);
    }
    return BigDecimal.ZERO;
  }
}
