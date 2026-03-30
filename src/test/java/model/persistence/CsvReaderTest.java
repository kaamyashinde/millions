package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CsvReaderTest {

  @TempDir
  Path tempDir;

  private Stock testStock1;
  private Stock testStock2;
  private Stock testStock3;

  @BeforeEach
  void setUp() {
    testStock1 = new Stock("NVDA", "Nvidia");
    testStock1.addNewSalesPrice(new BigDecimal("191.27"));

    testStock2 = new Stock("AAPL", "Apple Inc");
    testStock2.addNewSalesPrice(new BigDecimal("152.54"));

    testStock3 = new Stock("MSFT", "Microsoft");
    testStock3.addNewSalesPrice(new BigDecimal("404.43"));
  }

  @Test
  void testReadValidCsv() throws IOException {
    // Create a test CSV file
    String csvContent = "NVDA,Nvidia,191.27\nAAPL,Apple Inc,152.54\nMSFT,Microsoft,404.43\n";
    Path csvFile = tempDir.resolve("test.csv");
    Files.writeString(csvFile, csvContent);

    List<Stock> stocks = CsvReader.readCsv(tempDir, "test");

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

  @Test
  void testReadCsvWithComments() throws IOException {
    String csvContent = "# This is a comment\nNVDA,Nvidia,191.27\n# Another comment\nAAPL,Apple Inc,152.54\n";
    Path csvFile = tempDir.resolve("with_comments.csv");
    Files.writeString(csvFile, csvContent);

    List<Stock> stocks = CsvReader.readCsv(tempDir, "with_comments");

    assertEquals(2, stocks.size());
    assertEquals("NVDA", stocks.get(0).getSymbol());
    assertEquals("AAPL", stocks.get(1).getSymbol());
  }

  @Test
  void testReadCsvWithEmptyLines() throws IOException {
    String csvContent = "NVDA,Nvidia,191.27\n\nAAPL,Apple Inc,152.54\n\n";
    Path csvFile = tempDir.resolve("with_empty_lines.csv");
    Files.writeString(csvFile, csvContent);

    List<Stock> stocks = CsvReader.readCsv(tempDir, "with_empty_lines");

    assertEquals(2, stocks.size());
    assertEquals("NVDA", stocks.get(0).getSymbol());
    assertEquals("AAPL", stocks.get(1).getSymbol());
  }

  @Test
  void testReadCsvWithInvalidLines() throws IOException {
    String csvContent = "NVDA,Nvidia,191.27\nINVALID_LINE\nAAPL,Apple Inc,152.54\n";
    Path csvFile = tempDir.resolve("with_invalid.csv");
    Files.writeString(csvFile, csvContent);

    List<Stock> stocks = CsvReader.readCsv(tempDir, "with_invalid");

    assertEquals(2, stocks.size());
    assertEquals("NVDA", stocks.get(0).getSymbol());
    assertEquals("AAPL", stocks.get(1).getSymbol());
  }

  @Test
  void testReadEmptyCsv() throws IOException {
    Path csvFile = tempDir.resolve("empty.csv");
    Files.writeString(csvFile, "");

    List<Stock> stocks = CsvReader.readCsv(tempDir, "empty");

    assertEquals(0, stocks.size());
  }

  @Test
  void testReadCsvFromInputStream() {
    String csvContent = "# header\nNVDA,Nvidia,191.27\nAAPL,Apple Inc,152.54\n";
    List<Stock> stocks =
        CsvReader.readCsv(
            new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

    assertEquals(2, stocks.size());
    assertEquals("NVDA", stocks.get(0).getSymbol());
    assertEquals("AAPL", stocks.get(1).getSymbol());
  }
}
