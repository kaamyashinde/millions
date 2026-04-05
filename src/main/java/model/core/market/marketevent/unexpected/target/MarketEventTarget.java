package model.core.market.marketevent.unexpected.target;

import java.util.Set;
import model.core.market.stock.Stock;

/**
 * Describes which stocks are affected by a market event.
 *
 * @author kevindmzazali
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
