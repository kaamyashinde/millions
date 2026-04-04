# Plan: Remove Overkill Design Patterns

This plan removes or simplifies the four patterns flagged as **overkill** in the architecture review: redundant **Command** usage, **Decorator** image loading, internal **Observer** for level recalculation, and the **singleton + utility** setup for `PlayerLevel`. It is ordered so each phase has clear prerequisites and a testable checkpoint.

**Out of scope here (optional / lower priority):** Chain-of-Responsibility for registration validation (borderline; keep unless you want one sequential `validateRegistration` method), and broader refactors (decomposing `Exchange`, fixing `Purchase`/`Sale` downcasts).

---

## Phase 1 — Remove `PlayerLevelObserver` (internal Observer)

**Goal:** Stop disguising `recalculateLevel()` as an observer. Keep `PlayerObserver` for real subscribers (e.g. `LevelUpNotificationObserver`).

**Steps**

1. In `Player`, remove construction-time registration of `PlayerLevelObserver` and delete `PlayerLevelObserver.java`.
2. Call `recalculateLevel()` explicitly wherever level-relevant state changes today. **Current behavior:** observers run only after `addMoney` and `withdrawMoney` (the only methods that call `notifyObservers()`). Mirror that by calling `recalculateLevel()` at the end of:
   - `addMoney(BigDecimal)`
   - `withdrawMoney(BigDecimal)`
3. Update Javadoc on `Player` / `recalculateLevel()` to state that level is updated on cash changes (and still after `Player.restore` via existing `recalculateLevel()` there).
4. **Tests:** Delete or rewrite `PlayerLevelObserverTest` — behavior is covered by `PlayerTest` / `PlayerLevelTest` if you assert level after money-changing flows. Ensure `LevelUpNotificationObserverTest` still passes (it uses `PlayerObserver` spies, not `PlayerLevelObserver`).

**Risk:** If any code path mutates portfolio or other level inputs **without** going through `addMoney`/`withdrawMoney`, level could become stale. Today that would also skip `notifyObservers()`, so this is **behavior-preserving**. Future work: if you add `notifyObservers()` to other mutators, add `recalculateLevel()` there too.

**Checkpoint:** All tests green; GUI level-up toasts still work (they depend on `LevelUpNotificationObserver`, not `PlayerLevelObserver`).

---

## Phase 2 — Collapse image loading to one implementation (remove Decorator stack)

**Goal:** Replace `FileImageLoader` + `ValidatingImageLoader` + `ImageLoaderDecorator` + optional `FallbackImageLoader` with a **single** class (e.g. `AvatarImageLoader` or `DiskImageLoader`) that:

- Returns `null` for `null` or non-regular paths (current `ValidatingImageLoader` behavior).
- Loads via `Files.newInputStream` like `FileImageLoader`.
- Optionally accepts a fallback `Image` in the constructor if you still need fallback anywhere; if nothing uses `FallbackImageLoader` in production, omit fallback until needed.

**Steps**

1. Grep for `FallbackImageLoader` in `src/main` — if unused in app code, drop fallback from the merged class or keep one optional constructor parameter.
2. Implement the merged class implementing `ImageLoader` (keep the **functional interface** — one method — that is not overkill).
3. Replace usages in `SessionWorkspaceView`, `ProfileEditorDialog`, `PlayerPortfolioPanel`, `LeaderboardPanel` (currently `new ValidatingImageLoader(new FileImageLoader())`).
4. Delete: `ImageLoaderDecorator`, `ValidatingImageLoader`, `FileImageLoader`, and `FallbackImageLoader` if folded in or unused.
5. **Tests:** Replace per-class tests with one test class for the merged loader (null path, missing file, valid file). Remove `ImageLoaderDecoratorTest` if the abstract class is gone.

**Checkpoint:** UI avatars still load; tests cover happy path and invalid paths.

---

## Phase 3 — Replace trade `TradeCommand` hierarchy with direct execution

**Goal:** Remove the interface + `describe()` + five command classes without losing behavior. `Exchange` remains the public API for buy/sell.

**Preferred approach (minimal churn)**

1. Add **private** methods on `Exchange` (or a **package-private** `TradeOperations` class in `model.trade` with only static/instance helpers — **not** a GoF Command):
   - `executeBuy(Player, String symbol, BigDecimal quantity)` → same body as `BuyCommand.execute`
   - `executeSell(Player, Share share)` → same as `SellCommand`
   - Same for budget buy, sell-by-qty loop, sell-up-to-target-net loop (lift bodies from existing classes).
2. Change public `Exchange` methods (`buy`, `buyUpToBudget`, `sell`, `sellByQuantity`, `sellUpToTargetNet`) to call these helpers directly instead of `new XCommand(...).execute(...)`.
3. Delete `TradeCommand.java` and the five command classes.
4. **Tests:** Move assertions from `BuyCommandTest`, `SellCommandTest`, etc. to `ExchangeTest` (or new `ExchangeTradeTest`) that call `exchange.buy(...)`, `exchange.sell(...)`, etc. Keep the same fixtures and expectations.

**Alternative (if you want smaller `Exchange`):** Move the lifted methods to a final package-private class `ExchangeTrades` constructed with `(Exchange exchange)` and call it from `Exchange`. Still no `TradeCommand` interface.

**Checkpoint:** No `TradeCommand` types left; trading tests pass via `Exchange` API.

---

## Phase 4 (optional, larger) — `PlayerLevel` as an `enum`

**Goal:** Remove `NoviceLevel` / `InvestorLevel` / `SpeculatorLevel` classes, `PlayerLevels` utility, and the sealed interface, if you want a simpler model.

**Steps**

1. Define `public enum PlayerLevel { NOVICE, INVESTOR, SPECULATOR }` with methods `displayName()`, `maxTradeSize(Player)`, `qualifies(Player)` implemented per constant (or delegate to small private static helpers).
2. Implement transition logic as a **static** method `PlayerLevel resolve(Player player)` on the enum (or on `Player`), replacing `PlayerLevels.NOVICE.checkTransition(this)`.
3. Update **persistence:** `SavedRunMapper` / JSON that stores level **by name** — ensure enum `name()` matches stored strings (`NOVICE`, etc.). Adjust mapping if anything used class names or different strings.
4. Replace `PlayerLevels.NOVICE` with `PlayerLevel.NOVICE` everywhere; delete old types.
5. Run all persistence and GUI tests that reference levels.

**When to skip:** If you prefer sealed types for coursework documentation or exhaustive switches across files, **keep Phase 4 out** and stop after Phases 1–3.

**Checkpoint:** Same runtime behavior; simpler type graph; serialization verified.

---

## Suggested order and effort

| Phase | Effort | Risk |
|------|--------|------|
| 1 — Observer removal | Small | Low |
| 2 — Image loader merge | Small | Low |
| 3 — Trade command removal | Medium | Low (mechanical) |
| 4 — Enum levels | Medium–large | Medium (persistence) |

**Recommended sequence:** **1 → 2 → 3**, then **4** only if you want the extra simplification.

---

## Verification checklist (after all desired phases)

- [ ] `mvn test` (or project test command) passes.
- [ ] Manual smoke: login, trade, avatar display, leaderboard avatars, level-up toast after qualifying actions.
- [ ] Grep confirms removed types are gone: `TradeCommand`, `PlayerLevelObserver`, `ValidatingImageLoader` (as applicable), old `NoviceLevel` (if Phase 4).
- [ ] Update `prog_practices.md` / course docs if they still describe the old patterns.

---

## Rollback strategy

Land each phase in a **separate commit** (or PR). If something breaks late, revert the last commit rather than mixing partial refactors.
