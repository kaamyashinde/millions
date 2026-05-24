package view.layout;

/**
 * Layouts that adjust their structure when the host window is resized.
 */
public interface ResponsiveLayout {

  /**
   * Called when the scene width or height changes.
   *
   * @param width current scene width in pixels
   * @param height current scene height in pixels
   */
  void onWindowResized(double width, double height);
}
