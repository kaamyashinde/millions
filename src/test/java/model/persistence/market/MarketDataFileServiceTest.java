package model.persistence.market;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import model.exception.market.MarketDataImportException;
import model.persistence.profile.ProfilePaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarketDataFileServiceTest {

  private static final String DEFAULT_RESOURCE = "/data/demo-stocks.csv";

  @TempDir
  Path tempDir;

  private MarketDataFileService service;
  private ProfilePaths profilePaths;

  @BeforeEach
  void setUp() {
    profilePaths = new ProfilePaths(tempDir);
    service = new MarketDataFileService(profilePaths, MarketDataFileServiceTest.class, DEFAULT_RESOURCE);
  }

  @Test
  void installDefault_copiesBundledCsvIntoProfileDirectory() {
    String username = "alice";

    MarketData marketData = service.installDefault(username);

    Path profileCsv = profilePaths.marketDataFile(username);
    assertTrue(Files.isRegularFile(profileCsv));
    assertTrue(marketData.stocks().size() >= 3);
    assertFalse(marketData.funds().isEmpty());
  }

  @Test
  void importFromFile_copiesAndParsesCustomCsv() throws IOException {
    String username = "bob";
    Path source = tempDir.resolve("custom.csv");
    Files.writeString(source, """
        STOCK,TEST,Test Co,10.00
        """);

    MarketData marketData = service.importFromFile(source, username);

    assertEquals(1, marketData.stocks().size());
    assertEquals("TEST", marketData.stocks().getFirst().getSymbol());
    assertTrue(Files.isRegularFile(profilePaths.marketDataFile(username)));
  }

  @Test
  void importFromFile_rejectsNonCsvExtension() throws IOException {
    Path source = tempDir.resolve("custom.txt");
    Files.writeString(source, "STOCK,TEST,Test Co,10.00");

    MarketDataImportException thrown = assertThrows(
        MarketDataImportException.class,
        () -> service.importFromFile(source, "bob"));

    assertEquals("Market data file must be a .csv file.", thrown.getMessage());
  }

  @Test
  void importFromFile_rejectsInvalidContent() throws IOException {
    Path source = tempDir.resolve("bad.csv");
    Files.writeString(source, "not,a,valid,market,row");

    MarketDataImportException thrown = assertThrows(
        MarketDataImportException.class,
        () -> service.importFromFile(source, "bob"));

    assertEquals(
        "Could not read market data. Check that rows use STOCK or FUND format.",
        thrown.getMessage());
  }

  @Test
  void loadForProfile_selfHealsMissingFile() throws IOException {
    String username = "legacy";
    Files.createDirectories(profilePaths.profileDirectory(username));

    MarketData marketData = service.loadForProfile(username);

    assertTrue(Files.isRegularFile(profilePaths.marketDataFile(username)));
    assertFalse(marketData.stocks().isEmpty());
  }

  @Test
  void loadForProfile_readsExistingProfileFile() throws IOException {
    String username = "carol";
    service.installDefault(username);

    MarketData marketData = service.loadForProfile(username);

    assertFalse(marketData.stocks().isEmpty());
  }
}
