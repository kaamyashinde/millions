package view;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Collects login and registration input for the session-based GUI shell.
 */
public class AuthPane extends BorderPane {

  private static final String FIELD_STYLE =
      "-fx-border-radius: 6;"
          + "-fx-background-radius: 6;";

  private final ListView<String> registeredUsersView = new ListView<>();
  private final TextField loginUsernameField = new TextField();
  private final PasswordField loginPinField = new PasswordField();
  private final TextField registerUsernameField = new TextField();
  private final PasswordField registerPinField = new PasswordField();
  private final TextField registerStartingMoneyField = new TextField();
  private final Label statusLabel = new Label();

  /**
   * Builds the authentication view with login and registration forms.
   *
   * @param users currently registered usernames
   * @param allowReturnToSession whether the caller should show a return button
   * @param loginAction callback invoked for login requests
   * @param registerAction callback invoked for registration requests
   * @param returnAction callback invoked when returning to the current session
   * @param helpAction callback to open the welcome / help content
   */
  public AuthPane(
      List<String> users,
      boolean allowReturnToSession,
      LoginAction loginAction,
      RegisterAction registerAction,
      Runnable returnAction,
      Runnable helpAction) {
    setPadding(new Insets(20));
    setStyle("-fx-background-color: #121212;");

    Text heading = new Text("Millions");
    heading.setFont(Font.font("System", FontWeight.BOLD, 28));
    heading.setStyle("-fx-fill: #e0e0e0;");

    Label intro = new Label("Log in or create a profile. Use Help anytime for a short tour.");
    intro.setWrapText(true);
    intro.setStyle("-fx-text-fill: #bdbdbd;");

    VBox top = new VBox(8, heading, intro);
    setTop(top);

    registeredUsersView.getItems().setAll(users);
    registeredUsersView.setPlaceholder(new Label("No registered users yet."));
    registeredUsersView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
    registeredUsersView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
      if (newUser != null) {
        loginUsernameField.setText(newUser);
        registerUsernameField.setText(newUser);
      }
    });

    VBox usersBox = new VBox(8, labelMuted("Registered users"), registeredUsersView);
    usersBox.setPrefWidth(200);

    loginUsernameField.setPromptText("Username");
    loginPinField.setPromptText("PIN (4-8 digits)");
    styleField(loginUsernameField);
    styleField(loginPinField);

    Button loginButton = new Button("Log in");
    styleAccentButton(loginButton);
    loginButton.setOnAction(_ -> loginAction.run(
        loginUsernameField.getText(),
        loginPinField.getText()));

    GridPane loginGrid = new GridPane();
    loginGrid.setHgap(10);
    loginGrid.setVgap(10);
    loginGrid.addRow(0, labelMuted("Username"), loginUsernameField);
    loginGrid.addRow(1, labelMuted("PIN"), loginPinField);
    loginGrid.add(loginButton, 1, 2);

    VBox loginBox = new VBox(12, loginGrid);
    Tab loginTab = new Tab("Log in", loginBox);
    loginTab.setClosable(false);

    registerUsernameField.setPromptText("Username");
    registerPinField.setPromptText("PIN (4-8 digits)");
    registerStartingMoneyField.setPromptText("Starting money");
    styleField(registerUsernameField);
    styleField(registerPinField);
    styleField(registerStartingMoneyField);

    Button registerButton = new Button("Create profile");
    styleAccentButton(registerButton);
    registerButton.setOnAction(_ -> registerAction.run(
        registerUsernameField.getText(),
        registerPinField.getText(),
        registerStartingMoneyField.getText()));

    GridPane registerGrid = new GridPane();
    registerGrid.setHgap(10);
    registerGrid.setVgap(10);
    registerGrid.addRow(0, labelMuted("Username"), registerUsernameField);
    registerGrid.addRow(1, labelMuted("PIN"), registerPinField);
    registerGrid.addRow(2, labelMuted("Starting money"), registerStartingMoneyField);
    registerGrid.add(registerButton, 1, 3);

    VBox registerBox = new VBox(12, registerGrid);
    Tab registerTab = new Tab("Create account", registerBox);
    registerTab.setClosable(false);

    TabPane tabPane = new TabPane(loginTab, registerTab);
    tabPane.setStyle("-fx-background-color: #1e1e1e;");
    HBox.setHgrow(tabPane, Priority.ALWAYS);

    HBox center = new HBox(24, usersBox, tabPane);
    center.setAlignment(Pos.TOP_LEFT);
    setCenter(center);

    Button helpButton = new Button("Help");
    styleButton(helpButton);
    helpButton.setOnAction(_ -> helpAction.run());

    Button returnButton = new Button("Back to current session");
    styleButton(returnButton);
    returnButton.setVisible(allowReturnToSession);
    returnButton.setManaged(allowReturnToSession);
    returnButton.setOnAction(_ -> returnAction.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    statusLabel.setWrapText(true);
    statusLabel.setStyle("-fx-text-fill: #e57373;");
    HBox bottom = new HBox(12, statusLabel, spacer, helpButton, returnButton);
    bottom.setAlignment(Pos.CENTER_LEFT);
    bottom.setPadding(new Insets(16, 0, 0, 0));
    setBottom(bottom);
  }

  private static Label labelMuted(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-text-fill: #9e9e9e;");
    return l;
  }

  /**
   * Updates the visible status message.
   *
   * @param message status text to display
   */
  public void setStatus(String message) {
    statusLabel.setText(message);
  }

  /**
   * Returns the visible status text.
   *
   * @return status label text
   */
  public String getStatus() {
    return statusLabel.getText();
  }

  /**
   * Returns how many registered usernames are currently displayed.
   *
   * @return displayed user count
   */
  public int getRegisteredUserCount() {
    return registeredUsersView.getItems().size();
  }

  /**
   * Fills the login form for tests or higher-level helpers.
   *
   * @param username username to show
   * @param pin PIN to show
   */
  public void setLoginValues(String username, String pin) {
    loginUsernameField.setText(username);
    loginPinField.setText(pin);
  }

  /**
   * Fills the registration form for tests or higher-level helpers.
   *
   * @param username username to show
   * @param pin PIN to show
   * @param startingMoney starting money text to show
   */
  public void setRegistrationValues(String username, String pin, String startingMoney) {
    registerUsernameField.setText(username);
    registerPinField.setText(pin);
    registerStartingMoneyField.setText(startingMoney);
  }

  private static void styleField(TextField field) {
    field.setStyle(
        FIELD_STYLE
            + "-fx-background-color: #2d2d2d; -fx-text-fill: #e0e0e0; -fx-prompt-text-fill: #757575;");
  }

  private static void styleButton(Button button) {
    button.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;"
            + "-fx-text-fill: #e0e0e0;");
  }

  private static void styleAccentButton(Button button) {
    button.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;"
            + "-fx-background-color: #2e7d32;"
            + "-fx-text-fill: #ffffff;");
  }

  /**
   * Handles login requests from the auth view.
   */
  @FunctionalInterface
  public interface LoginAction {
    void run(String username, String pin);
  }

  /**
   * Handles registration requests from the auth view.
   */
  @FunctionalInterface
  public interface RegisterAction {
    void run(String username, String pin, String startingMoney);
  }
}
