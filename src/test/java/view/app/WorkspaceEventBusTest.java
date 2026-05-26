package view.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static view.app.events.WorkspaceEventType.MARKET_CHANGED;
import static view.app.events.WorkspaceEventType.PORTFOLIO_CHANGED;
import static view.app.events.WorkspaceEventType.SAVINGS_CHANGED;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import view.app.events.WorkspaceEventBus;

/**
 * Tests automatic workspace event dispatch.
 */
class WorkspaceEventBusTest {

  @Test
  void publishInvokesSubscribedHandler() {
    WorkspaceEventBus events = new WorkspaceEventBus();
    AtomicInteger calls = new AtomicInteger();

    events.subscribe(PORTFOLIO_CHANGED, calls::incrementAndGet);
    events.publish(PORTFOLIO_CHANGED);

    assertEquals(1, calls.get());
  }

  @Test
  void closedSubscriptionStopsFutureNotifications() {
    WorkspaceEventBus events = new WorkspaceEventBus();
    AtomicInteger calls = new AtomicInteger();

    WorkspaceEventBus.Subscription subscription =
        events.subscribe(PORTFOLIO_CHANGED, calls::incrementAndGet);
    subscription.close();
    events.publish(PORTFOLIO_CHANGED);

    assertEquals(0, calls.get());
  }

  @Test
  void multiEventPublishRunsSharedHandlerOnce() {
    WorkspaceEventBus events = new WorkspaceEventBus();
    AtomicInteger calls = new AtomicInteger();
    Runnable handler = calls::incrementAndGet;

    events.subscribe(PORTFOLIO_CHANGED, handler);
    events.subscribe(MARKET_CHANGED, handler);
    events.publish(PORTFOLIO_CHANGED, MARKET_CHANGED);

    assertEquals(1, calls.get());
  }

  @Test
  void savingsAndPortfolioPublishRunsSharedHandlerOnce() {
    WorkspaceEventBus events = new WorkspaceEventBus();
    AtomicInteger calls = new AtomicInteger();
    Runnable handler = calls::incrementAndGet;

    events.subscribe(SAVINGS_CHANGED, handler);
    events.subscribe(PORTFOLIO_CHANGED, handler);
    events.publish(SAVINGS_CHANGED, PORTFOLIO_CHANGED);

    assertEquals(1, calls.get());
  }
}
