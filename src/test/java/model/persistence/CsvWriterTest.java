package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.core.market.Stock;
import model.core.market.fund.Fund;
import model.core.market.fund.FundComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvWriterTest {

  @TempDir
  Path tempDir;

  private Stock testStock1;
  private Stock testStock2;
  private Stock testStock3;
  private Fund blendFund;

  @BeforeEach
  void setUp() {
    testStock1 = new Stock("AAPL", "Apple Inc.");
    testStock1.addNewSalesPrice(new BigDecimal("150.25"));

    testStock2 = new Stock("GOOGL", "Alphabet Inc.");
    testStock2.addNewSalesPrice(new BigDecimal("140.50"));

    testStock3 = new Stock("MSFT", "Microsoft Corp.");
    testStock3.addNewSalesPrice(new BigDecimal("380.75"));

    blendFund = new Fund(
        "BLEND",
        "Blend Fund",
        List.of(
            new FundComponent(testStock3, new BigDecimal("0.20")),
            new FundComponent(testStock1, new BigDecimal("0.50")),
            new FundComponent(testStock2, new BigDecimal("0.30"))));
  }

  @Test
  void writeMarketDataToFile_writesStocksAndFundsInMixedFormat() throws IOException {
    MarketData marketData =
        new MarketData(List.of(testStock1, testStock2, testStock3), List.of(blendFund));

    CsvWriter.writeMarketDataToFile(tempDir, "test_multiple", marketData);

    Path csvFile = tempDir.resolve("test_multiple.csv");
    assertTrue(Files.exists(csvFile), "CSV file should be created");

    List<String> lines = Files.readAllLines(csvFile);
    assertEquals(4, lines.size(), "File should contain stock and fund rows");
    assertEquals("STOCK,AAPL,Apple Inc.,150.25", lines.get(0));
    assertEquals("STOCK,GOOGL,Alphabet Inc.,140.50", lines.get(1));
    assertEquals("STOCK,MSFT,Microsoft Corp.,380.75", lines.get(2));
    assertEquals("FUND,BLEND,Blend Fund,AAPL:0.50,GOOGL:0.30,MSFT:0.20", lines.get(3));
  }

  @Test
  void writeMarketDataToFile_withEmptyMarketData_createsEmptyFile() throws IOException {
    MarketData marketData = MarketData.empty();

    CsvWriter.writeMarketDataToFile(tempDir, "test_empty", marketData);

    Path csvFile = tempDir.resolve("test_empty.csv");
    assertTrue(Files.exists(csvFile), "CSV file should be created even for empty market data");

    List<String> lines = Files.readAllLines(csvFile);
    assertEquals(0, lines.size(), "File should be empty");
  }

  @Test
  void writeCsvToFile_withSingleStock_stillUsesMixedSchema() throws IOException {
    List<Stock> stocks = List.of(testStock1);

    CsvWriter.writeCsvToFile(tempDir, "test_single", stocks);

    Path csvFile = tempDir.resolve("test_single.csv");
    assertTrue(Files.exists(csvFile), "CSV file should be created");

    List<String> lines = Files.readAllLines(csvFile);
    assertEquals(1, lines.size(), "File should contain 1 line");
    assertEquals("STOCK,AAPL,Apple Inc.,150.25", lines.get(0));
  }

  @Test
  void writeCsvToFile_appendsExtensionToCsvFileName() throws IOException {
    List<Stock> stocks = List.of(testStock1);

    CsvWriter.writeCsvToFile(tempDir, "test_extension", stocks);

    Path csvFile = tempDir.resolve("test_extension.csv");
    assertTrue(Files.exists(csvFile), "File should have .csv extension");
    assertFalse(Files.exists(tempDir.resolve("test_extension")),
        "File without extension should not exist");
  }

  @Test
  void writeAndRead_roundTrip_preservesStocksAndFunds() throws IOException {
    MarketData original =
        new MarketData(List.of(testStock1, testStock2, testStock3), List.of(blendFund));

    CsvWriter.writeMarketDataToFile(tempDir, "roundtrip", original);

    MarketData loaded = CsvReader.readMarketData(tempDir, "roundtrip");

    assertEquals(3, loaded.stocks().size(), "Should read 3 stocks");
    assertEquals(1, loaded.funds().size(), "Should read 1 fund");
    assertEquals("AAPL", loaded.stocks().get(0).getSymbol());
    assertEquals("Apple Inc.", loaded.stocks().get(0).getCompany());
    assertEquals(new BigDecimal("150.25"), loaded.stocks().get(0).getSalesPrice());
    assertEquals("BLEND", loaded.funds().getFirst().getSymbol());
    assertEquals(new BigDecimal("0.50"),
        loaded.funds().getFirst().getComponents().getFirst().weight());
  }
}
