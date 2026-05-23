package controller;

import model.core.player.Player;

import static util.Validator.checkNotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import model.core.asset.Stock;
import model.session.ActiveSession;
import model.session.SessionService;
import view.components.notification.LevelUpNotificationObserver;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;

/**
 * Orchestrates session-scoped workspace state and child page controllers.
 */
public class WorkspaceController {

  private static final int TICKER_PREVIEW_MAX = 8;

  private final ActiveSession session;
  private final SessionService sessionService;
  private final NotificationService notifications;
  private final NotificationsController notificationsTab;
  private final PortfolioController portfolio;
  private final StocksController stocks;
  private final StockDetailController stockDetail;
  private final FundsController funds;
  private final FundDetailController fundDetail;
  private final SavingsController savings;
  private final SavedRunsController savedRuns;
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
    this.notificationsTab = new NotificationsController(notifications);
    Path avatarPath = sessionService.avatarPath(session.normalizedUsername());
    this.portfolio = new PortfolioController(session.exchange(), session.player(), avatarPath);
    this.stocks = new StocksController(session.exchange());
    this.stockDetail = new StockDetailController(session.exchange());
    this.funds = new FundsController(session.exchange());
    this.fundDetail = new FundDetailController();
    this.savings =
        new SavingsController(session.exchange(), session.player(), notifications);
    this.savedRuns = new SavedRunsController(sessionService);
    this.leaderboard = new LeaderboardController(sessionService);
    this.learningHub = new LearningHubController();
    this.quiz = new QuizController();
    showLoadedNotifications();
    session.player().addObserver(
        new LevelUpNotificationObserver(notifications, session.player().getPlayerLevel()));
  }

  public ActiveSession getSession() {
    return session;
  }

  public SessionService getSessionService() {
    return sessionService;
  }

  public NotificationService getNotifications() {
    return notifications;
  }

  public NotificationsController getNotificationsTab() {
    return notificationsTab;
  }

  public PortfolioController getPortfolio() {
    return portfolio;
  }

  public StocksController getStocks() {
    return stocks;
  }

  public StockDetailController getStockDetail() {
    return stockDetail;
  }

  public FundsController getFunds() {
    return funds;
  }

  public FundDetailController getFundDetail() {
    return fundDetail;
  }

  public SavingsController getSavings() {
    return savings;
  }

  public SavedRunsController getSavedRuns() {
    return savedRuns;
  }

  public LeaderboardController getLeaderboard() {
    return leaderboard;
  }

  public LearningHubController getLearningHub() {
    return learningHub;
  }

  public QuizController getQuiz() {
    return quiz;
  }

  public ProfileEditorController createProfileEditorController() {
    return new ProfileEditorController(sessionService);
  }

  public String getSessionSummary() {
    return "Logged in as "
        + session.player().getName()
        + " ("
        + session.username()
        + ") | Trading day "
        + session.exchange().getDay();
  }

  public Path getAvatarPath() {
    return sessionService.avatarPath(session.normalizedUsername());
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
    List<Stock> stockList = session.exchange().findStocks("");
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
