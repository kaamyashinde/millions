package view.dialogs;

import controller.TradeResult;
import controller.TradingController;
import java.math.BigDecimal;
import java.util.Optional;
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
import util.I18n;
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
    final Stage stage = createStage(owner, "Buy shares");
    TextField symbolField = new TextField(symbol != null ? symbol : "");
    symbolField.setPromptText("Symbol");

    ToggleGroup modeGroup = new ToggleGroup();
    RadioButton quantityMode = new RadioButton(I18n.get("trade.buy.mode.quantity"));
    quantityMode.setToggleGroup(modeGroup);
    quantityMode.setSelected(true);
    RadioButton budgetMode = new RadioButton(I18n.get("trade.buy.mode.investmentAmount"));
    budgetMode.setToggleGroup(modeGroup);

    TextField amountField = new TextField();
    amountField.setPromptText(I18n.get("trade.buy.amount.prompt"));

    Label balanceHint = new Label();
    Label ownedHint = new Label();
    balanceHint.setWrapText(true);
    ownedHint.setWrapText(true);
    Label unitPriceHint = new Label("Unit price: —");
    Label quantityHint = new Label("Quantity: —");
    Label beforeCommissionHint = new Label("Before commission: —");
    Label commissionHint = new Label("Commission: —");
    Label afterCommissionHint = new Label("After commission: —");
    ThemeStyles.addStyleClasses(afterCommissionHint, "trade-estimate-total");
    VBox estimateBox =
        new VBox(
            4,
            new Label("Cost estimate"),
            unitPriceHint,
            quantityHint,
            beforeCommissionHint,
            commissionHint,
            afterCommissionHint);
    ThemeStyles.addStyleClasses(estimateBox, "trade-estimate-card");

    final Label errorLabel = createErrorLabel();

    Runnable updateHints = () -> updateBuyHints(
        controller, symbolField, amountField, quantityMode.isSelected(),
        balanceHint, ownedHint, unitPriceHint, quantityHint, beforeCommissionHint,
        commissionHint, afterCommissionHint);
    symbolField.textProperty().addListener((obs, previous, current) -> updateHints.run());
    amountField.textProperty().addListener((obs, previous, current) -> updateHints.run());
    modeGroup.selectedToggleProperty().addListener((obs, previous, current) -> updateHints.run());
    updateHints.run();

    Button confirm = new Button("Confirm");
    confirm.setDefaultButton(true);
    confirm.setOnAction(unused -> {
      errorLabel.setText("");
      TradeResult result = quantityMode.isSelected()
          ? controller.buyByQuantity(symbolField.getText(), amountField.getText())
          : controller.buyUpToBudget(symbolField.getText(), amountField.getText());
      handleResult(result, errorLabel, onCompleted, stage);
    });

    Button cancel = new Button("Cancel");
    cancel.setCancelButton(true);
    cancel.setOnAction(unused -> stage.close());

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Symbol"), symbolField);
    form.addRow(1, new Label("Mode"), new HBox(12, quantityMode, budgetMode));
    form.addRow(2, new Label("Amount"), amountField);

    VBox root = new VBox(
        10,
        form,
        estimateBox,
        balanceHint,
        ownedHint,
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
    final Stage stage = createStage(owner, "Sell shares");
    TextField symbolField = new TextField(symbol != null ? symbol : "");
    symbolField.setPromptText("Symbol");

    ToggleGroup modeGroup = new ToggleGroup();
    RadioButton quantityMode = new RadioButton(I18n.get("trade.sell.mode.quantity"));
    quantityMode.setToggleGroup(modeGroup);
    quantityMode.setSelected(true);
    RadioButton amountMode = new RadioButton(I18n.get("trade.sell.mode.amount"));
    amountMode.setToggleGroup(modeGroup);
    RadioButton sellAllMode = new RadioButton(I18n.get("trade.sell.mode.all"));
    sellAllMode.setToggleGroup(modeGroup);

    TextField amountField = new TextField();
    amountField.setPromptText(I18n.get("trade.sell.amount.prompt"));

    Label ownedHint = new Label();
    ownedHint.setWrapText(true);
    Label unitPriceHint = new Label("Unit price: —");
    Label quantityHint = new Label("Quantity: —");
    Label grossHint = new Label("Gross proceeds: —");
    Label commissionHint = new Label("Commission: —");
    Label taxHint = new Label("Capital gains tax: —");
    Label netProceedsHint = new Label("Net proceeds: —");
    ThemeStyles.addStyleClasses(netProceedsHint, "trade-estimate-total");
    VBox estimateBox =
        new VBox(
            4,
            new Label("Sale estimate"),
            unitPriceHint,
            quantityHint,
            grossHint,
            commissionHint,
            taxHint,
            netProceedsHint);
    ThemeStyles.addStyleClasses(estimateBox, "trade-estimate-card");

    final Label errorLabel = createErrorLabel();

    Runnable updateHints = () -> updateSellHints(
        controller, symbolField, amountField,
        amountMode, sellAllMode,
        ownedHint, unitPriceHint, quantityHint, grossHint,
        commissionHint, taxHint, netProceedsHint);
    symbolField.textProperty().addListener((_, _, _) -> updateHints.run());
    amountField.textProperty().addListener((_, _, _) -> updateHints.run());
    modeGroup.selectedToggleProperty().addListener((_, _, _) -> {
      amountField.setDisable(sellAllMode.isSelected());
      if (sellAllMode.isSelected()) {
        amountField.setText("");
      }
      updateHints.run();
    });
    updateHints.run();

    Button confirm = new Button("Confirm");
    confirm.setDefaultButton(true);
    confirm.setOnAction(unused -> {
      errorLabel.setText("");
      TradeResult result;
      if (sellAllMode.isSelected()) {
        result = controller.sellAllForSymbol(symbolField.getText());
      } else if (amountMode.isSelected()) {
        result = controller.sellUpToTargetNet(symbolField.getText(), amountField.getText());
      } else {
        result = controller.sellByQuantity(symbolField.getText(), amountField.getText());
      }
      handleResult(result, errorLabel, onCompleted, stage);
    });

    Button cancel = new Button("Cancel");
    cancel.setCancelButton(true);
    cancel.setOnAction(unused -> stage.close());

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Symbol"), symbolField);
    form.addRow(1, new Label("Mode"), new HBox(12, quantityMode, amountMode, sellAllMode));
    form.addRow(2, new Label("Amount"), amountField);

    VBox root = new VBox(
        10,
        form,
        estimateBox,
        ownedHint,
        errorLabel,
        new HBox(10, confirm, cancel));
    root.setPadding(new Insets(16));
    root.setAlignment(Pos.TOP_LEFT);
    showStage(stage, root, symbolField, amountField, confirm, cancel);
  }

  private static void updateBuyHints(
      TradingController controller,
      TextField symbolField,
      TextField amountField,
      boolean quantityMode,
      Label balanceHint,
      Label ownedHint,
      Label unitPriceHint,
      Label quantityHint,
      Label beforeCommissionHint,
      Label commissionHint,
      Label afterCommissionHint) {
    String sym = symbolField.getText();
    balanceHint.setText("Cash balance: " + controller.formatMoney(controller.getCashBalance()));
    BigDecimal owned = controller.getOwnedQuantity(sym);
    ownedHint.setText("You own " + controller.formatQuantity(owned) + " share(s)");
    Optional<TradingController.BuyEstimate> estimate =
        quantityMode
            ? controller.estimateBuyByQuantity(sym, amountField.getText())
            : controller.estimateBuyForBudget(sym, amountField.getText());
    applyBuyEstimate(
        controller,
        estimate,
        unitPriceHint,
        quantityHint,
        beforeCommissionHint,
        commissionHint,
        afterCommissionHint);
  }

  static void applyBuyEstimate(
      TradingController controller,
      Optional<TradingController.BuyEstimate> estimate,
      Label unitPriceHint,
      Label quantityHint,
      Label beforeCommissionHint,
      Label commissionHint,
      Label afterCommissionHint) {
    if (estimate.isEmpty()) {
      unitPriceHint.setText("Unit price: —");
      quantityHint.setText("Quantity: —");
      beforeCommissionHint.setText("Before commission: —");
      commissionHint.setText("Commission: —");
      afterCommissionHint.setText("After commission: —");
      return;
    }
    TradingController.BuyEstimate value = estimate.get();
    unitPriceHint.setText("Unit price: " + controller.formatMoney(value.unitPrice()));
    quantityHint.setText("Quantity: " + controller.formatQuantity(value.quantity()));
    beforeCommissionHint.setText("Before commission: " + controller.formatMoney(value.gross()));
    commissionHint.setText("Commission: " + controller.formatMoney(value.commission()));
    afterCommissionHint.setText("After commission: " + controller.formatMoney(value.total()));
  }

  private static void updateSellHints(
      TradingController controller,
      TextField symbolField,
      TextField amountField,
      RadioButton amountMode,
      RadioButton sellAllMode,
      Label ownedHint,
      Label unitPriceHint,
      Label quantityHint,
      Label grossHint,
      Label commissionHint,
      Label taxHint,
      Label netProceedsHint) {
    String sym = symbolField.getText();
    BigDecimal owned = controller.getOwnedQuantity(sym);
    ownedHint.setText("You own " + controller.formatQuantity(owned) + " share(s)");

    Optional<TradingController.SellEstimate> estimate;
    if (sellAllMode.isSelected()) {
      estimate = controller.estimateSellAll(sym);
    } else if (amountMode.isSelected()) {
      estimate = controller.estimateSellByQuantity(sym, amountField.getText());
    } else {
      estimate = controller.estimateSellByQuantity(sym, amountField.getText());
    }
    applySellEstimate(controller, estimate, unitPriceHint, quantityHint, grossHint,
        commissionHint, taxHint, netProceedsHint);
  }

  static void applySellEstimate(
      TradingController controller,
      Optional<TradingController.SellEstimate> estimate,
      Label unitPriceHint,
      Label quantityHint,
      Label grossHint,
      Label commissionHint,
      Label taxHint,
      Label netProceedsHint) {
    if (estimate.isEmpty()) {
      unitPriceHint.setText("Unit price: —");
      quantityHint.setText("Quantity: —");
      grossHint.setText("Gross proceeds: —");
      commissionHint.setText("Commission: —");
      taxHint.setText("Capital gains tax: —");
      netProceedsHint.setText("Net proceeds: —");
      return;
    }
    TradingController.SellEstimate value = estimate.get();
    unitPriceHint.setText("Unit price: " + controller.formatMoney(value.unitPrice()));
    quantityHint.setText("Quantity: " + controller.formatQuantity(value.quantity()));
    grossHint.setText("Gross proceeds: " + controller.formatMoney(value.gross()));
    commissionHint.setText("Commission: " + controller.formatMoney(value.commission()));
    taxHint.setText("Capital gains tax: " + controller.formatMoney(value.tax()));
    netProceedsHint.setText("Net proceeds: " + controller.formatMoney(value.netProceeds()));
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
    ThemeStyles.addStyleClasses(errorLabel, "text-error");
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
    if (stage.getTitle() != null && stage.getTitle().toLowerCase().contains("sell")) {
      ThemeStyles.styleDangerButton(confirm);
    } else {
      ThemeStyles.styleAccentButton(confirm);
    }
    ThemeStyles.styleButton(cancel);
    stage.setScene(scene);
    stage.showAndWait();
  }
}
