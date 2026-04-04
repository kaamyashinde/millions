package cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import model.Exchange;
import model.Player;
import model.Stock;
import model.Share;
import model.analysis.MetricStatus;
import model.analysis.MetricValue;
import model.analysis.PerformanceComparison;
import model.analysis.PortfolioPerformanceService;
import model.exception.InsufficientFundsException;
import model.exception.InsufficientSharesException;
import model.exception.ShareNotFoundException;
import model.fund.Fund;
import model.fund.FundComponent;
import model.persistence.MarketData;
import model.persistence.MarketDataLoader;
import model.savings.RegularSavingsPlan;
import model.savings.RegularSavingsProcessor;
import model.savings.SavingsInstallmentMode;
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

  private static final String DEMO_MARKET_DATA_RESOURCE = "/data/demo-stocks.csv";

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
  private static final int ADD_SAVINGS = 10;
  private static final int LIST_SAVINGS = 11;
  private static final int REMOVE_SAVINGS = 12;
  private static final int EDIT_SAVINGS = 13;
  private static final int LIST_FUNDS = 14;
  private static final int SEARCH_FUNDS = 15;
  private static final int VIEW_FUND_DETAILS = 16;
  private static final int INVALID_MENU_CHOICE = -1;

  private static final Scanner input = new Scanner(System.in);
  private static final PortfolioPerformanceService portfolioPerformanceService =
      new PortfolioPerformanceService();

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
   * Initialises the UserInterface from the bundled demo market-data file.
   */
  private static void init() {
    MarketData marketData = MarketDataLoader.loadFromResource(
        UserInterface.class,
        DEMO_MARKET_DATA_RESOURCE);
    if (marketData.stocks().isEmpty()) {
      throw new IllegalStateException("Could not load demo market data from "
          + DEMO_MARKET_DATA_RESOURCE);
    }
    exchange = new Exchange("NYSE", marketData.stocks(), marketData.funds());
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
    System.out.println(I18n.get("menu.option.savings.add"));
    System.out.println(I18n.get("menu.option.savings.list"));
    System.out.println(I18n.get("menu.option.savings.remove"));
    System.out.println(I18n.get("menu.option.savings.edit"));
    System.out.println(I18n.get("menu.option.funds.list"));
    System.out.println(I18n.get("menu.option.funds.search"));
    System.out.println(I18n.get("menu.option.funds.view"));
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
      case ADD_SAVINGS -> addSavingsPlan();
      case LIST_SAVINGS -> listSavingsPlans();
      case REMOVE_SAVINGS -> removeSavingsPlan();
      case EDIT_SAVINGS -> editSavingsPlan();
      case LIST_FUNDS -> listFunds();
      case SEARCH_FUNDS -> searchFunds();
      case VIEW_FUND_DETAILS -> viewFundDetails();
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
      printPerformanceComparison();
      return;
    }
    System.out.println(I18n.get("portfolio.header"));
    player.getPortfolio().getShares().forEach(share -> System.out.println(
        I18n.format(
            "portfolio.line",
            share.getAsset().getSymbol(),
            share.getAsset().getDisplayName(),
            share.getQuantity(),
            share.getPurchasePrice(),
            share.getAsset().getSalesPrice(),
            share.getAsset().getAssetType())));
    printPerformanceComparison();
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
   * Lists all funds available on the exchange with symbol, name, and derived current price.
   */
  private static void listFunds() {
    List<Fund> funds = exchange.findFunds("");
    if (funds.isEmpty()) {
      System.out.println(I18n.get("funds.none"));
      return;
    }
    System.out.println(I18n.format("funds.onExchange", exchange.getName()));
    funds.forEach(fund -> System.out.println(
        I18n.format("fund.line", fund.getSymbol(), fund.getDisplayName(), fund.getSalesPrice())));
  }

  /**
   * Searches listed funds by symbol or fund name.
   */
  private static void searchFunds() {
    input.nextLine();
    System.out.println(I18n.get("prompt.search.funds"));
    String term = input.nextLine().trim();
    List<Fund> results = exchange.findFunds(term);
    if (results.isEmpty()) {
      System.out.println(I18n.format("fund.search.none", term));
      return;
    }
    System.out.println(I18n.format("fund.search.found", results.size()));
    results.forEach(fund -> System.out.println(
        I18n.format("fund.line", fund.getSymbol(), fund.getDisplayName(), fund.getSalesPrice())));
  }

  /**
   * Shows one fund and its underlying stock weights.
   */
  private static void viewFundDetails() {
    input.nextLine();
    System.out.println(I18n.get("prompt.fund.symbol"));
    String symbol = input.nextLine().trim().toUpperCase();
    if (symbol.isEmpty()) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    Fund fund = exchange.getFund(symbol);
    if (fund == null) {
      System.out.println(I18n.format("error.fundNotOnExchange", symbol));
      return;
    }
    System.out.println(I18n.format(
        "fund.details.header",
        fund.getSymbol(),
        fund.getDisplayName(),
        fund.getSalesPrice()));
    for (FundComponent component : fund.getComponents()) {
      System.out.println(I18n.format(
          "fund.details.line",
          component.stock().getSymbol(),
          component.stock().getCompany(),
          component.weight()));
    }
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
    if (!exchange.hasAsset(symbol)) {
      System.out.println(I18n.format("error.assetNotOnExchange", symbol));
      return;
    }
    System.out.println(I18n.get("prompt.buy.mode"));
    int buyMode;
    try {
      buyMode = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    try {
      if (buyMode == 1) {
        System.out.println(I18n.get("prompt.quantity.buy"));
        BigDecimal quantity = new BigDecimal(input.nextLine().trim());
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
          System.out.println(
              I18n.get("invalid.input") + " " + I18n.get("validation.quantity.positive"));
          return;
        }
        exchange.buy(symbol, quantity, player);
        System.out.println(I18n.format("buy.success", quantity, symbol));
        printPerformanceComparison();
      } else if (buyMode == 2) {
        System.out.println(I18n.get("prompt.maxSpend.buy"));
        BigDecimal maxSpend = new BigDecimal(input.nextLine().trim());
        if (maxSpend.compareTo(BigDecimal.ZERO) <= 0) {
          System.out.println(I18n.get("invalid.input"));
          return;
        }
        exchange.buyUpToBudget(symbol, maxSpend, player);
        System.out.println(I18n.format("buy.success.budget", symbol, maxSpend));
        printPerformanceComparison();
      } else {
        System.out.println(I18n.get("invalid.input"));
      }
    } catch (NumberFormatException e) {
      System.out.println(I18n.get("invalid.input"));
    } catch (InsufficientFundsException ignored) {
      System.out.println(I18n.get("error.insufficientFunds"));
    } catch (IllegalArgumentException e) {
      System.out.println(I18n.get("error.invalidArgument"));
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
    input.nextLine();
    System.out.println(I18n.get("prompt.sell.mode"));
    int sellMode;
    try {
      sellMode = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    try {
      switch (sellMode) {
        case 1 -> sellWholeHolding(shares);
        case 2 -> sellByQuantityCli();
        case 3 -> sellByTargetNetCli();
        default -> System.out.println(I18n.get("invalid.input"));
      }
    } catch (ShareNotFoundException e) {
      System.out.println(I18n.format("error.shareNotFound", e.getStockSymbol(), e.getPlayerName()));
    } catch (InsufficientSharesException e) {
      System.out.println(I18n.format("error.insufficientShares", e.getSymbol()));
    } catch (NumberFormatException e) {
      System.out.println(I18n.get("invalid.input"));
    } catch (IllegalArgumentException e) {
      System.out.println(I18n.get("error.invalidArgument"));
    }
  }

  private static void sellWholeHolding(List<Share> shares) {
    System.out.println(I18n.get("holdings.header"));
    for (int i = 0; i < shares.size(); i++) {
      Share s = shares.get(i);
      System.out.println(I18n.format(
          "holding.line",
          i + 1,
          s.getAsset().getSymbol(),
          s.getQuantity(),
          s.getPurchasePrice(),
          s.getAsset().getSalesPrice(),
          s.getAsset().getAssetType()));
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
          toSell.getAsset().getSymbol()));
      printPerformanceComparison();
    } catch (InputMismatchException e) {
      System.out.println(I18n.get("invalid.input"));
      input.nextLine();
    }
  }

  /**
   * Prompts for symbol and quantity and sells that many shares in FIFO order via
   * {@link Exchange#sellByQuantity}.
   */
  private static void sellByQuantityCli() {
    System.out.println(I18n.get("prompt.symbol"));
    String symbol = input.nextLine().trim().toUpperCase();
    if (symbol.isEmpty() || !exchange.hasAsset(symbol)) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("prompt.sell.quantity"));
    BigDecimal qty = new BigDecimal(input.nextLine().trim());
    if (qty.signum() <= 0) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    List<Transaction> txs = exchange.sellByQuantity(symbol, qty, player);
    System.out.println(I18n.format("sell.success.multi", txs.size(), symbol));
    printPerformanceComparison();
  }

  /**
   * Prompts for symbol and target net proceeds and sells FIFO slices via
   * {@link Exchange#sellUpToTargetNet}.
   */
  private static void sellByTargetNetCli() {
    System.out.println(I18n.get("prompt.symbol"));
    String symbol = input.nextLine().trim().toUpperCase();
    if (symbol.isEmpty() || !exchange.hasAsset(symbol)) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("prompt.sell.targetNet"));
    BigDecimal target = new BigDecimal(input.nextLine().trim());
    if (target.signum() <= 0) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    List<Transaction> txs = exchange.sellUpToTargetNet(symbol, target, player);
    System.out.println(I18n.format("sell.success.multi", txs.size(), symbol));
    printPerformanceComparison();
  }

  /**
   * Advances the exchange one trading day, then runs {@link RegularSavingsProcessor} for the
   * skipped interval and prints any symbols whose installments were skipped for lack of funds.
   */
  private static void advanceDay() {
    int before = exchange.getDay();
    exchange.advance();
    System.out.println(I18n.format("day.advanced", exchange.getDay()));
    if (player != null) {
      List<String> skipped =
          RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
      for (String sym : skipped) {
        System.out.println(I18n.format("savings.warning.skip", sym));
      }
      printPerformanceComparison();
    }
  }

  /**
   * Prints the player's portfolio metrics beside the market benchmark metrics.
   */
  private static void printPerformanceComparison() {
    PerformanceComparison comparison = portfolioPerformanceService.compareAgainstMarket(player, exchange);
    String rowFormat = "   %-18s %-26s %-26s%n";
    System.out.println(I18n.get("performance.header"));
    System.out.printf(
        rowFormat,
        I18n.get("performance.column.metric"),
        I18n.get("performance.column.portfolio"),
        I18n.get("performance.column.market"));
    printMetricRow(
        rowFormat,
        I18n.get("performance.metric.return"),
        comparison.portfolio().returnPercent(),
        comparison.benchmark().returnPercent(),
        true);
    printMetricRow(
        rowFormat,
        I18n.get("performance.metric.volatility"),
        comparison.portfolio().volatility(),
        comparison.benchmark().volatility(),
        true);
    printMetricRow(
        rowFormat,
        I18n.get("performance.metric.sharpe"),
        comparison.portfolio().sharpeRatio(),
        comparison.benchmark().sharpeRatio(),
        false);
  }

  /**
   * Prints one side-by-side metric row.
   *
   * @param rowFormat printf row format
   * @param label metric label
   * @param portfolioMetric player metric
   * @param benchmarkMetric market metric
   * @param percentDisplay whether the metric should be formatted as a percent
   */
  private static void printMetricRow(
      String rowFormat,
      String label,
      MetricValue portfolioMetric,
      MetricValue benchmarkMetric,
      boolean percentDisplay) {
    System.out.printf(
        rowFormat,
        label,
        formatMetricValue(portfolioMetric, percentDisplay),
        formatMetricValue(benchmarkMetric, percentDisplay));
  }

  /**
   * Formats one metric value for CLI display.
   *
   * @param metric metric to format
   * @param percentDisplay whether the metric should be formatted as a percent
   * @return user-facing metric text
   */
  private static String formatMetricValue(MetricValue metric, boolean percentDisplay) {
    if (!metric.isAvailable()) {
      return I18n.get(metricStatusKey(metric.status()));
    }
    BigDecimal value = metric.value();
    if (percentDisplay) {
      return value.multiply(BigDecimal.valueOf(100))
          .setScale(2, RoundingMode.HALF_UP)
          .toPlainString() + "%";
    }
    return value.setScale(3, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * Maps analysis-layer metric statuses to CLI translation keys.
   *
   * @param status unavailable metric status
   * @return translation key for that status
   */
  private static String metricStatusKey(MetricStatus status) {
    return switch (status) {
      case AVAILABLE -> throw new IllegalArgumentException("Available metrics do not need a status key.");
      case NO_TRADES -> "performance.unavailable.noTrades";
      case INSUFFICIENT_HISTORY -> "performance.unavailable.history";
      case ZERO_VOLATILITY -> "performance.unavailable.zeroVolatility";
    };
  }

  /**
   * Interactive flow to create a {@link RegularSavingsPlan} and attach it to the player.
   */
  private static void addSavingsPlan() {
    if (isPlayerMissing()) {
      return;
    }
    input.nextLine();
    System.out.println(I18n.get("savings.prompt.symbol"));
    String symbol = input.nextLine().trim().toUpperCase();
    if (symbol.isEmpty() || !exchange.hasAsset(symbol)) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("savings.prompt.mode"));
    int modeChoice;
    try {
      modeChoice = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    SavingsInstallmentMode mode =
        modeChoice == 1 ? SavingsInstallmentMode.FIXED_SHARES : SavingsInstallmentMode.BUDGET;
    if (modeChoice != 1 && modeChoice != 2) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    BigDecimal amount;
    try {
      if (mode == SavingsInstallmentMode.FIXED_SHARES) {
        System.out.println(I18n.get("savings.prompt.amountShares"));
        amount = new BigDecimal(input.nextLine().trim());
      } else {
        System.out.println(I18n.get("savings.prompt.amountBudget"));
        amount = new BigDecimal(input.nextLine().trim());
      }
      if (amount.signum() <= 0) {
        System.out.println(I18n.get("invalid.input"));
        return;
      }
    } catch (NumberFormatException e) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("savings.prompt.frequency"));
    int freq;
    try {
      freq = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    int interval = readIntervalForFrequencyChoice(freq);
    if (interval < 0) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    RegularSavingsPlan plan =
        new RegularSavingsPlan(symbol, mode, amount, interval, exchange.getDay());
    player.addRegularSavingsPlan(plan);
    System.out.println(I18n.format("savings.added", symbol, plan.getNextDueDay()));
  }

  /**
   * Prints all regular savings plans (1-based index, mode, amounts, next due day).
   */
  private static void listSavingsPlans() {
    if (isPlayerMissing()) {
      return;
    }
    input.nextLine();
    List<RegularSavingsPlan> plans = player.getRegularSavingsPlans();
    if (plans.isEmpty()) {
      System.out.println(I18n.get("savings.list.empty"));
      return;
    }
    for (int i = 0; i < plans.size(); i++) {
      RegularSavingsPlan p = plans.get(i);
      System.out.println(I18n.format(
          "savings.list.line",
          i + 1,
          p.getSymbol(),
          p.getMode(),
          p.getAmount(),
          p.getIntervalDays(),
          p.getNextDueDay(),
          p.isActive()));
    }
  }

  /**
   * Resolves trading days between installments from a frequency menu choice, after the frequency
   * line has been read with {@code nextInt} and consumed with {@code nextLine}.
   *
   * @param freq 1 = weekly, 2 = biweekly, 3 = monthly, 4 = custom (reads another line)
   * @return positive interval, or -1 if invalid
   */
  private static int readIntervalForFrequencyChoice(int freq) {
    if (freq == 1) {
      return 5;
    }
    if (freq == 2) {
      return 10;
    }
    if (freq == 3) {
      return 22;
    }
    if (freq == 4) {
      System.out.println(I18n.get("savings.prompt.customDays"));
      try {
        int custom = Integer.parseInt(input.nextLine().trim());
        if (custom <= 0) {
          return -1;
        }
        return custom;
      } catch (NumberFormatException e) {
        return -1;
      }
    }
    return -1;
  }

  /**
   * Prompts for a plan index and updates mode, amount, interval, and active; symbol is unchanged.
   */
  private static void editSavingsPlan() {
    if (isPlayerMissing()) {
      return;
    }
    input.nextLine();
    List<RegularSavingsPlan> plans = player.getRegularSavingsPlans();
    if (plans.isEmpty()) {
      System.out.println(I18n.get("savings.list.empty"));
      return;
    }
    for (int i = 0; i < plans.size(); i++) {
      RegularSavingsPlan p = plans.get(i);
      System.out.println(I18n.format(
          "savings.list.line",
          i + 1,
          p.getSymbol(),
          p.getMode(),
          p.getAmount(),
          p.getIntervalDays(),
          p.getNextDueDay(),
          p.isActive()));
    }
    System.out.println(I18n.format("savings.prompt.editIndex", plans.size()));
    int idx;
    try {
      idx = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    if (idx < 1 || idx > plans.size()) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    RegularSavingsPlan plan = plans.get(idx - 1);
    System.out.println(I18n.format("savings.prompt.symbolLocked", plan.getSymbol()));
    System.out.println(I18n.get("savings.prompt.mode"));
    int modeChoice;
    try {
      modeChoice = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    if (modeChoice != 1 && modeChoice != 2) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    SavingsInstallmentMode mode =
        modeChoice == 1 ? SavingsInstallmentMode.FIXED_SHARES : SavingsInstallmentMode.BUDGET;
    BigDecimal amount;
    try {
      if (mode == SavingsInstallmentMode.FIXED_SHARES) {
        System.out.println(I18n.get("savings.prompt.amountShares"));
        amount = new BigDecimal(input.nextLine().trim());
      } else {
        System.out.println(I18n.get("savings.prompt.amountBudget"));
        amount = new BigDecimal(input.nextLine().trim());
      }
      if (amount.signum() <= 0) {
        System.out.println(I18n.get("invalid.input"));
        return;
      }
    } catch (NumberFormatException e) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("savings.prompt.frequency"));
    int freq;
    try {
      freq = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    int interval = readIntervalForFrequencyChoice(freq);
    if (interval < 0) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    System.out.println(I18n.get("savings.prompt.active"));
    int activeChoice;
    try {
      activeChoice = input.nextInt();
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    if (activeChoice != 0 && activeChoice != 1) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    input.nextLine();
    plan.setMode(mode);
    try {
      plan.setAmount(amount);
      plan.setIntervalDays(interval);
    } catch (IllegalArgumentException | NullPointerException ex) {
      System.out.println(I18n.get("invalid.input"));
      return;
    }
    plan.setActive(activeChoice == 1);
    System.out.println(I18n.get("savings.updated"));
  }

  /**
   * Lists plans then prompts for a 1-based index to remove via {@link Player#removeRegularSavingsPlanAt(int)}.
   */
  private static void removeSavingsPlan() {
    if (isPlayerMissing()) {
      return;
    }
    input.nextLine();
    List<RegularSavingsPlan> plans = player.getRegularSavingsPlans();
    if (plans.isEmpty()) {
      System.out.println(I18n.get("savings.list.empty"));
      return;
    }
    listSavingsPlans();
    System.out.println(I18n.format("savings.prompt.removeIndex", plans.size()));
    try {
      int idx = input.nextInt();
      if (player.removeRegularSavingsPlanAt(idx)) {
        System.out.println(I18n.get("savings.removed"));
      } else {
        System.out.println(I18n.get("invalid.input"));
      }
    } catch (InputMismatchException e) {
      input.nextLine();
      System.out.println(I18n.get("invalid.input"));
    }
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
      String sym = t.getShare().getAsset().getSymbol();
      String qty = t.getShare().getQuantity().toString();
      System.out.println(I18n.format("transaction.line", t.getDay(), type, sym, qty));
    });
  }
}
