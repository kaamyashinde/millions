package model.core.market;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import model.core.asset.Stock;

/**
 * Builds market mover lists from the latest stock price changes.
 */
final class MarketMovers {

  private MarketMovers() {
  }

  /**
   * Returns stocks with positive latest price changes, sorted largest first.
   *
   * @param stocks stocks to inspect
   * @param limit maximum number of rows
   * @return top gainers
   */
  static List<Stock> gainers(Collection<Stock> stocks, int limit) {
    return byPriceChange(stocks, limit, 1, true);
  }

  /**
   * Returns stocks with negative latest price changes, sorted smallest first.
   *
   * @param stocks stocks to inspect
   * @param limit maximum number of rows
   * @return top losers
   */
  static List<Stock> losers(Collection<Stock> stocks, int limit) {
    return byPriceChange(stocks, limit, -1, false);
  }

  /**
   * Filters stocks by latest price-change sign and sorts them by change size.
   *
   * @param stocks stocks to inspect
   * @param limit maximum number of rows
   * @param sign required latest price-change sign
   * @param descending whether larger changes should come first
   * @return matching stocks in display order
   */
  private static List<Stock> byPriceChange(
      Collection<Stock> stocks,
      int limit,
      int sign,
      boolean descending) {
    Comparator<Stock> comparator = Comparator.comparing(Stock::getLatestPriceChange);
    return stocks.stream()
        .filter(stock -> stock.getLatestPriceChange().signum() == sign)
        .sorted(descending ? comparator.reversed() : comparator)
        .limit(limit)
        .toList();
  }
}
