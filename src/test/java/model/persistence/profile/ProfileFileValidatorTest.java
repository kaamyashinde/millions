package model.persistence.profile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;
import model.persistence.ProfileFile;
import org.junit.jupiter.api.Test;

class ProfileFileValidatorTest {

  @Test
  void validate_acceptsCompleteProfile() {
    assertDoesNotThrow(() -> ProfileFileValidator.validate(validProfile()));
  }

  @Test
  void validate_rejectsMissingTextFields() {
    assertValidationMessage(
        validProfileWithUsername(" "),
        "Missing or blank username.");
    assertValidationMessage(
        new ProfileFile(
            "Alice",
            "",
            "hash",
            null,
            false,
            "Alice",
            BigDecimal.TEN,
            BigDecimal.TEN,
            List.of(),
            List.of(),
            List.of(),
            "NYSE",
            1,
            List.of(),
            List.of(),
            null),
        "Missing or blank normalizedUsername.");
    assertValidationMessage(
        profileWithPinHash(" "),
        "Missing or blank pinHash.");
    assertValidationMessage(
        profileWithPlayerName(" "),
        "Missing or blank playerName.");
    assertValidationMessage(
        profileWithExchangeName(" "),
        "Missing or blank exchangeName.");
  }

  @Test
  void validate_rejectsMissingMoneyFieldsAndInvalidDay() {
    assertValidationMessage(
        profileWithStartingMoney(null),
        "Missing startingMoney.");
    assertValidationMessage(
        profileWithCash(null),
        "Missing cash.");
    assertValidationMessage(
        profileWithDay(0),
        "day must be at least 1.");
  }

  @Test
  void validate_rejectsMismatchedNormalizedUsername() {
    ProfileFile profile = new ProfileFile(
        "Alice",
        "bob",
        "hash",
        null,
        false,
        "Alice",
        BigDecimal.TEN,
        BigDecimal.TEN,
        List.of(),
        List.of(),
        List.of(),
        "NYSE",
        1,
        List.of(),
        List.of(),
        null);

    assertValidationMessage(
        profile,
        "normalizedUsername does not match username: Alice");
  }

  @Test
  void validate_rejectsNullEntriesInLists() {
    assertValidationMessage(
        profileWithHoldings(listWithNull()),
        "Null entry in holdings list.");
    assertValidationMessage(
        profileWithTransactions(listWithNull()),
        "Null entry in transactions list.");
    assertValidationMessage(
        profileWithSavings(listWithNull()),
        "Null entry in savings list.");
    assertValidationMessage(
        profileWithStockPrices(listWithNull()),
        "Null entry in stockPrices list.");
    assertValidationMessage(
        profileWithEvents(listWithNull()),
        "Null entry in events list.");
  }

  @Test
  void validate_acceptsNonNullListEntries() {
    ProfileFile profile = profileWithHoldings(List.of(
        new ProfileFile.HoldingRow("AAPL", BigDecimal.ONE, BigDecimal.TEN)));

    assertDoesNotThrow(() -> ProfileFileValidator.validate(profile));
  }

  @Test
  void privateConstructor_isCoveredForUtilityClass() throws Exception {
    Constructor<ProfileFileValidator> constructor =
        ProfileFileValidator.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    constructor.newInstance();
  }

  private static void assertValidationMessage(ProfileFile profile, String expected) {
    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> ProfileFileValidator.validate(profile));
    assertEquals(expected, thrown.getMessage());
  }

  private static ProfileFile validProfile() {
    return new ProfileFile(
        "Alice",
        "alice",
        "hash",
        null,
        false,
        "Alice",
        BigDecimal.TEN,
        BigDecimal.TEN,
        List.of(),
        List.of(),
        List.of(),
        "NYSE",
        1,
        List.of(),
        List.of(),
        null);
  }

  private static ProfileFile validProfileWithUsername(String username) {
    return new ProfileFile(
        username,
        "alice",
        "hash",
        null,
        false,
        "Alice",
        BigDecimal.TEN,
        BigDecimal.TEN,
        List.of(),
        List.of(),
        List.of(),
        "NYSE",
        1,
        List.of(),
        List.of(),
        null);
  }

  private static ProfileFile profileWithPinHash(String pinHash) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(),
        profile.normalizedUsername(),
        pinHash,
        profile.displayName(),
        profile.hasSeenWelcome(),
        profile.playerName(),
        profile.startingMoney(),
        profile.cash(),
        profile.holdings(),
        profile.transactions(),
        profile.savings(),
        profile.exchangeName(),
        profile.day(),
        profile.stockPrices(),
        profile.events(),
        profile.lastEvent());
  }

  private static ProfileFile profileWithPlayerName(String playerName) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(),
        profile.normalizedUsername(),
        profile.pinHash(),
        profile.displayName(),
        profile.hasSeenWelcome(),
        playerName,
        profile.startingMoney(),
        profile.cash(),
        profile.holdings(),
        profile.transactions(),
        profile.savings(),
        profile.exchangeName(),
        profile.day(),
        profile.stockPrices(),
        profile.events(),
        profile.lastEvent());
  }

  private static ProfileFile profileWithExchangeName(String exchangeName) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(),
        profile.normalizedUsername(),
        profile.pinHash(),
        profile.displayName(),
        profile.hasSeenWelcome(),
        profile.playerName(),
        profile.startingMoney(),
        profile.cash(),
        profile.holdings(),
        profile.transactions(),
        profile.savings(),
        exchangeName,
        profile.day(),
        profile.stockPrices(),
        profile.events(),
        profile.lastEvent());
  }

  private static ProfileFile profileWithStartingMoney(BigDecimal startingMoney) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(),
        profile.normalizedUsername(),
        profile.pinHash(),
        profile.displayName(),
        profile.hasSeenWelcome(),
        profile.playerName(),
        startingMoney,
        profile.cash(),
        profile.holdings(),
        profile.transactions(),
        profile.savings(),
        profile.exchangeName(),
        profile.day(),
        profile.stockPrices(),
        profile.events(),
        profile.lastEvent());
  }

  private static ProfileFile profileWithCash(BigDecimal cash) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(),
        profile.normalizedUsername(),
        profile.pinHash(),
        profile.displayName(),
        profile.hasSeenWelcome(),
        profile.playerName(),
        profile.startingMoney(),
        cash,
        profile.holdings(),
        profile.transactions(),
        profile.savings(),
        profile.exchangeName(),
        profile.day(),
        profile.stockPrices(),
        profile.events(),
        profile.lastEvent());
  }

  private static ProfileFile profileWithDay(int day) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(),
        profile.normalizedUsername(),
        profile.pinHash(),
        profile.displayName(),
        profile.hasSeenWelcome(),
        profile.playerName(),
        profile.startingMoney(),
        profile.cash(),
        profile.holdings(),
        profile.transactions(),
        profile.savings(),
        profile.exchangeName(),
        day,
        profile.stockPrices(),
        profile.events(),
        profile.lastEvent());
  }

  private static ProfileFile profileWithHoldings(List<ProfileFile.HoldingRow> holdings) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(), profile.normalizedUsername(), profile.pinHash(), profile.displayName(),
        profile.hasSeenWelcome(), profile.playerName(), profile.startingMoney(), profile.cash(),
        holdings, profile.transactions(), profile.savings(), profile.exchangeName(), profile.day(),
        profile.stockPrices(), profile.events(), profile.lastEvent());
  }

  private static ProfileFile profileWithTransactions(List<ProfileFile.TxRow> transactions) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(), profile.normalizedUsername(), profile.pinHash(), profile.displayName(),
        profile.hasSeenWelcome(), profile.playerName(), profile.startingMoney(), profile.cash(),
        profile.holdings(), transactions, profile.savings(), profile.exchangeName(), profile.day(),
        profile.stockPrices(), profile.events(), profile.lastEvent());
  }

  private static ProfileFile profileWithSavings(List<ProfileFile.SavingsRow> savings) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(), profile.normalizedUsername(), profile.pinHash(), profile.displayName(),
        profile.hasSeenWelcome(), profile.playerName(), profile.startingMoney(), profile.cash(),
        profile.holdings(), profile.transactions(), savings, profile.exchangeName(), profile.day(),
        profile.stockPrices(), profile.events(), profile.lastEvent());
  }

  private static ProfileFile profileWithStockPrices(List<ProfileFile.PriceRow> stockPrices) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(), profile.normalizedUsername(), profile.pinHash(), profile.displayName(),
        profile.hasSeenWelcome(), profile.playerName(), profile.startingMoney(), profile.cash(),
        profile.holdings(), profile.transactions(), profile.savings(), profile.exchangeName(),
        profile.day(), stockPrices, profile.events(), profile.lastEvent());
  }

  private static ProfileFile profileWithEvents(List<ProfileFile.EventRow> events) {
    ProfileFile profile = validProfile();
    return new ProfileFile(
        profile.username(), profile.normalizedUsername(), profile.pinHash(), profile.displayName(),
        profile.hasSeenWelcome(), profile.playerName(), profile.startingMoney(), profile.cash(),
        profile.holdings(), profile.transactions(), profile.savings(), profile.exchangeName(),
        profile.day(), profile.stockPrices(), events, profile.lastEvent());
  }

  @SuppressWarnings("unchecked")
  private static <T> List<T> listWithNull() {
    return (List<T>) java.util.Arrays.asList((Object) null);
  }
}
