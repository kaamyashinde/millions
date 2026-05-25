package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StocksControllerTest {

  private StocksController controller;

  @BeforeEach
  void setUp() {
    Stock apple = stock("AAPL", "Apple Inc.", "150.00");
    Stock microsoft = stock("MSFT", "Microsoft Corporation", "300.00");
    Stock alphabet = stock("GOOGL", "Alphabet Inc.", "2800.00");
    Exchange exchange = new ExchangeBuilder("NYSE")
        .stocks(List.of(microsoft, apple, alphabet))
        .build();
    controller = new StocksController(exchange);
  }

  @Test
  void setSearchTerm_filtersBySymbol() {
    controller.setSearchTerm("ms");

    assertEquals(1, controller.getStocks().size());
    assertEquals("MSFT", controller.getStocks().getFirst().getSymbol());
    assertEquals("MSFT", controller.getSelectedStock().getSymbol());
  }

  @Test
  void setSearchTerm_filtersByCompanyNameCaseInsensitive() {
    controller.setSearchTerm("apple");

    assertEquals(1, controller.getStocks().size());
    assertEquals("AAPL", controller.getStocks().getFirst().getSymbol());
  }

  @Test
  void setSearchTerm_blankResetsAllRows() {
    controller.setSearchTerm("micro");
    controller.setSearchTerm("  ");

    assertEquals(3, controller.getStocks().size());
    assertEquals("AAPL", controller.getStocks().getFirst().getSymbol());
  }

  @Test
  void setSearchTerm_clearsSelectionWhenNoRowsMatch() {
    controller.setSearchTerm("tesla");

    assertEquals(0, controller.getStocks().size());
    assertNull(controller.getSelectedStock());
  }

  @Test
  void setSearchTerm_preservesSelectionWhenStillVisible() {
    controller.setSelectedStock(controller.getStocks().stream()
        .filter(stock -> stock.getSymbol().equals("GOOGL"))
        .findFirst()
        .orElseThrow());

    controller.setSearchTerm("inc");

    assertEquals("GOOGL", controller.getSelectedStock().getSymbol());
  }

  @Test
  void getTopWinners_sortsByPercentGainDescending() {
    StocksController movers = new StocksController(
        new ExchangeBuilder("NYSE")
            .stocks(List.of(
                stockWithPrices("AAPL", "Apple Inc.", "100.00", "105.00"),
                stockWithPrices("MSFT", "Microsoft Corporation", "20.00", "22.00"),
                stockWithPrices("GOOGL", "Alphabet Inc.", "50.00", "51.00")))
            .build());

    List<MarketMover> winners = movers.getTopWinners(3);

    assertEquals(List.of("MSFT", "AAPL", "GOOGL"),
        winners.stream().map(MarketMover::symbol).toList());
    assertEquals("22.00", winners.getFirst().currentPrice().toPlainString());
    assertEquals("2.00", winners.getFirst().absoluteChange().toPlainString());
    assertEquals("0.10000000", winners.getFirst().percentChange().toPlainString());
  }

  @Test
  void getTopLosers_sortsByPercentLossAscending() {
    StocksController movers = new StocksController(
        new ExchangeBuilder("NYSE")
            .stocks(List.of(
                stockWithPrices("AAPL", "Apple Inc.", "100.00", "95.00"),
                stockWithPrices("MSFT", "Microsoft Corporation", "20.00", "18.00"),
                stockWithPrices("GOOGL", "Alphabet Inc.", "50.00", "49.00")))
            .build());

    List<MarketMover> losers = movers.getTopLosers(3);

    assertEquals(List.of("MSFT", "AAPL", "GOOGL"),
        losers.stream().map(MarketMover::symbol).toList());
    assertEquals("-0.10000000", losers.getFirst().percentChange().toPlainString());
  }

  @Test
  void getTopMovers_ignoresSinglePriceAndUnchangedStocks() {
    StocksController movers = new StocksController(
        new ExchangeBuilder("NYSE")
            .stocks(List.of(
                stockWithPrices("AAPL", "Apple Inc.", "100.00"),
                stockWithPrices("MSFT", "Microsoft Corporation", "20.00", "20.00"),
                stockWithPrices("GOOGL", "Alphabet Inc.", "50.00", "55.00")))
            .build());

    assertEquals(List.of("GOOGL"),
        movers.getTopWinners(10).stream().map(MarketMover::symbol).toList());
    assertTrue(movers.getTopLosers(10).isEmpty());
  }

  @Test
  void getTopMovers_respectsLimit() {
    StocksController movers = new StocksController(
        new ExchangeBuilder("NYSE")
            .stocks(List.of(
                stockWithPrices("AAPL", "Apple Inc.", "100.00", "105.00"),
                stockWithPrices("MSFT", "Microsoft Corporation", "20.00", "22.00")))
            .build());

    assertEquals(1, movers.getTopWinners(1).size());
    assertTrue(movers.getTopWinners(0).isEmpty());
  }

  private static Stock stock(String symbol, String company, String price) {
    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(new BigDecimal(price));
    return stock;
  }

  private static Stock stockWithPrices(String symbol, String company, String... prices) {
    Stock stock = new Stock(symbol, company);
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
