package model.analysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes Fibonacci retracement price levels from a high and low pivot.
 *
 * <p>Uses the standard seven ratios (0%, 23.6%, 38.2%, 50%, 61.8%, 78.6%, 100%) and the formula:
 * {@code price = high − (high − low) × ratio}.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public class FibonacciRetracement {

  /**
   * A single Fibonacci level with a display name and computed price.
   *
   * @param name  the human-readable ratio label (e.g. "61.8%")
   * @param price the retracement price at this level
   */
  public record Level(String name, BigDecimal price) {

  }

  private static final double[] RATIOS = {0.0, 0.236, 0.382, 0.5, 0.618, 0.786, 1.0};
  private static final String[] NAMES = {"0%", "23.6%", "38.2%", "50%", "61.8%", "78.6%", "100%"};

  private FibonacciRetracement() {
  }

  /**
   * Computes the seven retracement levels between {@code high} and {@code low}.
   *
   * @param high the upper pivot price
   * @param low  the lower pivot price
   * @return list of seven {@link Level} records ordered from 0% to 100%
   */
  public static List<Level> compute(BigDecimal high, BigDecimal low) {
    BigDecimal range = high.subtract(low);
    List<Level> levels = new ArrayList<>();
    for (int i = 0; i < RATIOS.length; i++) {
      BigDecimal ratio = BigDecimal.valueOf(RATIOS[i]);
      BigDecimal price = high.subtract(range.multiply(ratio)).setScale(2, RoundingMode.HALF_UP);
      levels.add(new Level(NAMES[i], price));
    }
    return levels;
  }
}
