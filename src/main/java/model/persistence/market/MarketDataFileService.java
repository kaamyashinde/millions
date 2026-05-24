package model.persistence.market;


import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import model.exception.market.MarketDataImportException;
import model.persistence.io.CsvReader;
import model.persistence.profile.ProfilePaths;

/**
 * Copies, validates, and loads per-profile market-data CSV files.
 */
public final class MarketDataFileService {

  public static final long MAX_FILE_BYTES = 1024 * 1024;

  private final ProfilePaths profilePaths;
  private final Class<?> defaultResourceAnchor;
  private final String defaultResourcePath;

  /**
   * @param profilePaths          profile path resolver
   * @param defaultResourceAnchor class used to load the bundled default CSV
   * @param defaultResourcePath   classpath resource path for the default CSV
   */
  public MarketDataFileService(
      ProfilePaths profilePaths,
      Class<?> defaultResourceAnchor,
      String defaultResourcePath) {
    this.profilePaths = profilePaths;
    this.defaultResourceAnchor = defaultResourceAnchor;
    this.defaultResourcePath = defaultResourcePath;
  }

  /**
   * Copies the bundled default market-data file into the profile directory and returns parsed data.
   *
   * @param normalizedUsername canonical profile key
   * @return parsed market data from the installed file
   */
  public MarketData installDefault(String normalizedUsername) {
    Path destination = profilePaths.marketDataFile(normalizedUsername);
    try {
      Files.createDirectories(destination.getParent());
      try (InputStream input = defaultResourceAnchor.getResourceAsStream(defaultResourcePath)) {
        if (input == null) {
          throw new IllegalStateException(
              "Missing default market data resource: " + defaultResourcePath);
        }
        Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException exception) {
      throw new MarketDataImportException("Could not save market data file.");
    }
    return parseProfileFile(destination);
  }

  /**
   * Validates a user-selected CSV, copies it into the profile directory, and returns parsed data.
   *
   * @param source             path to the uploaded CSV
   * @param normalizedUsername canonical profile key
   * @return parsed market data from the copied file
   */
  public MarketData importFromFile(Path source, String normalizedUsername) {
    validateUploadSource(source);
    Path destination = profilePaths.marketDataFile(normalizedUsername);
    try {
      Files.createDirectories(destination.getParent());
      Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      throw new MarketDataImportException("Could not save market data file.");
    }
    return parseProfileFile(destination);
  }

  /**
   * Loads market data for a profile, installing the default CSV when the profile file is missing.
   *
   * @param normalizedUsername canonical profile key
   * @return parsed market data for the profile
   */
  public MarketData loadForProfile(String normalizedUsername) {
    Path destination = profilePaths.marketDataFile(normalizedUsername);
    if (!Files.isRegularFile(destination)) {
      return installDefault(normalizedUsername);
    }
    return parseProfileFile(destination);
  }

  /**
   * @param normalizedUsername canonical profile key
   * @return path to the profile's market-data CSV
   */
  public Path marketDataPath(String normalizedUsername) {
    return profilePaths.marketDataFile(normalizedUsername);
  }

  private void validateUploadSource(Path source) {
    if (source == null) {
      throw new MarketDataImportException("Market data file not found.");
    }
    String fileName = source.getFileName() == null ? "" : source.getFileName().toString();
    if (!fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
      throw new MarketDataImportException("Market data file must be a .csv file.");
    }
    if (!Files.isRegularFile(source)) {
      throw new MarketDataImportException("Market data file not found.");
    }
    try {
      long size = Files.size(source);
      if (size > MAX_FILE_BYTES) {
        throw new MarketDataImportException("Market data file is too large (max 1 MB).");
      }
    } catch (IOException exception) {
      throw new MarketDataImportException("Market data file not found.");
    }
  }

  private MarketData parseProfileFile(Path destination) {
    try {
      MarketData marketData = CsvReader.readMarketDataFromFile(destination);
      if (marketData.stocks().isEmpty()) {
        throw new MarketDataImportException("Market data file must contain at least one stock.");
      }
      return marketData;
    } catch (MarketDataImportException exception) {
      throw exception;
    } catch (IllegalArgumentException | UncheckedIOException exception) {
      throw new MarketDataImportException(
          "Could not read market data. Check that rows use STOCK or FUND format.");
    }
  }
}
