# Millions JavaFX Application

This project is a JavaFX-based stock market simulation game built for the IDATT2003 course. It was developed collaboratively using pair programming, with a focus on object-oriented design and educational gamification.

## Table of Contents

- [Features](#features)
- [How to Run](#how-to-run)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements)
- [Contributors](#contributors)

---

## Features

- **JavaFX GUI**: Tab-based desktop interface for trading, portfolio tracking, learning content, and saved runs.
- **Trading Simulation**: Buy/sell stocks and funds, advance trading days, and track net worth over time.
- **Market Movers**: Stocks view includes Winners and Losers tables for top daily movers.
- **Portfolio Analytics**: View holdings, balance, benchmark comparison, and key performance metrics.
- **Transaction History**: Dedicated Transactions tab for reviewing trade activity over time.
- **Persistence**: Auto-save per user profile plus named saved runs in JSON format.
- **Profile Editor**: Update display name/avatar and manage profile actions from the workspace.
- **Player Progression**: Level system (Novice, Investor, Speculator) with progression-based constraints.
- **Learning Mode**: Built-in learning hub articles and quizzes for investment concepts.
- **Local Leaderboard**: Compare saved run performance across local profiles on the same machine.

---

## How to Run

### Prerequisites

- Java 25 (JDK)
- Maven 3.9+

### Running the Application

1. Clone the repository and navigate to the project root.
2. Run:

   ```sh
   mvn javafx:run
   ```

This launches the login screen and then the main workspace.

---

## Usage

1. **Login or Register** to create/load your local profile.
2. **Trade Stocks and Funds** by buying with budget and selling by quantity.
3. **Advance Trading Days** to update prices and trigger market changes.
4. **Track Progress** in the Portfolio tab (cash, net worth, holdings, performance).
5. **Save and Resume** using auto-save and saved runs. Profiles are stored under `~/.millions/profiles/`.

---

## Project Structure

```text
src/main/java/
  Main.java                 # CLI entry point (optional; GUI uses MillionsApp)
  cli/                      # Terminal menu UI (UserInterface)
  controller/               # Bridges session/model state to JavaFX pages
  model/
    analysis/               # Recommendations, performance metrics, chart tools
    core/                   # Domain: stocks, funds, exchange, player, portfolio
    exception/              # Typed domain exceptions (auth, trading, market, …)
    learning/               # Learning hub content model and quiz logic
    persistence/            # JSON/file I/O for profiles, game state, market data
    session/                # Auth, profiles, leaderboard, validation, exit game
    trading/                # Buy/sell commands, transactions, savings, calculators
  util/                     # CSV validation, i18n helpers, shared utilities
  view/
    app/                    # MillionsApp entry, workspace tabs, event bus
    components/             # Reusable UI: charts, tables, toasts, notifications
    dialogs/                # Modal dialogs (profile editor, exit game, …)
    layout/                 # Shared layouts (auth, workspace shell)
    pages/                  # Tab pages: auth, stocks, funds, portfolio, quiz, …
    theme/                  # CSS theme loading (light/dark)
    validation/             # JavaFX form validation helpers

src/main/resources/
  css/                      # Application stylesheets
  data/                     # Bundled market CSV and event templates
  learninghub/              # Markdown lessons, quiz JSON, catalog
  messages*.properties      # UI strings (incl. Norwegian nb variant)

src/test/java/              # JUnit tests mirroring model/controller/view packages
```

Architecture: **model** (rules) → **controller** (wiring) → **view** (JavaFX only, no FXML).

---

## Future Improvements

- Improve first-time onboarding and in-app help
- Persist and visualize learning progress more clearly

---

## Contributors

- **Kevin Dennis Mazali**
- **Kaamya Shinde**
