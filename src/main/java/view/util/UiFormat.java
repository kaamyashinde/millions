package view.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats decimal values for JavaFX display without changing domain calculations.
 */
public final class UiFormat {

  private static final int DISPLAY_SCALE = 2;

  private UiFormat() {}

  /**
   * Formats a decimal value with two fractional digits.
   *
   * @param value value to format
   * @return two-decimal display text, or {@code "-"} when value is null
   */
  public static String decimal(BigDecimal value) {
    if (value == null) {
      return "-";
    }
    return value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * Formats a decimal value as a percentage with two fractional digits.
   *
   * @param value fractional percent value, where {@code 0.1} means 10%
   * @return two-decimal percent display text
   */
  public static String percent(BigDecimal value) {
    if (value == null) {
      return "-";
    }
    return decimal(value.multiply(BigDecimal.valueOf(100))) + "%";
  }
}
