package model.fund;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import model.market.Stock;
import model.market.fund.Fund;
import model.market.fund.FundComponent;
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
}
