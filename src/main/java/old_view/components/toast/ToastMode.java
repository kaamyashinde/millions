package old_view.components.toast;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Enum representing the different modes (severity levels) of a toast notification. Each mode
 * defines its own symbol, icon shape, and optional hex metadata ({@link #getColorHex()}).
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-03-29
 */
public enum ToastMode {
  ERROR("#FF4444", "i") {
    @Override
    public Shape createShape() {
      double r = ICON_SIZE / 2.0;
      Polygon hex = new Polygon();
      for (int i = 0; i < 6; i++) {
        double angle = Math.toRadians(-30 + 60 * i);
        hex.getPoints().addAll(r * Math.cos(angle), r * Math.sin(angle));
      }
      hex.setFill(Color.TRANSPARENT);
      hex.setStroke(DEFAULT_STROKE);
      hex.setStrokeWidth(STROKE);
      return hex;
    }
  },

  WARNING("#FFA500", "i") {
    @Override
    public Shape createShape() {
      double h = ICON_SIZE / 2.0;
      Polygon triangle = new Polygon(
          0.0, -h,
          h, h,
          -h, h
      );
      triangle.setFill(Color.TRANSPARENT);
      triangle.setStroke(DEFAULT_STROKE);
      triangle.setStrokeWidth(STROKE);
      return triangle;
    }
  },

  INFO("#2196F3", "i") {
    @Override
    public Shape createShape() {
      Rectangle rect = new Rectangle(ICON_SIZE, ICON_SIZE);
      rect.setArcWidth(6);
      rect.setArcHeight(6);
      rect.setFill(Color.TRANSPARENT);
      rect.setStroke(DEFAULT_STROKE);
      rect.setStrokeWidth(STROKE);
      return rect;
    }
  },

  SUCCESS("#4CAF50", "✓") {
    @Override
    public Shape createShape() {
      Circle circle = new Circle(ICON_SIZE / 2.0);
      circle.setFill(Color.TRANSPARENT);
      circle.setStroke(DEFAULT_STROKE);
      circle.setStrokeWidth(STROKE);
      return circle;
    }
  };

  static final double ICON_SIZE = 40;
  static final double STROKE = 2;
  private static final Color DEFAULT_STROKE = Color.BLACK;

  private final String colorHex;
  private final String symbol;

  ToastMode(String colorHex, String symbol) {
    this.colorHex = colorHex;
    this.symbol = symbol;
  }

  /**
   * Builds and returns the outline shape for this mode with a default stroke.
   *
   * @return the styled shape
   */
  public abstract Shape createShape();

  /**
   * Returns the hex color associated with this toast mode.
   *
   * @return the color hex string for this mode
   */
  public String getColorHex() {
    return colorHex;
  }

  /**
   * Returns the symbol rendered inside this mode's icon.
   *
   * @return the icon symbol for this mode
   */
  public String getSymbol() {
    return symbol;
  }
}
