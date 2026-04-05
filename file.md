
Here is a **practical plan** that matches your course rule (“patterns only when they earn their keep”) and pushes you toward **8+/10** by improving **cohesion**, **lowering coupling**, and using **interfaces / hierarchy / polymorphism where the domain actually branches**—not by adding new named patterns.

---

## What “over 8” means for this project

A strict grader will raise scores when:

1. **Abstractions match real variation** — `Purchase` vs `Sale`, `Stock` vs `Fund` are expressed as **behavior on the types**, not `instanceof` in services.
2. **Big units are split by reason to change** — especially `Exchange` and `UserInterface`, without inventing a “Command pattern” unless you truly need undo/queues/scripting.
3. **Named patterns appear only where they remove duplication or isolate volatility** — keep Strategy for market/recommendation, Template Method on `Transaction`, Facade for session; **drop** patterns that are just composition tricks (e.g. a three-link CoR that never grows).

Below is an ordered plan; each phase has a measurable effect on the rubric.

---

## Phase A — Polymorphism on `Transaction` (high impact, medium effort)

**Problem today:** `GameStateMapper`, `PortfolioPerformanceService`, and CLI branch on concrete types. That is **accidental coupling**: every new transaction kind would force edits in three places.

**Direction (effective OOP, not a new “pattern”):**

- On `Transaction`, add **two small polymorphic hooks** (names illustrative):
  - **Persistence:** e.g. `String persistenceKind()` or `enum` + `name()` for JSON (`PURCHASE` / `SALE`). `GameStateMapper.toTransactionSnapshot` calls **only** this; no `instanceof`.
  - **Analytics replay:** e.g. `BigDecimal replayForPerformance(Portfolio portfolio, BigDecimal cash)` implemented in `Purchase` / `Sale` using the same calculator fields you already have (`PurchaseCalculator` / `SaleCalculator`). `PortfolioPerformanceService.applyTransaction` becomes a one-liner delegating to `transaction.replayForPerformance(...)`.

**Why this raises the score:** real **hierarchy + polymorphism**, Open/Closed for new transaction types, **mapper and analysis decoupled** from subclasses.

**Tests:** extend `Transaction` / `Exchange` / mapper tests so behavior is unchanged; add one test that would fail if someone reintroduces `instanceof` in the mapper.

**CLI:** replace `t instanceof Purchase` with something derived from the same API (e.g. `persistenceKind()` or a tiny `Transaction` method used only for display), so the CLI does not own type knowledge either.

---

## Phase B — Historical pricing on `InvestableAsset` (high impact, small–medium effort)

**Problem today:** `HistoricalAssetPriceService.getPriceOnDay` uses `instanceof Stock` / `Fund`. The service is a **type switch** pretending to be a service.

**Direction:**

- Add to `InvestableAsset`:  
  `BigDecimal getPriceOnTradingDay(int day)`  
  (or a name your team agrees on).
- **`Stock`:** implement using the existing historical price list (same rules as today’s `getStockPriceOnDay`).
- **`Fund`:** implement using components and each component stock’s `getPriceOnTradingDay` (same math as today’s `getFundPriceOnDay`).

Then `HistoricalAssetPriceService` either **disappears** or becomes a thin wrapper that only validates `day` and delegates to `asset.getPriceOnTradingDay(day)`—one code path, **no subtype tests**.

**Why this raises the score:** polymorphism lives on the **domain types** that actually vary; analysis code stops knowing the class graph.

---

## Phase C — Replace decorative Chain-of-Responsibility with ordinary validation (low effort, clears “pattern theater”)

**Problem today:** `RegistrationValidator` + `then()` is a **named pattern** for a **fixed, tiny** pipeline. It does not buy extensibility you use.

**Direction:**

- One class, e.g. `RegistrationValidation`, with **private** methods or package-private helpers: `validateUsername`, `validatePin`, `validateStartingMoney`, composed in one `validate(...)` returning `ValidationResult`.
- `AuthService` calls that single entry point (same public API).

**Why this raises the score:** same behavior, **less conceptual surface**; graders who penalize “patterns without benefit” stop seeing CoR as mandatory reading.

---

## Phase D — Simplify `PlayerLevel` without losing OOP (medium effort)

**Problem today:** sealed interface + three singleton classes + `PlayerLevels` is a lot of structure for three states.

**Direction:**

- Prefer a **`enum PlayerLevel`** with methods: `displayName()`, `maxTradeSize(Player)`, `qualifies(Player)`, and a static `PlayerLevel resolve(Player)` (or `resolveFrom(Player)`).  
  That is still **polymorphism** (enum constants with different behavior), with **far less coupling** between tiny types.

**Persistence:** keep stable string ids (`NOVICE`, …) as you already document on `PlayerLevel`.

**Why this raises the score:** cohesion of “progression rules” in one place; easier to read than a tiny sealed graph.

---

## Phase E — Split `Exchange` by responsibility (medium–large effort, biggest cohesion win)

**Problem today:** ~600 lines mixing listings, simulation, events, and trading. Multiple reasons to change ⇒ **low cohesion**, central coupling.

**Direction (no need to name a pattern):**

- Extract **package-private** collaborators used only by `Exchange`, e.g.:
  - **Listings / lookup** — maps, `findStocks`, `getAsset`, …
  - **Market clock / prices / events** — `advanceDay`, strategies, history
  - **Trade execution** — the private `executeBuy` / `executeSell*` helpers (could be a `final` class `ExchangeTrades` with a back-reference to `Exchange` only if it needs `getDay()` / `getAsset`)

`Exchange` stays the **facade** your CLI/GUI already use; internals get **high cohesion** files.

**Why this raises the score:** this is the main lever to move **cohesion** and **coupling** toward 8+ without adding textbook patterns.

---

## Phase F — Decompose `UserInterface` (large file, high cohesion gain)

**Problem today:** ~1200 lines in one CLI class is a cohesion anchor for the whole repo.

**Direction:**

- Split by **feature areas** into package-private classes under `cli/` (e.g. trading flow, savings, profile, reports), each taking the dependencies it needs (`Exchange`, `Player`, persistence callback).
- **Not** a GoF Command hierarchy unless you later need macro recording or server-side execution—plain **functions or small classes** are enough.

**Why this raises the score:** same “no fake patterns” rule, big **cohesion** improvement.

---

## What to **keep** (already effective)

- **Strategy** for recommendations and market simulation (volatility / events).
- **Template method** on `Transaction.commit`.
- **Facade** `SessionService` + wiring in `SessionServiceFactory`.
- **Observer** for real UI reactions (`PlayerObserver`), not for internal bookkeeping.
- **`ImageLoader` as a single-method interface** with one implementation (`DiskImageLoader`) — interface is justified (testing / swapping), not decorative.

---

## Suggested order and how far you need to go for **8+**

| Order | Phase | Effort | Lifts mainly |
|------|--------|--------|----------------|
| 1 | **A** Transaction polymorphism | Medium | Patterns / OOP / coupling |
| 2 | **B** Pricing on `InvestableAsset` | Small–medium | OOP / coupling |
| 3 | **C** Flatten registration validation | Small | “Effective patterns” perception |
| 4 | **D** `PlayerLevel` enum | Medium | Simplicity + cohesion |
| 5 | **E** Split `Exchange` | Medium–large | Cohesion / coupling |
| 6 | **F** Split CLI | Large | Cohesion |

**Realistic path to 8+:** complete **A + B + C** and at least **substantial progress on E** (or **E + F** if `Exchange` split is deferred). **D** helps the “not over-engineered” narrative. You do **not** need new named patterns to get there—you need **behavior on the right types** and **smaller, focused modules**.

---

## How you can argue it in a report (one sentence)

You standardized variation at **`Transaction` and `InvestableAsset`**, removed a **CoR that did not scale**, simplified **player levels**, and **refactored large classes** for cohesion—keeping Strategy/Template/Facade/Observer only where they **isolate changing algorithms or real notifications**.

If you want this turned into a **checklist file** in the repo (like your existing `plan-remove-overkill-patterns.md`), say so and we can align phase names and file paths with your branch.