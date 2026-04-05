package model.stockinfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import model.market.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockFinancialInfoProviderTest {

  private StockFinancialInfoProvider provider;

  @BeforeEach
  void setUp() {
    provider = new StockFinancialInfoProvider();
  }

  @Test
  void forSymbol_isDeterministic() {
    StockFinancialInfo a = provider.forSymbol("AAPL");
    StockFinancialInfo b = provider.forSymbol("AAPL");
    assertEquals(a.revenue(), b.revenue());
    assertEquals(a.profit(), b.profit());
    assertEquals(a.health(), b.health());
  }

  @Test
  void forSymbol_differentSymbols_canDiffer() {
    StockFinancialInfo aapl = provider.forSymbol("AAPL");
    StockFinancialInfo msft = provider.forSymbol("MSFT");
    boolean someFieldDiffers =
        !aapl.revenue().equals(msft.revenue())
            || !aapl.profit().equals(msft.profit())
            || !aapl.health().equals(msft.health());
    assertTrue(someFieldDiffers, "Expected mock data to vary across symbols");
  }

  @Test
  void forStock_delegatesToSymbol() {
    Stock stock = new Stock("TEST", "Test Co");
    assertEquals(provider.forSymbol("TEST"), provider.forStock(stock));
  }

  @Test
  void health_followsMarginRule() {
    StockFinancialInfo info = provider.forSymbol("ZZRULE");
    BigDecimal margin =
        info.revenue().signum() > 0
            ? info.profit().divide(info.revenue(), 6, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    if (info.revenue().signum() > 0 && info.profit().signum() > 0) {
      if (margin.compareTo(new BigDecimal("0.10")) >= 0) {
        assertEquals(CompanyHealth.STRONG, info.health());
      } else {
        assertEquals(CompanyHealth.WEAK, info.health());
      }
    } else {
      assertEquals(CompanyHealth.WEAK, info.health());
    }
  }

  @Test
  void formatMoney_usesMillionsSuffix() {
    BigDecimal amount = new BigDecimal("47500000.00");
    String formatted = provider.formatMoney(amount);
    assertEquals("$47.5M", formatted);
  }

  @Test
  void formatMoney_negativeProfit() {
    String formatted = provider.formatMoney(new BigDecimal("-2500000"));
    assertEquals("-$2.5M", formatted);
  }
}
