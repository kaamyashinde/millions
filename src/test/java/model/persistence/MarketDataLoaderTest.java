package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.List;
import model.core.market.Exchange;
import model.core.market.Stock;
import org.junit.jupiter.api.Test;

class MarketDataLoaderTest {

  private static final String DEMO_RESOURCE = "/data/demo-stocks.csv";

  @Test
  void loadFromResource_readsBundledDemoMarketData() {
    MarketData marketData =
        MarketDataLoader.loadFromResource(MarketDataLoaderTest.class, DEMO_RESOURCE);

    assertEquals(4, marketData.stocks().size());
    assertEquals(2, marketData.funds().size());
    assertEquals(List.of("NVDA", "AAPL", "MSFT", "GOOGL"),
        marketData.stocks().stream().map(Stock::getSymbol).toList());
  }

  @Test
  void loadFromResource_canBootstrapExchangeWithFunds() {
    MarketData marketData =
        MarketDataLoader.loadFromResource(MarketDataLoaderTest.class, DEMO_RESOURCE);
    Exchange exchange = new Exchange.Builder("NYSE")
        .stocks(marketData.stocks())
        .funds(marketData.funds())
        .build();

    assertFalse(exchange.getFunds().isEmpty());
    assertEquals(6, exchange.getAssets().size());
  }
}
