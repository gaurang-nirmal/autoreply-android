# AutoReply Android — Claude Reference

## Tech Stack

- Kotlin + Jetpack Compose (Material 3) + MVVM
- Hilt DI | Room DB (v14) | StateFlow | Coroutines
- Retrofit + OkHttp + Gson | WorkManager
- NotificationListenerService (primary reply path) | AccessibilityService (fallback)
- Single Activity (`MainActivity`) + Compose Navigation (`AppNavGraph`)
- Package: `com.psspl.autoreply`

## Global Rules

- MVP-first — no overengineering, no premature abstraction
- Modify ONLY files required for the current task
- Follow existing patterns exactly (see examples below)
- StateFlow only — never LiveData
- No XML layouts — Compose everywhere
- Accessibility automation is core, but notification direct reply is preferred

---

## Database

- Class: `database/AppDatabase.kt`
- **Current version: 15**
- Migration strategy: `fallbackToDestructiveMigration()` in `di/DatabaseModule.kt`
  → Just bump version number + add columns to entity. No SQL migration SQL needed.
- Settings row: single-row table, id always = 1, upserted via `Insert(REPLACE)`

### Key Entities

| Entity                        | Table                      | Notes                             |
|-------------------------------|----------------------------|-----------------------------------|
| `AppSettingsEntity`           | `app_settings`             | Global config, single row (id=1)  |
| `KeywordRuleEntity`           | `keyword_rules`            | Per-keyword reply rules           |
| `MenuReplyEntity`             | `menu_replies`             | Menu reply configs                |
| `MenuReplyItemEntity`         | `menu_reply_items`         | Items inside a menu               |
| `MenuSessionEntity`           | `menu_sessions`            | Per-contact menu navigation state |
| `ReplyNotificationEntity`     | `reply_notifications`      | Reply history log                 |
| `ReplyTimingConfigEntity`     | `reply_timing_configs`     | Per-reply-type timing config      |
| `ReplyLimitTrackingEntity`    | `reply_limit_tracking`     | Per-contact reply count           |
| `SpreadsheetEntity`           | `spreadsheets`             | Linked Google Sheets              |
| `SpreadsheetRuleEntity`       | `spreadsheet_rules`        | Cached rules from sheets          |
| `WelcomeMessageConfigEntity`  | `welcome_message_config`   | Welcome message settings          |
| `WelcomeMessageContactEntity` | `welcome_message_contacts` | Contacts who received welcome     |
| `SupportedAppEntity`          | `supported_apps`           | Apps user has enabled             |
| `DefaultMessageEntity`        | `default_messages`         | Dashboard quick-reply presets     |
| `FollowUpConfigEntity`        | `follow_up_config`         | Follow-up message settings        |
| `NoteEntity`                  | `notes`                    | Internal notes                    |
| `DirectMessageEntity`         | `direct_messages`          | Direct message history            |

### AppSettingsEntity — current columns (db v14)

```
id, is_auto_reply_enabled, max_active_apps, notifications_last_viewed_at,
app_lock_enabled, theme_mode, auto_reply_message,
reply_type (CUSTOM|KEYWORD|SPREADSHEET|MENU|AI_REPLY|SERVER),
messages_expanded, spreadsheet_auto_sync, spreadsheet_sync_interval_hours,
spreadsheet_auto_save, spreadsheet_save_sheet_id,
server_reply_url, server_reply_header_name, server_reply_header_value,
updated_at
```

---

## Reply Types (enum `ReplyType`)

File: `ui/screens/autoreplyconfig/ReplyType.kt`

```
CUSTOM | KEYWORD | SPREADSHEET | MENU | AI_REPLY | SERVER
```

Selected in `AutoReplyConfigScreen`. Stored as string in `app_settings.reply_type`.

---

## Key Files Map

### Entry Points

| File                          | Role                                         |
|-------------------------------|----------------------------------------------|
| `MainActivity.kt`             | Single activity; hosts Compose nav           |
| `navigation/AppNavGraph.kt`   | All screen routes + navigation wiring        |
| `navigation/BottomNavItem.kt` | Bottom nav tabs                              |
| `di/DatabaseModule.kt`        | Room setup, all DAO providers                |
| `di/NetworkModule.kt`         | OkHttpClient, Retrofit, ApiService providers |
| `di/AuthModule.kt`            | Auth DataStore binding                       |

### Core Service

| File                                      | Role                                            |
|-------------------------------------------|-------------------------------------------------|
| `service/MessengerNotificationService.kt` | Notification listener; dispatches reply by type |
| `engine/ReplyTimingEvaluator.kt`          | Timing/limit gate for all reply types           |
| `engine/MenuReplyEngine.kt`               | Menu reply state machine                        |

### Repository Layer

| File                                         | Role                                    |
|----------------------------------------------|-----------------------------------------|
| `repository/AppSettingsRepository.kt`        | All app settings read/write             |
| `repository/KeywordRuleRepository.kt`        | Keyword rules CRUD                      |
| `repository/SpreadsheetRepository.kt`        | Spreadsheet + rules + Google Sheets API |
| `repository/ReplyNotificationsRepository.kt` | Reply history                           |
| `repository/WelcomeMessageRepository.kt`     | Welcome message logic                   |
| `repository/ReplyTimingRepository.kt`        | Timing configs                          |

### Network

| File                              | Role                                     |
|-----------------------------------|------------------------------------------|
| `data/network/ApiService.kt`      | App backend (AI, auth, training prompts) |
| `data/remote/SheetsApiService.kt` | Google Sheets API                        |
| `data/remote/DriveApiService.kt`  | Google Drive API                         |

### Screen Inventory

| Screen                  | Package                      | Notes                                      |
|-------------------------|------------------------------|--------------------------------------------|
| `DashboardScreen`       | `ui/screens/dashboard`       | Home screen                                |
| `AutoReplyConfigScreen` | `ui/screens/autoreplyconfig` | Reply type selector + message              |
| `MenuScreen`            | `ui/screens/menu`            | Feature grid (add new features here)       |
| `AiReplyScreen`         | `ui/screens/ai`              | AI provider config                         |
| `RulesScreen`           | `ui/screens/rules`           | Keyword rules list                         |
| `MenuReplyScreen`       | `ui/screens/menureply`       | Menu reply CRUD                            |
| `SpreadsheetScreen`     | `ui/screens/spreadsheet`     | Spreadsheet management                     |
| `ReplyTimingScreen`     | `ui/screens/replytiming`     | Timing config (shared, keyed by replyType) |
| `WelcomeMessageScreen`  | `ui/screens/welcomemessage`  | Welcome message config                     |
| `SupportedAppsScreen`   | `ui/screens/supportedapps`   | Enable/disable apps                        |
| `SettingsScreen`        | `ui/screens/settings`        | App settings                               |

---

## Notification Reply Dispatch Flow

```
MessengerNotificationService.onNotificationPosted()
  → check isAutoReplyEnabled
  → check supportedAppsRepository.isAppEnabled(package)
  → check welcomeMessage (highest priority)
  → branch on settings.replyType:
      "menu"        → handleMenuReply()
      "spreadsheet" → handleSpreadsheetReply()
      "ai_reply"    → handleAiReply()     [calls ApiService.getAiReply()]
      "server"      → handleServerReply() [calls user's own URL via OkHttp]
      else          → handleKeywordReply() (covers CUSTOM + KEYWORD)
```

Each handler applies `replyTimingEvaluator.evaluate(contactKey)` before sending.
After successful send: `replyTimingEvaluator.recordReply(contactKey)` + insert to
`replyNotificationsRepository`.

---

## DI / Networking Quick Ref

- `OkHttpClient` (singleton) — has `AuthInterceptor` (adds app backend JWT) + logging
- **For user-server calls** (server reply): inject `@Named("Plain") OkHttpClient` — no auth
  interceptor
- `ApiService` — Retrofit interface for app backend
- Google APIs use separate `@Named("SheetsRetrofit")` / `@Named("DriveRetrofit")` instances

---

## Pattern: Adding a New Screen

### 1. Create screen package

`ui/screens/<feature>/`

- `<Feature>Screen.kt` — Composable
- `<Feature>ViewModel.kt` — `@HiltViewModel`, uses StateFlow

### 2. Add route to AppNavGraph

```kotlin
private const val ROUTE_<FEATURE> = "<feature>"

// in NavHost block:
composable(ROUTE_<FEATURE>) {
    <Feature>Screen(onBack = { navController.popBackStack() })
}
```

### 3. Wire navigation from MenuScreen (if feature card)

Add `onNavigateTo<Feature>: () -> Unit` param to `MenuScreen`.
Add case in `when (feature.title)` block.
Wire in `AppNavGraph` at the `MenuScreen` composable call.

### 4. If it needs new DB columns

Add `@ColumnInfo` fields to `AppSettingsEntity` (or new entity).
Bump `AppDatabase.version` by 1.
Add `setX()` method in `AppSettingsRepository`.

---

## UI Components (reuse these)

- `AppTopBar` — standard top bar with title/navigation/actions
- `AppCard` — elevated card container
- `AppButton` — primary button
- `AppAlertDialog` — custom alert dialog
- `ConfirmationDialog` — yes/no confirmation
- `SettingsItem` — row with icon, title, subtitle, trailing arrow
- `EmptyState` — empty list placeholder
- `LoadingIndicator` — centered progress
- `Spacing` — spacing constants (`Spacing.xs/sm/md/lg/xl/xxl/xxxl`)

## Theme

- Teal accent: `Color(0xFF128C7E)`
- Material 3 color scheme
- Dark/Light/System theme via `ThemeMode` enum

---

## Reference Files

- Detailed architecture: `.claude/architecture.md`
- Coding guidelines: `.claude/coding-guidelines.md`
- Project context: `.claude/project-context.md`
- Skills: `.claude/skills/`
- Current task: `.claude/tasks/current-task.md`
