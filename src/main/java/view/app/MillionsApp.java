package view.app;

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.persistence.MarketData;
import model.persistence.MarketDataLoader;
import model.session.ActiveSession;
import model.session.AuthenticationException;
import model.session.DuplicateUsernameException;
import model.session.SessionService;
import model.session.SessionServiceFactory;
import controller.WorkspaceController;
import view.layout.WorkspaceLayout;
import view.pages.auth.LoginPage;
import view.pages.auth.RegisterPage;
import view.pages.funds.FundsPage;
import view.pages.leaderboard.LeaderboardPage;
import view.pages.learning.LearningHubPage;
import view.pages.notifications.NotificationsPage;
import view.pages.portfolio.PlayerPortfolioPage;
import view.pages.quiz.QuizLauncherPage;
import view.pages.saved.SavedRunsPage;
import view.pages.savings.SavingsPage;
import view.pages.stocks.StocksPage;
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
    } catch (IllegalArgumentException e) {
      loginPage.setStatus(mapValidationMessage(e.getMessage()));
    }
  }

  private void handleRegistration(String username, String pin, String startingMoneyText) {
    RegisterPage registerPage = (RegisterPage) scene.getRoot();
    try {
      BigDecimal startingMoney = new BigDecimal(startingMoneyText.trim());
      ActiveSession session = sessionService.register(username, pin.toCharArray(), startingMoney);
      onSessionStarted(session);
    } catch (NumberFormatException e) {
      registerPage.setStatus("Starting money must be a valid number.");
    } catch (DuplicateUsernameException e) {
      registerPage.setStatus("That username is already taken.");
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
    TabPane tabs = buildWorkspaceTabs(ctrl);
    WorkspaceLayout[] ref = new WorkspaceLayout[1];
    WorkspaceLayout workspace = new WorkspaceLayout(
        ctrl.getNotifications(),
        tabs,
        () -> { /* profile editor: placeholder */ },
        ctrl::refreshAll,
        () -> { /* help: placeholder */ },
        () -> switchUser(ctrl, ref[0]),
        () -> logout(ctrl));
    ref[0] = workspace;
    workspace.setSessionSummary(ctrl.getSessionSummary());
    workspace.loadHeaderAvatar(ctrl.getAvatarPath());
    return workspace;
  }

  private TabPane buildWorkspaceTabs(WorkspaceController ctrl) {
    ActiveSession session = ctrl.getSession();
    SessionService svc = ctrl.getSessionService();

    PlayerPortfolioPage portfolioPage = new PlayerPortfolioPage(
        session.exchange(), session.player(), ctrl.getAvatarPath());
    StocksPage stocksPage = new StocksPage(session.exchange());
    FundsPage fundsPage = new FundsPage(session.exchange());
    SavingsPage savingsPage = new SavingsPage(ctrl.getSavings(), ctrl::refreshAll);
    SavedRunsPage savedRunsPage = new SavedRunsPage(svc, ctrl::refreshAll);
    LeaderboardPage leaderboardPage = new LeaderboardPage(svc);
    LearningHubPage learningHubPage = new LearningHubPage();
    QuizLauncherPage quizPage = new QuizLauncherPage();
    NotificationsPage notificationsPage = new NotificationsPage(ctrl.getNotificationsTab());

    Tab portfolioTab = makeTab("Portfolio", portfolioPage);
    Tab stocksTab = makeTab("Stocks", stocksPage);
    Tab fundsTab = makeTab("Funds", fundsPage);
    Tab savingsTab = makeTab("Savings", savingsPage);
    Tab savedRunsTab = makeTab("Saved runs", savedRunsPage);
    Tab leaderboardTab = makeTab("Leaderboard", leaderboardPage);
    Tab learningTab = makeTab("Learning hub", learningHubPage);
    Tab quizTab = makeTab("Quiz", quizPage);
    Tab notificationsTab = makeTab("Notifications", notificationsPage);

    TabPane tabs = new TabPane(
        portfolioTab, stocksTab, fundsTab, savingsTab,
        savedRunsTab, leaderboardTab, learningTab, quizTab, notificationsTab);
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    return tabs;
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
        MillionsApp::loadMarketData,
        EXCHANGE_NAME);
  }

  private static MarketData loadMarketData() {
    MarketData data = MarketDataLoader.loadFromResource(MillionsApp.class, MARKET_DATA_RESOURCE);
    if (data.isEmpty()) {
      throw new IllegalStateException(
          "Could not load bundled market data from " + MARKET_DATA_RESOURCE);
    }
    return data;
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
