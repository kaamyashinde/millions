package cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import model.Exchange;
import model.Player;
import model.Stock;

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

  private static void setUpPlayer() {
    // TODO: implement
    System.out.println("-> Set up player not yet implemented.");
  }

  private static void viewBalance() {
    System.out.println("-> View balance not yet implemented.");
  }

  private static void viewPortfolio() {
    System.out.println("-> View portfolio not yet implemented.");
  }

  private static void listStocks() {
    System.out.println("-> List stocks not yet implemented.");
  }

  private static void searchStocks() {
    System.out.println("-> Search stocks not yet implemented.");
  }

  private static void buyShares() {
    System.out.println("-> Buy shares not yet implemented.");
  }

  private static void sellShares() {
    System.out.println("-> Sell shares not yet implemented.");
  }

  private static void advanceWeek() {
    System.out.println("-> Advance week not yet implemented.");
  }

  private static void viewTransactions() {
    System.out.println("-> View transactions not yet implemented.");
  }
}
