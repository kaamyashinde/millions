package model.persistence.io;

import java.nio.file.Path;
import model.exception.market.MarketDataImportException;
import model.persistence.market.MarketData;
import util.IoLogger;

/**
 * Reads mixed market-data CSV files from an arbitrary path.
 */
public final class CsvMarketDataReader implements TextDocumentReader<MarketData, MarketDataImportException> {

  @Override
  public MarketData read(Path path) throws MarketDataImportException {
    try {
      return CsvReader.readMarketDataFromFile(path);
    } catch (MarketDataImportException exception) {
      throw exception;
    } catch (IllegalArgumentException exception) {
      IoLogger.logFailure("Parse market-data CSV", path, exception);
      throw new MarketDataImportException(
          "Could not read market data. Check that rows use STOCK or FUND format.",
          exception);
    } catch (Exception exception) {
      IoLogger.logFailure("Read market-data CSV", path, exception);
      throw new MarketDataImportException("Could not read market data file: " + path, exception);
    }
  }
}
