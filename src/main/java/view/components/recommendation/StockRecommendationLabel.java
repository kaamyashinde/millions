package view.components.recommendation;

import static model.utils.Validator.checkNotNull;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.analysis.recommendation.StockRecommendation;
import view.theme.ThemeStyles;

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
    getStyleClass().removeAll("recommendation-buy", "recommendation-hold", "recommendation-sell");
    ThemeStyles.addStyleClasses(this, "recommendation-badge", styleClassFor(recommendation));
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
   * Maps a recommendation to its badge style class.
   *
   * @param recommendation recommendation to style
   * @return style class name
   */
  private static String styleClassFor(StockRecommendation recommendation) {
    return switch (recommendation) {
      case BUY -> "recommendation-buy";
      case HOLD -> "recommendation-hold";
      case SELL -> "recommendation-sell";
    };
  }
}
