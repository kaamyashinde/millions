package model.analysis.stockinfo;

import java.math.BigDecimal;

/**
 * Mock revenue, profit, and health snapshot for a listed stock. Values are not persisted and are
 * derived deterministically from the symbol.
 *
 * @param revenue mock total revenue in currency units (same scale as {@link #profit})
 * @param profit  mock net profit in currency units
 * @param health  qualitative indicator derived from the mock margin
 */
public record StockFinancialInfo(BigDecimal revenue, BigDecimal profit, CompanyHealth health) {

  /**
   * Creates a financial snapshot with defensive copies of decimal fields.
   */
  public StockFinancialInfo {
    revenue = revenue.stripTrailingZeros();
    profit = profit.stripTrailingZeros();
  }
}
