package model.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import model.core.market.fund.Fund;
import model.core.market.fund.FundComponent;
import model.core.market.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistoricalAssetPriceServiceTest {

  private HistoricalAssetPriceService historicalAssetPriceService;

  @BeforeEach
  void setUp() {
    historicalAssetPriceService = new HistoricalAssetPriceService();
  }

  @Test
  void getFundPriceOnDay_derivesHistoricalValueFromWeightedStocks() {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00", "110.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "50.00", "55.00");
    Fund fund = new Fund(
        "TECHX",
        "Tech Fund",
        List.of(
            new FundComponent(apple, new BigDecimal("0.60")),
            new FundComponent(microsoft, new BigDecimal("0.40"))));

    BigDecimal dayTwoPrice = historicalAssetPriceService.getFundPriceOnDay(fund, 2);

    assertEquals(0, new BigDecimal("88.00").compareTo(dayTwoPrice));
  }

  /**
   * Creates a stock with ordered historical prices.
   *
   * @param symbol  stock symbol
   * @param company display name
   * @param prices  ordered prices, oldest to newest
   * @return stock populated with the supplied prices
   */
  private static Stock stockWithPrices(String symbol, String company, String... prices) {
    Stock stock = new Stock(symbol, company);
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
