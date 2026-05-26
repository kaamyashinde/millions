package model.persistence.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.core.asset.Stock;
import model.exception.market.MarketDataImportException;
import model.exception.persistence.PersistenceException;
import model.persistence.market.MarketData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for {@link CsvMarketDataReader} and {@link CsvMarketDataWriter} file I/O
 * and exception wrapping. CSV grammar and validation are covered in {@link CsvReaderTest}.
 */
class CsvMarketDataReaderWriterTest {

  @TempDir
  Path tempDir;

  @Test
  void reader_readsValidFileOnDisk() throws Exception {
    Path csv = tempDir.resolve("market.csv");
    Files.writeString(csv, "STOCK,ABC,Abc Corp,12.34\n");

    MarketData marketData = new CsvMarketDataReader().read(csv);

    assertEquals(1, marketData.stocks().size());
  }

  @Test
  void reader_wrapsInvalidRows() throws Exception {
    Path csv = tempDir.resolve("bad.csv");
    Files.writeString(csv, "NOPE,ABC,Abc Corp,12.34\n");

    MarketDataImportException thrown = assertThrows(
        MarketDataImportException.class,
        () -> new CsvMarketDataReader().read(csv));

    assertEquals(
        "Could not read market data. Check that rows use STOCK or FUND format.",
        thrown.getMessage());
  }

  @Test
  void reader_wrapsIoFailures() {
    Path missing = tempDir.resolve("missing.csv");

    MarketDataImportException thrown = assertThrows(
        MarketDataImportException.class,
        () -> new CsvMarketDataReader().read(missing));

    assertTrue(thrown.getMessage().contains("Could not read market data file"));
  }

  @Test
  void writer_writesMarketDataToExactPath() throws Exception {
    Path csv = tempDir.resolve("market.csv");
    Stock stock = new Stock("ABC", "Abc Corp");
    stock.addNewSalesPrice(new BigDecimal("12.34"));

    new CsvMarketDataWriter().write(csv, new MarketData(List.of(stock), List.of()));

    assertEquals(List.of("STOCK,ABC,Abc Corp,12.34"), Files.readAllLines(csv));
  }

  @Test
  void writer_wrapsFailures() throws Exception {
    Path directoryAsTarget = tempDir.resolve("market.csv");
    Files.createDirectories(directoryAsTarget);

    PersistenceException thrown = assertThrows(
        PersistenceException.class,
        () -> new CsvMarketDataWriter().write(directoryAsTarget, MarketData.empty()));

    assertTrue(thrown.getMessage().contains("Could not write market data CSV"));
  }
}
