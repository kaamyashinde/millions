package model.core.market.pricing;


import model.core.market.event.MarketEvent;
import model.core.market.event.SymbolMarketEventTarget;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import model.core.asset.Stock;

/**
 * Creates occasional stock-specific market events with a larger shock than the normal daily band.
 *
 * @author OpenAI
 * @version 1.0.0
 * @since 2026-04-04
 */
public class RandomMarketEventStrategy implements MarketEventStrategy {

  private static final double DEFAULT_EVENT_PROBABILITY = 0.18;

  private final double eventProbability;
  private final List<EventTemplate> templates;

  /**
   * Creates a strategy with built-in event templates and probability.
   */
  public RandomMarketEventStrategy() {
    this(DEFAULT_EVENT_PROBABILITY, defaultTemplates());
  }

  /**
   * Creates a strategy with the given probability and templates.
   *
   * @param eventProbability probability that any given advance generates an event
   * @param templates candidate event templates used when an event is triggered
   */
  public RandomMarketEventStrategy(double eventProbability, List<EventTemplate> templates) {
    this.eventProbability = eventProbability;
    this.templates = List.copyOf(templates);
  }

  /**
   * Generates a stock-specific event when the probability check succeeds.
   *
   * @param listedStocks stocks available on the exchange
   * @param tradingDay current trading day
   * @param random random source used for event selection
   * @return generated event, or empty when no event occurs
   */
  @Override
  public Optional<MarketEvent> maybeCreateEvent(List<Stock> listedStocks, int tradingDay, Random random) {
    checkNotNull(listedStocks, "Listed stocks");
    checkNotNull(random, "Random");
    if (listedStocks.isEmpty() || random.nextDouble() >= eventProbability) {
      return Optional.empty();
    }

    Stock stock = listedStocks.get(random.nextInt(listedStocks.size()));
    EventTemplate template = templates.get(random.nextInt(templates.size()));
    BigDecimal priceFactor = BigDecimal.valueOf(template.samplePriceFactor(random));
    return Optional.of(
        new MarketEvent(
            tradingDay,
            stock.getSymbol() + ": " + template.title(),
            template.description(stock),
            new SymbolMarketEventTarget(Set.of(stock.getSymbol())),
            priceFactor));
  }

  private static List<EventTemplate> defaultTemplates() {
    return List.of(
        new EventTemplate(
            "Earnings beat expectations",
            "%s reported stronger earnings than expected, lifting sentiment around %s.",
            1.08,
            1.18),
        new EventTemplate(
            "Product launch gains traction",
            "%s announced strong demand for a new release, boosting confidence in %s.",
            1.07,
            1.14),
        new EventTemplate(
            "Regulatory setback",
            "%s faces a regulatory setback that raises fresh uncertainty around %s.",
            0.82,
            0.93),
        new EventTemplate(
            "Guidance cut rattles investors",
            "%s lowered guidance, causing a sharp repricing in %s.",
            0.84,
            0.94));
  }

  /**
   * Defines a reusable text template and shock range for a random market event.
   *
   * @param title short event title
   * @param descriptionTemplate sentence template with company then symbol placeholders
   * @param minFactor minimum multiplicative shock
   * @param maxFactor maximum multiplicative shock
   */
  public record EventTemplate(
      String title,
      String descriptionTemplate,
      double minFactor,
      double maxFactor
  ) {

    /**
     * Builds the user-facing description for the selected stock.
     *
     * @param stock affected stock
     * @return rendered description text
     */
    public String description(Stock stock) {
      return descriptionTemplate.formatted(stock.getCompany(), stock.getSymbol());
    }

    /**
     * Samples a multiplicative price factor within the template's configured range.
     *
     * @param random random source used for sampling
     * @return sampled multiplicative factor
     */
    public double samplePriceFactor(Random random) {
      return random.nextDouble(minFactor, maxFactor);
    }
  }
}
