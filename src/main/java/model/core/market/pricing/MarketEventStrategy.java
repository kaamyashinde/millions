package model.core.market.pricing;


import static util.Validator.checkNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import model.core.asset.Stock;
import model.core.market.event.MarketEvent;
import model.core.market.event.SymbolMarketEventTarget;

/**
 * Decides whether a rare market event should occur on a given trading day.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface MarketEventStrategy {

  /** Default probability of producing a market event on each trading day. */
  double DEFAULT_EVENT_PROBABILITY = 0.18;

  /** Classpath resource containing default event templates. */
  String DEFAULT_TEMPLATE_RESOURCE = "data/market-event-templates.json";

  /**
   * Creates the standard random event strategy using templates stored in application resources.
   *
   * @return default resource-backed random event strategy
   */
  static MarketEventStrategy randomFromResources() {
    return randomFromResources(DEFAULT_EVENT_PROBABILITY, DEFAULT_TEMPLATE_RESOURCE);
  }

  /**
   * Creates a random event strategy from a template resource.
   *
   * @param eventProbability probability of producing an event on each trading day
   * @param templateResource classpath resource containing event templates
   * @return resource-backed random event strategy
   */
  static MarketEventStrategy randomFromResources(double eventProbability, String templateResource) {
    List<EventTemplate> templates = loadTemplates(templateResource);
    return (listedStocks, tradingDay, random) -> {
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
    };
  }

  /**
   * Attempts to create a market event for the given trading day.
   *
   * @param listedStocks stocks listed on the exchange
   * @param tradingDay trading day being generated
   * @param random random source for stochastic strategies
   * @return generated event, or {@link Optional#empty()} when no event occurs
   */
  Optional<MarketEvent> maybeCreateEvent(List<Stock> listedStocks, int tradingDay, Random random);

  private static List<EventTemplate> loadTemplates(String resourceName) {
    checkNotNull(resourceName, "Template resource");
    try (InputStream input =
        MarketEventStrategy.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (input == null) {
        throw new IllegalStateException("Missing market event template resource: " + resourceName);
      }
      List<EventTemplate> templates =
          new ObjectMapper().readValue(input, new TypeReference<>() {});
      if (templates.isEmpty()) {
        throw new IllegalStateException("Market event template resource contains no templates.");
      }
      return List.copyOf(templates);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to load market event templates: " + resourceName, e);
    }
  }

  /**
   * Resource-backed market event template.
   *
   * @param title short event title
   * @param descriptionTemplate formatted description with company and symbol placeholders
   * @param minFactor minimum sampled price factor
   * @param maxFactor maximum sampled price factor
   */
  record EventTemplate(
      String title,
      String descriptionTemplate,
      double minFactor,
      double maxFactor
  ) {

    /**
     * Validates required template text.
     */
    public EventTemplate {
      checkNotNull(title, "Title");
      checkNotNull(descriptionTemplate, "Description template");
    }

    String description(Stock stock) {
      return descriptionTemplate.formatted(stock.getCompany(), stock.getSymbol());
    }

    double samplePriceFactor(Random random) {
      return random.nextDouble(minFactor, maxFactor);
    }
  }
}
