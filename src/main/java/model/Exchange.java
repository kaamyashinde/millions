package model;

import static model.utils.Validator.requirePositive;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.stream.Collectors;
import model.exception.InsufficientFundsException;
import model.exception.InsufficientSharesException;
import model.transactioncalculator.SaleCalculator;
import model.transaction.Purchase;
import model.transaction.Sale;
import model.transaction.Transaction;
import model.transaction.TransactionSizing;

/**
 * A class representing the Exchange Market in the system.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 02-02-2026
 */

public class Exchange {

  private static final double DAILY_SIGMA = 0.05 / Math.sqrt(7);
  private final String name;
  private final Map<String, Stock> stockMap;
  private final Random random;
  private int day;

  /**
   * Constructor for Exchange.
   *
   * @param name   the name of the exchange
   * @param stocks the list of stocks available in the exchange
   */
  public Exchange(String name, List<Stock> stocks) {
    this.name = name;
    this.day = 1;
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
    Purchase purchase = new Purchase(shareToBuy, this.getDay());
    purchase.commit(player);
    return purchase;
  }

  /**
   * Buys as many shares as possible without exceeding {@code maxSpend} total cost (including
   * purchase commission), and without spending more cash than the player currently has. Uses the
   * same {@link Purchase} flow as {@link #buy(String, BigDecimal, Player)}.
   *
   * @param symbol   the stock symbol
   * @param maxSpend upper bound on total purchase cost (gross + commission)
   * @param player   the player making the purchase
   * @return the Purchase transaction
   * @throws NullPointerException         if {@code maxSpend} is null
   * @throws IllegalArgumentException     if the symbol is unknown or {@code maxSpend} is not
   *                                      positive
   * @throws InsufficientFundsException   if no positive quantity fits the budget and cash available
   */
  public Transaction buyUpToBudget(String symbol, BigDecimal maxSpend, Player player) {
    Stock stock = this.getStock(symbol);
    if (stock == null) {
      throw new IllegalArgumentException("Unknown stock symbol: " + symbol);
    }
    requirePositive(maxSpend, "maxSpend");
    BigDecimal budget = maxSpend.min(player.getMoney());
    BigDecimal quantity = TransactionSizing.maxQuantityForBudget(stock, budget);
    if (quantity.signum() <= 0) {
      throw new InsufficientFundsException();
    }
    return buy(symbol, quantity, player);
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
   * Gets the current trading day of the exchange.
   *
   * @return the current day
   */
  public int getDay() {
    return day;
  }

  /**
   * Sells shares of a stock for a player. This method creates a Sale transaction and commits it.
   *
   * @param share  the share to sell
   * @param player the player making the sale
   * @return the Sale transaction
   */
  public Transaction sell(Share share, Player player) {
    Sale sale = new Sale(share, this.getDay());
    sale.commit(player);
    return sale;
  }

  /**
   * Sells a total quantity of the given symbol using FIFO lots (oldest holding first). May perform
   * several {@link Sale} transactions if the quantity spans multiple lots. Partial lots are split so
   * cost basis is preserved per lot.
   *
   * @param symbol   the stock symbol
   * @param quantity total shares to sell
   * @param player   the player
   * @return all sale transactions, in order
   * @throws NullPointerException     if {@code quantity} is null
   * @throws IllegalArgumentException if {@code quantity} is not positive
   */
  public List<Transaction> sellByQuantity(String symbol, BigDecimal quantity, Player player) {
    requirePositive(quantity, "quantity");
    if (player.getPortfolio().totalQuantityForSymbol(symbol).compareTo(quantity) < 0) {
      throw new InsufficientSharesException(symbol, quantity);
    }
    List<Transaction> transactions = new ArrayList<>();
    BigDecimal remaining = quantity;
    while (remaining.signum() > 0) {
      Share slice = player.getPortfolio().buildNextFifoSaleSlice(symbol, remaining);
      if (slice == null) {
        throw new InsufficientSharesException(symbol, quantity);
      }
      transactions.add(sell(slice, player));
      remaining = remaining.subtract(slice.getQuantity());
    }
    return transactions;
  }

  /**
   * Sells shares of the symbol in FIFO order, stopping when the cumulative net proceeds credited to
   * the player (per {@link SaleCalculator#calculateTotal()}) would
   * exceed {@code targetNet}, or when there are no more shares. May perform several sales.
   *
   * @param symbol    the stock symbol
   * @param targetNet desired maximum total net cash to raise
   * @param player    the player
   * @return all sale transactions, in order (may be empty if nothing could be sold)
   * @throws NullPointerException     if {@code targetNet} is null
   * @throws IllegalArgumentException if {@code targetNet} is not positive
   */
  public List<Transaction> sellUpToTargetNet(String symbol, BigDecimal targetNet, Player player) {
    requirePositive(targetNet, "targetNet");
    List<Transaction> transactions = new ArrayList<>();
    BigDecimal remainingTarget = targetNet;
    while (remainingTarget.signum() > 0
        && player.getPortfolio().totalQuantityForSymbol(symbol).signum() > 0) {
      Share slice =
          player.getPortfolio().buildNextFifoSliceForTargetNet(symbol, remainingTarget);
      if (slice == null) {
        break;
      }
      transactions.add(sell(slice, player));
      BigDecimal net = new SaleCalculator(slice).calculateTotal();
      remainingTarget = remainingTarget.subtract(net);
    }
    return transactions;
  }

  /**
   * Advances the exchange to the next trading day and updates stock prices. Daily moves use a band
   * scaled from the former weekly ±5% by 1/√7 so seven independent days have comparable volatility
   * to one former weekly step.
   */
  public void advance() {
    advance(1);
  }

  /**
   * Advances the exchange by the requested number of trading days and updates stock prices for each
   * skipped day.
   *
   * @param days the number of trading days to advance
   * @throws IllegalArgumentException if {@code days} is negative
   */
  public void advance(int days) {
    if (days < 0) {
      throw new IllegalArgumentException("Days to advance cannot be negative");
    }
    for (int i = 0; i < days; i++) {
      advanceOneDay();
    }
  }

  /**
   * Advances the exchange by one trading day and updates stock prices.
   */
  private void advanceOneDay() {
    this.day += 1;
    this.stockMap.values().forEach(stock -> {
      double factor = 1 + this.random.nextDouble(-DAILY_SIGMA, DAILY_SIGMA);
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
