package view.util;

import model.trading.savings.SavingsInstallmentMode;
import util.I18n;

/**
 * Localized display labels for {@link SavingsInstallmentMode}.
 */
public final class SavingsInstallmentLabels {

  private SavingsInstallmentLabels() {}

  /**
   * @param mode installment sizing mode
   * @return user-facing label for the current locale
   */
  public static String label(SavingsInstallmentMode mode) {
    return switch (mode) {
      case FIXED_SHARES -> I18n.get("savings.mode.fixedShares");
      case BUDGET -> I18n.get("savings.mode.investmentAmount");
    };
  }
}
