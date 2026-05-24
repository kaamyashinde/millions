package view.app.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Publishes workspace-scoped events to UI observers.
 */
public class WorkspaceEventBus {

  private final Map<WorkspaceEventType, List<Runnable>> subscribers =
      new EnumMap<>(WorkspaceEventType.class);

  /**
   * Registers a handler for one event type.
   *
   * @param eventType event to observe
   * @param handler handler invoked when the event is published
   * @return subscription that removes the handler when closed
   */
  public Subscription subscribe(WorkspaceEventType eventType, Runnable handler) {
    subscribers.computeIfAbsent(eventType, _ -> new ArrayList<>()).add(handler);
    return new Subscription(() -> unsubscribe(eventType, handler));
  }

  /**
   * Publishes one or more events to their current subscribers.
   *
   * @param eventTypes events to publish, in callback order
   */
  public void publish(WorkspaceEventType... eventTypes) {
    Set<Runnable> handlersToRun = new LinkedHashSet<>();
    for (WorkspaceEventType eventType : eventTypes) {
      handlersToRun.addAll(subscribers.getOrDefault(eventType, List.of()));
    }
    for (Runnable handler : handlersToRun) {
      handler.run();
    }
  }

  private void unsubscribe(WorkspaceEventType eventType, Runnable handler) {
    List<Runnable> handlers = subscribers.get(eventType);
    if (handlers == null) {
      return;
    }
    handlers.remove(handler);
    if (handlers.isEmpty()) {
      subscribers.remove(eventType);
    }
  }

  /**
   * Removes a registered event handler when closed.
   */
  public static final class Subscription implements AutoCloseable {

    private final Runnable unsubscribe;
    private boolean closed;

    private Subscription(Runnable unsubscribe) {
      this.unsubscribe = unsubscribe;
    }

    /**
     * Unsubscribes the handler once.
     */
    @Override
    public void close() {
      if (closed) {
        return;
      }
      unsubscribe.run();
      closed = true;
    }
  }
}
