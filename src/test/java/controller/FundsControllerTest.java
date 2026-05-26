package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import org.junit.jupiter.api.Test;

class FundsControllerTest {

  @Test
  void constructor_nullExchange_throwsNullPointerException() {
    NullPointerException thrown = assertThrows(
        NullPointerException.class,
        () -> new FundsController(null));

    assertEquals("Exchange cannot be null", thrown.getMessage());
  }

  @Test
  void refresh_preservesSelectionBySymbol() {
    Exchange exchange = exchangeWithFunds("FUND_A", "FUND_B");
    FundsController controller = new FundsController(exchange);
    Fund second = controller.getFunds().get(1);
    controller.setSelectedFund(second);

    controller.refresh();

    assertEquals("FUND_B", controller.getSelectedFund().getSymbol());
  }

  @Test
  void setSelectedFund_null_clearsSelection() {
    FundsController controller = new FundsController(exchangeWithFunds("FUND_A"));
    controller.setSelectedFund(null);

    assertNull(controller.getSelectedFund());
  }

  @Test
  void getMetaText_includesExchangeNameAndFundCount() {
    FundsController controller = new FundsController(exchangeWithFunds("FUND_A", "FUND_B"));

    String meta = controller.getMetaText();

    assertTrue(meta.contains("NYSE"));
    assertTrue(meta.contains("2 fund(s)"));
  }

  private static Exchange exchangeWithFunds(String... symbols) {
    Stock stock = new Stock("BASE", "Base Corp");
    stock.addNewSalesPrice(BigDecimal.TEN);
    List<Fund> funds = java.util.Arrays.stream(symbols)
        .map(symbol -> new Fund(symbol, symbol + " Name", List.of(new FundComponent(stock, BigDecimal.ONE))))
        .toList();
    return new ExchangeBuilder("NYSE").stocks(List.of(stock)).funds(funds).build();
  }
}
