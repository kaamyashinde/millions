package view.app.events;

/**
 * Describes workspace state changes that visible pages can observe.
 */
public enum WorkspaceEventType {
  /** Player holdings, balance, or derived portfolio metrics changed. */
  PORTFOLIO_CHANGED,

  /** Market day, listed asset prices, or market event context changed. */
  MARKET_CHANGED,

  /** Regular savings plans or their due dates changed. */
  SAVINGS_CHANGED,

  /** Profile display name or avatar changed. */
  PROFILE_CHANGED,

  /** Buy or sell transaction history changed. */
  TRANSACTIONS_CHANGED,

  /** Persisted leaderboard data changed. */
  LEADERBOARD_CHANGED
}
