# Millions JavaFX Application

This project is a JavaFX-based application for simulating a stock game, developed as part of the IDATT2003 course. The application demonstrates the use of object-oriented design principles and best practices, and was developed collaboratively by two students using pair programming. The application was expanded from the core functional requirements to a more educational game by adding gamification elements.

## Table of Contents

- [Features](#features)
- [How to Run](#how-to-run)
- [Usage](#usage)
- [Saving and Loading](#saving-and-loading)
- [Player Management](#player-management)
- [Project Structure](#project-structure)
- [Contributors](#contributors)
- [Future Improvements](#future-improvements)

---

## Features

### Core simulation

- **Register / log in** with username and PIN; optional starting cash on registration
- Simulated **exchange** with individual stocks and composite **funds** (weighted baskets of stocks)
- **Buy and sell shares** — budget-based buys and quantity-based sells using the Command pattern; sales use **FIFO** lot ordering
- **Advance trading days** — market prices update and random **market events** may occur; regular savings plans execute on advance via **Savings → Advance 1 trading day** (the primary UI path)
- **Portfolio view** — cash balance, net worth, trading day, holdings table, and portfolio-vs-benchmark metrics (return, volatility, Sharpe ratio)
- **Stock detail** — price chart with optional market-event markers, mock fundamentals, trend-based buy/hold/sell recommendation, and educational chart tools (Fibonacci retracement, Elliott Wave, moon-phase overlay)
- **Commission (~1%) and capital-gains tax (~30%)** applied on sales (`SaleCalculator`)
- **Saved playthroughs** — named snapshots, delete, opt-in for leaderboard comparison
- **Local leaderboard** across profiles on the same machine (not online multiplayer)

### Educational / gamification

- **Player levels** — Novice → Investor → Speculator with trade-size limits; level-up notifications via observer pattern
- **Learning hub** — 10 Markdown articles rendered in a WebView (`src/main/resources/learninghub/`)
- **Quizzes** tied to learning content; wrong answers can link to related reading material
- In-app **notifications / toasts** for trades, level-ups, and other events

### Workspace tabs

Portfolio, Stocks, Funds, Savings, Saved runs, Leaderboard, Learning hub, Quiz, Notifications.

### Course requirements

Mapped against the IDATT2003 functional requirements. Full design discussion, diagrams, and screenshots are in the course report repo: [`../Z_idatt2003_grp24_millions`](../Z_idatt2003_grp24_millions).

| Requirement | Notes |
| --- | --- | --- |
| Start game with starting capital + stock data from file | Bundled `demo-stocks.csv`; no per-user file picker at registration |
| Stock info and statistics | Stock detail, charts, mock fundamentals, recommendations |
| Buy and sell shares | Budget-based buy + quantity sell |
| Transaction history |  Model + CLI menu option; no dedicated JavaFX transactions tab yet |
| Market winners/losers |  `Exchange.getGainers` / `getLosers` — not surfaced in the Stocks UI |
| Advance week → prices update | Implemented as **trading days** (not weeks) |
| Net worth and status | Portfolio tab |
| Sell all holdings and exit | Not implemented in CLI or GUI — see [Future improvements](#future-improvements) |
| Multi-user / continue later | Local profiles under `~/.millions/profiles` |

### Tech stack

Java 25, JavaFX 25.0.1 (programmatic UI — no FXML), Jackson 2.21.2 (JSON persistence), CommonMark 0.22.0 (learning content), JUnit 6.0.1, Maven 3.9+. CI builds on JDK 25 via GitHub Actions (`.github/workflows/workflow.yml`).

---

## How to Run

### Prerequisites

- **JDK 25** (Temurin or compatible)
- **Apache Maven 3.9+**
- A normal desktop environment for the GUI (CI uses `xvfb-run` for headless builds)
- No separate JavaFX SDK install — OpenJFX is pulled by Maven

### Running the application

1. **Clone the repository** and navigate to the project root.
2. **Run the JavaFX application** using Maven:

   ```sh
   mvn javafx:run
   ```

   This launches the login screen. After authentication you enter the tabbed workspace.

   The course workflow expects this JavaFX entry point (`view.app.MillionsApp`). A secondary **CLI** exists via `Main.java` → `cli.UserInterface` for IDE or advanced use; there is no Maven exec target for it.

### Tests and packaging

```sh
mvn test              # unit tests
mvn clean package     # compile, test, package (+ Javadoc JAR)
```

Course evaluators: see [`docs/TEST_RUBRIC_MAPPING.md`](docs/TEST_RUBRIC_MAPPING.md) for rubric-to-test mapping.

---

## Usage

First-time flow in the JavaFX app:

1. **Launch** → choose **Login** or **Register**.
   - Username: 3–32 characters (letters, numbers, `_`, `-`)
   - PIN: 4–8 digits
   - Starting money: non-negative number
2. After login, the **tabbed workspace** opens; the header shows session summary and avatar.
3. **Stocks / Funds** — browse the market, open detail views (chart, recommendation, market events), buy by budget or sell by quantity. Sales deduct commission and capital-gains tax on profits.
4. **Savings** — create and manage regular savings plans. Use **Advance 1 trading day** here to move simulation time forward (do not use the Notifications tab for time advance — that button is a demo placeholder).
5. **Portfolio** — review balance, net worth, trading day, holdings, and performance vs benchmark.
6. **Saved runs** — save the current state with a label, toggle leaderboard inclusion, delete old runs.
7. **Learning hub / Quiz** — read articles and test knowledge; quizzes link to reading on wrong answers.
8. **Switch user** or **Logout** from the workspace header. Closing the window auto-saves the active session.

Welcome preferences (`hasSeenWelcome` in `preferences.json`) are persisted per profile; the welcome dialog component exists but is not yet shown from the JavaFX workspace.

---

## Saving and Loading

Two persistence concepts:

| Mechanism | What | When |
| --- | --- | --- |
| **Auto game state** | Full player + exchange snapshot per profile | Logout, window close, switching users, registering while another session is active |
| **Saved runs** | Optional labeled snapshots for comparison / leaderboard | User action on the Saved runs tab |

### On-disk layout

Profiles are stored under `~/.millions/profiles/`:

```
~/.millions/profiles/<normalized-username>/
  account.json       # username, hashed PIN, optional display name
  game-state.json    # player + exchange snapshot
  preferences.json   # e.g. welcome dialog seen
  avatar.png         # optional profile image
  runs/<uuid>.json   # saved playthrough snapshots
```

PINs are hashed via `PinHashingService`. New sessions always load the bundled market universe from `src/main/resources/data/demo-stocks.csv`. Login restores the last saved game state via `AuthService` and `GamePersistenceService`.

---

## Player Management

- **Registration** — unique username, hashed PIN, starting balance; creates a fresh exchange from bundled CSV data
- **Login** — PIN verification, then load saved snapshot
- **Profile metadata** — display name (defaults to username), avatar from PNG/JPEG upload (`ProfileService`)
- **Progression** — three levels (Novice, Investor, Speculator) with different `maxTradeSize` caps; upgrades when portfolio/value thresholds are met
- **Delete profile** — available in the CLI (menu option 21, PIN required). The JavaFX profile editor button in the header is still a placeholder
- **i18n** — CLI supports `java Main nb` for Norwegian (`messages_nb.properties`); JavaFX UI is English-first

Profiles are local to the machine — this is not online multiplayer.

---

## Project Structure

```
src/main/java/
  view/          JavaFX pages, components, theme, app entry (MillionsApp)
  controller/    Workspace and page controllers
  model/         Domain: market, player, trading, persistence, session, learning, analysis
  cli/           Terminal UI (UserInterface)
  util/          I18n, MarkdownLoader, validators
src/main/resources/
  data/          demo-stocks.csv
  learninghub/   Markdown lessons
  css/           Stylesheets
  messages*.properties
src/test/java/   JUnit tests mirroring model/controller packages
docs/            Test rubric mapping (TEST_RUBRIC_*.md)
```

Architecture: **model** holds business rules; **controllers** wire session state to views; **views** are JavaFX-only (MVC, no FXML).

---

## Future Improvements

From code placeholders, course report conclusions, and user testing feedback:

- In-app **help / onboarding** (header help button is a placeholder)
- **Profile editor** in the GUI: display name, avatar upload, delete profile
- **Transaction history** tab in JavaFX (model and CLI already support it)
- **Market gainers/losers** surfaced in the Stocks UI (model ready)
- **Sell entire portfolio on exit** (course use case — not implemented)
- **Learning progress** persisted across sessions
- Optional custom market-data CSV at registration (report screenshot; not in current register flow)
- Clearer **commission/fee** labeling in the UI
- Simpler navigation for beginners (fewer overwhelming tabs)
- Wire or remove the misleading **Advance by weeks** demo control on the Notifications tab
- Show the **welcome dialog** on first login (backend preferences already exist)

---

## Contributors

- **Kevin Dennis Mazali**
- **Kaamya Shinde**

---
