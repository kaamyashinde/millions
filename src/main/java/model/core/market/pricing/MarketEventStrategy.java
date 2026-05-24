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

/** Decides whether a rare market event should occur on a given trading day. */
public interface MarketEventStrategy {

  double DEFAULT_EVENT_PROBABILITY = 0.18;
  String DEFAULT_TEMPLATE_RESOURCE = "data/market-event-templates.json";

  /** Creates the standard random event strategy using templates stored in application resources. */
  static MarketEventStrategy randomFromResources() {
    return randomFromResources(DEFAULT_EVENT_PROBABILITY, DEFAULT_TEMPLATE_RESOURCE);
  }

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

  record EventTemplate(
      String title,
      String descriptionTemplate,
      double minFactor,
      double maxFactor
  ) {

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
