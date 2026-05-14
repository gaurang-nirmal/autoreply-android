# Architecture — AutoReply Android

## Pattern

- MVVM + Repository Pattern
- Single Activity (`MainActivity`) + Jetpack Compose Navigation
- Hilt for DI throughout

## Real Package Structure (com.psspl.autoreply)

```
com.psspl.autoreply/
├── AutoReplyApp.kt              # Application class (Hilt)
├── MainActivity.kt              # Single activity entry point
├── MainViewModel.kt             # Theme mode + app lock state

├── data/
│   ├── auth/                    # Google auth (AuthRepository, GoogleCredentialProvider)
│   │   └── model/               # AuthResult, AuthUser
│   ├── network/                 # App backend
│   │   ├── ApiService.kt        # Retrofit interface (AI, training prompts)
│   │   └── model/               # AiReplyRequest/Response, AiConfigModels, etc.
│   ├── remote/                  # Google APIs
│   │   ├── SheetsApiService.kt
│   │   ├── DriveApiService.kt
│   │   ├── AuthApiService.kt
│   │   ├── interceptor/         # AuthInterceptor, CurlLoggingInterceptor
│   │   └── model/               # Google API request/response models
│   └── repository/              # AI config repository (AiConfigRepository)

├── database/
│   ├── AppDatabase.kt           # Room DB, version=14, fallbackToDestructiveMigration
│   ├── dao/                     # One DAO per entity
│   └── entity/                  # Room entities (see CLAUDE.md entity table)

├── di/
│   ├── AppModule.kt             # (currently empty, for future use)
│   ├── AuthModule.kt            # DataStore + AuthRepository binding
│   ├── DatabaseModule.kt        # Room + all DAO providers
│   └── NetworkModule.kt         # OkHttpClient, Retrofit, ApiService, SheetsRetrofit, DriveRetrofit

├── engine/
│   ├── MenuReplyEngine.kt       # Menu reply conversation state machine
│   └── ReplyTimingEvaluator.kt  # Timing/limit gate (shared by all reply types)

├── navigation/
│   ├── AppNavGraph.kt           # All composable routes
│   ├── AppBottomNavBar.kt       # Bottom navigation bar
│   └── BottomNavItem.kt         # Bottom nav tab definitions

├── repository/
│   ├── AppSettingsRepository.kt
│   ├── KeywordRuleRepository.kt
│   ├── MenuReplyRepository.kt
│   ├── SpreadsheetRepository.kt  # Google Sheets sync + rule cache
│   ├── ReplyNotificationsRepository.kt
│   ├── ReplyTimingRepository.kt
│   ├── WelcomeMessageRepository.kt
│   ├── DefaultMessageRepository.kt
│   ├── DirectMessageRepository.kt
│   ├── FollowUpRepository.kt
│   └── NoteRepository.kt

├── service/
│   ├── MessengerNotificationService.kt  # NotificationListenerService — core reply dispatch
│   └── SpreadsheetSyncWorker.kt         # WorkManager worker for background sheet sync

├── ui/
│   ├── auth/                    # AuthViewModel, AuthState
│   ├── components/              # Shared composables (AppTopBar, AppCard, AppButton, etc.)
│   ├── theme/                   # Color, Type, Shape, Spacing
│   └── screens/
│       ├── ai/                  # AiReplyScreen, AiSettingsScreen, AiParametersScreen, TrainAiScreen, AiTextPromptScreen
│       ├── appsecurity/         # AppSecurityScreen
│       ├── automaticon/         # AutomaticOnScreen
│       ├── autoreplyconfig/     # AutoReplyConfigScreen, AutoReplyConfigViewModel, ReplyType enum
│       ├── backuprestore/       # BackupRestoreScreen
│       ├── dashboard/           # DashboardScreen, DashboardViewModel
│       ├── directmessage/       # DirectMessageScreen
│       ├── display/             # DisplayScreen (theme selector)
│       ├── followup/            # FollowUpMessageScreen, FollowUpHistoryScreen, FollowUpManageScreen
│       ├── help/                # HelpScreen
│       ├── invitefriend/        # InviteFriendScreen
│       ├── login/               # LoginScreen, LoginViewModel
│       ├── menu/                # MenuScreen (feature grid), MenuViewModel
│       ├── menureply/           # MenuReplyScreen, AddEditMenuReplyScreen, children/more options
│       ├── notes/               # NotesScreen, NoteEditorScreen
│       ├── notworking/          # NotWorkingScreen
│       ├── replyheaderfooter/   # ReplyHeaderFooterScreen
│       ├── replynotifications/  # ReplyNotificationsScreen (reply history)
│       ├── replytime/           # ReplyTimeScreen
│       ├── replytiming/         # ReplyTimingScreen, ReplyLimitListScreen, ReplyMode enum
│       ├── rules/               # RulesScreen, KeywordReplyFormScreen
│       ├── serverreply/         # [PLANNED] ServerReplyScreen, ServerReplyViewModel
│       ├── settings/            # SettingsScreen
│       ├── spreadsheet/         # SpreadsheetScreen, AddSpreadsheetScreen, ViewSpreadsheetScreen
│       ├── supportedapps/       # SupportedAppsScreen
│       ├── upgrade/             # UpgradeScreen
│       └── welcomemessage/      # WelcomeMessageScreen, WelcomeMessageEditScreen

├── backup/
│   ├── KeywordRuleBackupManager.kt
│   └── MenuReplyBackupManager.kt

└── utils/                       # AppLogger, AppConstants, AppLockManager, ThemeMode, MatchType, etc.
```

## Notification Reply Flow (detailed)

```
onNotificationPosted(sbn)
  │
  ├─ extract: sender = EXTRA_TITLE, message = EXTRA_TEXT
  ├─ gate 1: isAutoReplyEnabled == true
  ├─ gate 2: supportedAppsRepository.isAppEnabled(packageName)
  ├─ contactKey = "$packageName:${sender.trim().lowercase()}"
  │
  ├─ welcome check (highest priority):
  │    welcomeConfig.isEnabled && shouldSendWelcome(contactKey)
  │    → handleWelcomeMessage() → sendDirectReply() → return
  │
  └─ branch on settings.replyType.lowercase():
       "menu"        → handleMenuReply()
       "spreadsheet" → handleSpreadsheetReply()
       "ai_reply"    → handleAiReply()       ← calls ApiService
       "server"      → handleServerReply()   ← calls user's URL via OkHttp
       else          → handleKeywordReply()  ← covers CUSTOM + KEYWORD
```

## Timing Gate (all reply types apply this)

```kotlin
when (replyTimingEvaluator.evaluate(contactKey)) {
    is TimingDecision.Block → return (skip reply)
    is TimingDecision.Delay → delay(decision.seconds * 1000L)
    is TimingDecision.Allow → proceed
}
// after successful send:
replyTimingEvaluator.recordReply(contactKey)
```

## DI Graph Summary

```
SingletonComponent
├── DatabaseModule    → AppDatabase → all DAOs → all Repositories
├── NetworkModule     → OkHttpClient → Retrofit → ApiService
│                     → @Named("Plain") OkHttpClient (no auth, for user server calls)
│                     → @Named("SheetsRetrofit") → SheetsApiService
│                     → @Named("DriveRetrofit") → DriveApiService
└── AuthModule        → DataStore → AuthRepository
```
