package model.core.market;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.market.event.MarketEvent;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.pricing.MarketEventStrategy;

/**
 * Builds exchanges with default random, pricing, event, day, and event-history settings.
 */
public final class ExchangeBuilder {

  private static final double DAILY_SIGMA = 0.05 / Math.sqrt(7);

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
   * Creates a builder for an exchange with the given display name.
   *
   * @param name exchange display name
   */
  public ExchangeBuilder(String name) {
    this.name = Objects.requireNonNull(name, "name");
  }

  /**
   * Sets the listed stocks.
   *
   * @param stocks listed stocks
   * @return this builder
   */
  public ExchangeBuilder stocks(List<Stock> stocks) {
    this.stocks = stocks != null ? stocks : List.of();
    return this;
  }

  /**
   * Sets the listed funds.
   *
   * @param funds listed funds
   * @return this builder
   */
  public ExchangeBuilder funds(List<Fund> funds) {
    this.funds = funds != null ? funds : List.of();
    return this;
  }

  /**
   * Sets the current trading day.
   *
   * @param day current trading day, at least 1
   * @return this builder
   */
  public ExchangeBuilder day(int day) {
    this.day = day;
    return this;
  }

  /**
   * Sets the random source for simulation.
   *
   * @param random random source
   * @return this builder
   */
  public ExchangeBuilder random(Random random) {
    this.random = random;
    return this;
  }

  /**
   * Sets the baseline daily price move strategy.
   *
   * @param strategy price move strategy
   * @return this builder
   */
  public ExchangeBuilder dailyPriceMoveStrategy(DailyPriceMoveStrategy strategy) {
    this.dailyPriceMoveStrategy = strategy;
    return this;
  }

  /**
   * Sets the rare market event strategy.
   *
   * @param strategy market event strategy
   * @return this builder
   */
  public ExchangeBuilder marketEventStrategy(MarketEventStrategy strategy) {
    this.marketEventStrategy = strategy;
    return this;
  }

  /**
   * Sets prior market events, oldest first.
   *
   * @param history prior event history
   * @return this builder
   */
  public ExchangeBuilder marketEventHistory(List<MarketEvent> history) {
    this.marketEventHistory = history != null ? history : List.of();
    return this;
  }

  /**
   * Sets the latest event for the current day.
   *
   * @param event latest market event
   * @return this builder
   */
  public ExchangeBuilder lastMarketEvent(MarketEvent event) {
    this.lastMarketEvent = event;
    return this;
  }

  /**
   * Validates inputs and returns a fully initialized exchange.
   *
   * @return new exchange instance
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
        marketEventStrategy != null
            ? marketEventStrategy
            : MarketEventStrategy.randomFromResources();
    return new Exchange(
        name,
        List.copyOf(stocks),
        List.copyOf(funds),
        resolvedRandom,
        resolvedDaily,
        resolvedEvents,
        day,
        new ArrayList<>(marketEventHistory),
        Optional.ofNullable(lastMarketEvent));
  }
}
