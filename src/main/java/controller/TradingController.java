package controller;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import model.core.asset.InvestableAsset;
import model.core.asset.Share;
import model.core.market.Exchange;
import model.core.player.Player;
import model.exception.trading.InsufficientFundsException;
import model.exception.trading.InsufficientSharesException;
import model.exception.trading.ShareNotFoundException;
import model.trading.calculator.PurchaseCalculator;
import model.trading.transaction.TransactionSizing;
import model.trading.transaction.Transaction;
import util.I18n;
import util.Validator;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;
import view.util.UiFormat;

/**
 * Executes buy and sell operations for the GUI, mapping domain errors to user-facing messages.
 *
 * <p>The controller wraps {@link Exchange} trade commands and converts model exceptions into
 * localized {@link TradeResult} values for dialogs and notifications.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-05-23
 */
public class TradingController {

  private final Exchange exchange;
  private final Player player;
  private final NotificationService notifications;

  /**
   * Display-friendly purchase estimate for the current price and quantity.
   *
   * @param unitPrice current unit price
   * @param quantity estimated quantity to buy
   * @param gross cost before commission
   * @param commission purchase commission
   * @param total total purchase cost after commission
   */
  public record BuyEstimate(
      BigDecimal unitPrice,
      BigDecimal quantity,
      BigDecimal gross,
      BigDecimal commission,
      BigDecimal total) {}

  /**
   * Creates a trading controller for one active player.
   *
   * @param exchange exchange used for trades
   * @param player active player
   * @param notifications notification service for success toasts
   */
  public TradingController(Exchange exchange, Player player, NotificationService notifications) {
    checkNotNull(exchange, "Exchange");
    checkNotNull(player, "Player");
    checkNotNull(notifications, "notifications");
    this.exchange = exchange;
    this.player = player;
    this.notifications = notifications;
  }

  /**
   * Exposes the exchange used to execute trades.
   *
   * @return exchange used to execute trades
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Exposes the player affected by trades.
   *
   * @return player whose portfolio and cash balance are changed by trades
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the player's current cash balance.
   *
   * @return player's current cash balance
   */
  public BigDecimal getCashBalance() {
    return player.getMoney();
  }

  /**
   * Returns the player's quantity for a symbol across all FIFO lots.
   *
   * @param symbol stock or fund symbol
   * @return total quantity held across all lots
   */
  public BigDecimal getOwnedQuantity(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      return BigDecimal.ZERO;
    }
    return player.getPortfolio().totalQuantityForSymbol(symbol.trim().toUpperCase());
  }

  /**
   * Looks up the latest price for a listed asset.
   *
   * @param symbol stock or fund symbol
   * @return latest listed price when the symbol exists
   */
  public Optional<BigDecimal> getLatestPrice(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      return Optional.empty();
    }
    InvestableAsset asset = exchange.listings().getAsset(symbol.trim().toUpperCase());
    if (asset == null) {
      return Optional.empty();
    }
    return Optional.of(asset.getSalesPrice());
  }

  /**
   * Formats a monetary value for trade dialogs.
   *
   * @param value amount to format
   * @return plain string with two decimal places
   */
  public String formatMoney(BigDecimal value) {
    return UiFormat.decimal(value);
  }

  /**
   * @param value quantity to format
   * @return plain string with two decimal places
   */
  public String formatQuantity(BigDecimal value) {
    return UiFormat.decimal(value);
  }

  /**
   * Estimates purchase cost for a fixed quantity.
   *
   * @param symbol stock or fund symbol
   * @param quantityText quantity as entered by the user
   * @return estimate when the symbol and quantity are valid
   */
  public Optional<BuyEstimate> estimateBuyByQuantity(String symbol, String quantityText) {
    Optional<InvestableAsset> asset = findAsset(symbol);
    Optional<BigDecimal> quantity = parsePositiveAmountOrEmpty(quantityText);
    if (asset.isEmpty() || quantity.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(createBuyEstimate(asset.get(), quantity.get()));
  }

  /**
   * Estimates purchase cost for a max-spend amount.
   *
   * @param symbol stock or fund symbol
   * @param maxSpendText spending cap as entered by the user
   * @return estimate when the symbol and budget can buy a positive quantity
   */
  public Optional<BuyEstimate> estimateBuyForBudget(String symbol, String maxSpendText) {
    Optional<InvestableAsset> asset = findAsset(symbol);
    Optional<BigDecimal> maxSpend = parsePositiveAmountOrEmpty(maxSpendText);
    if (asset.isEmpty() || maxSpend.isEmpty()) {
      return Optional.empty();
    }
    BigDecimal quantity = TransactionSizing.maxQuantityForBudget(asset.get(), maxSpend.get());
    if (quantity.signum() <= 0) {
      return Optional.empty();
    }
    return Optional.of(createBuyEstimate(asset.get(), quantity));
  }

  /**
   * Buys a fixed quantity of shares.
   *
   * @param symbol stock or fund symbol
   * @param quantityText quantity as entered by the user
   * @return success or failure with a user-facing message
   */
  public TradeResult buyByQuantity(String symbol, String quantityText) {
    return execute(symbol, quantityText, normalized -> {
      BigDecimal quantity = parsePositiveAmount(quantityText);
      Transaction tx = exchange.buy(normalized, quantity, player);
      Share share = tx.getShare();
      return I18n.format("buy.success", share.getQuantity(), normalized);
    });
  }

  /**
   * Buys as many shares as possible within a spending budget.
   *
   * @param symbol stock or fund symbol
   * @param maxSpendText maximum spend as entered by the user
   * @return success or failure with a user-facing message
   */
  public TradeResult buyUpToBudget(String symbol, String maxSpendText) {
    return execute(symbol, maxSpendText, normalized -> {
      BigDecimal maxSpend = parsePositiveAmount(maxSpendText);
      exchange.buyUpToBudget(normalized, maxSpend, player);
      return I18n.format("buy.success.budget", normalized, maxSpend);
    });
  }

  /**
   * Sells a quantity of shares in FIFO order across lots.
   *
   * @param symbol stock or fund symbol
   * @param quantityText quantity as entered by the user
   * @return success or failure with a user-facing message
   */
  public TradeResult sellByQuantity(String symbol, String quantityText) {
    return execute(symbol, quantityText, normalized -> {
      BigDecimal quantity = parsePositiveAmount(quantityText);
      List<Transaction> txs = exchange.sellByQuantity(normalized, quantity, player);
      if (txs.size() == 1) {
        Share share = txs.getFirst().getShare();
        return I18n.format("sell.success", share.getQuantity(), normalized);
      }
      return I18n.format("sell.success.multi", txs.size(), normalized);
    });
  }

  private TradeResult execute(String symbol, String amountText, TradeOperation operation) {
    Optional<String> symbolError = validateSymbol(symbol);
    if (symbolError.isPresent()) {
      return new TradeResult.Failure(symbolError.get());
    }
    String normalized = symbol.trim().toUpperCase();
    try {
      String message = operation.run(normalized);
      notifications.show(ToastMode.SUCCESS, "Trade", stripCliPrefix(message));
      return new TradeResult.Success(message);
    } catch (NumberFormatException e) {
      return new TradeResult.Failure(I18n.get("invalid.input"));
    } catch (IllegalArgumentException e) {
      return mapIllegalArgument(e);
    } catch (InsufficientFundsException e) {
      return new TradeResult.Failure(I18n.get("error.insufficientFunds"));
    } catch (InsufficientSharesException e) {
      return new TradeResult.Failure(I18n.format("error.insufficientShares", e.getSymbol()));
    } catch (ShareNotFoundException e) {
      return new TradeResult.Failure(
          I18n.format("error.shareNotFound", e.getStockSymbol(), e.getPlayerName()));
    }
  }

  private static String stripCliPrefix(String message) {
    if (message == null) {
      return "";
    }
    return message.startsWith("-> ") ? message.substring(3) : message;
  }

  private Optional<String> validateSymbol(String symbol) {
    if (symbol == null || symbol.trim().isEmpty()) {
      return Optional.of(I18n.get("invalid.input"));
    }
    String normalized = symbol.trim().toUpperCase();
    if (!exchange.listings().hasAsset(normalized)) {
      return Optional.of(I18n.format("error.assetNotOnExchange", normalized));
    }
    return Optional.empty();
  }

  private Optional<InvestableAsset> findAsset(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(exchange.listings().getAsset(symbol.trim().toUpperCase()));
  }

  private static Optional<BigDecimal> parsePositiveAmountOrEmpty(String text) {
    try {
      BigDecimal value = parsePositiveAmount(text);
      return Optional.of(value);
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  private static BuyEstimate createBuyEstimate(InvestableAsset asset, BigDecimal quantity) {
    BigDecimal unitPrice = asset.getSalesPrice();
    PurchaseCalculator calculator = new PurchaseCalculator(new Share(asset, quantity, unitPrice));
    return new BuyEstimate(
        unitPrice,
        quantity,
        calculator.calculateGross(),
        calculator.calculateCommission(),
        calculator.calculateTotal());
  }

  private static BigDecimal parsePositiveAmount(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("empty");
    }
    BigDecimal value = new BigDecimal(text.trim());
    Validator.requirePositive(value, "amount");
    return value;
  }

  private static TradeResult.Failure mapIllegalArgument(IllegalArgumentException e) {
    String msg = e.getMessage();
    if (msg != null && msg.contains("positive")) {
      return new TradeResult.Failure(I18n.get("validation.quantity.positive"));
    }
    return new TradeResult.Failure(I18n.get("error.invalidArgument"));
  }

  @FunctionalInterface
  private interface TradeOperation {
    String run(String normalizedSymbol) throws InsufficientFundsException,
        InsufficientSharesException, ShareNotFoundException;
  }
}
