package model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A class representing the Exchange Market in the system.
 *
 * @author kevindmazali
 * @version 0.0.1
 * @since 02-02-2026
 */

public class Exchange {

  private final String name;
  private int week;

  private final Map<String, Stock> stockMap;
  private final Random random;

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
   * Gets the current week of the exchange.
   *
   * @return the current week
   */
  public int getWeek() {
    return week;
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
   * Gets the stock by its symbol.
   *
   * @param symbol the stock symbol
   * @return the stock object
   */
  public Stock getStock(String symbol) {
    return stockMap.get(symbol);
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

}
