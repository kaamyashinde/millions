package model;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a stock in a company. This has a unique stock symbol, f.e.g., "AAPL" for Apple Inc.
 * The stock is sold on {@code Exchange} for a price that updates weekly.
 *
 * @author kaamyashinde
 * @version 0.0.1
 * @since 30-01-2026
 */


public class Stock {


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
   * Gets the latest price of the stock.
   *
   * @return The latest price of the stock.
   */
  public BigDecimal getSalesPrice() {
    return price.getLast();
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
