package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import org.junit.jupiter.api.Test;

class StocksControllerTest {

  @Test
  void constructor_nullExchange_throwsNullPointerException() {
    NullPointerException thrown = assertThrows(
        NullPointerException.class,
        () -> new StocksController(null));

    assertEquals("Exchange cannot be null", thrown.getMessage());
  }

  @Test
  void refresh_preservesSelectionBySymbol() {
    Exchange exchange = exchangeWithStocks("AAA", "BBB");
    StocksController controller = new StocksController(exchange);
    Stock second = controller.getStocks().get(1);
    controller.setSelectedStock(second);

    controller.refresh();

    assertEquals("BBB", controller.getSelectedStock().getSymbol());
  }

  @Test
  void setSelectedStock_null_clearsSelection() {
    StocksController controller = new StocksController(exchangeWithStocks("AAA"));
    controller.setSelectedStock(null);

    assertNull(controller.getSelectedStock());
  }

  @Test
  void getMarketHistoryFor_null_returnsEmptyList() {
    StocksController controller = new StocksController(exchangeWithStocks("AAA"));

    assertTrue(controller.getMarketHistoryFor(null).isEmpty());
  }

  @Test
  void getMetaText_includesExchangeNameAndListingCount() {
    StocksController controller = new StocksController(exchangeWithStocks("AAA", "BBB"));

    String meta = controller.getMetaText();

    assertTrue(meta.contains("NYSE"));
    assertTrue(meta.contains("2 listing(s)"));
  }

  private static Exchange exchangeWithStocks(String... symbols) {
    List<Stock> stocks = java.util.Arrays.stream(symbols)
        .map(symbol -> {
          Stock stock = new Stock(symbol, symbol + " Corp");
          stock.addNewSalesPrice(BigDecimal.TEN);
          return stock;
        })
        .toList();
    return new ExchangeBuilder("NYSE").stocks(stocks).build();
  }
}
