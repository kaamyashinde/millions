package model;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import model.transaction.Purchase;
import model.transaction.Sale;
import model.transaction.Transaction;

/**
 * A class representing the Exchange Market in the system.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 02-02-2026
 */

public class Exchange {

  private final String name;
  private final Map<String, Stock> stockMap;
  private final Random random;
  private int week;

  /**
   * Constructor for Exchange.
   *
   * @param name   the name of the exchange
   * @param stocks the list of stocks available in the exchange
   */
  public Exchange(String name, List<Stock> stocks) {
    this.name = name;
    this.week = 1;
    this.stockMap = stocks.stream()
        .collect(Collectors.toMap(Stock::getSymbol, s -> s));
    this.random = new Random();
  }

  /**
   * Gets the name of the exchange.
   *
   * @return the name of the exchange
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the stock by its symbol.
   *
   * @param symbol the stock symbol
   * @return the stock object
   */
  public boolean hasStock(String symbol) {
    return stockMap.containsKey(symbol);
  }

  /**
   * Finds stocks by a search term in their symbol or company name.
   *
   * @param searchTerm the search term
   * @return the list of matching stocks
   */
  public List<Stock> findStocks(String searchTerm) {
    String lowerCaseTerm = searchTerm.toLowerCase();
    return stockMap.values().stream()
        .filter(stock -> stock.getSymbol().toLowerCase().contains(lowerCaseTerm)
            || stock.getCompany().toLowerCase().contains(lowerCaseTerm))
        .toList();
  }

  /**
   * Buys shares of a stock for a player. This method creates a Purchase transaction and commits
   * it.
   *
   * @param symbol   the stock symbol
   * @param quantity the quantity of shares to buy
   * @param player   the player making the purchase
   * @return the Purchase transaction
   */
  public Transaction buy(String symbol, BigDecimal quantity, Player player) {
    Stock stockToBuy = this.getStock(symbol);
    Share shareToBuy = new Share(stockToBuy, quantity, stockToBuy.getSalesPrice());
    Purchase purchase = new Purchase(shareToBuy, this.getWeek());
    purchase.commit(player);
    return purchase;
  }

  /**
   * Gets the stock by its symbol.
   *
   * @param symbol the stock symbol
   * @return the stock object
   */
  public Stock getStock(String symbol) {
    return stockMap.get(symbol);
  }

  /**
   * Gets the current week of the exchange.
   *
   * @return the current week
   */
  public int getWeek() {
    return week;
  }

  /**
   * Sells shares of a stock for a player. This method creates a Sale transaction and commits it.
   *
   * @param share  the share to sell
   * @param player the player making the sale
   * @return the Sale transaction
   */
  public Transaction sell(Share share, Player player) {
    Sale sale = new Sale(share, this.getWeek());
    sale.commit(player);
    return sale;
  }

  /**
   * Advances the exchange to the next week and updates stock prices.
   */
  public void advance() {
    this.week += 1;
    this.stockMap.values().forEach(stock -> {
      double factor = 1 + this.random.nextDouble(-0.05, 0.05); // −5% to +5%
      stock.addNewSalesPrice(
          stock.getSalesPrice().multiply(BigDecimal.valueOf(factor)));
    });
  }

  /**
   * Gets the top gainers in the exchange based on their latest price change. The gainers are sorted
   * in descending order of their price change, and the method returns a list of the top gainers up
   * to the specified limit.
   *
   * @param limit the maximum number of gainers to return
   * @return a list of the top gainers in the exchange, sorted by their latest price change in
   * descending order
   */
  public List<Stock> getGainers(int limit) {
    return getByPriceChange(limit, 1, true);
  }

  /**
   * A helper method to get stocks by their price change sign and sort them by their latest price
   * change.
   *
   * @param limit      the maximum number of stocks to return
   * @param sign       the sign of the price change (positive for gainers, negative for losers)
   * @param descending whether to sort in descending order (true for gainers, false for losers)
   * @return a list of stocks filtered and sorted by their latest price change
   */
  private List<Stock> getByPriceChange(int limit, int sign, boolean descending) {
    Comparator<Stock> comparator =
        Comparator.comparing(Stock::getLatestPriceChange);

    return stockMap.values().stream()
        .filter(stock -> stock.getLatestPriceChange().signum() == sign)
        .sorted(descending ? comparator.reversed() : comparator)
        .limit(limit)
        .toList();
  }

  /**
   * Gets the top losers in the exchange based on their latest price change. The losers are sorted
   * in ascending order of their price change, and the method returns a list of the top losers up to
   * the specified limit.
   *
   * @param limit the maximum number of losers to return
   * @return a list of the top losers in the exchange, sorted by their latest price change in
   * ascending order
   */
  public List<Stock> getLosers(int limit) {
    return getByPriceChange(limit, -1, false);
  }

}
