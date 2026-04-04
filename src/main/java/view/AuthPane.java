package view;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
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
   */
  public AuthPane(
      List<String> users,
      boolean allowReturnToSession,
      LoginAction loginAction,
      RegisterAction registerAction,
      Runnable returnAction) {
    setPadding(new Insets(20));

    Text heading = new Text("Millions GUI");
    heading.setFont(Font.font("System", FontWeight.BOLD, 28));

    Label intro = new Label("Register a profile or log into an existing one.");
    intro.setWrapText(true);

    VBox top = new VBox(8, heading, intro);
    setTop(top);

    registeredUsersView.getItems().setAll(users);
    registeredUsersView.setPlaceholder(new Label("No registered users yet."));
    registeredUsersView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
      if (newUser != null) {
        loginUsernameField.setText(newUser);
      }
    });

    VBox usersBox = new VBox(8, new Label("Registered users"), registeredUsersView);
    usersBox.setPrefWidth(180);

    loginUsernameField.setPromptText("Username");
    loginPinField.setPromptText("PIN (4-8 digits)");
    styleField(loginUsernameField);
    styleField(loginPinField);

    Button loginButton = new Button("Log in");
    styleButton(loginButton);
    loginButton.setOnAction(_ -> loginAction.run(
        loginUsernameField.getText(),
        loginPinField.getText()));

    GridPane loginGrid = new GridPane();
    loginGrid.setHgap(10);
    loginGrid.setVgap(10);
    loginGrid.addRow(0, new Label("Username"), loginUsernameField);
    loginGrid.addRow(1, new Label("PIN"), loginPinField);
    loginGrid.add(loginButton, 1, 2);

    VBox loginBox = new VBox(10, new Label("Log into a profile"), loginGrid);
    HBox.setHgrow(loginBox, Priority.ALWAYS);

    registerUsernameField.setPromptText("Username");
    registerPinField.setPromptText("PIN (4-8 digits)");
    registerStartingMoneyField.setPromptText("Starting money");
    styleField(registerUsernameField);
    styleField(registerPinField);
    styleField(registerStartingMoneyField);

    Button registerButton = new Button("Register");
    styleButton(registerButton);
    registerButton.setOnAction(_ -> registerAction.run(
        registerUsernameField.getText(),
        registerPinField.getText(),
        registerStartingMoneyField.getText()));

    GridPane registerGrid = new GridPane();
    registerGrid.setHgap(10);
    registerGrid.setVgap(10);
    registerGrid.addRow(0, new Label("Username"), registerUsernameField);
    registerGrid.addRow(1, new Label("PIN"), registerPinField);
    registerGrid.addRow(2, new Label("Starting money"), registerStartingMoneyField);
    registerGrid.add(registerButton, 1, 3);

    VBox registerBox = new VBox(10, new Label("Create a profile"), registerGrid);
    HBox.setHgrow(registerBox, Priority.ALWAYS);

    HBox forms = new HBox(20, usersBox, loginBox, registerBox);
    forms.setAlignment(Pos.TOP_LEFT);
    setCenter(forms);

    Button returnButton = new Button("Back to current session");
    styleButton(returnButton);
    returnButton.setVisible(allowReturnToSession);
    returnButton.setManaged(allowReturnToSession);
    returnButton.setOnAction(_ -> returnAction.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    statusLabel.setWrapText(true);
    HBox bottom = new HBox(12, statusLabel, spacer, returnButton);
    bottom.setAlignment(Pos.CENTER_LEFT);
    bottom.setPadding(new Insets(16, 0, 0, 0));
    setBottom(bottom);
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
    field.setStyle(FIELD_STYLE);
  }

  private static void styleButton(Button button) {
    button.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
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
