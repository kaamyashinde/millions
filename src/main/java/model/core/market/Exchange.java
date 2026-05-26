package model.core.market;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.market.event.MarketEvent;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.pricing.MarketEventStrategy;
import model.core.player.Player;
import model.exception.trading.InsufficientFundsException;
import model.trading.calculator.SaleCalculator;
import model.trading.command.buy.BuyCommand;
import model.trading.command.buy.BuyUpToBudgetCommand;
import model.trading.command.sell.SellAllHoldingsCommand;
import model.trading.command.sell.SellByQuantityCommand;
import model.trading.command.sell.SellCommand;
import model.trading.command.sell.SellUpToTargetNetCommand;
import model.trading.transaction.Purchase;
import model.trading.transaction.Sale;
import model.trading.transaction.Transaction;

/**
 * A class representing the Exchange Market in the system.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 02-02-2026
 */

public class Exchange {

  private final String name;
  private final ExchangeListings listings;
  private final MarketSimulator simulator;

  /**
   * Full constructor used by package-level builders and factories.
   */
  Exchange(
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
    this.listings = new ExchangeListings(stocks, funds);
    this.simulator = new MarketSimulator(
        random,
        dailyPriceMoveStrategy,
        marketEventStrategy,
        day,
        marketEventHistory,
        lastMarketEvent);
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
   * Returns the read-only listing registry for stocks, funds, and assets.
   *
   * @return exchange listings
   */
  public ExchangeListings listings() {
    return listings;
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
   * Gets the current trading day of the exchange.
   *
   * @return the current day
   */
  public int getDay() {
    return simulator.getDay();
  }

  /**
   * Returns the most recent market event generated during {@link #advance()}, if any.
   *
   * @return latest market event for the current trading day
   */
  public Optional<MarketEvent> getLastMarketEvent() {
    return simulator.getLastMarketEvent();
  }

  /**
   * Returns the chronological market-event history recorded by the exchange.
   *
   * @return immutable list of generated market events, oldest first
   */
  public List<MarketEvent> getMarketEventHistory() {
    return simulator.getMarketEventHistory();
  }

  /**
   * Returns the recorded market events that affected the given stock symbol.
   *
   * @param symbol stock symbol whose relevant events should be returned
   * @return immutable list of matching market events, oldest first
   */
  public List<MarketEvent> getMarketEventsForStock(String symbol) {
    return simulator.getMarketEventsForStock(symbol);
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
   * several {@link Sale} transactions if the quantity spans multiple lots. Partial lots are split
   * so that
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
    simulator.advance(days, listings.getStocks());
  }

  /**
   * Gets the top gainers in the exchange based on their latest price change. The gainers are sorted
   * in descending order of their price change, and the method returns a list of the top gainers up
   * to the specified limit.
   *
   * @param limit the maximum number of gainers to return
   * @return a list of the top gainers in the exchange, sorted by their latest price change in
   *     descending order
   */
  public List<Stock> getGainers(int limit) {
    return MarketMovers.gainers(listings.getStocks(), limit);
  }

  /**
   * Gets the top losers in the exchange based on their latest price change. The losers are sorted
   * in ascending order of their price change, and the method returns a list of the top losers up to
   * the specified limit.
   *
   * @param limit the maximum number of losers to return
   * @return a list of the top losers in the exchange, sorted by their latest price change in
   *     ascending order
   */
  public List<Stock> getLosers(int limit) {
    return MarketMovers.losers(listings.getStocks(), limit);
  }

}
