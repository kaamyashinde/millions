package controller;

import static util.Validator.checkNotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import model.exception.auth.AuthenticationException;
import model.exception.auth.RegistrationValidationException;
import model.session.ExitGameResult;
import model.session.SessionService;

/**
 * Handles exit-game flow: liquidate holdings and delete the active profile.
 */
public final class ExitGameController {

  private final SessionService sessionService;

  /**
   * @param sessionService session API for exit operations
   */
  public ExitGameController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  public SessionService getSessionService() {
    return sessionService;
  }

  /**
   * @return number of distinct symbols currently held
   */
  public int countHeldSymbols() {
    return sessionService.getActiveSession()
        .map(session -> {
          Set<String> symbols = new LinkedHashSet<>();
          session.player().getPortfolio().getShares().stream()
              .map(share -> share.getAsset().getSymbol())
              .forEach(symbols::add);
          return symbols.size();
        })
        .orElse(0);
  }

  /**
   * @return true when the active player holds at least one share lot
   */
  public boolean hasHoldings() {
    return countHeldSymbols() > 0;
  }

  /**
   * Liquidates all holdings, clears savings plans, and deletes the profile.
   *
   * @param pin PIN confirming the action
   * @return summary of the exit operation
   * @throws AuthenticationException if PIN is wrong
   * @throws RegistrationValidationException if PIN format is invalid
   */
  public ExitGameResult exitGameAndDeleteProfile(char[] pin)
      throws AuthenticationException, RegistrationValidationException {
    return sessionService.exitGameAndDeleteProfile(pin);
  }
}
