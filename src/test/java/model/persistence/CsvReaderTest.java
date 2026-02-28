package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import model.Stock;
import org.junit.jupiter.api.Test;

public class CsvReaderTest {

  @Test
  void testReadValidCsv() {
    List<Stock> stocks = CsvReader.readCsv("src/test/resources/test-reading.csv");

    assertEquals(3, stocks.size());

    assertEquals("NVDA", stocks.get(0).getSymbol());
    assertEquals("Nvidia", stocks.get(0).getCompany());
    assertEquals(new BigDecimal("191.27"), stocks.get(0).getSalesPrice());

    assertEquals("AAPL", stocks.get(1).getSymbol());
    assertEquals("Apple Inc", stocks.get(1).getCompany());
    assertEquals(new BigDecimal("152.54"), stocks.get(1).getSalesPrice());

    assertEquals("MSFT", stocks.get(2).getSymbol());
    assertEquals("Microsoft", stocks.get(2).getCompany());
    assertEquals(new BigDecimal("404.43"), stocks.get(2).getSalesPrice());
  }
}
