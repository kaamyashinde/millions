package model.persistence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.Player;
import model.market.Exchange;
import model.market.InvestableAsset;
import model.market.Share;
import model.market.Stock;
import model.market.fund.Fund;
import model.market.fund.FundComponent;
import model.market.marketevent.unexpected.MarketEvent;
import model.market.marketevent.unexpected.target.SymbolMarketEventTarget;
import model.savings.RegularSavingsPlan;
import model.transaction.Purchase;
import model.transaction.Sale;
import model.transaction.Transaction;

/**
 * Converts between mutable domain objects and immutable persisted snapshots.
 */
public final class GameStateMapper {

  /**
   * Current JSON snapshot schema version.
   */
  public static final int SCHEMA_VERSION = 1;

  private final String exchangeName;

  /**
   * Creates a mapper for the configured exchange name.
   *
   * @param exchangeName name assigned to new exchanges created from bundled market data
   */
  public GameStateMapper(String exchangeName) {
    this.exchangeName = exchangeName;
  }

  /**
   * Creates a fresh exchange instance from bundled market definitions.
   *
   * @param marketData bundled stock and fund definitions
   * @return new exchange with day 1 and empty event history
   */
  public Exchange createFreshExchange(MarketData marketData) {
    Map<String, Stock> restoredStocks = rebuildStocks(
        marketData.stocks().stream()
            .map(stock -> new GameStateSnapshot.StockSnapshot(
                stock.getSymbol(),
                stock.getCompany(),
                List.copyOf(stock.getHistoricalPrices())))
            .toList());
    List<Fund> restoredFunds = rebuildFunds(marketData.funds(), restoredStocks);
    return new Exchange.Builder(exchangeName)
        .stocks(List.copyOf(restoredStocks.values()))
        .funds(restoredFunds)
        .build();
  }

  /**
   * Converts the current player and exchange into a persisted snapshot.
   *
   * @param player   active player
   * @param exchange active exchange
   * @return persisted snapshot
   */
  public GameStateSnapshot toSnapshot(Player player, Exchange exchange) {
    return new GameStateSnapshot(
        SCHEMA_VERSION,
        toPlayerSnapshot(player),
        toExchangeSnapshot(exchange));
  }

  /**
   * Rebuilds an exchange from persisted state and bundled market definitions.
   *
   * @param snapshot   stored exchange snapshot
   * @param marketData latest bundled market definitions
   * @return restored exchange instance
   */
  public Exchange restoreExchange(GameStateSnapshot.ExchangeSnapshot snapshot,
      MarketData marketData) {
    Map<String, Stock> restoredStocks = rebuildStocks(snapshot.stocks());
    List<Fund> restoredFunds = rebuildFunds(marketData.funds(), restoredStocks);
    List<MarketEvent> history = snapshot.marketEventHistory().stream()
        .map(this::toMarketEvent)
        .toList();
    MarketEvent lastMarketEvent = snapshot.lastMarketEvent() == null
        ? null
        : toMarketEvent(snapshot.lastMarketEvent());
    return new Exchange.Builder(snapshot.name())
        .stocks(List.copyOf(restoredStocks.values()))
        .funds(restoredFunds)
        .day(snapshot.day())
        .marketEventHistory(history)
        .lastMarketEvent(lastMarketEvent)
        .build();
  }

  /**
   * Rebuilds a player from persisted state using assets from the restored exchange.
   *
   * @param snapshot stored player snapshot
   * @param exchange restored exchange that owns the referenced assets
   * @return restored player
   */
  public Player restorePlayer(GameStateSnapshot.PlayerSnapshot snapshot, Exchange exchange) {
    List<Share> holdings = snapshot.holdings().stream()
        .map(shareSnapshot -> toShare(shareSnapshot, exchange))
        .toList();
    List<Transaction> transactions = snapshot.transactions().stream()
        .map(transactionSnapshot -> toTransaction(transactionSnapshot, exchange))
        .toList();
    List<RegularSavingsPlan> savingsPlans = snapshot.savingsPlans().stream()
        .map(this::toSavingsPlan)
        .toList();
    return Player.restore(
        snapshot.name(),
        snapshot.startingMoney(),
        snapshot.currentMoney(),
        holdings,
        transactions,
        savingsPlans);
  }

  private GameStateSnapshot.PlayerSnapshot toPlayerSnapshot(Player player) {
    return new GameStateSnapshot.PlayerSnapshot(
        player.getName(),
        player.getStartingMoney(),
        player.getMoney(),
        player.getPortfolio().getShares().stream()
            .map(this::toShareSnapshot)
            .toList(),
        player.getTransactionArchive().getAllTransactions().stream()
            .map(this::toTransactionSnapshot)
            .toList(),
        player.getRegularSavingsPlans().stream()
            .map(this::toSavingsPlanSnapshot)
            .toList());
  }

  private GameStateSnapshot.ExchangeSnapshot toExchangeSnapshot(Exchange exchange) {
    return new GameStateSnapshot.ExchangeSnapshot(
        exchange.getName(),
        exchange.getDay(),
        exchange.getStocks().stream()
            .map(this::toStockSnapshot)
            .toList(),
        exchange.getMarketEventHistory().stream()
            .map(this::toMarketEventSnapshot)
            .toList(),
        exchange.getLastMarketEvent()
            .map(this::toMarketEventSnapshot)
            .orElse(null));
  }

  private GameStateSnapshot.StockSnapshot toStockSnapshot(Stock stock) {
    return new GameStateSnapshot.StockSnapshot(
        stock.getSymbol(),
        stock.getCompany(),
        List.copyOf(stock.getHistoricalPrices()));
  }

  private GameStateSnapshot.ShareSnapshot toShareSnapshot(Share share) {
    return new GameStateSnapshot.ShareSnapshot(
        share.getAsset().getSymbol(),
        share.getQuantity(),
        share.getPurchasePrice());
  }

  private GameStateSnapshot.TransactionSnapshot toTransactionSnapshot(Transaction transaction) {
    String type = transaction instanceof Purchase ? "PURCHASE" : "SALE";
    return new GameStateSnapshot.TransactionSnapshot(
        type,
        transaction.getShare().getAsset().getSymbol(),
        transaction.getShare().getQuantity(),
        transaction.getShare().getPurchasePrice(),
        transaction.getDay());
  }

  private GameStateSnapshot.RegularSavingsPlanSnapshot toSavingsPlanSnapshot(
      RegularSavingsPlan plan) {
    return new GameStateSnapshot.RegularSavingsPlanSnapshot(
        plan.getSymbol(),
        plan.getMode(),
        plan.getAmount(),
        plan.getIntervalDays(),
        plan.getNextDueDay(),
        plan.isActive());
  }

  private GameStateSnapshot.MarketEventSnapshot toMarketEventSnapshot(MarketEvent event) {
    return new GameStateSnapshot.MarketEventSnapshot(
        event.day(),
        event.title(),
        event.description(),
        event.getAffectedSymbols().stream().sorted().toList(),
        event.priceFactor());
  }

  private Map<String, Stock> rebuildStocks(List<GameStateSnapshot.StockSnapshot> stockSnapshots) {
    return stockSnapshots.stream().collect(Collectors.toMap(
        GameStateSnapshot.StockSnapshot::symbol,
        stockSnapshot -> {
          Stock stock = new Stock(stockSnapshot.symbol(), stockSnapshot.company());
          stockSnapshot.historicalPrices().forEach(stock::addNewSalesPrice);
          return stock;
        },
        (left, right) -> left,
        LinkedHashMap::new));
  }

  private List<Fund> rebuildFunds(List<Fund> baseFunds, Map<String, Stock> restoredStocks) {
    return baseFunds.stream()
        .map(fund -> new Fund(
            fund.getSymbol(),
            fund.getDisplayName(),
            fund.getComponents().stream()
                .map(component -> new FundComponent(
                    requireStock(restoredStocks, component.stock().getSymbol()),
                    component.weight()))
                .toList()))
        .toList();
  }

  private Share toShare(GameStateSnapshot.ShareSnapshot snapshot, Exchange exchange) {
    InvestableAsset asset = exchange.getAsset(snapshot.assetSymbol());
    if (asset == null) {
      throw new IllegalStateException("Unknown asset in saved profile: " + snapshot.assetSymbol());
    }
    return new Share(asset, snapshot.quantity(), snapshot.purchasePrice());
  }

  private Transaction toTransaction(GameStateSnapshot.TransactionSnapshot snapshot,
      Exchange exchange) {
    Share share = toShare(
        new GameStateSnapshot.ShareSnapshot(
            snapshot.assetSymbol(),
            snapshot.quantity(),
            snapshot.purchasePrice()),
        exchange);
    if ("PURCHASE".equals(snapshot.type())) {
      return new Purchase(share, snapshot.day());
    }
    if ("SALE".equals(snapshot.type())) {
      return new Sale(share, snapshot.day());
    }
    throw new IllegalStateException(
        "Unknown transaction type in saved profile: " + snapshot.type());
  }

  private RegularSavingsPlan toSavingsPlan(GameStateSnapshot.RegularSavingsPlanSnapshot snapshot) {
    RegularSavingsPlan plan = new RegularSavingsPlan(
        snapshot.symbol(),
        snapshot.mode(),
        snapshot.amount(),
        snapshot.intervalDays(),
        snapshot.nextDueDay() - snapshot.intervalDays());
    plan.setNextDueDay(snapshot.nextDueDay());
    plan.setActive(snapshot.active());
    return plan;
  }

  private MarketEvent toMarketEvent(GameStateSnapshot.MarketEventSnapshot snapshot) {
    return new MarketEvent(
        snapshot.day(),
        snapshot.title(),
        snapshot.description(),
        new SymbolMarketEventTarget(Set.copyOf(snapshot.affectedSymbols())),
        snapshot.priceFactor());
  }

  private Stock requireStock(Map<String, Stock> restoredStocks, String symbol) {
    Stock stock = restoredStocks.get(symbol);
    if (stock == null) {
      throw new IllegalStateException("Missing stock in restored exchange: " + symbol);
    }
    return stock;
  }
}
