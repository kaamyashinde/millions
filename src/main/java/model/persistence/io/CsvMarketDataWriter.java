package model.persistence.io;

import java.nio.file.Path;
import model.exception.persistence.PersistenceException;
import model.persistence.market.MarketData;
import util.IoLogger;

/**
 * Writes mixed market-data CSV files to an arbitrary path.
 */
public final class CsvMarketDataWriter implements TextDocumentWriter<MarketData, PersistenceException> {

  /**
   * Creates a market-data CSV writer.
   */
  public CsvMarketDataWriter() {
  }

  @Override
  public void write(Path path, MarketData value) throws PersistenceException {
    try {
      CsvWriter.writeMarketDataToPath(path, value);
    } catch (PersistenceException exception) {
      throw exception;
    } catch (Exception exception) {
      IoLogger.logFailure("Write market-data CSV", path, exception);
      throw new PersistenceException("Could not write market data CSV: " + path, exception);
    }
  }
}
