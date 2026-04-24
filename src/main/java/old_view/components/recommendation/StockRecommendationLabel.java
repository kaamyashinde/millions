package old_view.components.recommendation;

import static model.utils.Validator.checkNotNull;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import recommendation.StockRecommendation;

/**
 * Reusable JavaFX label that renders a stock recommendation as a styled badge.
 *
 * <p>The control centralizes text and colour mapping for {@link StockRecommendation} so multiple
 * views can show recommendations consistently.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class StockRecommendationLabel extends Label {

  private static final String BASE_STYLE =
      "-fx-background-radius: 999;"
          + "-fx-border-radius: 999;"
          + "-fx-padding: 6 12 6 12;"
          + "-fx-font-weight: bold;";

  private StockRecommendation recommendation;

  /**
   * Creates a label configured for the supplied recommendation.
   *
   * @param recommendation recommendation to render
   */
  public StockRecommendationLabel(StockRecommendation recommendation) {
    setFont(Font.font("System", FontWeight.BOLD, 12));
    setRecommendation(recommendation);
  }

  /**
   * Updates the rendered recommendation and its badge styling.
   *
   * @param recommendation recommendation to render
   * @throws NullPointerException if {@code recommendation} is null
   */
  public final void setRecommendation(StockRecommendation recommendation) {
    checkNotNull(recommendation, "Recommendation");
    this.recommendation = recommendation;
    setText(recommendation.getDisplayText());
    setStyle(BASE_STYLE + styleFor(recommendation));
  }

  /**
   * Returns the recommendation currently rendered by the badge.
   *
   * @return current recommendation
   */
  public StockRecommendation getRecommendation() {
    return recommendation;
  }

  /**
   * Maps a recommendation to its badge colours.
   *
   * @param recommendation recommendation to style
   * @return JavaFX inline style fragment
   */
  private static String styleFor(StockRecommendation recommendation) {
    return switch (recommendation) {
      case BUY -> "-fx-background-color: #e8f7ee;-fx-border-color: #1f9d55;-fx-text-fill: #176f3d;";
      case HOLD ->
          "-fx-background-color: #fff6d9;-fx-border-color: #c58b00;-fx-text-fill: #8a5a00;";
      case SELL ->
          "-fx-background-color: #fdeaea;-fx-border-color: #cc3d3d;-fx-text-fill: #9d2020;";
    };
  }
}
