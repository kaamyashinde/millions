package model.persistence;

import java.util.List;
import model.market.Stock;
import model.market.fund.Fund;

/**
 * Immutable persisted market payload containing all loaded stocks and funds.
 */
public record MarketData(List<Stock> stocks, List<Fund> funds) {

  /**
   * Creates a normalized market-data bundle with immutable list copies.
   *
   * @param stocks loaded stock definitions
   * @param funds  loaded fund definitions
   */
  public MarketData {
    stocks = List.copyOf(stocks);
    funds = List.copyOf(funds);
  }

  /**
   * Returns an empty market-data bundle.
   *
   * @return market data with no stocks and no funds
   */
  public static MarketData empty() {
    return new MarketData(List.of(), List.of());
  }

  /**
   * Returns whether the market bundle contains no assets at all.
   *
   * @return {@code true} when both stocks and funds are empty
   */
  public boolean isEmpty() {
    return stocks.isEmpty() && funds.isEmpty();
  }
}
