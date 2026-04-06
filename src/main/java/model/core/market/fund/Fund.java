package model.core.market.fund;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.core.market.InvestableAsset;
import model.core.market.stock.Stock;
import model.utils.Validator;

/**
 * Represents a fund that holds a weighted basket of stocks. The fund's price is derived from the
 * weighted average of the underlying stocks' prices.
 *
 * @author kevindmazali
 * @version 1.1.0
 */

public class Fund implements InvestableAsset {

  private final String symbol;
  private final String name;

  // Stock → weight (must sum to 1.0)
  private final Map<Stock, BigDecimal> holdings;

  /**
   * Creates a fund with the given symbol, name, and stock holdings. The weights of the stocks must
   * sum to 1.0, and all stocks must have the same length of historical price data.
   *
   * @param symbol   the unique trading symbol for this fund (e.g., "TECHFUND")
   * @param name     the display name for this fund (e.g., "Tech Growth Fund")
   * @param holdings a map of stocks to their corresponding weights in the fund (e.g., {AAPL: 0.5,
   *                 MSFT: 0.5})
   */
  public Fund(String symbol, String name, Map<Stock, BigDecimal> holdings) {
    Validator.checkNotNull(symbol, "Symbol");
    Validator.checkNotNull(name, "Name");
    Validator.checkNotNull(holdings, "Holdings");

    if (holdings.isEmpty()) {
      throw new IllegalArgumentException("Fund must contain at least one stock.");
    }

    this.symbol = symbol.toUpperCase();
    this.name = name;
    this.holdings = Map.copyOf(holdings);

    validateWeights(this.holdings);
    validateHistoryLengths(this.holdings);
  }

  /**
   * Gets the unique trading symbol for this fund.
   *
   * @return the uppercase trading symbol (e.g., "TECHFUND")
   */
  @Override
  public String getSymbol() {
    return symbol;
  }

  /**
   * @return display name (e.g., "Tech Growth Fund")
   */
  @Override
  public String getDisplayName() {
    return name;
  }

  /**
   * Returns the broad asset type label for UI output.
   *
   * @return human-readable asset type (e.g., "Fund")
   */
  @Override
  public String getAssetType() {
    return "Fund";
  }

  /**
   * Calculates the current sales price of the fund as the weighted average of the underlying
   * stocks' sales prices.
   *
   * @return current sales price of the fund
   */
  @Override
  public BigDecimal getSalesPrice() {
    return holdings.entrySet().stream()
        .map(entry -> entry.getKey().getSalesPrice().multiply(entry.getValue()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates the historical prices of the fund as the weighted average of the underlying stocks'
   * historical prices for each day.
   *
   * @return historical prices of the fund, ordered from oldest to newest
   */
  @Override
  public List<BigDecimal> getHistoricalPrices() {
    int size = holdings.keySet().iterator().next().getHistoricalPrices().size();
    List<BigDecimal> result = new ArrayList<>();

    for (int day = 0; day < size; day++) {
      BigDecimal price = BigDecimal.ZERO;

      for (Map.Entry<Stock, BigDecimal> entry : holdings.entrySet()) {
        BigDecimal stockPrice = entry.getKey().getHistoricalPrices().get(day);
        price = price.add(stockPrice.multiply(entry.getValue()));
      }

      result.add(price);
    }

    return List.copyOf(result);
  }

  /**
   * Validates that the weights of the stocks in the fund sum to 1.0. Throws an exception if the
   * validation fails.
   *
   * @param holdings the map of stocks to their corresponding weights in the fund
   * @throws IllegalArgumentException if the weights do not sum to 1.0
   */
  private static void validateWeights(Map<Stock, BigDecimal> holdings) {
    BigDecimal total = holdings.values().stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (total.compareTo(BigDecimal.ONE) != 0) {
      throw new IllegalArgumentException("Weights must sum to 1.0");
    }
  }

  /**
   * Validates that all stocks in the fund have the same length of historical price data. Throws an
   * exception if the validation fails.
   *
   * @param holdings the map of stocks to their corresponding weights in the fund
   */
  private static void validateHistoryLengths(Map<Stock, BigDecimal> holdings) {
    int expected = holdings.keySet().iterator().next().getHistoricalPrices().size();

    boolean mismatch = holdings.keySet().stream()
        .anyMatch(stock -> stock.getHistoricalPrices().size() != expected);

    if (mismatch) {
      throw new IllegalArgumentException("All stocks must have same history length.");
    }
  }

  /**
   * Returns the map of stocks to their corresponding weights in the fund. The returned map is
   * unmodifiable.
   *
   * @return an unmodifiable view of the fund's holdings
   */
  public Map<Stock, BigDecimal> getHoldings() {
    return holdings;
  }
}