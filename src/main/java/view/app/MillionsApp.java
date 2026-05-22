package view.app;

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.persistence.MarketData;
import model.persistence.MarketDataLoader;
import model.session.ActiveSession;
import model.session.AuthenticationException;
import model.session.DuplicateUsernameException;
import model.session.RegistrationValidationException;
import model.session.SessionService;
import model.session.SessionServiceFactory;
import view.dialogs.WelcomeDialog;
import view.layout.WorkspaceLayout;
import view.pages.auth.AuthPlayerLeaderboardPanel;
import view.pages.auth.LoginPage;
import view.pages.auth.RegisterPage;

/**
 * JavaFX entry point for the Millions application.
 */
public class MillionsApp extends Application {

  private static final String EXCHANGE_NAME = "NYSE";
  private static final String MARKET_DATA_RESOURCE = "/data/demo-stocks.csv";
  private static final int WINDOW_WIDTH = 1100;
  private static final int WINDOW_HEIGHT = 720;

  private SessionService sessionService;
  private Stage primaryStage;
  private Scene scene;
  private WorkspaceLayout workspaceLayout;
  private boolean allowReturnToSession;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    this.sessionService = createSessionService();

    scene = new Scene(buildLoginPage(), WINDOW_WIDTH, WINDOW_HEIGHT);
    stage.setScene(scene);
    stage.setTitle("Millions");
    stage.setOnCloseRequest(_ -> shutdown());
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  private LoginPage buildLoginPage() {
    return new LoginPage(
        this::handleLogin,
        () -> scene.setRoot(buildRegisterPage()),
        buildLeaderboardPanel(),
        this::openHelp,
        allowReturnToSession,
        this::returnToCurrentSession);
  }

  private RegisterPage buildRegisterPage() {
    return new RegisterPage(
        this::handleRegistration,
        () -> scene.setRoot(buildLoginPage()),
        buildLeaderboardPanel(),
        this::openHelp,
        allowReturnToSession,
        this::returnToCurrentSession);
  }

  private AuthPlayerLeaderboardPanel buildLeaderboardPanel() {
    return new AuthPlayerLeaderboardPanel(sessionService.listLeaderboardEntries());
  }

  private void showAuthView(boolean allowReturn) {
    allowReturnToSession = allowReturn;
    if (workspaceLayout != null) {
      workspaceLayout.dispose();
      workspaceLayout = null;
    }
    scene.setRoot(buildLoginPage());
    primaryStage.setTitle("Millions");
  }

  private void handleLogin(String username, String pin) {
    if (!(scene.getRoot() instanceof LoginPage loginPage)) {
      return;
    }
    try {
      ActiveSession session = sessionService.login(username, pin.toCharArray());
      onSessionStarted(session);
    } catch (AuthenticationException e) {
      loginPage.setStatus("Invalid username or PIN.");
    }
  }

  private void handleRegistration(String username, String pin, String startingMoneyText) {
    if (!(scene.getRoot() instanceof RegisterPage registerPage)) {
      return;
    }
    try {
      BigDecimal startingMoney = new BigDecimal(startingMoneyText.trim());
      ActiveSession session = sessionService.register(username, pin.toCharArray(), startingMoney);
      onSessionStarted(session);
    } catch (NumberFormatException e) {
      registerPage.setStatus("Starting money must be a valid number.");
    } catch (DuplicateUsernameException e) {
      registerPage.setStatus("That username is already taken.");
    } catch (RegistrationValidationException e) {
      registerPage.setStatus(e.getMessage());
    }
  }

  private void onSessionStarted(ActiveSession session) {
    allowReturnToSession = false;
    workspaceLayout =
        SessionWorkspaceBuilder.build(
            session,
            sessionService,
            this::openHelp,
            this::handleLogout,
            this::beginSwitchUserFlow,
            sessionService::saveActiveSession,
            () -> {
              handleLogout();
            });
    scene.setRoot(workspaceLayout);
    primaryStage.setTitle("Millions — " + session.username());
    Platform.runLater(this::showWelcomeIfNeeded);
  }

  private void openHelp() {
    var window = scene != null ? scene.getWindow() : null;
    WelcomeDialog.show(window);
  }

  private void showWelcomeIfNeeded() {
    try {
      if (sessionService.hasSeenWelcome()) {
        return;
      }
      WelcomeDialog.show(scene.getWindow());
      sessionService.markWelcomeSeen();
    } catch (IllegalStateException ignored) {
      // No active session
    }
  }

  private void handleLogout() {
    if (sessionService.logout()) {
      if (workspaceLayout != null) {
        workspaceLayout.dispose();
        workspaceLayout = null;
      }
      showAuthView(false);
    }
  }

  private void beginSwitchUserFlow() {
    if (workspaceLayout != null) {
      workspaceLayout.dispose();
      workspaceLayout = null;
    }
    sessionService.saveActiveSession();
    showAuthView(true);
  }

  private void returnToCurrentSession() {
    sessionService.getActiveSession().ifPresent(this::onSessionStarted);
  }

  private void shutdown() {
    sessionService.saveActiveSession();
    if (workspaceLayout != null) {
      workspaceLayout.dispose();
      workspaceLayout = null;
    }
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

}
