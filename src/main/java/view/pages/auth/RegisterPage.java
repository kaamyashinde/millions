package view.pages.auth;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import view.layout.AuthLayout;
import view.theme.ThemeStyles;
import view.validation.AuthFormValidation;

/**
 * Registration page for the authentication flow.
 */
public class RegisterPage extends AuthLayout {

  private final TextField usernameField = new TextField();
  private final PasswordField pinField = new PasswordField();
  private final TextField startingMoneyField = new TextField();
  private final Label marketDataFileNameLabel = new Label("Default market data");
  private final Label marketDataStatusLabel = new Label();
  private Path selectedMarketDataFile;

  /**
   * Creates a registration page without a side panel.
   *
   * @param registerAction registration submit handler
   * @param toLogin navigate to login
   */
  public RegisterPage(RegisterAction registerAction, Runnable toLogin) {
    this(registerAction, toLogin, null, false, null);
  }

  /**
   * Creates a registration page with optional side content and return action.
   *
   * @param registerAction registration submit handler
   * @param toLogin navigate to login
   * @param sidePanel optional leaderboard panel
   * @param showReturnToSession show return button
   * @param returnAction return to active session
   */
  public RegisterPage(
      RegisterAction registerAction,
      Runnable toLogin,
      Node sidePanel,
      boolean showReturnToSession,
      Runnable returnAction) {
    super(buildFormShell(), "Login", toLogin, sidePanel, showReturnToSession, returnAction);
    VBox form = (VBox) getContentSlot().getChildren().get(0);
    wireForm(form, registerAction);
  }

  private static VBox buildFormShell() {
    VBox form = new VBox(16);
    ThemeStyles.addStyleClasses(form, "auth-form");
    return form;
  }

  private void wireForm(VBox form, RegisterAction registerAction) {
    Label heading = new Label("Create Your Profile");
    ThemeStyles.addStyleClasses(heading, "heading-lg");

    Label subheading = new Label("Choose a username, PIN, and starting balance.");
    ThemeStyles.addStyleClasses(subheading, "text-subheading");

    usernameField.setPromptText("Username");
    pinField.setPromptText("PIN (4–8 digits)");
    startingMoneyField.setPromptText("Starting Money");
    ThemeStyles.styleField(usernameField);
    ThemeStyles.styleField(pinField);
    ThemeStyles.styleField(startingMoneyField);
    usernameField.setMaxWidth(Double.MAX_VALUE);
    pinField.setMaxWidth(Double.MAX_VALUE);
    startingMoneyField.setMaxWidth(Double.MAX_VALUE);
    AuthFormValidation.restrictPinInput(pinField);

    Label usernameErrorLabel = createFieldErrorLabel();
    Label pinErrorLabel = createFieldErrorLabel();
    Label startingMoneyErrorLabel = createFieldErrorLabel();
    wireLiveValidation(usernameField, usernameErrorLabel, AuthFormValidation::usernameError);
    wireLiveValidation(pinField, pinErrorLabel, AuthFormValidation::pinError);
    wireLiveValidation(
        startingMoneyField, startingMoneyErrorLabel, AuthFormValidation::startingMoneyError);

    marketDataFileNameLabel.setWrapText(true);
    ThemeStyles.addStyleClasses(marketDataFileNameLabel, "text-subheading");
    marketDataStatusLabel.setWrapText(true);
    marketDataStatusLabel.setVisible(false);
    marketDataStatusLabel.setManaged(false);
    ThemeStyles.addStyleClasses(marketDataStatusLabel, "text-subheading");

    Button chooseMarketDataButton = new Button("Choose market data…");
    chooseMarketDataButton.getStyleClass().add("auth-market-data-button");
    ThemeStyles.styleButton(chooseMarketDataButton);
    chooseMarketDataButton.setOnAction(_ -> chooseMarketDataFile());

    Button clearMarketDataButton = new Button("Use default");
    clearMarketDataButton.getStyleClass().add("auth-market-data-button");
    ThemeStyles.styleButton(clearMarketDataButton);
    clearMarketDataButton.setOnAction(_ -> clearMarketDataFile());

    HBox marketDataActions = new HBox(8, chooseMarketDataButton, clearMarketDataButton);
    marketDataActions.getStyleClass().add("auth-market-data-actions");
    VBox marketDataRow = new VBox(6,
        new Label("Market data (optional)"),
        marketDataActions,
        marketDataFileNameLabel,
        marketDataStatusLabel);
    marketDataRow.getStyleClass().add("auth-market-data-row");

    Button registerButton = new Button("Create Profile");
    registerButton.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.styleAccentButton(registerButton);
    registerButton.setOnAction(
        _ ->
            registerAction.run(
                usernameField.getText(),
                pinField.getText(),
                startingMoneyField.getText(),
                Optional.ofNullable(selectedMarketDataFile)));
    registerButton.disableProperty().bind(
        Bindings.createBooleanBinding(
            () -> AuthFormValidation.usernameError(usernameField.getText()).isPresent()
                || AuthFormValidation.pinError(pinField.getText()).isPresent()
                || AuthFormValidation.startingMoneyError(startingMoneyField.getText()).isPresent()
                || usernameField.getText().isBlank()
                || pinField.getText().isBlank()
                || startingMoneyField.getText().isBlank(),
            usernameField.textProperty(),
            pinField.textProperty(),
            startingMoneyField.textProperty()));

    VBox usernameRow = new VBox(4, usernameField, usernameErrorLabel);
    VBox pinRow = new VBox(4, pinField, pinErrorLabel);
    VBox startingMoneyRow = new VBox(4, startingMoneyField, startingMoneyErrorLabel);
    VBox fields = new VBox(10, usernameRow, pinRow, startingMoneyRow, marketDataRow);
    fields.setMaxWidth(360);
    ThemeStyles.addStyleClasses(fields, "auth-form-fields");

    VBox content = new VBox(8, heading, subheading, new VBox(12, fields, registerButton));
    ThemeStyles.addStyleClasses(content, "auth-form-content");
    form.setAlignment(Pos.CENTER);
    form.setMaxWidth(360);
    form.setPadding(new Insets(0, 32, 0, 32));
    form.getChildren().setAll(content);
  }

  private void chooseMarketDataFile() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Market data");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("CSV files", "*.csv"));
    Window owner = getScene() == null ? null : getScene().getWindow();
    java.io.File file = chooser.showOpenDialog(owner);
    if (file != null) {
      selectedMarketDataFile = file.toPath();
      marketDataFileNameLabel.setText(file.getName());
      clearMarketDataStatus();
    }
  }

  private void clearMarketDataFile() {
    selectedMarketDataFile = null;
    marketDataFileNameLabel.setText("Default market data");
    clearMarketDataStatus();
  }

  /**
   * Sets the market data upload status text.
   *
   * @param message status text, or blank to hide the status
   */
  public void setMarketDataStatus(String message) {
    if (message == null || message.isBlank()) {
      clearMarketDataStatus();
      return;
    }
    marketDataStatusLabel.setText(message);
    marketDataStatusLabel.setVisible(true);
    marketDataStatusLabel.setManaged(true);
  }

  private void clearMarketDataStatus() {
    marketDataStatusLabel.setText("");
    marketDataStatusLabel.setVisible(false);
    marketDataStatusLabel.setManaged(false);
  }

  public void setStatus(String message) {
    super.setStatus(message);
  }

  /**
   * Returns the current footer status text.
   *
   * @return current status text
   */
  public String getStatus() {
    return super.getStatus();
  }

  /**
   * Sets form values, primarily for tests.
   *
   * @param username username field value
   * @param pin PIN field value
   * @param startingMoney starting money field value
   */
  public void setValues(String username, String pin, String startingMoney) {
    usernameField.setText(username);
    pinField.setText(pin);
    startingMoneyField.setText(startingMoney);
  }

  /**
   * Clears all user-entered registration fields.
   */
  public void clearForm() {
    usernameField.clear();
    pinField.clear();
    startingMoneyField.clear();
    clearMarketDataFile();
    setStatus("");
  }

  private static Label createFieldErrorLabel() {
    Label label = new Label();
    ThemeStyles.addStyleClasses(label, "auth-field-error");
    label.setWrapText(true);
    label.setVisible(false);
    label.setManaged(false);
    return label;
  }

  private static void wireLiveValidation(
      TextInputControl field,
      Label errorLabel,
      Function<String, Optional<String>> validator) {
    field.textProperty().addListener((obs, oldText, text) -> {
      if (text.isBlank()) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        field.getStyleClass().remove("field-invalid");
        return;
      }
      Optional<String> error = validator.apply(text);
      if (error.isPresent()) {
        errorLabel.setText(error.get());
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("field-invalid")) {
          field.getStyleClass().add("field-invalid");
        }
      } else {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        field.getStyleClass().remove("field-invalid");
      }
    });
  }

  /**
   * Callback invoked when the registration form is submitted.
   */
  @FunctionalInterface
  public interface RegisterAction {

    /**
     * Runs the registration action.
     *
     * @param username username field value
     * @param pin PIN field value
     * @param startingMoney starting money field value
     * @param marketDataFile optional custom market-data CSV
     */
    void run(String username, String pin, String startingMoney, Optional<Path> marketDataFile);
  }
}
