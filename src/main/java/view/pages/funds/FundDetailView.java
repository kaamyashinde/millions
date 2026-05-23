package view.pages.funds;

import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import view.components.table.AppTableView;

/**
 * Dedicated fund detail view showing the latest derived price and composite holdings.
 */
public class FundDetailView extends BorderPane {

  private final Label titleLabel = new Label("Fund Details");
  private final Label subtitleLabel = new Label("Select a fund to inspect its stock composition.");
  private final Label latestPriceLabel = new Label("Latest Price: -");
  private final AppTableView<FundComponent> componentList =
      new AppTableView<>("No component holdings to show.");
  private Fund selectedFund;

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
            "Weight", FundComponent::weight, BigDecimal::toPlainString);
    componentList.getColumns().addAll(symbolColumn, companyColumn, weightColumn);

    VBox content = new VBox(12, titleLabel, subtitleLabel, latestPriceLabel, componentList);
    VBox.setVgrow(componentList, Priority.ALWAYS);
    setCenter(content);
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
      return;
    }
    titleLabel.setText(fund.getSymbol() + " · " + fund.getDisplayName());
    subtitleLabel.setText("Composite fund built from weighted stock holdings.");
    latestPriceLabel.setText("Latest price: " + fund.getSalesPrice().toPlainString());
    componentList.setItems(FXCollections.observableArrayList(fund.getComponents()));
  }

  /**
   * Refreshes the currently selected fund after stock prices move.
   */
  public void refresh() {
    showFund(selectedFund);
  }
}
