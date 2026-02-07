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
import model.transaction.Transaction;

/**
 * The UserInterface class is responsible for handling the user input and output
 * for the Millions stock trading application.
 * The class provides a menu for the user to set up a player, view portfolio and balance,
 * list and search stocks, buy and sell shares, advance the week, and view transaction history.
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
  private static final int ADVANCE_WEEK = 8;
  private static final int VIEW_TRANSACTIONS = 9;

  private static final Scanner input = new Scanner(System.in);
  private static final String INVALID_INPUT = "-> Invalid input, please try again.";

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
    System.out.println("""
        =================== MILLIONS - Stock Trading ===================

        Hello and welcome to Millions. Manage your portfolio and trade stocks.
        """);
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
    System.out.println("""
        ------------ MENU -----------
        0. Exit the application
        1. Set up player (name, starting money)
        2. View balance
        3. View portfolio
        4. List all stocks
        5. Search stocks
        6. Buy shares
        7. Sell shares
        8. Advance week
        9. View transaction history
        -----------------------------
        What would you like to do? (Enter a number between 0 and 9).
        """);
    return input.nextInt();
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
      case ADVANCE_WEEK -> advanceWeek();
      case VIEW_TRANSACTIONS -> viewTransactions();
      default -> System.out.println(INVALID_INPUT);
    }
  }

  /**
   * Ends the application.
   */
  private static void quit() {
    running = false;
    System.out.println("Thank you for using Millions!");
    System.out.println("Successfully exited the application.");
    System.exit(0);
  }

  /**
   * Ensures a player has been set up. Prints a message if not.
   *
   * @return true if player is not null, false otherwise
   */
  private static boolean requirePlayer() {
    if (player == null) {
      System.out.println("-> You need to set up a player first (option 1).");
      return false;
    }
    return true;
  }

  /**
   * Sets up the player. Prompts for name and starting money and creates a new Player.
   */
  private static void setUpPlayer() {
    input.nextLine();
    System.out.println("Enter your name: ");
    String name = input.nextLine().trim();
    if (name.isEmpty()) {
      System.out.println(INVALID_INPUT);
      return;
    }
    System.out.println("Enter your starting money (e.g. 10000.00): ");
    try {
      BigDecimal startingMoney = new BigDecimal(input.nextLine().trim());
      if (startingMoney.compareTo(BigDecimal.ZERO) < 0) {
        System.out.println(INVALID_INPUT + " Starting money must be non-negative.");
        return;
      }
      player = new Player(name, startingMoney);
      System.out.println("-> Player '" + name + "' created with " + startingMoney + " in balance.");
    } catch (NumberFormatException e) {
      System.out.println(INVALID_INPUT);
    }
  }

  /**
   * Prints the current balance of the player.
   */
  private static void viewBalance() {
    if (!requirePlayer()) {
      return;
    }
    System.out.println("-> Your current balance: " + player.getMoney());
  }

  /**
   * Prints the player's portfolio (all shares held).
   */
  private static void viewPortfolio() {
    if (!requirePlayer()) {
      return;
    }
    if (player.getPortfolio().getShares().isEmpty()) {
      System.out.println("-> Your portfolio is empty.");
      return;
    }
    System.out.println("-> Your portfolio:");
    player.getPortfolio().getShares().forEach(share -> System.out.println(
        "   " + share.getStock().getSymbol() + " (" + share.getStock().getCompany() + "): "
            + share.getQuantity() + " shares @ " + share.getPurchasePrice() + " (current: "
            + share.getStock().getSalesPrice() + ")"));
  }

  /**
   * Lists all stocks available on the exchange with symbol, company and current price.
   */
  private static void listStocks() {
    List<Stock> stocks = exchange.findStocks("");
    if (stocks.isEmpty()) {
      System.out.println("-> No stocks available.");
      return;
    }
    System.out.println("-> Stocks on " + exchange.getName() + ":");
    stocks.forEach(stock -> System.out.println(
        "   " + stock.getSymbol() + " - " + stock.getCompany() + " | Price: " + stock.getSalesPrice()));
  }

  /**
   * Searches stocks by symbol or company name and prints matching results.
   */
  private static void searchStocks() {
    input.nextLine();
    System.out.println("Enter search term (symbol or company name): ");
    String term = input.nextLine().trim();
    List<Stock> results = exchange.findStocks(term);
    if (results.isEmpty()) {
      System.out.println("-> No stocks found matching '" + term + "'.");
      return;
    }
    System.out.println("-> Found " + results.size() + " stock(s):");
    results.forEach(stock -> System.out.println(
        "   " + stock.getSymbol() + " - " + stock.getCompany() + " | Price: " + stock.getSalesPrice()));
  }

  /**
   * Buys shares of a stock. Prompts for stock symbol and quantity, then commits the purchase.
   */
  private static void buyShares() {
    if (!requirePlayer()) {
      return;
    }
    input.nextLine();
    System.out.println("Enter the stock symbol (e.g. AAPL): ");
    String symbol = input.nextLine().trim().toUpperCase();
    if (symbol.isEmpty()) {
      System.out.println(INVALID_INPUT);
      return;
    }
    if (!exchange.hasStock(symbol)) {
      System.out.println("-> Stock '" + symbol + "' not found on the exchange.");
      return;
    }
    System.out.println("Enter the quantity to buy: ");
    try {
      BigDecimal quantity = new BigDecimal(input.nextLine().trim());
      if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println(INVALID_INPUT + " Quantity must be positive.");
        return;
      }
      Transaction purchase = exchange.buy(symbol, quantity, player);
      System.out.println("-> Successfully bought " + quantity + " share(s) of " + symbol + ".");
    } catch (NumberFormatException e) {
      System.out.println(INVALID_INPUT);
    } catch (InsufficientFundsException e) {
      System.out.println("-> " + e.getMessage());
    }
  }

  /**
   * Sells a share from the portfolio. Lists holdings with numbers and prompts for the index to sell.
   */
  private static void sellShares() {
    if (!requirePlayer()) {
      return;
    }
    List<Share> shares = player.getPortfolio().getShares();
    if (shares.isEmpty()) {
      System.out.println("-> Your portfolio is empty. Nothing to sell.");
      return;
    }
    System.out.println("-> Your holdings (enter the number to sell that lot):");
    for (int i = 0; i < shares.size(); i++) {
      Share s = shares.get(i);
      System.out.println("   " + (i + 1) + ". " + s.getStock().getSymbol() + " - " + s.getQuantity()
          + " shares @ " + s.getPurchasePrice() + " (current: " + s.getStock().getSalesPrice() + ")");
    }
    System.out.println("Enter the number of the holding to sell (1 to " + shares.size() + "): ");
    try {
      int index = input.nextInt();
      if (index < 1 || index > shares.size()) {
        System.out.println(INVALID_INPUT);
        return;
      }
      Share toSell = shares.get(index - 1);
      exchange.sell(toSell, player);
      System.out.println("-> Successfully sold " + toSell.getQuantity() + " share(s) of "
          + toSell.getStock().getSymbol() + ".");
    } catch (InputMismatchException e) {
      System.out.println(INVALID_INPUT);
      input.nextLine();
    } catch (ShareNotFoundException e) {
      System.out.println("-> " + e.getMessage());
    }
  }

  private static void advanceWeek() {
    System.out.println("-> Advance week not yet implemented.");
  }

  private static void viewTransactions() {
    System.out.println("-> View transactions not yet implemented.");
  }
}
