package model.core.asset.info;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Random;
import model.core.asset.Stock;

/**
 * Supplies deterministic mock fundamentals per stock symbol so UI can show simplified financials
 * without extending {@link Stock} or persistence.
 */
public class StockFinancialInfoProvider {

  private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000L);
  private static final BigDecimal MIN_REVENUE_M = BigDecimal.valueOf(10);
  private static final BigDecimal REVENUE_SPAN_M = BigDecimal.valueOf(90);
  /** Minimum positive profit-to-revenue ratio to classify as {@link CompanyHealth#STRONG}. */
  private static final BigDecimal STRONG_MARGIN = new BigDecimal("0.10");

  /**
   * Creates a stock financial info provider.
   */
  public StockFinancialInfoProvider() {
  }

  /**
   * Builds mock fundamentals for the given ticker symbol.
   *
   * @param symbol non-null stock symbol
   * @return immutable mock financial snapshot
   */
  public StockFinancialInfo forSymbol(String symbol) {
    Objects.requireNonNull(symbol, "symbol");
    Random rnd = new Random(symbol.hashCode());
    BigDecimal revenueMillions = MIN_REVENUE_M.add(randomFraction(rnd).multiply(REVENUE_SPAN_M))
        .setScale(1, RoundingMode.HALF_UP);
    BigDecimal revenue = revenueMillions.multiply(MILLION);
    double margin = rnd.nextDouble() * 0.30 - 0.05;
    BigDecimal profit =
        revenue.multiply(BigDecimal.valueOf(margin)).setScale(2, RoundingMode.HALF_UP);
    CompanyHealth health = classifyHealth(revenue, profit);
    return new StockFinancialInfo(revenue, profit, health);
  }

  /**
   * Same as {@link #forSymbol(String)} using the stock's symbol.
   *
   * @param stock non-null stock
   * @return mock financial snapshot
   */
  public StockFinancialInfo forStock(Stock stock) {
    Objects.requireNonNull(stock, "stock");
    return forSymbol(stock.getSymbol());
  }

  /**
   * Formats a mock monetary amount for display (millions with one decimal when large).
   *
   * @param amount full currency amount
   * @return compact USD-style string
   */
  public String formatMoney(BigDecimal amount) {
    Objects.requireNonNull(amount, "amount");
    BigDecimal abs = amount.abs();
    if (abs.compareTo(MILLION) >= 0) {
      BigDecimal millions = amount.divide(MILLION, 1, RoundingMode.HALF_UP);
      String sign = amount.signum() < 0 ? "-" : "";
      return sign + "$" + millions.abs().toPlainString() + "M";
    }
    return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static BigDecimal randomFraction(Random rnd) {
    return BigDecimal.valueOf(rnd.nextDouble());
  }

  private static CompanyHealth classifyHealth(BigDecimal revenue, BigDecimal profit) {
    if (revenue.signum() <= 0) {
      return CompanyHealth.WEAK;
    }
    if (profit.signum() <= 0) {
      return CompanyHealth.WEAK;
    }
    BigDecimal margin = profit.divide(revenue, 4, RoundingMode.HALF_UP);
    return margin.compareTo(STRONG_MARGIN) >= 0 ? CompanyHealth.STRONG : CompanyHealth.WEAK;
  }
}
