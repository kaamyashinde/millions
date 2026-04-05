package model.market.marketevent.unexpected;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.Set;
import model.market.Stock;
import model.market.marketevent.unexpected.target.MarketEventTarget;

/**
 * Immutable description of a rare market event and the price shock it applies.
 *
 * @param day         trading day on which the event occurred
 * @param title       short user-facing title
 * @param description user-facing description shown in the UI
 * @param target      target describing which stocks are affected
 * @param priceFactor multiplicative shock applied after the normal daily move
 * @author OpenAI
 * @version 1.0.0
 * @since 2026-04-04
 */
public record MarketEvent(
    int day,
    String title,
    String description,
    MarketEventTarget target,
    BigDecimal priceFactor
) {

  /**
   * Validates the required fields for a market event.
   */
  public MarketEvent {
    checkNotNull(title, "Title");
    checkNotNull(description, "Description");
    checkNotNull(target, "Target");
    checkNotNull(priceFactor, "Price factor");
  }

  /**
   * Returns whether the event affects the supplied stock.
   *
   * @param stock stock to test
   * @return {@code true} when the event target includes the stock
   */
  public boolean affects(Stock stock) {
    return target.affects(stock);
  }

  /**
   * Applies the event shock to an already computed baseline price.
   *
   * @param baselinePrice price after normal daily movement
   * @return price after the event shock is applied
   */
  public BigDecimal applyTo(BigDecimal baselinePrice) {
    checkNotNull(baselinePrice, "Baseline price");
    return baselinePrice.multiply(priceFactor);
  }

  /**
   * Returns the affected stock symbols for display and testing.
   *
   * @return immutable set of affected stock symbols
   */
  public Set<String> getAffectedSymbols() {
    return target.getAffectedSymbols();
  }
}
