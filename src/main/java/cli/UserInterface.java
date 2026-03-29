package cli;

import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import model.Exchange;
import model.Player;
import model.Stock;
import model.Share;
import model.exception.InsufficientFundsException;
import model.exception.ShareNotFoundException;
import model.transaction.Purchase;
import model.transaction.Transaction;
import util.I18n;

/**
 * The UserInterface class is responsible for handling the user input and output
 * for the Millions stock trading application.
 * The class provides a menu for the user to set up a player, view portfolio and balance,
 * list and search stocks, buy and sell shares, advance the trading day, and view transaction history.
 */
public class UserInterface {

  private static final int QUIT = 0;
  private static final int SET_UP_PLAYER = 1;
  private static final int VIEW_BALANCE = 2;
  private static final int VIEW_PORTFOLIO = 3;
  private static final int LIST_STOCKS = 4;
  private static final int SEARCH_STOCKS = 5;
  private static final int BUY_SHARES = 6;
  private static final int SELL_SHARES = 7;
  private static final int ADVANCE_DAY = 8;
  private static final int VIEW_TRANSACTIONS = 9;
  private static final int INVALID_MENU_CHOICE = -1;

  private static final Scanner input = new Scanner(System.in);

  private static boolean running = true;
  private static Exchange exchange;
  private static Player player;

  /**
   * Constructor for the UserInterface class.
   */
  private UserInterface() {
  }

  /**
   * Launches the UserInterface. Runs init and start.
   */
  public static void launch() {
    init();
    start();
  }

  /**
   * Initialises the UserInterface. Creates a default exchange with a set of stocks
   * so that the user can list, search, buy and sell without loading from file.
   */
  private static void init() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("150.00"));
    Stock google = new Stock("GOOGL", "Alphabet Inc.");
    google.addNewSalesPrice(new BigDecimal("2800.00"));
    Stock microsoft = new Stock("MSFT", "Microsoft Corporation");
    microsoft.addNewSalesPrice(new BigDecimal("300.00"));
    exchange = new Exchange("NYSE", List.of(apple, google, microsoft));
    player = null;
  }

  /**
   * Starts the UserInterface. Prints a welcome message and runs the menu loop
   * until the user chooses to quit.
   */
  private static void start() {
    System.out.println(I18n.get("app.welcome.banner"));
    System.out.println();
    System.out.println(I18n.get("app.welcome.body"));
    while (running) {
      triggerChoice();
    }
  }

  /**
   * Shows the menu and reads the user's choice.
   *
   * @return the integer corresponding to the action the user wants to perform
   */
  private static int showMenu() {
    System.out.println(I18n.get("menu.header"));
    System.out.println(I18n.get("menu.option.exit"));
    System.out.println(I18n.get("menu.option.setup"));
    System.out.println(I18n.get("menu.option.balance"));
    System.out.println(I18n.get("menu.option.portfolio"));
    System.out.println(I18n.get("menu.option.list"));
    System.out.println(I18n.get("menu.option.search"));
    System.out.println(I18n.get("menu.option.buy"));
    System.out.println(I18n.get("menu.option.sell"));
    System.out.println(I18n.get("menu.option.day"));
    System.out.println(I18n.get("menu.option.transactions"));
    System.out.println(I18n.get("menu.footer"));
    System.out.println(I18n.get("menu.prompt"));
    try {
      return input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      return INVALID_MENU_CHOICE;
    }
  }

  /**
   * Uses the user input from showMenu to trigger the corresponding action.
   */
  private static void triggerChoice() {
    int choice = showMenu();
    switch (choice) {
      case QUIT -> quit();
      case SET_UP_PLAYER -> setUpPlayer();
      case VIEW_BALANCE -> viewBalance();
      case VIEW_PORTFOLIO -> viewPortfolio();
      case LIST_STOCKS -> listStocks();
      case SEARCH_STOCKS -> searchStocks();
      case BUY_SHARES -> buyShares();
      case SELL_SHARES -> sellShares();
      case ADVANCE_DAY -> advanceDay();
      case VIEW_TRANSACTIONS -> viewTransactions();
      default -> System.out.println(I18n.get("invalid.input"));
    }
  }

  /**
   * Ends the application.
   */
  private static void quit() {
    running = false;
    System.out.println(I18n.get("quit.thanks"));
    System.out.println(I18n.get("quit.success"));
    System.exit(0);
  }

  /**
   * Checks whether a player has been set up. Prints a message if not.
   *
   * @return true if player is null, false otherwise
   */
  private static boolean isPlayerMissing() {
    if (player == null) {
      System.out.println(I18n.get("require.player"));
      return true;
    }
    return false;
  }

  /**
   * Sets up the player. Prompts for name and starting money and creates a new Player.
   */
  private static void setUpPlayer() {
    input.nextLine();
    System.out.println(I18n.get("prompt.name"));
    String name = input.nextLine().trim();
    if (name.isEmpty()) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("prompt.startingMoney"));
    try {
      BigDecimal startingMoney = new BigDecimal(input.nextLine().trim());
      if (startingMoney.compareTo(BigDecimal.ZERO) < 0) {
        System.out.println(
            I18n.get("invalid.input") + " " + I18n.get("validation.startingMoney.nonNegative"));
        return;
      }
      player = new Player(name, startingMoney);
      System.out.println(I18n.format("player.created", name, startingMoney));
    } catch (NumberFormatException e) {
      System.out.println(I18n.get("invalid.input"));
    }
  }

  /**
   * Prints the current balance of the player.
   */
  private static void viewBalance() {
    if (isPlayerMissing()) {
      return;
    }
    System.out.println(I18n.format("balance.current", player.getMoney()));
  }

  /**
   * Prints the player's portfolio (all shares held).
   */
  private static void viewPortfolio() {
    if (isPlayerMissing()) {
      return;
    }
    if (player.getPortfolio().getShares().isEmpty()) {
      System.out.println(I18n.get("portfolio.empty"));
      return;
    }
    System.out.println(I18n.get("portfolio.header"));
    player.getPortfolio().getShares().forEach(share -> System.out.println(
        I18n.format(
            "portfolio.line",
            share.getStock().getSymbol(),
            share.getStock().getCompany(),
            share.getQuantity(),
            share.getPurchasePrice(),
            share.getStock().getSalesPrice())));
  }

  /**
   * Lists all stocks available on the exchange with symbol, company and current price.
   */
  private static void listStocks() {
    List<Stock> stocks = exchange.findStocks("");
    if (stocks.isEmpty()) {
      System.out.println(I18n.get("stocks.none"));
      return;
    }
    System.out.println(I18n.format("stocks.onExchange", exchange.getName()));
    stocks.forEach(stock -> System.out.println(
        I18n.format("stock.line", stock.getSymbol(), stock.getCompany(), stock.getSalesPrice())));
  }

  /**
   * Searches stocks by symbol or company name and prints matching results.
   */
  private static void searchStocks() {
    input.nextLine();
    System.out.println(I18n.get("prompt.search"));
    String term = input.nextLine().trim();
    List<Stock> results = exchange.findStocks(term);
    if (results.isEmpty()) {
      System.out.println(I18n.format("search.none", term));
      return;
    }
    System.out.println(I18n.format("search.found", results.size()));
    results.forEach(stock -> System.out.println(
        I18n.format("stock.line", stock.getSymbol(), stock.getCompany(), stock.getSalesPrice())));
  }

  /**
   * Buys shares of a stock. Prompts for stock symbol and quantity, then commits the purchase.
   */
  private static void buyShares() {
    if (isPlayerMissing()) {
      return;
    }
    input.nextLine();
    System.out.println(I18n.get("prompt.symbol"));
    String symbol = input.nextLine().trim().toUpperCase();
    if (symbol.isEmpty()) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    if (!exchange.hasStock(symbol)) {
      System.out.println(I18n.format("error.stockNotOnExchange", symbol));
      return;
    }
    System.out.println(I18n.get("prompt.quantity.buy"));
    try {
      BigDecimal quantity = new BigDecimal(input.nextLine().trim());
      if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println(
            I18n.get("invalid.input") + " " + I18n.get("validation.quantity.positive"));
        return;
      }
      exchange.buy(symbol, quantity, player);
      System.out.println(I18n.format("buy.success", quantity, symbol));
    } catch (NumberFormatException e) {
      System.out.println(I18n.get("invalid.input"));
    } catch (InsufficientFundsException ignored) {
      System.out.println(I18n.get("error.insufficientFunds"));
    }
  }

  /**
   * Sells a share from the portfolio. Lists holdings with numbers and prompts for the index to sell.
   */
  private static void sellShares() {
    if (isPlayerMissing()) {
      return;
    }
    List<Share> shares = player.getPortfolio().getShares();
    if (shares.isEmpty()) {
      System.out.println(I18n.get("portfolio.empty.sell"));
      return;
    }
    System.out.println(I18n.get("holdings.header"));
    for (int i = 0; i < shares.size(); i++) {
      Share s = shares.get(i);
      System.out.println(I18n.format(
          "holding.line",
          i + 1,
          s.getStock().getSymbol(),
          s.getQuantity(),
          s.getPurchasePrice(),
          s.getStock().getSalesPrice()));
    }
    System.out.println(I18n.format("prompt.holdingIndex", shares.size()));
    try {
      int index = input.nextInt();
      if (index < 1 || index > shares.size()) {
        System.out.println(I18n.get("invalid.input"));
        return;
      }
      Share toSell = shares.get(index - 1);
      exchange.sell(toSell, player);
      System.out.println(I18n.format("sell.success", toSell.getQuantity(),
          toSell.getStock().getSymbol()));
    } catch (InputMismatchException e) {
      System.out.println(I18n.get("invalid.input"));
      input.nextLine();
    } catch (ShareNotFoundException e) {
      System.out.println(I18n.format("error.shareNotFound", e.getStockSymbol(), e.getPlayerName()));
    }
  }

  /**
   * Advances the exchange to the next trading day and updates stock prices.
   */
  private static void advanceDay() {
    exchange.advance();
    System.out.println(I18n.format("day.advanced", exchange.getDay()));
  }

  /**
   * Prints the player's transaction history up to the current trading day.
   */
  private static void viewTransactions() {
    if (isPlayerMissing()) {
      return;
    }
    int day = exchange.getDay();
    List<Transaction> transactions = player.getTransactionArchive().getTransactions(day);
    if (transactions.isEmpty()) {
      System.out.println(I18n.get("transactions.none"));
      return;
    }
    System.out.println(I18n.format("transactions.header", day));
    transactions.forEach(t -> {
      String type = t instanceof Purchase ? I18n.get("tx.type.purchase") : I18n.get("tx.type.sale");
      String sym = t.getShare().getStock().getSymbol();
      String qty = t.getShare().getQuantity().toString();
      System.out.println(I18n.format("transaction.line", t.getDay(), type, sym, qty));
    });
  }
}
