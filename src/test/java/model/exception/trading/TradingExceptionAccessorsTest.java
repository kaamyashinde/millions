package model.exception.trading;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.player.Player;
import org.junit.jupiter.api.Test;

class TradingExceptionAccessorsTest {

  @Test
  void insufficientSharesExposesSymbolAndRequestedQuantity() {
    InsufficientSharesException exception =
        new InsufficientSharesException("AAPL", new BigDecimal("3"));

    assertEquals("AAPL", exception.getSymbol());
    assertEquals(new BigDecimal("3"), exception.getRequestedQuantity());
  }

  @Test
  void shareNotFoundExposesStockSymbolAndPlayerName() {
    Stock stock = new Stock("AAPL", "Apple");
    stock.addNewSalesPrice(new BigDecimal("100"));
    Player player = new Player("Alice", new BigDecimal("1000"));
    ShareNotFoundException exception = new ShareNotFoundException(
        new Share(stock, BigDecimal.ONE, new BigDecimal("100")),
        player);

    assertEquals("AAPL", exception.getStockSymbol());
    assertEquals("Alice", exception.getPlayerName());
  }
}
