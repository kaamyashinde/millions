package model.marketevent;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import model.Stock;

/**
 * Decides whether a rare market event should occur on a given trading day.
 *
 * @author OpenAI
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface MarketEventStrategy {

  /**
   * Optionally creates an event for the current trading day.
   *
   * @param listedStocks stocks available on the exchange
   * @param tradingDay current trading day after the day increment
   * @param random random source used for selection
   * @return generated event, or {@link Optional#empty()} when no event occurs
   */
  Optional<MarketEvent> maybeCreateEvent(List<Stock> listedStocks, int tradingDay, Random random);
}
