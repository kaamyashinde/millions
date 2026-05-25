package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FundsControllerTest {

  private FundsController controller;

  @BeforeEach
  void setUp() {
    Stock apple = stock("AAPL", "Apple Inc.", "150.00");
    Stock microsoft = stock("MSFT", "Microsoft Corporation", "300.00");
    Fund tech = fund("TECHX", "Tech Titans Blend Fund", apple, microsoft);
    Fund growth = fund("GROW", "Global Growth Fund", apple, microsoft);
    Exchange exchange = new Exchange.Builder("NYSE")
        .stocks(List.of(apple, microsoft))
        .funds(List.of(tech, growth))
        .build();
    controller = new FundsController(exchange);
  }

  @Test
  void setSearchTerm_filtersBySymbol() {
    controller.setSearchTerm("tech");

    assertEquals(1, controller.getFunds().size());
    assertEquals("TECHX", controller.getFunds().getFirst().getSymbol());
    assertEquals("TECHX", controller.getSelectedFund().getSymbol());
  }

  @Test
  void setSearchTerm_filtersByFundNameCaseInsensitive() {
    controller.setSearchTerm("growth");

    assertEquals(1, controller.getFunds().size());
    assertEquals("GROW", controller.getFunds().getFirst().getSymbol());
  }

  @Test
  void setSearchTerm_blankResetsAllRows() {
    controller.setSearchTerm("growth");
    controller.setSearchTerm("");

    assertEquals(2, controller.getFunds().size());
    assertEquals("GROW", controller.getFunds().getFirst().getSymbol());
  }

  @Test
  void setSearchTerm_clearsSelectionWhenNoRowsMatch() {
    controller.setSearchTerm("bond");

    assertEquals(0, controller.getFunds().size());
    assertNull(controller.getSelectedFund());
  }

  @Test
  void setSearchTerm_preservesSelectionWhenStillVisible() {
    controller.setSelectedFund(controller.getFunds().stream()
        .filter(fund -> fund.getSymbol().equals("TECHX"))
        .findFirst()
        .orElseThrow());

    controller.setSearchTerm("fund");

    assertEquals("TECHX", controller.getSelectedFund().getSymbol());
  }

  private static Stock stock(String symbol, String company, String price) {
    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(new BigDecimal(price));
    return stock;
  }

  private static Fund fund(String symbol, String name, Stock first, Stock second) {
    return new Fund(
        symbol,
        name,
        List.of(
            new FundComponent(first, new BigDecimal("0.50")),
            new FundComponent(second, new BigDecimal("0.50"))));
  }
}
