package model.trade;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import model.Exchange;
import model.Player;
import model.Stock;
import model.transaction.Transaction;
import model.transactioncalculator.SaleCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SellUpToTargetNetCommandTest {

  private Exchange exchange;
  private Player player;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    exchange = new Exchange.Builder("NYSE").stocks(List.of(stock)).build();
    player = new Player("T", new BigDecimal("100000.00"));
    new BuyCommand(exchange, "AAPL", new BigDecimal("100")).execute(player);
  }

  @Test
  void execute_respectsNetCap() {
    BigDecimal target = new BigDecimal("500");
    SellUpToTargetNetCommand cmd =
        new SellUpToTargetNetCommand(exchange, "AAPL", target);
    List<Transaction> txs = cmd.execute(player);

    BigDecimal sumNet =
        txs.stream()
            .map(t -> new SaleCalculator(t.getShare()).calculateTotal())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertTrue(sumNet.compareTo(target) <= 0);
    assertTrue(sumNet.signum() > 0);
  }

  @Test
  void describe_mentionsTargetAndSymbol() {
    SellUpToTargetNetCommand cmd =
        new SellUpToTargetNetCommand(exchange, "AAPL", new BigDecimal("500"));
    assertTrue(cmd.describe().contains("AAPL"));
    assertTrue(cmd.describe().contains("500"));
  }
}
