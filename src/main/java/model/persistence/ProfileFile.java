package model.persistence;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.core.asset.InvestableAsset;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import model.core.market.event.MarketEvent;
import model.core.market.event.SymbolMarketEventTarget;
import model.core.player.Player;
import model.persistence.market.MarketData;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;
import model.trading.transaction.Purchase;
import model.trading.transaction.Sale;
import model.trading.transaction.Transaction;

/**
 * Single JSON document for one user profile: account, game state, and preferences.
 *
 * <p>The record is intentionally persistence-shaped: nested row records capture holdings,
 * transactions, savings plans, stock prices, and market events in a Jackson-friendly structure.
 * Use {@link #capture(Player, Exchange, String, String, String, String, boolean)} to serialize live
 * model state and {@link #restore(MarketData)} to rebuild an active session.
 *
 * @param username original username as entered by the player
 * @param normalizedUsername normalized username used for file names and PIN hashing
 * @param pinHash hashed PIN payload
 * @param displayName display name shown in the UI
 * @param hasSeenWelcome whether the welcome screen has already been acknowledged
 * @param playerName saved player name
 * @param startingMoney player's initial cash amount
 * @param cash player's current cash balance
 * @param holdings persisted share lots
 * @param transactions persisted transaction history
 * @param savings persisted regular savings plans
 * @param exchangeName saved exchange display name
 * @param day saved exchange trading day
 * @param stockPrices persisted stock price histories
 * @param events persisted market event history
 * @param lastEvent most recent market event, or {@code null}
 *
 * @author kaamyashinde
 * @contributor kevindmazali
 * @version 1.0.0
 * @since 2026-05-23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileFile(
    String username,
    String normalizedUsername,
    String pinHash,
    String displayName,
    boolean hasSeenWelcome,
    String playerName,
    BigDecimal startingMoney,
    BigDecimal cash,
    List<HoldingRow> holdings,
    List<TxRow> transactions,
    List<SavingsRow> savings,
    String exchangeName,
    int day,
    List<PriceRow> stockPrices,
    List<EventRow> events,
  EventRow lastEvent
) {

  /**
   * Normalizes nullable persisted lists to empty immutable lists.
   */
  public ProfileFile {
    holdings = emptyIfNull(holdings);
    transactions = emptyIfNull(transactions);
    savings = emptyIfNull(savings);
    stockPrices = emptyIfNull(stockPrices);
    events = emptyIfNull(events);
  }

  /**
   * Persisted share lot row.
   *
   * @param symbol held asset symbol
   * @param quantity share quantity in the lot
   * @param purchasePrice original purchase price for the lot
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record HoldingRow(String symbol, BigDecimal quantity, BigDecimal purchasePrice) {}

  /**
   * Persisted transaction row.
   *
   * @param type transaction type name, such as {@code PURCHASE} or {@code SALE}
   * @param symbol traded asset symbol
   * @param quantity traded quantity
   * @param purchasePrice price stored on the transaction share
   * @param day exchange day when the transaction occurred
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TxRow(String type, String symbol, BigDecimal quantity, BigDecimal purchasePrice, int day) {}

  /**
   * Persisted regular savings plan row.
   *
   * @param symbol target asset symbol
   * @param mode installment sizing mode
   * @param amount share quantity or budget amount, depending on {@code mode}
   * @param intervalDays trading days between installments
   * @param nextDueDay next day on which the plan should run
   * @param active whether the plan should be processed
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SavingsRow(
      String symbol,
      SavingsInstallmentMode mode,
      BigDecimal amount,
      int intervalDays,
      int nextDueDay,
      boolean active) {}

  /**
   * Persisted stock price history row.
   *
   * @param symbol stock symbol
   * @param prices historical prices in trading-day order
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record PriceRow(String symbol, List<BigDecimal> prices) {}

  /**
   * Persisted market event row.
   *
   * @param day trading day on which the event occurred
   * @param title short event title
   * @param description event description
   * @param symbols affected stock symbols
   * @param priceFactor multiplicative price factor applied by the event
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EventRow(
      int day,
      String title,
      String description,
      List<String> symbols,
      BigDecimal priceFactor) {}

  /**
   * Restored runtime session assembled from a profile file and market data.
   *
   * @param player restored player
   * @param exchange restored exchange
   */
  public record RestoredSession(Player player, Exchange exchange) {}

  /**
   * Hashes a PIN with the normalized username as a simple profile-local salt.
   *
   * @param normalizedUsername normalized username used in the hash payload
   * @param pin PIN characters
   * @return lowercase hexadecimal SHA-256 hash
   */
  public static String hashPin(String normalizedUsername, char[] pin) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      String payload = normalizedUsername + ":" + new String(pin);
      byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 not available.", exception);
    }
  }

  /**
   * Checks whether a PIN matches the stored hash.
   *
   * @param pin PIN characters to verify
   * @return {@code true} when the PIN matches this profile
   */
  public boolean matchesPin(char[] pin) {
    return pinHash != null && pinHash.equals(hashPin(normalizedUsername, pin));
  }

  /**
   * Captures live player and exchange state into a persistence record.
   *
   * @param player player to persist
   * @param exchange exchange to persist
   * @param username original username
   * @param normalizedUsername normalized username
   * @param pinHash hashed PIN
   * @param displayName optional display name
   * @param hasSeenWelcome whether the welcome screen has been acknowledged
   * @return profile file snapshot
   */
  public static ProfileFile capture(
      Player player,
      Exchange exchange,
      String username,
      String normalizedUsername,
      String pinHash,
      String displayName,
      boolean hasSeenWelcome) {
    return new ProfileFile(
        username,
        normalizedUsername,
        pinHash,
        displayName,
        hasSeenWelcome,
        player.getName(),
        player.getStartingMoney(),
        player.getMoney(),
        player.getPortfolio().getShares().stream()
            .map(share -> new HoldingRow(
                share.getAsset().getSymbol(),
                share.getQuantity(),
                share.getPurchasePrice()))
            .toList(),
        player.getTransactionArchive().getAllTransactions().stream()
            .map(tx -> {
              String type = tx.getTypeName().toUpperCase();
              return new TxRow(
                  type,
                  tx.getShare().getAsset().getSymbol(),
                  tx.getShare().getQuantity(),
                  tx.getShare().getPurchasePrice(),
                  tx.getDay());
            })
            .toList(),
        player.getRegularSavingsPlans().stream()
            .map(plan -> new SavingsRow(
                plan.getSymbol(),
                plan.getMode(),
                plan.getAmount(),
                plan.getIntervalDays(),
                plan.getNextDueDay(),
                plan.isActive()))
            .toList(),
        exchange.getName(),
        exchange.getDay(),
        exchange.getStocks().stream()
            .map(stock -> new PriceRow(stock.getSymbol(), List.copyOf(stock.getHistoricalPrices())))
            .toList(),
        exchange.getMarketEventHistory().stream()
            .map(ProfileFile::toEventRow)
            .toList(),
        exchange.getLastMarketEvent().map(ProfileFile::toEventRow).orElse(null));
  }

  /**
   * Restores runtime model objects from this persistence record.
   *
   * @param marketData base market data for fund definitions and stock names
   * @return restored player and exchange
   */
  public RestoredSession restore(MarketData marketData) {
    Map<String, Stock> stocksBySymbol = rebuildStocks(stockPrices, marketData);
    List<Fund> funds = rebuildFunds(marketData.funds(), stocksBySymbol);
    List<MarketEvent> history = events.stream().map(ProfileFile::toMarketEvent).toList();
    MarketEvent last = lastEvent == null ? null : toMarketEvent(lastEvent);
    Exchange exchange = new Exchange.Builder(exchangeName)
        .stocks(List.copyOf(stocksBySymbol.values()))
        .funds(funds)
        .day(day)
        .marketEventHistory(history)
        .lastMarketEvent(last)
        .build();

    List<Share> shareHoldings = holdings.stream()
        .map(row -> toShare(row, exchange))
        .toList();
    List<Transaction> txList = transactions.stream()
        .map(row -> toTransaction(row, exchange))
        .toList();
    List<RegularSavingsPlan> plans = savings.stream()
        .map(ProfileFile::toSavingsPlan)
        .toList();
    Player player = Player.restore(playerName, startingMoney, cash, shareHoldings, txList, plans);
    return new RestoredSession(player, exchange);
  }

  /**
   * Creates a fresh exchange from market data for a new profile.
   *
   * @param marketData market data used to seed listings
   * @param exchangeName exchange display name
   * @return fresh exchange with copied stock history
   */
  public static Exchange createFreshExchange(MarketData marketData, String exchangeName) {
    Map<String, Stock> stocks = marketData.stocks().stream()
        .collect(Collectors.toMap(
            Stock::getSymbol,
            stock -> {
              Stock copy = new Stock(stock.getSymbol(), stock.getCompany());
              stock.getHistoricalPrices().forEach(copy::addNewSalesPrice);
              return copy;
            },
            (left, right) -> left,
            LinkedHashMap::new));
    List<Fund> funds = rebuildFunds(marketData.funds(), stocks);
    return new Exchange.Builder(exchangeName)
        .stocks(List.copyOf(stocks.values()))
        .funds(funds)
        .build();
  }

  /**
   * Returns a copy with a new display name.
   *
   * @param displayName new display name
   * @return profile copy with updated display name
   */
  public ProfileFile withDisplayName(String displayName) {
    return new ProfileFile(
        username, normalizedUsername, pinHash, displayName, hasSeenWelcome,
        playerName, startingMoney, cash, holdings, transactions, savings,
        exchangeName, day, stockPrices, events, lastEvent);
  }

  /**
   * Returns a copy with the welcome flag set.
   *
   * @return profile copy marked as having seen the welcome screen
   */
  public ProfileFile withWelcomeSeen() {
    return new ProfileFile(
        username, normalizedUsername, pinHash, displayName, true,
        playerName, startingMoney, cash, holdings, transactions, savings,
        exchangeName, day, stockPrices, events, lastEvent);
  }

  private static <T> List<T> emptyIfNull(List<T> list) {
    return list == null ? List.of() : list;
  }

  private static EventRow toEventRow(MarketEvent event) {
    return new EventRow(
        event.day(),
        event.title(),
        event.description(),
        event.getAffectedSymbols().stream().sorted().toList(),
        event.priceFactor());
  }

  private static MarketEvent toMarketEvent(EventRow row) {
    return new MarketEvent(
        row.day(),
        row.title(),
        row.description(),
        new SymbolMarketEventTarget(Set.copyOf(row.symbols())),
        row.priceFactor());
  }

  private static Map<String, Stock> rebuildStocks(List<PriceRow> rows, MarketData marketData) {
    Map<String, String> companies = marketData.stocks().stream()
        .collect(Collectors.toMap(Stock::getSymbol, Stock::getCompany, (a, b) -> a));
    return rows.stream().collect(Collectors.toMap(
        PriceRow::symbol,
        row -> {
          String company = companies.getOrDefault(row.symbol(), row.symbol());
          Stock stock = new Stock(row.symbol(), company);
          row.prices().forEach(stock::addNewSalesPrice);
          return stock;
        },
        (left, right) -> left,
        LinkedHashMap::new));
  }

  private static List<Fund> rebuildFunds(List<Fund> baseFunds, Map<String, Stock> stocks) {
    return baseFunds.stream()
        .map(fund -> new Fund(
            fund.getSymbol(),
            fund.getDisplayName(),
            fund.getComponents().stream()
                .map(component -> new FundComponent(
                    requireStock(stocks, component.stock().getSymbol()),
                    component.weight()))
                .toList()))
        .toList();
  }

  private static Stock requireStock(Map<String, Stock> stocks, String symbol) {
    Stock stock = stocks.get(symbol);
    if (stock == null) {
      throw new IllegalStateException("Missing stock in restored exchange: " + symbol);
    }
    return stock;
  }

  private static Share toShare(HoldingRow row, Exchange exchange) {
    InvestableAsset asset = exchange.getAsset(row.symbol());
    if (asset == null) {
      throw new IllegalStateException("Unknown asset in saved profile: " + row.symbol());
    }
    return new Share(asset, row.quantity(), row.purchasePrice());
  }

  private static Transaction toTransaction(TxRow row, Exchange exchange) {
    Share share = toShare(new HoldingRow(row.symbol(), row.quantity(), row.purchasePrice()), exchange);
    if ("PURCHASE".equals(row.type())) {
      return new Purchase(share, row.day());
    }
    if ("SALE".equals(row.type())) {
      return new Sale(share, row.day());
    }
    throw new IllegalStateException("Unknown transaction type: " + row.type());
  }

  private static RegularSavingsPlan toSavingsPlan(SavingsRow row) {
    RegularSavingsPlan plan = new RegularSavingsPlan(
        row.symbol(),
        row.mode(),
        row.amount(),
        row.intervalDays(),
        row.nextDueDay() - row.intervalDays());
    plan.setNextDueDay(row.nextDueDay());
    plan.setActive(row.active());
    return plan;
  }
}
