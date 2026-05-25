package model.core.market;


import model.core.asset.InvestableAsset;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.player.Player;
import model.exception.trading.InsufficientFundsException;
import model.trading.calculator.SaleCalculator;
import model.trading.transaction.Purchase;
import model.trading.transaction.Sale;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.ArrayList;
import java.util.stream.Collectors;
import model.core.asset.fund.Fund;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.event.MarketEvent;
import model.core.market.pricing.MarketEventStrategy;
import model.trading.command.buy.BuyCommand;
import model.trading.command.buy.BuyUpToBudgetCommand;
import model.trading.command.sell.SellAllHoldingsCommand;
import model.trading.command.sell.SellByQuantityCommand;
import model.trading.command.sell.SellCommand;
import model.trading.command.sell.SellUpToTargetNetCommand;
import model.trading.transaction.Transaction;

/**
 * A class representing the Exchange Market in the system.
 *
 * <p>The exchange owns listed stocks and funds, applies daily price movement strategies, records
 * market events, and delegates buy/sell commands to trading command objects.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-02-03
 */

public class Exchange {

  private static final double DAILY_SIGMA = 0.05 / Math.sqrt(7);
  private static final int PRICE_SCALE = 2;
  private static final RoundingMode PRICE_ROUNDING = RoundingMode.HALF_UP;
  private final String name;
  private final Map<String, InvestableAsset> assetMap;
  private final Map<String, Stock> stockMap;
  private final Map<String, Fund> fundMap;
  private final Random random;
  private final DailyPriceMoveStrategy dailyPriceMoveStrategy;
  private final MarketEventStrategy marketEventStrategy;
  private final List<MarketEvent> marketEventHistory;
  private int day;
  private Optional<MarketEvent> lastMarketEvent;

  /**
   * Full constructor used only by {@link Builder}; builds a complete exchange in one step.
   */
  private Exchange(
      String name,
      List<Stock> stocks,
      List<Fund> funds,
      Random random,
      DailyPriceMoveStrategy dailyPriceMoveStrategy,
      MarketEventStrategy marketEventStrategy,
      int day,
      ArrayList<MarketEvent> marketEventHistory,
      Optional<MarketEvent> lastMarketEvent) {
    this.name = name;
    this.day = day;
    this.stockMap = stocks.stream()
        .collect(Collectors.toMap(Stock::getSymbol, s -> s));
    this.fundMap = funds.stream()
        .collect(Collectors.toMap(Fund::getSymbol, f -> f));
    this.assetMap = buildAssetMap(stocks, funds);
    this.random = random;
    this.dailyPriceMoveStrategy = dailyPriceMoveStrategy;
    this.marketEventStrategy = marketEventStrategy;
    this.marketEventHistory = marketEventHistory;
    this.lastMarketEvent = lastMarketEvent;
  }

  /**
   * Fluent builder for {@link Exchange}. Supplies defaults that match the former public
   * constructors (random source, price and event strategies, day 1, empty event history).
   */
  public static final class Builder {
    private final String name;
    private List<Stock> stocks = List.of();
    private List<Fund> funds = List.of();
    private int day = 1;
    private Random random;
    private DailyPriceMoveStrategy dailyPriceMoveStrategy;
    private MarketEventStrategy marketEventStrategy;
    private List<MarketEvent> marketEventHistory = List.of();
    private MarketEvent lastMarketEvent;

    /**
     * Creates a builder for an exchange with the required display name.
     *
     * @param name exchange display name (required)
     */
    public Builder(String name) {
      this.name = Objects.requireNonNull(name, "name");
    }

    /**
     * Sets the listed stocks.
     *
     * @param stocks listed stocks, or {@code null} for an empty list
     * @return this builder
     */
    public Builder stocks(List<Stock> stocks) {
      this.stocks = stocks != null ? stocks : List.of();
      return this;
    }

    /**
     * Sets the listed funds.
     *
     * @param funds listed funds, or {@code null} for an empty list
     * @return this builder
     */
    public Builder funds(List<Fund> funds) {
      this.funds = funds != null ? funds : List.of();
      return this;
    }

    /**
     * Sets the current trading day.
     *
     * @param day current trading day; must be at least {@code 1}
     * @return this builder
     */
    public Builder day(int day) {
      this.day = day;
      return this;
    }

    /**
     * Sets the random source for simulation.
     *
     * @param random random source, or {@code null} to create a default source
     * @return this builder
     */
    public Builder random(Random random) {
      this.random = random;
      return this;
    }

    /**
     * Sets the baseline daily price movement strategy.
     *
     * @param strategy daily movement strategy, or {@code null} for
     *     {@link DailyPriceMoveStrategy#uniform(double)}
     * @return this builder
     */
    public Builder dailyPriceMoveStrategy(DailyPriceMoveStrategy strategy) {
      this.dailyPriceMoveStrategy = strategy;
      return this;
    }

    /**
     * Sets the rare market event strategy.
     *
     * @param strategy market event strategy, or {@code null} for
     *     {@link MarketEventStrategy#randomFromResources()}
     * @return this builder
     */
    public Builder marketEventStrategy(MarketEventStrategy strategy) {
      this.marketEventStrategy = strategy;
      return this;
    }

    /**
     * Sets prior market events.
     *
     * @param history prior market events, oldest first, or {@code null} for an empty history
     * @return this builder
     */
    public Builder marketEventHistory(List<MarketEvent> history) {
      this.marketEventHistory = history != null ? history : List.of();
      return this;
    }

    /**
     * Sets the latest event for the current day.
     *
     * @param event latest market event, or {@code null} when no event has occurred
     * @return this builder
     */
    public Builder lastMarketEvent(MarketEvent event) {
      this.lastMarketEvent = event;
      return this;
    }

    /**
     * Validates inputs and returns a fully initialized exchange.
     *
     * @return new exchange instance
     * @throws IllegalArgumentException if {@code day} is less than 1
     */
    public Exchange build() {
      if (day < 1) {
        throw new IllegalArgumentException("Trading day must be at least 1.");
      }
      Random resolvedRandom = random != null ? random : new Random();
      DailyPriceMoveStrategy resolvedDaily =
          dailyPriceMoveStrategy != null
              ? dailyPriceMoveStrategy
              : DailyPriceMoveStrategy.uniform(DAILY_SIGMA);
      MarketEventStrategy resolvedEvents =
          marketEventStrategy != null ? marketEventStrategy : MarketEventStrategy.randomFromResources();
      List<Stock> stockList = List.copyOf(stocks);
      List<Fund> fundList = List.copyOf(funds);
      ArrayList<MarketEvent> historyCopy = new ArrayList<>(marketEventHistory);
      Optional<MarketEvent> last = Optional.ofNullable(lastMarketEvent);
      return new Exchange(
          name,
          stockList,
          fundList,
          resolvedRandom,
          resolvedDaily,
          resolvedEvents,
          day,
          historyCopy,
          last);
    }
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
    return stockMap.containsKey(symbol.toUpperCase());
  }

  /**
   * Checks whether any investable asset with the given symbol is listed.
   *
   * @param symbol stock or fund symbol
   * @return {@code true} when the asset exists
   */
  public boolean hasAsset(String symbol) {
    return assetMap.containsKey(symbol.toUpperCase());
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
   * Finds funds by symbol or display name.
   *
   * @param searchTerm search term
   * @return matching funds
   */
  public List<Fund> findFunds(String searchTerm) {
    String lowerCaseTerm = searchTerm.toLowerCase();
    return fundMap.values().stream()
        .filter(fund -> fund.getSymbol().toLowerCase().contains(lowerCaseTerm)
            || fund.getDisplayName().toLowerCase().contains(lowerCaseTerm))
        .toList();
  }

  /**
   * Finds all listed investable assets by symbol or display name.
   *
   * @param searchTerm search term
   * @return matching assets
   */
  public List<InvestableAsset> findAssets(String searchTerm) {
    String lowerCaseTerm = searchTerm.toLowerCase();
    return assetMap.values().stream()
        .filter(asset -> asset.getSymbol().toLowerCase().contains(lowerCaseTerm)
            || asset.getDisplayName().toLowerCase().contains(lowerCaseTerm))
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
    return new BuyCommand(this, symbol, quantity).execute(player).getFirst();
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
    return new BuyUpToBudgetCommand(this, symbol, maxSpend).execute(player).getFirst();
  }

  /**
   * Gets the stock by its symbol.
   *
   * @param symbol the stock symbol
   * @return the stock object
   */
  public Stock getStock(String symbol) {
    return stockMap.get(symbol.toUpperCase());
  }

  /**
   * Gets the fund by its symbol.
   *
   * @param symbol the fund symbol
   * @return the fund object
   */
  public Fund getFund(String symbol) {
    return fundMap.get(symbol.toUpperCase());
  }

  /**
   * Gets any listed investable asset by symbol.
   *
   * @param symbol stock or fund symbol
   * @return the matching asset, or {@code null}
   */
  public InvestableAsset getAsset(String symbol) {
    return assetMap.get(symbol.toUpperCase());
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
   * Returns the most recent market event generated during {@link #advance()}, if any.
   *
   * @return latest market event for the current trading day
   */
  public Optional<MarketEvent> getLastMarketEvent() {
    return lastMarketEvent;
  }

  /**
   * Returns the chronological market-event history recorded by the exchange.
   *
   * @return immutable list of generated market events, oldest first
   */
  public List<MarketEvent> getMarketEventHistory() {
    return List.copyOf(marketEventHistory);
  }

  /**
   * Returns the recorded market events that affected the given stock symbol.
   *
   * @param symbol stock symbol whose relevant events should be returned
   * @return immutable list of matching market events, oldest first
   */
  public List<MarketEvent> getMarketEventsForStock(String symbol) {
    return marketEventHistory.stream()
        .filter(event -> event.getAffectedSymbols().contains(symbol))
        .toList();
  }

  /**
   * Sells shares of a stock for a player. This method creates a Sale transaction and commits it.
   *
   * @param share  the share to sell
   * @param player the player making the sale
   * @return the Sale transaction
   */
  public Transaction sell(Share share, Player player) {
    return new SellCommand(this, share).execute(player).getFirst();
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
    return new SellByQuantityCommand(this, symbol, quantity).execute(player);
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
    return new SellUpToTargetNetCommand(this, symbol, targetNet).execute(player);
  }

  /**
   * Sells every symbol held in the player's portfolio using FIFO lots.
   *
   * @param player the player
   * @return all sale transactions (empty when the portfolio has no holdings)
   */
  public List<Transaction> sellAllHoldings(Player player) {
    return new SellAllHoldingsCommand(this).execute(player);
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
    List<Stock> listedStocks = List.copyOf(this.stockMap.values());
    this.lastMarketEvent = marketEventStrategy.maybeCreateEvent(listedStocks, this.day, this.random);
    this.lastMarketEvent.ifPresent(marketEventHistory::add);
    this.stockMap.values().forEach(stock -> {
      BigDecimal nextPrice = dailyPriceMoveStrategy.calculateNextPrice(stock, this.random);
      if (lastMarketEvent.isPresent() && lastMarketEvent.get().affects(stock)) {
        nextPrice = lastMarketEvent.get().applyTo(nextPrice);
      }
      stock.addNewSalesPrice(normalizePrice(nextPrice));
    });
  }

  /**
   * Rounds simulated market prices to currency precision before storing them in price history.
   *
   * @param price computed market price
   * @return price rounded to cents
   */
  private static BigDecimal normalizePrice(BigDecimal price) {
    return price.setScale(PRICE_SCALE, PRICE_ROUNDING);
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

  /**
   * Returns all listed stocks.
   *
   * @return immutable stock list
   */
  public List<Stock> getStocks() {
    return List.copyOf(stockMap.values());
  }

  /**
   * Returns all listed funds.
   *
   * @return immutable fund list
   */
  public List<Fund> getFunds() {
    return List.copyOf(fundMap.values());
  }

  /**
   * Returns all listed assets.
   *
   * @return immutable asset list
   */
  public List<InvestableAsset> getAssets() {
    return List.copyOf(assetMap.values());
  }

  /**
   * Combines stock and fund listings into one symbol-keyed registry.
   *
   * @param stocks listed stocks
   * @param funds listed funds
   * @return combined symbol map
   */
  private static Map<String, InvestableAsset> buildAssetMap(List<Stock> stocks, List<Fund> funds) {
    Map<String, InvestableAsset> assets = new java.util.HashMap<>();
    stocks.forEach(stock -> assets.put(stock.getSymbol(), stock));
    funds.forEach(fund -> assets.put(fund.getSymbol(), fund));
    return Map.copyOf(assets);
  }

}
