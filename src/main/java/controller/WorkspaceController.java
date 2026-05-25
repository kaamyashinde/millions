package controller;

import model.core.player.Player;

import static util.Validator.checkNotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import model.core.asset.Stock;
import model.session.ActiveSession;
import model.session.SessionService;
import model.trading.savings.RegularSavingsProcessor;
import util.Validator;
import view.components.notification.LevelUpNotificationObserver;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;

/**
 * Orchestrates session-scoped workspace state and child page controllers.
 *
 * <p>This is the composition root for the authenticated JavaFX workspace: it owns shared services,
 * creates child controllers, advances trading days, and refreshes session-bound views.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-04-30
 */
public class WorkspaceController {

  /** Maximum trading days that can be skipped in one action from the workspace header. */
  public static final int MAX_SKIP_TRADING_DAYS = 30;

  private static final int TICKER_PREVIEW_MAX = 8;

  private final ActiveSession session;
  private final SessionService sessionService;
  private final NotificationService notifications;
  private final PortfolioController portfolio;
  private final StocksController stocks;
  private final StockDetailController stockDetail;
  private final FundsController funds;
  private final FundDetailController fundDetail;
  private final SavingsController savings;
  private final TradingController trading;
  private final ExitGameController exitGame;
  private final LeaderboardController leaderboard;
  private final LearningHubController learningHub;
  private final QuizController quiz;

  /**
   * Builds all child controllers for one active session.
   *
   * @param session active session
   * @param sessionService session service
   */
  public WorkspaceController(ActiveSession session, SessionService sessionService) {
    checkNotNull(session, "session");
    checkNotNull(sessionService, "sessionService");
    this.session = session;
    this.sessionService = sessionService;
    this.notifications = new NotificationService();
    Path avatarPath = sessionService.avatarPath(session.normalizedUsername());
    this.portfolio = new PortfolioController(session.exchange(), session.player(), avatarPath);
    this.stocks = new StocksController(session.exchange());
    this.stockDetail = new StockDetailController(session.exchange());
    this.funds = new FundsController(session.exchange());
    this.fundDetail = new FundDetailController();
    this.savings = new SavingsController(session.exchange(), session.player());
    this.trading =
        new TradingController(session.exchange(), session.player(), notifications);
    this.exitGame = new ExitGameController(sessionService);
    this.leaderboard = new LeaderboardController(sessionService);
    this.learningHub = new LearningHubController();
    this.quiz = new QuizController();
    showLoadedNotifications();
    session.player().addObserver(
        new LevelUpNotificationObserver(notifications, session.player().getPlayerLevel()));
  }

  /**
   * Returns the active session represented by this workspace.
   *
   * @return active session
   */
  public ActiveSession getSession() {
    return session;
  }

  /**
   * Exposes the session service shared by workspace components.
   *
   * @return session service shared by workspace dialogs and controllers
   */
  public SessionService getSessionService() {
    return sessionService;
  }

  /**
   * Exposes the workspace notification service.
   *
   * @return notification service for workspace toasts
   */
  public NotificationService getNotifications() {
    return notifications;
  }

  /**
   * Returns the portfolio page controller.
   *
   * @return portfolio page controller
   */
  public PortfolioController getPortfolio() {
    return portfolio;
  }

  /**
   * Returns the stocks page controller.
   *
   * @return stocks page controller
   */
  public StocksController getStocks() {
    return stocks;
  }

  /**
   * Returns the stock detail controller.
   *
   * @return stock detail controller
   */
  public StockDetailController getStockDetail() {
    return stockDetail;
  }

  /**
   * Returns the funds page controller.
   *
   * @return funds page controller
   */
  public FundsController getFunds() {
    return funds;
  }

  /**
   * Returns the fund detail controller.
   *
   * @return fund detail controller
   */
  public FundDetailController getFundDetail() {
    return fundDetail;
  }

  /**
   * Returns the savings page controller.
   *
   * @return savings page controller
   */
  public SavingsController getSavings() {
    return savings;
  }

  /**
   * Returns the trading controller.
   *
   * @return trading controller shared by asset detail views
   */
  public TradingController getTrading() {
    return trading;
  }

  /**
   * Returns the exit-game controller.
   *
   * @return exit-game controller
   */
  public ExitGameController getExitGame() {
    return exitGame;
  }

  /**
   * Returns the leaderboard page controller.
   *
   * @return leaderboard page controller
   */
  public LeaderboardController getLeaderboard() {
    return leaderboard;
  }

  /**
   * Returns the learning hub controller.
   *
   * @return learning hub controller
   */
  public LearningHubController getLearningHub() {
    return learningHub;
  }

  /**
   * Returns the quiz controller.
   *
   * @return quiz controller
   */
  public QuizController getQuiz() {
    return quiz;
  }

  /**
   * Creates a fresh profile editor controller for the active session service.
   *
   * @return profile editor controller
   */
  public ProfileEditorController createProfileEditorController() {
    return new ProfileEditorController(sessionService);
  }

  /**
   * Builds a concise summary of the active session.
   *
   * @return concise text describing the active session and trading day
   */
  public String getSessionSummary() {
    return "Logged in as "
        + session.player().getName()
        + " ("
        + session.username()
        + ") | Trading day "
        + session.exchange().getDay();
  }

  /**
   * Resolves the avatar path for the active profile.
   *
   * @return avatar path for the active profile
   */
  public Path getAvatarPath() {
    return sessionService.avatarPath(session.normalizedUsername());
  }

  /**
   * Advances the exchange by the requested number of trading days, processes installments, and
   * surfaces notifications.
   *
   * @param daysText number of days to skip (validated 1–{@link #MAX_SKIP_TRADING_DAYS})
   * @return comma-separated symbols skipped for insufficient funds, or empty
   * @throws IllegalArgumentException when {@code daysText} is invalid
   */
  public String advanceTradingDays(String daysText) {
    int days =
        Validator.parsePositiveInt(daysText, "trading days", MAX_SKIP_TRADING_DAYS);
    var exchange = session.exchange();
    var player = session.player();
    int before = exchange.getDay();
    exchange.advance(days);
    List<String> skipped =
        RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
    savings.refreshPlans();
    exchange.getLastMarketEvent().ifPresent(event -> notifications.show(
        ToastMode.INFO,
        event.title(),
        event.description()));
    for (String sym : skipped) {
      notifications.show(
          ToastMode.WARNING,
          "Regular savings skipped",
          "Insufficient funds for " + sym + ".");
    }
    return String.join(", ", skipped);
  }

  /**
   * Refreshes all session-bound child controllers.
   */
  public void refreshAll() {
    portfolio.refresh();
    stocks.refresh();
    funds.refresh();
    savings.refreshPlans();
  }

  /**
   * Clears transient notification state before the workspace is discarded.
   */
  public void dispose() {
    notifications.clear();
  }

  private void showLoadedNotifications() {
    List<Stock> stockList = session.exchange().listings().findStocks("");
    String tickers = stockList.stream()
        .map(Stock::getSymbol)
        .limit(TICKER_PREVIEW_MAX)
        .collect(Collectors.joining(", "));
    if (stockList.size() > TICKER_PREVIEW_MAX) {
      tickers = tickers + ", …";
    }
    notifications.show(
        ToastMode.INFO,
        "Game loaded",
        session.exchange().getName()
            + " · "
            + stockList.size()
            + " stock(s): "
            + tickers);
    notifications.show(
        ToastMode.SUCCESS,
        "Player ready",
        session.player().getName() + " · balance " + session.player().getMoney().toPlainString());
  }
}
