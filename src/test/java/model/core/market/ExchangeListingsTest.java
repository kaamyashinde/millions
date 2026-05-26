package model.core.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import org.junit.jupiter.api.Test;

class ExchangeListingsTest {

  @Test
  void listings_findStocksFundsAndAssetsBySymbolOrName() {
    Stock apple = stock("AAPL", "Apple Inc.", "150.00");
    Fund techFund = new Fund(
        "TECHX",
        "Tech Titans Blend Fund",
        List.of(new FundComponent(apple, BigDecimal.ONE)));
    ExchangeListings listings = new ExchangeListings(List.of(apple), List.of(techFund));

    assertTrue(listings.hasStock("aapl"));
    assertFalse(listings.hasStock("TSLA"));
    assertTrue(listings.hasAsset("techx"));
    assertTrue(listings.hasAsset("AAPL"));
    assertEquals(apple, listings.getStock("AAPL"));
    assertNull(listings.getStock("TSLA"));
    assertEquals(techFund, listings.getFund("techx"));
    assertEquals(apple, listings.findStocks("apple").getFirst());
    assertEquals(techFund, listings.findFunds("titans").getFirst());
    assertEquals(techFund, listings.findAssets("blend").getFirst());
    assertTrue(listings.findAssets("aapl").stream()
        .anyMatch(asset -> asset.getSymbol().equals("AAPL")));
  }

  @Test
  void listings_findStocksByCompanyAndPartialMatchCaseInsensitively() {
    Stock apple = stock("AAPL", "Apple Inc.", "150.00");
    Stock microsoft = stock("MSFT", "Microsoft Corporation", "300.00");
    ExchangeListings listings = new ExchangeListings(List.of(apple, microsoft), List.of());

    assertEquals(apple, listings.findStocks("APPLE").getFirst());
    assertEquals(microsoft, listings.findStocks("Microsoft").getFirst());
    assertEquals(1, listings.findStocks("Inc").size());
    assertTrue(listings.findStocks("Tesla").isEmpty());
  }

  @Test
  void listings_returnImmutableSnapshots() {
    ExchangeListings listings = new ExchangeListings(
        List.of(stock("AAPL", "Apple Inc.", "150.00")),
        List.of());

    assertThrows(UnsupportedOperationException.class, () -> listings.getStocks().clear());
  }

  private static Stock stock(String symbol, String company, String price) {
    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(new BigDecimal(price));
    return stock;
  }
}
