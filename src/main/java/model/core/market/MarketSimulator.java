package model.core.market;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import model.core.asset.Stock;
import model.core.market.event.MarketEvent;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.pricing.MarketEventStrategy;

/**
 * Advances market time and records generated market events.
 */
final class MarketSimulator {

  private static final int PRICE_SCALE = 2;
  private static final RoundingMode PRICE_ROUNDING = RoundingMode.HALF_UP;

  private final Random random;
  private final DailyPriceMoveStrategy dailyPriceMoveStrategy;
  private final MarketEventStrategy marketEventStrategy;
  private final List<MarketEvent> marketEventHistory;

  private int day;
  private Optional<MarketEvent> lastMarketEvent;

  /**
   * Creates a simulator with the current day, strategies, and event history.
   *
   * @param random random source used by simulation strategies
   * @param dailyPriceMoveStrategy baseline daily price movement
   * @param marketEventStrategy optional market event source
   * @param day current trading day
   * @param marketEventHistory prior market events, oldest first
   * @param lastMarketEvent latest market event for the current day
   */
  MarketSimulator(
      Random random,
      DailyPriceMoveStrategy dailyPriceMoveStrategy,
      MarketEventStrategy marketEventStrategy,
      int day,
      List<MarketEvent> marketEventHistory,
      Optional<MarketEvent> lastMarketEvent) {
    this.random = random;
    this.dailyPriceMoveStrategy = dailyPriceMoveStrategy;
    this.marketEventStrategy = marketEventStrategy;
    this.day = day;
    this.marketEventHistory = new ArrayList<>(marketEventHistory);
    this.lastMarketEvent = lastMarketEvent;
  }

  /**
   * Returns the current trading day.
   *
   * @return current day
   */
  int getDay() {
    return day;
  }

  /**
   * Returns the latest market event for the current day, if one was generated.
   *
   * @return optional latest event
   */
  Optional<MarketEvent> getLastMarketEvent() {
    return lastMarketEvent;
  }

  /**
   * Returns the full generated event history.
   *
   * @return immutable event list
   */
  List<MarketEvent> getMarketEventHistory() {
    return List.copyOf(marketEventHistory);
  }

  /**
   * Returns events that affected the given stock symbol.
   *
   * @param symbol stock symbol
   * @return matching events, oldest first
   */
  List<MarketEvent> getMarketEventsForStock(String symbol) {
    return marketEventHistory.stream()
        .filter(event -> event.getAffectedSymbols().contains(symbol))
        .toList();
  }

  /**
   * Advances the simulation and updates each listed stock once per day.
   *
   * @param days number of trading days to advance
   * @param stocks listed stocks to update
   * @throws IllegalArgumentException if {@code days} is negative
   */
  void advance(int days, Collection<Stock> stocks) {
    if (days < 0) {
      throw new IllegalArgumentException("Days to advance cannot be negative");
    }
    for (int i = 0; i < days; i++) {
      advanceOneDay(stocks);
    }
  }

  /**
   * Advances one trading day and updates every listed stock.
   *
   * @param stocks listed stocks to update
   */
  private void advanceOneDay(Collection<Stock> stocks) {
    day += 1;
    List<Stock> listedStocks = List.copyOf(stocks);
    lastMarketEvent = marketEventStrategy.maybeCreateEvent(listedStocks, day, random);
    lastMarketEvent.ifPresent(marketEventHistory::add);
    listedStocks.forEach(this::updateStockPrice);
  }

  /**
   * Applies the daily move and any active market event to one stock.
   *
   * @param stock stock to update
   */
  private void updateStockPrice(Stock stock) {
    BigDecimal nextPrice = dailyPriceMoveStrategy.calculateNextPrice(stock, random);
    if (lastMarketEvent.isPresent() && lastMarketEvent.get().affects(stock)) {
      nextPrice = lastMarketEvent.get().applyTo(nextPrice);
    }
    stock.addNewSalesPrice(normalizePrice(nextPrice));
  }

  /**
   * Rounds simulated prices to currency precision before storing them.
   *
   * @param price computed market price
   * @return rounded price
   */
  private static BigDecimal normalizePrice(BigDecimal price) {
    return price.setScale(PRICE_SCALE, PRICE_ROUNDING);
  }
}
