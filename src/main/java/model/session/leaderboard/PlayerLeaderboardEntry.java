package model.session.leaderboard;

import java.math.BigDecimal;

/**
 * Immutable leaderboard snapshot for one saved player profile.
 *
 * @param username           display username
 * @param netWorth           current total net worth
 * @param totalReturnPercent total return as a decimal ratio (for example 0.25 = 25%)
 */
public record PlayerLeaderboardEntry(
    String username,
    BigDecimal netWorth,
    BigDecimal totalReturnPercent
) {

}
