package model.persistence.market;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import model.core.market.Exchange;
import model.core.asset.Stock;
import org.junit.jupiter.api.Test;

class MarketDataLoaderTest {

  private static final String DEMO_RESOURCE = "/data/demo-stocks.csv";

  @Test
  void loadFromResource_readsBundledDemoMarketData() {
    MarketData marketData = MarketDataLoader.loadFromResource(MarketDataLoaderTest.class, DEMO_RESOURCE);

    assertEquals(4, marketData.stocks().size());
    assertEquals(2, marketData.funds().size());
    assertEquals(List.of("NVDA", "AAPL", "MSFT", "GOOGL"),
        marketData.stocks().stream().map(Stock::getSymbol).toList());
  }

  @Test
  void loadFromResource_canBootstrapExchangeWithFunds() {
    MarketData marketData = MarketDataLoader.loadFromResource(MarketDataLoaderTest.class, DEMO_RESOURCE);
    Exchange exchange = new Exchange.Builder("NYSE")
        .stocks(marketData.stocks())
        .funds(marketData.funds())
        .build();

    assertFalse(exchange.getFunds().isEmpty());
    assertEquals(6, exchange.getAssets().size());
  }

  @Test
  void loadFromResource_returnsEmptyMarketDataForMissingResource() {
    MarketData marketData = MarketDataLoader.loadFromResource(
        MarketDataLoaderTest.class,
        "/data/missing.csv");

    assertTrue(marketData.isEmpty());
    assertEquals(List.of(), MarketData.empty().stocks());
    assertEquals(List.of(), MarketData.empty().funds());
  }

  @Test
  void marketDataIsEmpty_requiresBothListsToBeEmpty() {
    Stock stock = new Stock("ONE", "One");
    stock.addNewSalesPrice(BigDecimal.ONE);

    assertFalse(new MarketData(List.of(stock), List.of()).isEmpty());
  }

  @Test
  void privateConstructor_isCoveredForUtilityClass() throws Exception {
    Constructor<MarketDataLoader> constructor = MarketDataLoader.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    constructor.newInstance();
  }

  @Test
  void loadFromResource_returnsEmptyWhenResourceCloseFails() throws Exception {
    try (URLClassLoader loader = isolatedLoaderWithThrowingResource()) {
      Class<?> loaderClass = Class.forName(
          "model.persistence.market.MarketDataLoader",
          true,
          loader);
      Method method = loaderClass.getMethod("loadFromResource", Class.class, String.class);
      Object marketData = method.invoke(null, loaderClass, "/throw-on-close.csv");
      Method isEmpty = marketData.getClass().getMethod("isEmpty");

      assertEquals(true, isEmpty.invoke(marketData));
    }
  }

  private static URLClassLoader isolatedLoaderWithThrowingResource() throws Exception {
    URL classes = Path.of(System.getProperty("user.dir"), "target", "classes")
        .toUri()
        .toURL();
    return new URLClassLoader(new URL[] {classes}, ClassLoader.getSystemClassLoader()) {
      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals("model.persistence.market.MarketDataLoader")) {
          Class<?> loaded = findLoadedClass(name);
          if (loaded == null) {
            loaded = findClass(name);
          }
          if (resolve) {
            resolveClass(loaded);
          }
          return loaded;
        }
        return super.loadClass(name, resolve);
      }

      @Override
      public InputStream getResourceAsStream(String name) {
        if (name.equals("throw-on-close.csv")) {
          return new ByteArrayInputStream("STOCK,TST,Test,1.00\n".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
              throw new IOException("close failed");
            }
          };
        }
        return super.getResourceAsStream(name);
      }
    };
  }
}
