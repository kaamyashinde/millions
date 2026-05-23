package view.pages.auth;

import java.nio.file.Path;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import view.layout.AuthLayout;
import view.theme.ThemeStyles;

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
   * @param registerAction registration submit handler
   * @param toLogin navigate to login
   */
  public RegisterPage(RegisterAction registerAction, Runnable toLogin) {
    this(registerAction, toLogin, null, null, false, null);
  }

  /**
   * @param registerAction registration submit handler
   * @param toLogin navigate to login
   * @param sidePanel optional leaderboard panel
   * @param helpAction optional help handler
   * @param showReturnToSession show return button
   * @param returnAction return to active session
   */
  public RegisterPage(
      RegisterAction registerAction,
      Runnable toLogin,
      Node sidePanel,
      Runnable helpAction,
      boolean showReturnToSession,
      Runnable returnAction) {
    super(buildFormShell(), "Login", toLogin, sidePanel, helpAction, showReturnToSession, returnAction);
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

    VBox fields = new VBox(10, usernameField, pinField, startingMoneyField, marketDataRow);
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

  public String getStatus() {
    return super.getStatus();
  }

  public void setValues(String username, String pin, String startingMoney) {
    usernameField.setText(username);
    pinField.setText(pin);
    startingMoneyField.setText(startingMoney);
  }

  public void clearForm() {
    usernameField.clear();
    pinField.clear();
    startingMoneyField.clear();
    clearMarketDataFile();
    setStatus("");
  }

  @FunctionalInterface
  public interface RegisterAction {
    void run(String username, String pin, String startingMoney, Optional<Path> marketDataFile);
  }
}
