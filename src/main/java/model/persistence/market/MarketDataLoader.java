package model.persistence.market;


import java.io.IOException;
import java.io.InputStream;
import model.persistence.io.CsvReader;

/**
 * Shared startup loader for bundled market-data resources.
 */
public final class MarketDataLoader {

  private MarketDataLoader() {
  }

  /**
   * Loads market data from a classpath resource visible to the given anchor class.
   *
   * @param anchor class used to resolve the resource path
   * @param resourcePath absolute classpath resource path
   * @return parsed market data, or an empty bundle if the resource is missing
   */
  public static MarketData loadFromResource(Class<?> anchor, String resourcePath) {
    try (InputStream input = anchor.getResourceAsStream(resourcePath)) {
      if (input == null) {
        return MarketData.empty();
      }
      return CsvReader.readMarketData(input);
    } catch (IOException exception) {
      return MarketData.empty();
    }
  }
}
