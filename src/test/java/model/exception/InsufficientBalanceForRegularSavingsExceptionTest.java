package model.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class InsufficientBalanceForRegularSavingsExceptionTest {

  @Test
  void exposesSymbolAndCause() {
    InsufficientFundsException cause = new InsufficientFundsException();
    InsufficientBalanceForRegularSavingsException ex =
        new InsufficientBalanceForRegularSavingsException("MSFT", cause);
    assertEquals("MSFT", ex.getSymbol());
    assertNotNull(ex.getCause());
  }
}
