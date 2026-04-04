package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.persistence.MarketData;
import model.persistence.MarketDataLoader;
import model.session.SessionService;
import model.session.SessionServiceFactory;

/**
 * Session-aware JavaFX entry point for the multi-user GUI.
 */
public class MillionsGuiApp extends Application {

  private static final String EXCHANGE_NAME = "NYSE";
  private static final String MARKET_DATA_RESOURCE = "/data/demo-stocks.csv";

  @Override
  public void start(Stage stage) {
    GuiAppShell shell = new GuiAppShell(createSessionService());
    stage.setScene(new Scene(shell, 980, 720));
    stage.setTitle("Millions");
    stage.setOnCloseRequest(_ -> shell.shutdown());
    stage.show();
  }

  /**
   * Launches the session-aware JavaFX application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  private static SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        SessionServiceFactory.defaultProfilesRoot(),
        MillionsGuiApp::loadMarketData,
        EXCHANGE_NAME);
  }

  private static MarketData loadMarketData() {
    MarketData marketData = MarketDataLoader.loadFromResource(MillionsGuiApp.class, MARKET_DATA_RESOURCE);
    if (marketData.isEmpty()) {
      throw new IllegalStateException("Could not load bundled market data from " + MARKET_DATA_RESOURCE);
    }
    return marketData;
  }
}
