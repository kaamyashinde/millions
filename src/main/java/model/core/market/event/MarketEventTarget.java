package model.core.market.event;


import java.util.Set;
import model.core.asset.Stock;

/**
 * Describes which stocks are affected by a market event.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface MarketEventTarget {

  /**
   * Returns whether the given stock is affected by this target.
   *
   * @param stock stock to test
   * @return {@code true} when the stock is affected by the market event
   */
  boolean affects(Stock stock);

  /**
   * Returns the affected stock symbols for rendering and assertions.
   *
   * @return immutable set of affected symbols
   */
  Set<String> getAffectedSymbols();
}
