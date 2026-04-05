package model.core.market;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Represents a stock in a company. This has a unique stock symbol, f.e.g., "AAPL" for Apple Inc.
 * The stock is sold on {@code Exchange} for a price that updates each trading day.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 30-01-2026
 */


public class Stock implements InvestableAsset {


  private final String symbol;
  private final String company;
  private final List<BigDecimal> price;

  /**
   * This is the constructor for the Stock class.
   *
   * @param symbol  The symbol of the stock.
   * @param company The company name of the stock.
   * @throws NullPointerException if {@code symbol} or {@code company} is null.
   */
  public Stock(String symbol, String company) {
    checkNotNull(symbol, "Symbol");
    checkNotNull(company, "Company");
    this.symbol = symbol;
    this.company = company;
    this.price = new java.util.ArrayList<>();
  }


  /**
   * Gets the symbol of the stock.
   *
   * @return The symbol of the stock.
   */
  @Override
  public String getSymbol() {
    return symbol;
  }

  /**
   * Gets the company name of the stock.
   *
   * @return The company name of the stock.
   */
  public String getCompany() {
    return company;
  }

  /**
   * Returns the user-facing display name for this stock.
   *
   * @return company name
   */
  @Override
  public String getDisplayName() {
    return company;
  }

  /**
   * Adds a new sales price to the stock's price history.
   *
   * @param price The new sales price to be added.
   * @throws NullPointerException if {@code price} is null.
   */
  public void addNewSalesPrice(BigDecimal price) {
    checkNotNull(price, "Price");
    this.price.add(price);
  }

  /**
   * Returns the historical prices of the stock.
   *
   * @return An unmodifiable live view of the historical prices.
   */
  public List<BigDecimal> getHistoricalPrices() {
    return Collections.unmodifiableList(this.price);
  }

  /**
   * Returns the highest recorded price of a stock.
   *
   * @return The highest recorded price of the stock, or {@code BigDecimal.ZERO} if there are no
   * prices.
   */
  public BigDecimal getHighestPrice() {
    return this.price.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
  }

  /**
   * Returns the lowest recorded price of a stock.
   *
   * @return The lowest recorded price of the stock, or {@code BigDecimal.ZERO} if there are no
   * prices.
   */
  public BigDecimal getLowestPrice() {
    return this.price.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
  }

  /**
   * Returns the latest price change of a stock, calculated as the difference between the most
   * recent price and the previous price.
   *
   * @return The latest price change of the stock, or {@code BigDecimal.ZERO} if there are fewer
   * than two
   */
  public BigDecimal getLatestPriceChange() {
    int size = price.size();
    if (size < 2) {
      return BigDecimal.ZERO;
    }
    return price.get(size - 1)
        .subtract(price.get(size - 2));
  }

  /**
   * Gets the latest price of the stock.
   *
   * @return The latest price of the stock.
   */
  @Override
  public BigDecimal getSalesPrice() {
    return price.getLast();
  }

  /**
   * Returns the asset type label used in user-facing views.
   *
   * @return the string {@code Stock}
   */
  @Override
  public String getAssetType() {
    return "Stock";
  }

  /**
   * Returns a string representation of the Stock object.
   *
   * @return A string representation of the Stock object.
   */
  @Override
  public String toString() {
    return "Stock{"
        + "symbol='" + symbol + '\''
        + ", company='" + company + '\''
        + ", price=" + price
        + '}';
  }
}
