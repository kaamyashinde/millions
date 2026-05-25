package model.persistence.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.persistence.market.MarketData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for CSV parsing via {@link InputStream}. Path-based reads are limited to
 * delegation smoke tests; file I/O integration lives in {@link CsvMarketDataReaderWriterTest}.
 */
class CsvReaderTest {

  @TempDir
  Path tempDir;

  @Test
  void readMarketDataFromFile_pathDelegation_parsesSameAsStream() throws IOException {
    String csvContent = "STOCK,ZZZ,Zeta Corp,42.00\n";
    Path csvFile = tempDir.resolve("full-path.csv");
    Files.writeString(csvFile, csvContent);

    MarketData fromPath = CsvReader.readMarketDataFromFile(csvFile);
    MarketData fromStream = CsvReader.readMarketData(toStream(csvContent));

    assertEquals(fromStream.stocks().size(), fromPath.stocks().size());
    assertEquals("ZZZ", fromPath.stocks().getFirst().getSymbol());
  }

  @Test
  void readMarketData_fromInputStream_parsesStocksAndFunds() {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        FUND,TECH,Tech Fund,NVDA:1.00
        STOCK,AAPL,Apple Inc,152.54
        STOCK,MSFT,Microsoft,404.43
        FUND,BLEND,Blend Fund,AAPL:0.50,MSFT:0.50
        """;

    MarketData marketData = CsvReader.readMarketData(toStream(csvContent));

    assertEquals(3, marketData.stocks().size());
    assertEquals(2, marketData.funds().size());
    Stock firstStock = marketData.stocks().get(0);
    Fund firstFund = marketData.funds().get(0);
    assertEquals("NVDA", firstStock.getSymbol());
    assertEquals("Nvidia", firstStock.getCompany());
    assertEquals(new BigDecimal("191.27"), firstStock.getSalesPrice());
    assertEquals("TECH", firstFund.getSymbol());
    assertEquals(1, firstFund.getComponents().size());
  }

  @Test
  void readMarketData_fromInputStream_ignoresCommentsAndEmptyLines() {
    String csvContent = """
        # Comment
        STOCK,NVDA,Nvidia,191.27

        # Another comment
        STOCK,AAPL,Apple Inc,152.54
        FUND,PAIR,Pair Fund,NVDA:0.40,AAPL:0.60
        """;

    MarketData marketData = CsvReader.readMarketData(toStream(csvContent));

    assertEquals(2, marketData.stocks().size());
    assertEquals(1, marketData.funds().size());
  }

  @Test
  void readMarketData_fromInputStream_readsEmptyContent() {
    MarketData marketData = CsvReader.readMarketData(toStream(""));

    assertEquals(0, marketData.stocks().size());
    assertEquals(0, marketData.funds().size());
  }

  @Test
  void readMarketData_fromInputStream_parsesMixedRows() {
    String csvContent = """
        # header
        STOCK,NVDA,Nvidia,191.27
        STOCK,AAPL,Apple Inc,152.54
        FUND,PAIR,Pair Fund,NVDA:0.25,AAPL:0.75
        """;

    MarketData marketData = CsvReader.readMarketData(toStream(csvContent));

    assertEquals(2, marketData.stocks().size());
    assertEquals(1, marketData.funds().size());
    assertEquals("PAIR", marketData.funds().getFirst().getSymbol());
  }

  @Test
  void readMarketData_fromInputStream_throwsForUnknownFundComponentSymbol() {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        FUND,BROKEN,Broken Fund,MSFT:1.00
        """;

    assertThrows(
        IllegalArgumentException.class,
        () -> CsvReader.readMarketData(toStream(csvContent)));
  }

  @Test
  void readMarketData_fromInputStream_throwsForInvalidRecordType() {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        INVALID,NVDA,Nvidia,191.27
        """;

    assertThrows(
        IllegalArgumentException.class,
        () -> CsvReader.readMarketData(toStream(csvContent)));
  }

  @Test
  void readMarketData_fromInputStream_throwsForMalformedStockRow() {
    String csvContent = "STOCK,AAPL,Apple\n";

    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> CsvReader.readMarketData(toStream(csvContent)));

    assertTrue(thrown.getMessage().contains("Invalid stock row"));
  }

  @Test
  void readMarketData_fromInputStream_throwsForBadPrice() {
    String csvContent = "STOCK,AAPL,Apple Inc,not-a-number\n";

    assertThrows(
        NumberFormatException.class,
        () -> CsvReader.readMarketData(toStream(csvContent)));
  }

  @Test
  void readMarketData_fromInputStream_throwsForDuplicateAssetSymbol() {
    String csvContent = """
        STOCK,X,X Corp,1.00
        FUND,X,Duplicate Fund,X:1.00
        """;

    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> CsvReader.readMarketData(toStream(csvContent)));

    assertTrue(thrown.getMessage().contains("Duplicate asset symbol"));
  }

  @Test
  void readMarketData_fromInputStream_throwsForDuplicateStockSymbol() {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        STOCK,NVDA,Duplicate,100.00
        """;

    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> CsvReader.readMarketData(toStream(csvContent)));

    assertTrue(thrown.getMessage().contains("Duplicate stock symbol"));
  }

  @Test
  void readCsv_fromInputStream_returnsOnlyStocksFromMixedMarketData() {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        FUND,PAIR,Pair Fund,NVDA:1.00
        """;

    List<Stock> stocks = CsvReader.readCsv(toStream(csvContent));

    assertEquals(1, stocks.size());
    assertEquals("NVDA", stocks.getFirst().getSymbol());
  }

  private static InputStream toStream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }
}
