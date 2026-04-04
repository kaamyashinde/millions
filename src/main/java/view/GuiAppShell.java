package view;

import java.math.BigDecimal;
import java.util.List;
import javafx.application.Platform;
import javafx.stage.Window;
import model.session.ActiveSession;
import model.session.AuthenticationException;
import model.session.DuplicateUsernameException;
import model.session.SessionService;

/**
 * Top-level JavaFX shell that swaps between authentication and logged-in workspace views.
 */
public class GuiAppShell extends javafx.scene.layout.BorderPane {

  private final SessionService sessionService;
  private final SessionWorkspaceFactory workspaceFactory;
  private final boolean showWelcomeOnFirstSession;

  private AuthPane authPane;
  private SessionWorkspaceView workspaceView;

  /**
   * Builds a GUI shell backed by the supplied session service.
   *
   * @param sessionService session service used for register, login, logout, and save operations
   */
  public GuiAppShell(SessionService sessionService) {
    this(sessionService, new SessionWorkspaceFactory(), true);
  }

  /**
   * Builds a GUI shell with injectable collaborators for tests.
   *
   * @param sessionService session service used for profile lifecycle operations
   * @param workspaceFactory factory that creates one fresh workspace per active session
   */
  public GuiAppShell(SessionService sessionService, SessionWorkspaceFactory workspaceFactory) {
    this(sessionService, workspaceFactory, true);
  }

  /**
   * Builds a GUI shell with control over the first-login welcome dialog (disabled in headless tests).
   *
   * @param sessionService session service used for profile lifecycle operations
   * @param workspaceFactory factory that creates one fresh workspace per active session
   * @param showWelcomeOnFirstSession when {@code true}, shows welcome once per profile after login
   */
  public GuiAppShell(
      SessionService sessionService,
      SessionWorkspaceFactory workspaceFactory,
      boolean showWelcomeOnFirstSession) {
    this.sessionService = sessionService;
    this.workspaceFactory = workspaceFactory;
    this.showWelcomeOnFirstSession = showWelcomeOnFirstSession;
    setStyle("-fx-background-color: #121212;");
    showAuthView(false);
  }

  /**
   * Returns whether the authentication screen is currently visible.
   *
   * @return {@code true} when the auth pane is active
   */
  public boolean isShowingAuthView() {
    return authPane != null;
  }

  /**
   * Returns the active workspace, or {@code null} when the auth view is visible.
   *
   * @return current session workspace, if any
   */
  public SessionWorkspaceView getWorkspaceView() {
    return workspaceView;
  }

  /**
   * Returns the active auth pane, or {@code null} when logged in.
   *
   * @return current auth pane, if any
   */
  public AuthPane getAuthPane() {
    return authPane;
  }

  /**
   * Submits a registration request using the same flow as the visible auth form.
   *
   * @param username requested username
   * @param pin PIN text
   * @param startingMoney starting money text
   */
  public void submitRegistration(String username, String pin, String startingMoney) {
    handleRegistration(username, pin, startingMoney);
  }

  /**
   * Submits a login request using the same flow as the visible auth form.
   *
   * @param username username to log in
   * @param pin PIN text
   */
  public void submitLogin(String username, String pin) {
    handleLogin(username, pin);
  }

  /**
   * Logs out the current user and returns to the auth view.
   */
  public void logoutActiveUser() {
    if (sessionService.logout()) {
      disposeWorkspace();
      showAuthView(false);
    }
  }

  /**
   * Shows the auth view while keeping the current session available for a later return.
   */
  public void beginSwitchUserFlow() {
    disposeWorkspace();
    showAuthView(true);
  }

  /**
   * Saves active-session state and clears transient workspace resources.
   */
  public void shutdown() {
    sessionService.saveActiveSession();
    disposeWorkspace();
  }

  private void showAuthView(boolean allowReturnToSession) {
    authPane = new AuthPane(
        listRegisteredUsers(),
        allowReturnToSession,
        this::handleLogin,
        this::handleRegistration,
        this::returnToCurrentSession,
        this::openHelpWindow);
    workspaceView = null;
    setCenter(authPane);
  }

  private void showWorkspace(ActiveSession session) {
    disposeWorkspace();
    workspaceView = workspaceFactory.create(
        session,
        sessionService,
        this::openHelpWindow,
        this::logoutActiveUser,
        this::beginSwitchUserFlow,
        sessionService::saveActiveSession,
        () -> {
          disposeWorkspace();
          showAuthView(false);
        });
    authPane = null;
    setCenter(workspaceView);
    Platform.runLater(this::showWelcomeIfNeeded);
  }

  private void openHelpWindow() {
    Window owner = null;
    if (workspaceView != null && workspaceView.getScene() != null) {
      owner = workspaceView.getScene().getWindow();
    } else if (authPane != null && authPane.getScene() != null) {
      owner = authPane.getScene().getWindow();
    }
    WelcomeDialog.show(owner);
  }

  private void showWelcomeIfNeeded() {
    if (!showWelcomeOnFirstSession) {
      return;
    }
    try {
      if (sessionService.hasSeenWelcome()) {
        return;
      }
      Window owner = getScene() != null ? getScene().getWindow() : null;
      WelcomeDialog.show(owner);
      sessionService.markWelcomeSeen();
    } catch (IllegalStateException ignored) {
      // No active session (e.g. race); skip welcome.
    }
  }

  private void handleRegistration(String username, String pin, String startingMoneyText) {
    try {
      BigDecimal startingMoney = new BigDecimal(startingMoneyText.trim());
      ActiveSession session = sessionService.register(
          username,
          pin.toCharArray(),
          startingMoney);
      showWorkspace(session);
    } catch (NumberFormatException exception) {
      showAuthStatus("Starting money must be a valid number.");
    } catch (DuplicateUsernameException exception) {
      showAuthStatus("That username is already registered.");
    } catch (IllegalArgumentException exception) {
      showAuthStatus(mapValidationMessage(exception.getMessage()));
    }
  }

  private void handleLogin(String username, String pin) {
    try {
      ActiveSession session = sessionService.login(username, pin.toCharArray());
      showWorkspace(session);
    } catch (AuthenticationException exception) {
      showAuthStatus("Invalid username or PIN.");
    } catch (IllegalArgumentException exception) {
      showAuthStatus(mapValidationMessage(exception.getMessage()));
    }
  }

  private void returnToCurrentSession() {
    sessionService.getActiveSession().ifPresent(this::showWorkspace);
  }

  private void showAuthStatus(String message) {
    if (authPane != null) {
      authPane.setStatus(message);
    }
  }

  private List<String> listRegisteredUsers() {
    return sessionService.listRegisteredUsers();
  }

  private void disposeWorkspace() {
    if (workspaceView != null) {
      workspaceView.dispose();
      workspaceView = null;
    }
  }

  private static String mapValidationMessage(String message) {
    if (message == null) {
      return "Invalid input.";
    }
    return switch (message) {
      case "Username must be 3-32 characters using letters, numbers, underscores, or hyphens." ->
          "Username must be 3-32 characters using letters, numbers, underscores, or hyphens.";
      case "PIN must be 4 to 8 digits." -> "PIN must be 4 to 8 digits.";
      case "Starting money must be non-negative." -> "Starting money must be non-negative.";
      default -> "Invalid input.";
    };
  }
}
