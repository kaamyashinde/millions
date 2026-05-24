package model.core.market;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import org.junit.jupiter.api.Test;

class MarketMoversTest {

  @Test
  void gainersAndLosers_filterAndSortByLatestPriceChange() {
    Stock strongGainer = stockWithPrices("AAA", "Alpha", "100.00", "115.00");
    Stock smallGainer = stockWithPrices("BBB", "Beta", "100.00", "105.00");
    Stock loser = stockWithPrices("CCC", "Gamma", "100.00", "90.00");
    Stock flat = stockWithPrices("DDD", "Delta", "100.00", "100.00");
    List<Stock> stocks = List.of(smallGainer, loser, flat, strongGainer);

    assertEquals(List.of(strongGainer, smallGainer), MarketMovers.gainers(stocks, 10));
    assertEquals(List.of(loser), MarketMovers.losers(stocks, 10));
  }

  private static Stock stockWithPrices(
      String symbol,
      String company,
      String firstPrice,
      String secondPrice) {
    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(new BigDecimal(firstPrice));
    stock.addNewSalesPrice(new BigDecimal(secondPrice));
    return stock;
  }
}
