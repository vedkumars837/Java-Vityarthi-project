# E-Library — Digital Content Subscription System

A command-line Java application that models a digital library platform
where users subscribe to **Free / Basic / Premium** tiers to access a
catalog of **books and video courses**, with tier-based access control,
usage quotas, and admin-facing analytics.

Built as a coursework project for **Programming in Java**, demonstrating
OOP design (abstraction, inheritance, polymorphism, enums), custom
exception handling, file-based persistence, and a modular service-layer
architecture — with no external dependencies required to build or run.

## Overview

The system is split into three core functional modules:

1. **User & Subscription Management** — registration, login, subscription
   tier upgrades/downgrades, renewal, and expiry tracking.
2. **Content Catalog Management** — adding, searching, and browsing books
   and courses, each with a minimum subscription tier requirement.
3. **Access Control & Usage Tracking** — enforces tier eligibility and
   monthly access quotas whenever a user tries to access content, and
   logs every access event for reporting.

A fourth cross-cutting **Reporting module** builds analytics (most
accessed content, tier distribution, most active users, category
breakdown) on top of the three core modules.

## Features

- Multi-tier subscriptions (`FREE`, `BASIC`, `PREMIUM`) with distinct
  monthly access quotas and content-tier gating.
- Polymorphic content catalog: `Book` and `Course` both extend an
  abstract `Content` class.
- SHA-256 password hashing — plaintext passwords are never stored.
- Custom, dependency-free JSON persistence (`JsonUtil`) — no database
  or external library required.
- Seven custom checked exceptions for precise error handling
  (`AccessDeniedException`, `SubscriptionExpiredException`,
  `QuotaExceededException`, `ContentNotFoundException`,
  `UserNotFoundException`, `DuplicateUserException`,
  `InvalidInputException`).
- File-based logging (`logs/application.log`) of key events and errors.
- Admin console for catalog management and usage analytics.
- Pre-seeded sample catalog and a bootstrap admin account on first run.

## Technologies / Tools Used

- **Java 17+** (standard library only — `java.time`, `java.security`,
  `java.nio.file`, `java.util`)
- No external dependencies, build tool, or database required
- JSON files for persistence
- Mermaid diagrams for architecture/UML documentation (render natively
  on GitHub — see [`docs/`](docs/))

## Project Structure

```
elibrary-system/
├── src/com/elibrary/
│   ├── Main.java                     # CLI entry point
│   ├── model/                        # Domain model
│   │   ├── Content.java              # abstract base class
│   │   ├── Book.java
│   │   ├── Course.java
│   │   ├── SubscriptionTier.java     # enum: FREE, BASIC, PREMIUM
│   │   ├── Subscription.java
│   │   ├── User.java
│   │   └── AccessRecord.java
│   ├── service/                      # Business logic (3 core modules)
│   │   ├── CatalogService.java
│   │   ├── SubscriptionService.java
│   │   ├── AccessService.java
│   │   └── ReportService.java
│   ├── util/                         # Persistence, security, logging
│   │   ├── FileStorageService.java
│   │   ├── JsonUtil.java
│   │   ├── PasswordUtil.java
│   │   └── AppLogger.java
│   └── exception/                    # Custom checked exceptions
│       ├── ELibraryException.java
│       ├── AccessDeniedException.java
│       ├── SubscriptionExpiredException.java
│       ├── QuotaExceededException.java
│       ├── ContentNotFoundException.java
│       ├── UserNotFoundException.java
│       ├── DuplicateUserException.java
│       └── InvalidInputException.java
├── data/            # JSON data files (created automatically on first run)
├── logs/            # application.log (created automatically on first run)
├── docs/            # Architecture, UML, and data-schema diagrams
├── statement.md     # Problem statement, scope, target users
└── README.md
```

## Prerequisites

- **JDK 17 or later** installed and on your `PATH`.
  Check with:
  ```bash
  java -version
  javac -version
  ```
  If not installed, download from [Adoptium](https://adoptium.net/) or
  install via your package manager (e.g. `sudo apt install openjdk-17-jdk`
  on Ubuntu/Debian, `brew install openjdk@17` on macOS).

No other dependencies, build tools, or internet access are required —
the project compiles with plain `javac`.

## How to Install & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/<repo-name>.git
   cd <repo-name>
   ```

2. **Compile the project**
   ```bash
   javac -d bin $(find src -name "*.java")
   ```
   (On Windows PowerShell, use:
   `javac -d bin (Get-ChildItem -Recurse -Filter *.java -Path src).FullName`)

3. **Run the application**
   ```bash
   java -cp bin com.elibrary.Main
   ```

4. **First run behavior**
   - A `data/` folder is created automatically with three JSON files.
   - A `logs/application.log` file is created automatically.
   - A default **admin account** is bootstrapped:
     - Email: `admin@elibrary.com`
     - Password: `admin123`
   - A sample catalog of 18 items (9 books, 9 courses) is seeded automatically,
     spanning subjects including Programming, Computer Science, Data Science,
     EEE, English, DLCA, Calculus, Python, and C++.

5. **Using the app**
   - Choose **Register** to create a regular user account (pick a
     starting tier), or **Login** with the admin credentials above.
   - Regular users can browse/search the catalog, access content
     (subject to tier and quota checks), manage their subscription, and
     view their access history.
   - The admin can add/remove catalog content, view all users, and view
     usage reports.

## Instructions for Testing

Manual functional test checklist:

1. **Registration validation** — try registering with an invalid email
   (e.g. `notanemail`) or a password under 4 characters; confirm the
   system rejects it with a clear error and does not create a user.
2. **Duplicate registration** — register the same email twice; confirm
   the second attempt is rejected.
3. **Tier gating** — as a `FREE` user, attempt to access a `PREMIUM`
   book (e.g. "Advanced Algorithms"); confirm access is denied with an
   explanatory message.
4. **Quota enforcement** — as a `FREE` user (5 accesses/month), access
   5 pieces of eligible content, then attempt a 6th; confirm the 6th is
   rejected with a quota-exceeded message.
5. **Upgrade flow** — upgrade a `FREE` user to `PREMIUM` via the menu,
   then confirm previously denied content is now accessible and the
   monthly quota resets.
6. **Persistence** — perform some actions, exit the application, and
   relaunch it; confirm all users, catalog content, and access history
   are still present (loaded from `data/*.json`).
7. **Admin reports** — log in as admin, add a new book/course, then
   view reports; confirm the new item and updated access counts appear
   correctly.
8. **Logging** — after a session, inspect `logs/application.log` to
   confirm INFO entries for registrations/logins/accesses and ERROR
   entries for denied access attempts are recorded.

## Design Documentation

Detailed system architecture, workflow, UML (use case, class, sequence),
and data schema diagrams are in [`docs/architecture.md`](docs/architecture.md),
[`docs/uml.md`](docs/uml.md), and [`docs/data-schema.md`](docs/data-schema.md).
These render as diagrams automatically when viewed on GitHub.

## Non-Functional Requirements

| Requirement | How it's addressed |
|---|---|
| **Security** | Passwords hashed with SHA-256 before storage; input validation on all user-facing fields |
| **Reliability** | All file I/O and parsing wrapped in try/catch; the app degrades gracefully (logs errors) rather than crashing |
| **Maintainability** | Layered architecture (model / service / util / exception) with single-responsibility classes |
| **Usability** | Numbered CLI menus with clear prompts and human-readable error messages |
| **Logging/Monitoring** | Every registration, login, access grant/denial, and error is timestamped in `logs/application.log` |
| **Performance** | In-memory lists with linear search are appropriate at coursework scale; JSON is only read once at startup and written on mutation |

## Future Enhancements

- Migrate persistence to a relational database (e.g. SQLite/MySQL via JDBC).
- Add a REST API layer for a web or mobile front end.
- Content recommendation engine based on category access history.
- Automated monthly quota reset via a scheduled task rather than on
  tier-change/renewal only.

## License

Coursework project — for academic submission purposes.
