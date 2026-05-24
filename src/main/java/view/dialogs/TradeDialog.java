package view.dialogs;

import controller.TradeResult;
import controller.TradingController;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Modal dialog for buying or selling shares.
 */
public final class TradeDialog {

  private TradeDialog() {}

  /**
   * Opens a buy dialog for the given symbol.
   *
   * @param owner parent window
   * @param controller trading controller
   * @param symbol initial symbol (may be blank)
   * @param onCompleted invoked after a successful trade
   */
  public static void showBuy(
      Window owner, TradingController controller, String symbol, Runnable onCompleted) {
    Stage stage = createStage(owner, "Buy shares");
    TextField symbolField = new TextField(symbol != null ? symbol : "");
    symbolField.setPromptText("Symbol");

    ToggleGroup modeGroup = new ToggleGroup();
    RadioButton quantityMode = new RadioButton("Quantity");
    quantityMode.setToggleGroup(modeGroup);
    quantityMode.setSelected(true);
    RadioButton budgetMode = new RadioButton("Max spend");
    budgetMode.setToggleGroup(modeGroup);

    TextField amountField = new TextField();
    amountField.setPromptText("Shares or max spend");

    Label priceHint = new Label();
    Label balanceHint = new Label();
    Label ownedHint = new Label();
    Label estimateHint = new Label();
    priceHint.setWrapText(true);
    balanceHint.setWrapText(true);
    ownedHint.setWrapText(true);
    estimateHint.setWrapText(true);

    Label errorLabel = createErrorLabel();

    Runnable updateHints = () -> updateBuyHints(
        controller, symbolField, amountField, quantityMode.isSelected(),
        priceHint, balanceHint, ownedHint, estimateHint);
    symbolField.textProperty().addListener((_, _, _) -> updateHints.run());
    amountField.textProperty().addListener((_, _, _) -> updateHints.run());
    modeGroup.selectedToggleProperty().addListener((_, _, _) -> updateHints.run());
    updateHints.run();

    Button confirm = new Button("Confirm");
    confirm.setDefaultButton(true);
    confirm.setOnAction(_ -> {
      errorLabel.setText("");
      TradeResult result = quantityMode.isSelected()
          ? controller.buyByQuantity(symbolField.getText(), amountField.getText())
          : controller.buyUpToBudget(symbolField.getText(), amountField.getText());
      handleResult(result, errorLabel, onCompleted, stage);
    });

    Button cancel = new Button("Cancel");
    cancel.setCancelButton(true);
    cancel.setOnAction(_ -> stage.close());

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Symbol"), symbolField);
    form.addRow(1, new Label("Mode"), new HBox(12, quantityMode, budgetMode));
    form.addRow(2, new Label("Amount"), amountField);

    VBox root = new VBox(
        10,
        form,
        priceHint,
        balanceHint,
        ownedHint,
        estimateHint,
        errorLabel,
        new HBox(10, confirm, cancel));
    root.setPadding(new Insets(16));
    root.setAlignment(Pos.TOP_LEFT);
    showStage(stage, root, symbolField, amountField, confirm, cancel);
  }

  /**
   * Opens a sell dialog for the given symbol.
   *
   * @param owner parent window
   * @param controller trading controller
   * @param symbol initial symbol (may be blank)
   * @param onCompleted invoked after a successful trade
   */
  public static void showSell(
      Window owner, TradingController controller, String symbol, Runnable onCompleted) {
    Stage stage = createStage(owner, "Sell shares");
    TextField symbolField = new TextField(symbol != null ? symbol : "");
    symbolField.setPromptText("Symbol");

    TextField quantityField = new TextField();
    quantityField.setPromptText("Quantity to sell");

    Label ownedHint = new Label();
    ownedHint.setWrapText(true);
    Label errorLabel = createErrorLabel();

    Runnable updateHints = () -> {
      String sym = symbolField.getText();
      BigDecimal owned = controller.getOwnedQuantity(sym);
      ownedHint.setText("You own " + owned.toPlainString() + " share(s)");
    };
    symbolField.textProperty().addListener((_, _, _) -> updateHints.run());
    updateHints.run();

    Button confirm = new Button("Confirm");
    confirm.setDefaultButton(true);
    confirm.setOnAction(_ -> {
      errorLabel.setText("");
      TradeResult result = controller.sellByQuantity(symbolField.getText(), quantityField.getText());
      handleResult(result, errorLabel, onCompleted, stage);
    });

    Button cancel = new Button("Cancel");
    cancel.setCancelButton(true);
    cancel.setOnAction(_ -> stage.close());

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Symbol"), symbolField);
    form.addRow(1, new Label("Quantity"), quantityField);

    VBox root = new VBox(
        10, form, ownedHint, errorLabel, new HBox(10, confirm, cancel));
    root.setPadding(new Insets(16));
    root.setAlignment(Pos.TOP_LEFT);
    showStage(stage, root, symbolField, quantityField, confirm, cancel);
  }

  private static void updateBuyHints(
      TradingController controller,
      TextField symbolField,
      TextField amountField,
      boolean quantityMode,
      Label priceHint,
      Label balanceHint,
      Label ownedHint,
      Label estimateHint) {
    String sym = symbolField.getText();
    controller.getLatestPrice(sym).ifPresentOrElse(
        price -> priceHint.setText("Latest price: " + controller.formatMoney(price)),
        () -> priceHint.setText("Latest price: —"));
    balanceHint.setText("Cash balance: " + controller.formatMoney(controller.getCashBalance()));
    BigDecimal owned = controller.getOwnedQuantity(sym);
    ownedHint.setText("You own " + owned.toPlainString() + " share(s)");
    estimateHint.setText(estimateBuyCost(controller, sym, amountField.getText(), quantityMode));
  }

  private static String estimateBuyCost(
      TradingController controller, String symbol, String amountText, boolean quantityMode) {
    if (amountText == null || amountText.isBlank()) {
      return quantityMode ? "Estimated cost: —" : "Max spend: —";
    }
    if (!quantityMode) {
      return "Max spend: " + amountText.trim();
    }
    try {
      BigDecimal qty = new BigDecimal(amountText.trim());
      if (qty.signum() <= 0) {
        return "Estimated cost: —";
      }
      return controller.getLatestPrice(symbol)
          .map(price -> {
            BigDecimal estimate = price.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            return "Estimated cost (excl. commission): " + controller.formatMoney(estimate);
          })
          .orElse("Estimated cost: —");
    } catch (NumberFormatException e) {
      return "Estimated cost: —";
    }
  }

  private static void handleResult(
      TradeResult result, Label errorLabel, Runnable onCompleted, Stage stage) {
    if (result instanceof TradeResult.Success) {
      onCompleted.run();
      stage.close();
    } else if (result instanceof TradeResult.Failure failure) {
      errorLabel.setText(stripCliPrefix(failure.message()));
    }
  }

  private static String stripCliPrefix(String message) {
    if (message == null) {
      return "";
    }
    return message.startsWith("-> ") ? message.substring(3) : message;
  }

  private static Label createErrorLabel() {
    Label errorLabel = new Label();
    errorLabel.setWrapText(true);
    errorLabel.setStyle("-fx-text-fill: " + ThemePalette.ERROR + ";");
    return errorLabel;
  }

  private static Stage createStage(Window owner, String title) {
    Stage stage = new Stage();
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(title);
    return stage;
  }

  private static void showStage(
      Stage stage,
      VBox root,
      TextField primaryField,
      TextField secondaryField,
      Button confirm,
      Button cancel) {
    Scene scene = new Scene(
        root,
        ThemeStyles.dialogDimension(stage.getOwner(), 0.40, 380, 500),
        ThemeStyles.dialogDimension(stage.getOwner(), 0.50, 300, 420));
    ThemeStyles.install(scene);
    ThemeStyles.addStyleClasses(root, "dialog-root");
    ThemeStyles.styleField(primaryField);
    ThemeStyles.styleField(secondaryField);
    ThemeStyles.styleAccentButton(confirm);
    ThemeStyles.styleButton(cancel);
    stage.setScene(scene);
    stage.showAndWait();
  }
}
