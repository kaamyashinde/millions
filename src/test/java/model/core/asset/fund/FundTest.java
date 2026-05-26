package model.core.asset.fund;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import org.junit.jupiter.api.Test;

class FundTest {

  @Test
  void getSalesPrice_derivesValueFromUnderlyingStocks() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("100.00"));
    Stock microsoft = new Stock("MSFT", "Microsoft Corporation");
    microsoft.addNewSalesPrice(new BigDecimal("200.00"));
    Fund fund = new Fund(
        "BLEND",
        "Balanced Leaders Fund",
        List.of(
            new FundComponent(apple, new BigDecimal("0.25")),
            new FundComponent(microsoft, new BigDecimal("0.75"))));

    assertEquals(0, new BigDecimal("175.0000").compareTo(fund.getSalesPrice()));
  }

  @Test
  void getPriceOnDayDerivesHistoricalValueFromWeightedStocks() {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00", "110.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "50.00", "55.00");
    Fund fund = new Fund(
        "TECHX",
        "Tech Fund",
        List.of(
            new FundComponent(apple, new BigDecimal("0.60")),
            new FundComponent(microsoft, new BigDecimal("0.40"))));

    BigDecimal dayTwoPrice = fund.getPriceOnDay(2);

    assertEquals(0, new BigDecimal("88.00").compareTo(dayTwoPrice));
  }

  @Test
  void constructorNormalizesSymbolAndRejectsInvalidComponents() {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00");
    Fund fund = new Fund(
        "blend",
        "Blend Fund",
        List.of(new FundComponent(apple, BigDecimal.ONE)));

    assertEquals("BLEND", fund.getSymbol());
    assertEquals("Blend Fund", fund.getDisplayName());
    assertEquals("Fund", fund.getAssetType());
    assertThrows(NullPointerException.class, () -> new Fund(null, "Name", List.of()));
    assertThrows(NullPointerException.class, () -> new Fund("FUND", null, List.of()));
    assertThrows(NullPointerException.class, () -> new Fund("FUND", "Name", null));
    assertThrows(IllegalArgumentException.class, () -> new Fund("FUND", "Name", List.of()));
    assertThrows(IllegalArgumentException.class, () -> new Fund(
        "FUND",
        "Name",
        List.of(new FundComponent(apple, new BigDecimal("0.50")))));
  }

  private static Stock stockWithPrices(String symbol, String company, String... prices) {
    Stock stock = new Stock(symbol, company);
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
