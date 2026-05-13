# AutoReply — Android Auto-Reply Application

AutoReply is an Android application that automatically detects incoming messages from popular
messaging and social apps and sends pre-configured automated replies — powered by Android's
Accessibility Service and Notification Listener.

Inspired by [WhatsAuto](https://play.google.com/store/apps/details?id=com.guibais.whatsauto).

---

## Table of Contents

- [Features](#features)
- [Supported Applications](#supported-applications)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Permissions Required](#permissions-required)
- [Subscription / Plan](#subscription--plan)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Auto-Reply Automation** — Detect incoming messages and send configurable replies automatically
- **Multi-App Support** — Works across 11 popular messaging and social media platforms
- **Rule Engine** — Create keyword-based, menu-based, and custom reply rules
- **AI Reply** *(planned)* — AI-powered smart reply generation
- **Spreadsheet Import** *(planned)* — Bulk-import reply rules from spreadsheets
- **Welcome Messages** *(planned)* — Greet first-time contacts automatically
- **Server Integration** *(planned)* — Backend-driven rule management via REST API
- **Free Plan** — Auto-reply for up to 2 apps simultaneously at no cost

---

## Supported Applications

| App                 | Status  |
|---------------------|---------|
| WhatsApp            | Planned |
| WhatsApp Business   | Planned |
| Telegram            | Planned |
| Messenger           | Planned |
| Messenger Lite      | Planned |
| Instagram           | Planned |
| Twitter / X         | Planned |
| LinkedIn            | Planned |
| Signal              | Planned |
| Meta Business Suite | Planned |
| Viber               | Planned |

> **Note:** No unofficial APIs are used. All automation is done via Android Accessibility Service.

---

## Architecture

The project follows **MVVM + Repository Pattern** with a clean layered architecture:

```
UI Layer (Compose Screens)
        ↓
ViewModel Layer (StateFlow)
        ↓
Repository Layer
        ↓
Data Sources (Room DB / Remote API)
        ↓
Services (AccessibilityService / NotificationListenerService)
```

- **UI** — Jetpack Compose screens & reusable components
- **ViewModels** — Expose `StateFlow` state; no Android framework dependencies
- **Repositories** — Single source of truth for data; abstract local and remote sources
- **Room DB** — Local persistence for rules, app configs, and logs
- **Accessibility Service** — Reads UI and performs reply actions
- **Notification Listener** — Detects incoming message notifications
- **Backend** *(planned)* — Node.js + Express + PostgreSQL for remote rule management

---

## Tech Stack

### Android

| Layer        | Technology                                        |
|--------------|---------------------------------------------------|
| Language     | Kotlin                                            |
| UI           | Jetpack Compose + Material 3                      |
| Architecture | MVVM                                              |
| DI           | Hilt                                              |
| Database     | Room                                              |
| Async        | Coroutines + Flow                                 |
| Navigation   | Navigation Compose                                |
| Services     | AccessibilityService, NotificationListenerService |

### Backend *(planned)*

| Layer     | Technology |
|-----------|------------|
| Runtime   | Node.js    |
| Framework | Express.js |
| Database  | PostgreSQL |
| ORM       | Prisma     |

---

## Project Structure

```
AutoReply-Android/
├── app/
│   └── src/main/java/com/psspl/autoreply/
│       ├── AutoReplyApp.kt              # Hilt Application class
│       ├── MainActivity.kt             # Single-activity entry point
│       ├── di/
│       │   └── AppModule.kt            # Hilt dependency modules
│       ├── navigation/
│       │   ├── AppNavGraph.kt          # Navigation host & routes
│       │   ├── AppBottomNavBar.kt      # Bottom navigation bar
│       │   └── BottomNavItem.kt        # Nav item definitions
│       ├── ui/
│       │   ├── screens/
│       │   │   ├── dashboard/          # Dashboard screen + ViewModel
│       │   │   ├── rules/              # Rules management screen + ViewModel
│       │   │   ├── menu/               # Feature menu screen + ViewModel
│       │   │   │   └── components/     # FeatureMenuCard, FeatureItem
│       │   │   ├── supportedapps/      # Supported apps list + ViewModel
│       │   │   └── settings/           # Settings screen + ViewModel
│       │   ├── components/
│       │   │   ├── AppTopBar.kt        # Reusable top app bar
│       │   │   ├── AppCard.kt          # Reusable card wrapper
│       │   │   ├── AppButton.kt        # Primary & secondary buttons
│       │   │   ├── EmptyState.kt       # Empty state UI component
│       │   │   └── LoadingIndicator.kt # Loading indicator
│       │   └── theme/
│       │       ├── Theme.kt            # App theme (light/dark)
│       │       ├── Color.kt            # Color palette
│       │       ├── Type.kt             # Typography
│       │       ├── Shape.kt            # Shape tokens
│       │       └── Spacing.kt          # Spacing scale
│       └── utils/
│           └── AppConstants.kt         # App-wide constants
└── .claude/                            # Project context & documentation
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11+
- Android device or emulator running **Android 7.0 (API 24)** or higher
- A real device is strongly recommended for testing Accessibility Service

### Build & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/AutoReply-Android.git
   cd AutoReply-Android
   ```

2. **Open in Android Studio**
   ```
   File → Open → select the project root folder
   ```

3. **Sync Gradle**
   ```
   File → Sync Project with Gradle Files
   ```

4. **Run the app**
   ```
   Run → Run 'app'  (Shift + F10)
   ```

### Build Configuration

| Property           | Value                 |
|--------------------|-----------------------|
| `compileSdk`       | 36                    |
| `minSdk`           | 24 (Android 7.0)      |
| `targetSdk`        | 36                    |
| Java compatibility | Java 11               |
| Package            | `com.psspl.autoreply` |

---

## Permissions Required

| Permission                           | Purpose                                  |
|--------------------------------------|------------------------------------------|
| `BIND_ACCESSIBILITY_SERVICE`         | Read UI nodes and simulate reply actions |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Detect incoming message notifications    |
| `RECEIVE_BOOT_COMPLETED` *(planned)* | Restart service after device reboot      |
| `INTERNET` *(planned)*               | Backend API and AI reply integration     |

> Both Accessibility Service and Notification Listener require **manual user approval** in Android
> system settings. The app guides the user through this setup via the Settings screen.

---

## Subscription / Plan

| Feature              | Free Plan        | Premium *(planned)* |
|----------------------|------------------|---------------------|
| Auto-reply apps      | Up to **2 apps** | Unlimited           |
| Keyword rules        | Unlimited        | Unlimited           |
| AI reply             | -                | Included            |
| Spreadsheet import   | -                | Included            |
| Backend/server rules | -                | Included            |

---

## Roadmap

### Phase 1 — UI Scaffolding ✅

- [x] Jetpack Compose navigation with bottom bar
- [x] Dashboard, Rules, Menu, Supported Apps, Settings screens
- [x] Hilt dependency injection setup
- [x] Room database dependency ready
- [x] Material 3 theme, color palette, typography, spacing

### Phase 2 — Data Layer (In Progress)

- [ ] Room entities: `ReplyRule`, `AppConfig`, `MessageLog`
- [ ] DAOs and Repositories
- [ ] Hilt modules for DB and repositories

### Phase 3 — Core Services

- [ ] `NotificationListenerService` — parse incoming notifications
- [ ] `AccessibilityService` — open app, navigate to chat, send reply
- [ ] App-specific UI node selectors for each supported platform

### Phase 4 — Rule Engine

- [ ] Keyword matching engine
- [ ] Time-window / schedule conditions
- [ ] Priority and conflict resolution
- [ ] Menu-based multi-step reply flows

### Phase 5 — Advanced Features

- [ ] AI-powered reply (LLM integration)
- [ ] Spreadsheet/CSV rule import
- [ ] Welcome message for first-time contacts
- [ ] Multi-language support

### Phase 6 — Backend Integration

- [ ] Node.js + Express REST API
- [ ] PostgreSQL + Prisma schema
- [ ] Remote rule sync
- [ ] User account & subscription management

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Follow the [coding guidelines](.claude/coding-guidelines.md)
4. Commit your changes: `git commit -m "feat: add your feature"`
5. Push to the branch: `git push origin feature/your-feature`
6. Open a Pull Request

### Coding Conventions

- Prefer small, reusable classes and composables
- Use `StateFlow` in ViewModels, not `LiveData`
- Follow repository pattern — no direct DB/API calls from ViewModels
- Keep Compose UI pure — no side effects in composable bodies
- Real-device testing is required for Accessibility Service changes

---

## License

```
Copyright (c) 2026 PSSPL

Licensed under the MIT License.
See LICENSE file for details.
```

---

> **Important:** This app uses Android's Accessibility Service solely for UI automation to send
> replies. It does not use any unofficial or private APIs of the supported messaging platforms.
