package model.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvWriterTest {

  @TempDir
  Path tempDir;

  private Stock testStock1;
  private Stock testStock2;
  private Stock testStock3;

  @BeforeEach
  void setUp() {
    testStock1 = new Stock("AAPL", "Apple Inc.");
    testStock1.addNewSalesPrice(new BigDecimal("150.25"));

    testStock2 = new Stock("GOOGL", "Alphabet Inc.");
    testStock2.addNewSalesPrice(new BigDecimal("140.50"));

    testStock3 = new Stock("MSFT", "Microsoft Corp.");
    testStock3.addNewSalesPrice(new BigDecimal("380.75"));
  }

  @Test
  void writeCsvToFile_WithMultipleStocks_CreatesFileWithCorrectFormat() throws IOException {
    List<Stock> stocks = List.of(testStock1, testStock2, testStock3);

    CsvWriter.writeCsvToFile(tempDir, "test_multiple", stocks);

    Path csvFile = tempDir.resolve("test_multiple.csv");
    assertTrue(Files.exists(csvFile), "CSV file should be created");

    List<String> lines = Files.readAllLines(csvFile);
    assertEquals(3, lines.size(), "File should contain 3 lines");
    assertEquals("AAPL,Apple Inc.,150.25", lines.get(0));
    assertEquals("GOOGL,Alphabet Inc.,140.50", lines.get(1));
    assertEquals("MSFT,Microsoft Corp.,380.75", lines.get(2));
  }

  @Test
  void writeCsvToFile_WithEmptyList_CreatesEmptyFile() throws IOException {
    List<Stock> stocks = List.of();

    CsvWriter.writeCsvToFile(tempDir, "test_empty", stocks);

    Path csvFile = tempDir.resolve("test_empty.csv");
    assertTrue(Files.exists(csvFile), "CSV file should be created even for empty list");

    List<String> lines = Files.readAllLines(csvFile);
    assertEquals(0, lines.size(), "File should be empty");
  }

  @Test
  void writeCsvToFile_WithSingleStock_CreatesFileWithOneEntry() throws IOException {
    List<Stock> stocks = List.of(testStock1);

    CsvWriter.writeCsvToFile(tempDir, "test_single", stocks);

    Path csvFile = tempDir.resolve("test_single.csv");
    assertTrue(Files.exists(csvFile), "CSV file should be created");

    List<String> lines = Files.readAllLines(csvFile);
    assertEquals(1, lines.size(), "File should contain 1 line");
    assertEquals("AAPL,Apple Inc.,150.25", lines.get(0));
  }

  @Test
  void writeCsvToFile_CorrectlyFormatsStockData() throws IOException {
    Stock stock = new Stock("TSLA", "Tesla Inc.");
    stock.addNewSalesPrice(new BigDecimal("250.99"));
    List<Stock> stocks = List.of(stock);

    CsvWriter.writeCsvToFile(tempDir, "test_format", stocks);

    Path csvFile = tempDir.resolve("test_format.csv");
    String content = Files.readString(csvFile).trim();
    assertEquals("TSLA,Tesla Inc.,250.99", content);
  }

  @Test
  void writeCsvToFile_AppendsExtension_ToCsvFileName() throws IOException {
    List<Stock> stocks = List.of(testStock1);

    CsvWriter.writeCsvToFile(tempDir, "test_extension", stocks);

    Path csvFile = tempDir.resolve("test_extension.csv");
    assertTrue(Files.exists(csvFile), "File should have .csv extension");
    assertFalse(Files.exists(tempDir.resolve("test_extension")), "File without extension should not exist");
  }

  @Test
  void writeAndRead_RoundTrip_PreservesStockData() throws IOException {
    List<Stock> stocks = List.of(testStock1, testStock2, testStock3);

    CsvWriter.writeCsvToFile(tempDir, "roundtrip", stocks);

    List<Stock> readStocks = CsvReader.readCsv(tempDir, "roundtrip");

    assertEquals(3, readStocks.size(), "Should read 3 stocks");
    assertEquals("AAPL", readStocks.get(0).getSymbol());
    assertEquals("Apple Inc.", readStocks.get(0).getCompany());
    assertEquals(new BigDecimal("150.25"), readStocks.get(0).getSalesPrice());
  }
}
