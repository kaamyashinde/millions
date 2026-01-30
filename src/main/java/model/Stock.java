package model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a stock in a company. This has a unique stock symbol, f.e.g., "AAPL" for Apple Inc.
 * The stock is sold on Exchanges for a price that updates weekly.
 *
 * @author kaamyashinde
 * @version 0.0.1
 * @since 30-01-2026
 */

// TODO: Refer to the exchanges class when it is implemented

public class Stock {


  private final String symbol;
  private final String company;
  private final List<BigDecimal> price;


  public Stock(String symbol, String company, List<BigDecimal> price) {
    this.symbol = symbol;
    this.company = company;
    this.price = price;
  }


  public String getSymbol() {
    return symbol;
  }

  public String getCompany() {
    return company;
  }

  public List<BigDecimal> getPrice() {
    return price;
  }

  @Override
  public String toString() {
    return "Stock{" +
        "symbol='" + symbol + '\'' +
        ", company='" + company + '\'' +
        ", price=" + price +
        '}';
  }
}
