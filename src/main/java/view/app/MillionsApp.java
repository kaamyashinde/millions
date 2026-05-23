package view.app;

import static view.app.events.WorkspaceEventType.LEADERBOARD_CHANGED;
import static view.app.events.WorkspaceEventType.MARKET_CHANGED;
import static view.app.events.WorkspaceEventType.PORTFOLIO_CHANGED;
import static view.app.events.WorkspaceEventType.PROFILE_CHANGED;
import static view.app.events.WorkspaceEventType.SAVINGS_CHANGED;
import static view.app.events.WorkspaceEventType.TRANSACTIONS_CHANGED;

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import java.util.Optional;
import model.session.ActiveSession;
import model.exception.auth.AuthenticationException;
import model.exception.auth.DuplicateUsernameException;
import model.exception.market.MarketDataImportException;
import model.exception.persistence.PersistenceException;
import model.session.SessionService;
import model.session.SessionServiceFactory;
import controller.WorkspaceController;
import view.app.events.WorkspaceEventBus;
import view.dialogs.ProfileEditorDialog;
import view.layout.WorkspaceLayout;
import view.pages.auth.LoginPage;
import view.pages.auth.RegisterPage;
import view.pages.funds.FundsPage;
import view.pages.leaderboard.LeaderboardPage;
import view.pages.learning.LearningHubPage;
import view.pages.notifications.NotificationsPage;
import view.pages.portfolio.PlayerPortfolioPage;
import view.pages.quiz.QuizLauncherPage;
import view.pages.savings.SavingsPage;
import view.pages.stocks.StocksPage;
import view.pages.transactions.TransactionHistoryPage;
import view.theme.ThemeStyles;

/**
 * JavaFX entry point for the Millions application.
 *
 * <p>Boots the session service and displays the login page as the first screen.
 * Navigation between login and register swaps the scene root in place.
 * After a successful login or registration the application proceeds to the main workspace.
 */
public class MillionsApp extends Application {

  private static final String EXCHANGE_NAME = "NYSE";
  private static final String MARKET_DATA_RESOURCE = "/data/demo-stocks.csv";
  private static final int WINDOW_WIDTH = 1100;
  private static final int WINDOW_HEIGHT = 720;

  private SessionService sessionService;
  private Stage primaryStage;
  private Scene scene;
  private WorkspaceController currentWorkspace;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    this.sessionService = createSessionService();

    LoginPage loginPage = buildLoginPage();
    scene = new Scene(loginPage, WINDOW_WIDTH, WINDOW_HEIGHT);
    ThemeStyles.install(scene);

    stage.setScene(scene);
    stage.setTitle("Millions");
    stage.setOnCloseRequest(_ -> sessionService.saveActiveSession());
    stage.show();
  }

  /**
   * Launches the Millions JavaFX application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  private LoginPage buildLoginPage() {
    LoginPage loginPage = new LoginPage(
        this::handleLogin,
        () -> scene.setRoot(buildRegisterPage()));
    return loginPage;
  }

  private RegisterPage buildRegisterPage() {
    RegisterPage registerPage = new RegisterPage(
        this::handleRegistration,
        () -> scene.setRoot(buildLoginPage()));
    return registerPage;
  }

  private void handleLogin(String username, String pin) {
    LoginPage loginPage = (LoginPage) scene.getRoot();
    try {
      ActiveSession session = sessionService.login(username, pin.toCharArray());
      onSessionStarted(session);
    } catch (AuthenticationException e) {
      loginPage.setStatus("Invalid username or PIN.");
    } catch (PersistenceException e) {
      loginPage.setStatus("Profile data could not be read. Reset this profile or restore a backup.");
    } catch (IllegalArgumentException e) {
      loginPage.setStatus(mapValidationMessage(e.getMessage()));
    }
  }

  private void handleRegistration(
      String username,
      String pin,
      String startingMoneyText,
      Optional<java.nio.file.Path> marketDataFile) {
    RegisterPage registerPage = (RegisterPage) scene.getRoot();
    try {
      BigDecimal startingMoney = new BigDecimal(startingMoneyText.trim());
      ActiveSession session = sessionService.register(
          username, pin.toCharArray(), startingMoney, marketDataFile);
      onSessionStarted(session);
    } catch (NumberFormatException e) {
      registerPage.setStatus("Starting money must be a valid number.");
    } catch (DuplicateUsernameException e) {
      registerPage.setStatus("That username is already taken.");
    } catch (MarketDataImportException e) {
      registerPage.setMarketDataStatus(e.getMessage());
    } catch (IllegalArgumentException e) {
      registerPage.setStatus(mapValidationMessage(e.getMessage()));
    }
  }

  private void onSessionStarted(ActiveSession session) {
    if (currentWorkspace != null) {
      currentWorkspace.dispose();
    }
    currentWorkspace = new WorkspaceController(session, sessionService);
    WorkspaceLayout workspace = buildWorkspace(currentWorkspace);
    scene.setRoot(workspace);
    primaryStage.setTitle("Millions — " + session.username());
  }

  private WorkspaceLayout buildWorkspace(WorkspaceController ctrl) {
    Runnable onProfileDeleted = () -> onProfileDeleted(ctrl);
    WorkspaceEventBus events = new WorkspaceEventBus();
    TabPane tabs = buildWorkspaceTabs(ctrl, onProfileDeleted, events);
    WorkspaceLayout[] ref = new WorkspaceLayout[1];
    Runnable onProfileSaved = () -> {
      sessionService.saveActiveSession();
      events.publish(PROFILE_CHANGED, LEADERBOARD_CHANGED);
    };
    WorkspaceLayout workspace = new WorkspaceLayout(
        ctrl.getNotifications(),
        tabs,
        () -> {
          var window = ref[0].getScene() != null ? ref[0].getScene().getWindow() : null;
          ProfileEditorDialog.show(
              window,
              ctrl.createProfileEditorController(),
              ctrl.getExitGame(),
              onProfileSaved,
              onProfileDeleted);
        },
        () -> { /* help: placeholder */ },
        () -> switchUser(ctrl, ref[0]),
        () -> logout(ctrl),
        days -> {
          ctrl.advanceTradingDays(String.valueOf(days));
          sessionService.saveActiveSession();
          events.publish(
              MARKET_CHANGED,
              SAVINGS_CHANGED,
              PORTFOLIO_CHANGED,
              TRANSACTIONS_CHANGED,
              LEADERBOARD_CHANGED);
        });
    ref[0] = workspace;
    workspace.setSessionSummary(ctrl.getSessionSummary());
    workspace.loadHeaderAvatar(ctrl.getAvatarPath());
    registerHeaderObservers(events, workspace, ctrl);
    return workspace;
  }

  private void onProfileDeleted(WorkspaceController ctrl) {
    ctrl.dispose();
    currentWorkspace = null;
    scene.setRoot(buildLoginPage());
    primaryStage.setTitle("Millions");
  }

  private TabPane buildWorkspaceTabs(
      WorkspaceController ctrl,
      Runnable onProfileDeleted,
      WorkspaceEventBus events) {
    ActiveSession session = ctrl.getSession();
    SessionService svc = ctrl.getSessionService();

    Runnable onTradeComplete = () -> {
      sessionService.saveActiveSession();
      events.publish(PORTFOLIO_CHANGED, TRANSACTIONS_CHANGED, LEADERBOARD_CHANGED);
    };
    Runnable onSavingsChanged = () -> {
      sessionService.saveActiveSession();
      events.publish(
          SAVINGS_CHANGED,
          PORTFOLIO_CHANGED,
          TRANSACTIONS_CHANGED,
          LEADERBOARD_CHANGED);
    };

    PlayerPortfolioPage portfolioPage = new PlayerPortfolioPage(
        ctrl.getPortfolio(),
        ctrl.getTrading(),
        ctrl.getExitGame(),
        onTradeComplete,
        onProfileDeleted);
    StocksPage stocksPage = new StocksPage(
        ctrl.getStocks(), ctrl.getStockDetail(), ctrl.getTrading(), onTradeComplete);
    FundsPage fundsPage = new FundsPage(session.exchange(), ctrl.getTrading(), onTradeComplete);
    SavingsPage savingsPage = new SavingsPage(ctrl.getSavings(), onSavingsChanged);
    TransactionHistoryPage transactionsPage =
        new TransactionHistoryPage(session.exchange(), session.player());
    LeaderboardPage leaderboardPage = new LeaderboardPage(svc);
    LearningHubPage learningHubPage =
        new LearningHubPage(ctrl.getLearningHub(), ctrl.getQuiz());
    NotificationsPage notificationsPage = new NotificationsPage(ctrl.getNotificationsTab());

    Tab portfolioTab = makeTab("Portfolio", portfolioPage);
    Tab stocksTab = makeTab("Stocks", stocksPage);
    Tab fundsTab = makeTab("Funds", fundsPage);
    Tab savingsTab = makeTab("Savings", savingsPage);
    Tab transactionsTab = makeTab("Transactions", transactionsPage);

    portfolioTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        portfolioPage.refresh();
      }
    });
    stocksTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        stocksPage.refresh();
      }
    });
    fundsTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        fundsPage.refresh();
      }
    });
    transactionsTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        transactionsPage.refresh();
      }
    });
    savingsTab.selectedProperty().addListener((obs, oldVal, sel) -> {
      if (Boolean.TRUE.equals(sel)) {
        savingsPage.refresh();
      }
    });
    Tab leaderboardTab = makeTab("Leaderboard", leaderboardPage);
    Tab learningTab = makeTab("Learning Hub", learningHubPage);
    Tab quizTab = new Tab("Quiz");
    quizTab.setClosable(false);
    Tab notificationsTab = makeTab("Notifications", notificationsPage);

    TabPane tabs = new TabPane(
        portfolioTab, stocksTab, fundsTab, savingsTab, transactionsTab,
        leaderboardTab, learningTab, quizTab, notificationsTab);
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    java.util.function.Consumer<model.learning.content.LearningItem> openTopicInHub =
        item -> {
          learningHubPage.openTopic(item.id());
          tabs.getSelectionModel().select(learningTab);
        };
    quizTab.setContent(new QuizLauncherPage(
        ctrl.getQuiz(), ctrl.getLearningHub(), openTopicInHub));
    registerPageObservers(
        events,
        portfolioPage,
        stocksPage,
        fundsPage,
        savingsPage,
        transactionsPage,
        leaderboardPage);
    return tabs;
  }

  private static void registerPageObservers(
      WorkspaceEventBus events,
      PlayerPortfolioPage portfolioPage,
      StocksPage stocksPage,
      FundsPage fundsPage,
      SavingsPage savingsPage,
      TransactionHistoryPage transactionsPage,
      LeaderboardPage leaderboardPage) {
    Runnable portfolioRefresh = portfolioPage::refresh;
    Runnable stocksRefresh = stocksPage::refresh;
    Runnable fundsRefresh = fundsPage::refresh;
    Runnable savingsRefresh = savingsPage::refresh;
    Runnable transactionsRefresh = transactionsPage::refresh;
    Runnable leaderboardRefresh = leaderboardPage::refresh;

    events.subscribe(PORTFOLIO_CHANGED, portfolioRefresh);
    events.subscribe(MARKET_CHANGED, portfolioRefresh);
    events.subscribe(SAVINGS_CHANGED, portfolioRefresh);
    events.subscribe(PROFILE_CHANGED, portfolioRefresh);
    events.subscribe(MARKET_CHANGED, stocksRefresh);
    events.subscribe(PORTFOLIO_CHANGED, stocksRefresh);
    events.subscribe(MARKET_CHANGED, fundsRefresh);
    events.subscribe(PORTFOLIO_CHANGED, fundsRefresh);
    events.subscribe(SAVINGS_CHANGED, savingsRefresh);
    events.subscribe(MARKET_CHANGED, savingsRefresh);
    events.subscribe(TRANSACTIONS_CHANGED, transactionsRefresh);
    events.subscribe(SAVINGS_CHANGED, transactionsRefresh);
    events.subscribe(LEADERBOARD_CHANGED, leaderboardRefresh);
    events.subscribe(SAVINGS_CHANGED, leaderboardRefresh);
    events.subscribe(PROFILE_CHANGED, leaderboardRefresh);
  }

  private static void registerHeaderObservers(
      WorkspaceEventBus events,
      WorkspaceLayout workspace,
      WorkspaceController ctrl) {
    Runnable refreshSummary = () -> workspace.setSessionSummary(ctrl.getSessionSummary());
    Runnable refreshAvatar = () -> workspace.loadHeaderAvatar(ctrl.getAvatarPath());

    events.subscribe(MARKET_CHANGED, refreshSummary);
    events.subscribe(PROFILE_CHANGED, refreshSummary);
    events.subscribe(PROFILE_CHANGED, refreshAvatar);
  }

  private static Tab makeTab(String label, javafx.scene.Node content) {
    Tab tab = new Tab(label, content);
    tab.setClosable(false);
    return tab;
  }

  private void logout(WorkspaceController ctrl) {
    sessionService.saveActiveSession();
    ctrl.dispose();
    sessionService.logout();
    currentWorkspace = null;
    scene.setRoot(buildLoginPage());
    primaryStage.setTitle("Millions");
  }

  private void switchUser(WorkspaceController ctrl, WorkspaceLayout currentView) {
    sessionService.saveActiveSession();
    LoginPage loginPage = new LoginPage(
        this::handleLogin,
        () -> scene.setRoot(buildRegisterPage()),
        null, null, true,
        () -> scene.setRoot(currentView));
    scene.setRoot(loginPage);
    primaryStage.setTitle("Millions");
  }

  private static SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        SessionServiceFactory.defaultProfilesRoot(),
        MARKET_DATA_RESOURCE,
        MillionsApp.class,
        EXCHANGE_NAME);
  }

  private static String mapValidationMessage(String message) {
    if (message == null) {
      return "Invalid input.";
    }
    return switch (message) {
      case "Username must be 3-32 characters using letters, numbers, underscores, or hyphens." ->
          "Username must be 3-32 characters (letters, numbers, _ or -).";
      case "PIN must be 4 to 8 digits." -> "PIN must be 4 to 8 digits.";
      case "Starting money must be non-negative." -> "Starting money must be non-negative.";
      default -> "Invalid input.";
    };
  }
}
