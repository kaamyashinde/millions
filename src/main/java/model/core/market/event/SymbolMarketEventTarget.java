package model.core.market.event;


import static util.Validator.checkNotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import model.core.asset.Stock;

/**
 * Targets one or more stock symbols directly.
 *
 * <p>The symbols are copied into an immutable set so event targeting cannot be mutated after a
 * {@link MarketEvent} is created.
 *
 * @param affectedSymbols stock symbols affected by the event
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
 */
public record SymbolMarketEventTarget(Set<String> affectedSymbols) implements MarketEventTarget {

  /**
   * Normalises and validates the affected symbols.
   *
   * @param affectedSymbols stock symbols affected by the event
   */
  public SymbolMarketEventTarget {
    checkNotNull(affectedSymbols, "Affected symbols");
    affectedSymbols = Set.copyOf(new LinkedHashSet<>(affectedSymbols));
  }

  /**
   * Returns whether the supplied stock symbol belongs to this target set.
   *
   * @param stock stock to test
   * @return {@code true} when the stock symbol is included
   */
  @Override
  public boolean affects(Stock stock) {
    checkNotNull(stock, "Stock");
    return affectedSymbols.contains(stock.getSymbol());
  }

  /**
   * Returns the immutable set of affected symbols.
   *
   * @return immutable target symbol set
   */
  @Override
  public Set<String> getAffectedSymbols() {
    return affectedSymbols;
  }
}
