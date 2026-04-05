package model.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import model.analysis.technical.chartanalysis.FibonacciRetracement;
import org.junit.jupiter.api.Test;

class FibonacciRetracementTest {

  @Test
  void compute_returnsSevenNamedLevelsBetweenHighAndLow() {
    List<FibonacciRetracement.Level> levels =
        FibonacciRetracement.compute(new BigDecimal("200"), new BigDecimal("100"));

    assertEquals(7, levels.size());
    assertEquals("0%", levels.get(0).name());
    assertEquals(new BigDecimal("200.00"), levels.get(0).price());
    assertEquals("23.6%", levels.get(1).name());
    assertEquals(new BigDecimal("176.40"), levels.get(1).price());
    assertEquals("50%", levels.get(3).name());
    assertEquals(new BigDecimal("150.00"), levels.get(3).price());
    assertEquals("100%", levels.get(6).name());
    assertEquals(new BigDecimal("100.00"), levels.get(6).price());
  }
}
