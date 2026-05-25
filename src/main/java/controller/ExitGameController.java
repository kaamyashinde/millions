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
 *
 * <p>This controller delegates destructive session work to {@link SessionService} while exposing
 * small UI helpers for confirmation views.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-05-23
 */
public final class ExitGameController {

  private final SessionService sessionService;

  /**
   * Creates an exit-game controller for the active session service.
   *
   * @param sessionService session API for exit operations
   */
  public ExitGameController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  /**
   * Exposes the session API used by this controller.
   *
   * @return session API used by this controller
   */
  public SessionService getSessionService() {
    return sessionService;
  }

  /**
   * Counts how many distinct symbols the active player currently owns.
   *
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
   * Checks whether the active player has anything to liquidate.
   *
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
