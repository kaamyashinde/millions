package view.pages.funds;

import java.math.BigDecimal;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import controller.TradingController;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import view.components.table.AppTableView;
import view.dialogs.TradeDialog;
import view.theme.ThemeStyles;
import view.util.UiFormat;

/**
 * Dedicated fund detail view showing the latest derived price and composite holdings.
 */
public class FundDetailView extends BorderPane {

  private final Label titleLabel = new Label("Fund Details");
  private final Label subtitleLabel = new Label("Select a fund to inspect its stock composition.");
  private final Label latestPriceLabel = new Label("Latest Price: -");
  private final AppTableView<FundComponent> componentList =
      new AppTableView<>("No component holdings to show.");
  private final HBox tradeActionsBox = new HBox(10);
  private final Button buyButton = new Button("Buy");
  private Fund selectedFund;
  private TradingController tradingController;
  private Supplier<Window> dialogOwnerSupplier;
  private Runnable onTradeComplete;

  /**
   * Builds an initially empty fund detail view.
   */
  public FundDetailView() {
    setPadding(new Insets(16));
    setPrefWidth(380);

    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
    subtitleLabel.setWrapText(true);

    TableColumn<FundComponent, String> symbolColumn =
        AppTableView.createTextColumn("Symbol", c -> c.stock().getSymbol());
    TableColumn<FundComponent, String> companyColumn =
        AppTableView.createTextColumn("Company", c -> c.stock().getCompany());
    TableColumn<FundComponent, BigDecimal> weightColumn =
        AppTableView.createNumericColumn(
            "Weight", FundComponent::weight, UiFormat::decimal);
    componentList.getColumns().addAll(symbolColumn, companyColumn, weightColumn);

    ThemeStyles.styleAccentButton(buyButton);
    tradeActionsBox.getChildren().add(buyButton);
    tradeActionsBox.setVisible(false);
    tradeActionsBox.setManaged(false);

    VBox content = new VBox(12, titleLabel, subtitleLabel, latestPriceLabel, tradeActionsBox, componentList);
    VBox.setVgrow(componentList, Priority.ALWAYS);
    setCenter(content);
  }

  /**
   * Configures buy action shown when a fund is selected.
   *
   * @param trading controller used to execute buy actions
   * @param dialogOwnerSupplier supplies the owner window for trade dialogs
   * @param onTradeComplete callback invoked after a completed trade
   */
  public void setTradeHandlers(
      TradingController trading,
      Supplier<Window> dialogOwnerSupplier,
      Runnable onTradeComplete) {
    this.tradingController = trading;
    this.dialogOwnerSupplier = dialogOwnerSupplier;
    this.onTradeComplete = onTradeComplete;
    buyButton.setOnAction(_ -> {
      if (selectedFund == null || tradingController == null || dialogOwnerSupplier == null) {
        return;
      }
      TradeDialog.showBuy(
          dialogOwnerSupplier.get(),
          tradingController,
          selectedFund.getSymbol(),
          onTradeComplete);
    });
    updateTradeActions(selectedFund);
  }

  /**
   * Displays the selected fund and all of its component weights.
   *
   * @param fund selected fund, or {@code null} to show the empty state
   */
  public void showFund(Fund fund) {
    selectedFund = fund;
    if (fund == null) {
      titleLabel.setText("Fund details");
      subtitleLabel.setText("Select a fund to inspect its stock composition.");
      latestPriceLabel.setText("Latest price: -");
      componentList.setItems(FXCollections.observableArrayList());
      updateTradeActions(null);
      return;
    }
    titleLabel.setText(fund.getSymbol() + " · " + fund.getDisplayName());
    subtitleLabel.setText("Composite fund built from weighted stock holdings.");
    latestPriceLabel.setText("Latest price: " + UiFormat.decimal(fund.getSalesPrice()));
    componentList.setItems(FXCollections.observableArrayList(fund.getComponents()));
    updateTradeActions(fund);
  }

  /**
   * Refreshes the currently selected fund after stock prices move.
   */
  public void refresh() {
    showFund(selectedFund);
  }

  /**
   * Returns the fund currently displayed in the detail view.
   *
   * @return selected fund, or {@code null} when the view is empty
   */
  public Fund getSelectedFund() {
    return selectedFund;
  }

  private void updateTradeActions(Fund fund) {
    if (tradingController == null || fund == null) {
      tradeActionsBox.setVisible(false);
      tradeActionsBox.setManaged(false);
      return;
    }
    tradeActionsBox.setVisible(true);
    tradeActionsBox.setManaged(true);
    buyButton.setText("Buy " + fund.getSymbol());
  }
}
