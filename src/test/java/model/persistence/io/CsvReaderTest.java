package model.persistence.io;


import model.persistence.market.MarketData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvReaderTest {

  @TempDir
  Path tempDir;

  @Test
  void readMarketData_parsesStocksAndFundsFromPath() throws IOException {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        FUND,TECH,Tech Fund,NVDA:1.00
        STOCK,AAPL,Apple Inc,152.54
        STOCK,MSFT,Microsoft,404.43
        FUND,BLEND,Blend Fund,AAPL:0.50,MSFT:0.50
        """;
    Path csvFile = tempDir.resolve("test.csv");
    Files.writeString(csvFile, csvContent);

    MarketData marketData = CsvReader.readMarketData(tempDir, "test");

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
  void readMarketData_ignoresCommentsAndEmptyLines() throws IOException {
    String csvContent = """
        # Comment
        STOCK,NVDA,Nvidia,191.27

        # Another comment
        STOCK,AAPL,Apple Inc,152.54
        FUND,PAIR,Pair Fund,NVDA:0.40,AAPL:0.60
        """;
    Path csvFile = tempDir.resolve("with_comments.csv");
    Files.writeString(csvFile, csvContent);

    MarketData marketData = CsvReader.readMarketData(tempDir, "with_comments");

    assertEquals(2, marketData.stocks().size());
    assertEquals(1, marketData.funds().size());
  }

  @Test
  void readMarketData_throwsForUnknownFundComponentSymbol() throws IOException {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        FUND,BROKEN,Broken Fund,MSFT:1.00
        """;
    Path csvFile = tempDir.resolve("with_unknown_component.csv");
    Files.writeString(csvFile, csvContent);

    assertThrows(IllegalArgumentException.class, () -> CsvReader.readMarketData(tempDir, "with_unknown_component"));
  }

  @Test
  void readMarketData_throwsForInvalidRecordType() throws IOException {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        INVALID,NVDA,Nvidia,191.27
        """;
    Path csvFile = tempDir.resolve("with_invalid_type.csv");
    Files.writeString(csvFile, csvContent);

    assertThrows(IllegalArgumentException.class, () -> CsvReader.readMarketData(tempDir, "with_invalid_type"));
  }

  @Test
  void readMarketData_readsEmptyFile() throws IOException {
    Path csvFile = tempDir.resolve("empty.csv");
    Files.writeString(csvFile, "");

    MarketData marketData = CsvReader.readMarketData(tempDir, "empty");

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
    MarketData marketData = CsvReader.readMarketData(
        new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

    assertEquals(2, marketData.stocks().size());
    assertEquals(1, marketData.funds().size());
    assertEquals("PAIR", marketData.funds().getFirst().getSymbol());
  }

  @Test
  void readCsv_returnsOnlyStocksFromMixedMarketData() {
    String csvContent = """
        STOCK,NVDA,Nvidia,191.27
        FUND,PAIR,Pair Fund,NVDA:1.00
        """;

    List<Stock> stocks = CsvReader.readCsv(
        new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

    assertEquals(1, stocks.size());
    assertEquals("NVDA", stocks.getFirst().getSymbol());
  }
}
