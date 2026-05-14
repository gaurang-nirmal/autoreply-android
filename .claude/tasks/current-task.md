# Current Task: Server Reply Feature

## Goal

Allow users to configure their own server endpoint. When a notification arrives and reply type is
SERVER,
the app POSTs the message details to the user's URL and sends the server's response back as the
reply.

## Reference

Screenshots shared by user showing:

- "Server" screen (accessible from MenuScreen → "Server" card)
- Top bar: back arrow + "Server" title + "SAVE" action
- Server URL input with hint "Example: https://example.com/message.php"
- Header (Optional) — two side-by-side fields: "name" | "value"
- Read-only info card: Request Parameters (URL, Method=POST, Type=JSON, Request Body fields,
  Response Body format)
- Full-width teal "SEND TEST REQUEST" button
- "HOW IT WORKS?" text link → opens dialog explaining the 3-step flow

---

## Screens / Files to Create

### `ui/screens/serverreply/ServerReplyViewModel.kt`

State class:

```kotlin
data class ServerReplyUiState(
    val url: String = "",
    val headerName: String = "",
    val headerValue: String = "",
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: String? = null,   // success message or error text
    val testResultIsError: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val showHowItWorksDialog: Boolean = false,
)
```

ViewModel loads from AppSettingsRepository on init. Save writes back. Test makes live HTTP call.

### `ui/screens/serverreply/ServerReplyScreen.kt`

UI layout (top to bottom):

1. AppTopBar — title "Server", back icon, SAVE action
2. Server icon (teal circle, `Icons.Filled.Cloud` or similar)
3. Short description text: "Configure your server URL to get a reply for incoming messages."
4. Server URL — `OutlinedTextField`, full width, hint: "Example: https://example.com/message.php"
5. "Header (Optional)" label
6. Two side-by-side `OutlinedTextField` — "name" | "value" (weight 1f each, Row)
7. "Request parameters" info card (AppCard, read-only, monospace-style content):
   ```
   URL:    https://example.com/message.php
   Method: POST
   Type:   JSON
   ──────────────────────────
   Request Body
   {
     "app": name of the incoming message app,
     "sender": sender name of the incoming message,
     "message": message content,
     "group_name": group name of the incoming message,
     "phone": phone number of the sender from your contact list
   }
   ──────────────────────────
   Response Body
   {
     "reply": Reply message from the server
   }
   ```
8. Bottom row: `AppButton("SEND TEST REQUEST", fullWidth)` | `TextButton("HOW IT WORKS?")`
9. "How it works?" dialog (AppAlertDialog) when showHowItWorksDialog=true:
    - Title: "How it works ?"
    - Body:
        1. AutoReply will send an incoming message from your messaging app to your configured server
           URL.
        2. Once your server responds back with the reply message, AutoReply will send it to your
           messaging app.
        3. That's it!! Simple isn't it?
    - "DONE" button dismisses

Test result feedback: Snackbar (success in green tint, error in error color).

---

## Files to Modify

### 1. `database/entity/AppSettingsEntity.kt`

Add 3 new columns:

```kotlin
@ColumnInfo(name = "server_reply_url")
val serverReplyUrl: String = "",

@ColumnInfo(name = "server_reply_header_name")
val serverReplyHeaderName: String = "",

@ColumnInfo(name = "server_reply_header_value")
val serverReplyHeaderValue: String = "",
```

### 2. `database/AppDatabase.kt`

Bump version: `14` → `15`

### 3. `repository/AppSettingsRepository.kt`

Add:

```kotlin
val serverReplyUrl: Flow<String> = dao.observe().map { it?.serverReplyUrl ?: "" }
val serverReplyHeaderName: Flow<String> = dao.observe().map { it?.serverReplyHeaderName ?: "" }
val serverReplyHeaderValue: Flow<String> = dao.observe().map { it?.serverReplyHeaderValue ?: "" }

suspend fun setServerReplyConfig(url: String, headerName: String, headerValue: String) {
    val current = dao.get() ?: AppSettingsEntity()
    dao.insert(current.copy(serverReplyUrl = url, serverReplyHeaderName = headerName, serverReplyHeaderValue = headerValue))
}
```

### 4. `di/NetworkModule.kt`

Add plain OkHttpClient (no auth interceptor) for user-server calls:

```kotlin
@Provides
@Singleton
@Named("Plain")
fun providePlainOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
```

### 5. `service/MessengerNotificationService.kt`

- Inject `@Named("Plain") OkHttpClient` as `plainOkHttpClient`
- Add `REPLY_TYPE_SERVER = "server"` constant
- Add branch: `REPLY_TYPE_SERVER -> handleServerReply(sbn, appPackage, sender, message, contactKey)`
- Implement `handleServerReply()`:
    - Load settings, check url not empty
    - Apply timing gate
    - Build POST request:
        - URL: settings.serverReplyUrl
        - Body (JSON):
          `{"app": appPackage, "sender": sender, "message": message, "group_name": "", "phone": ""}`
        - If headerName not blank: add as request header
    - Execute synchronously (already on Dispatchers.IO)
    - Parse response JSON: extract `"reply"` field using Gson JsonParser
    - If reply blank/null → log and return
    - sendDirectReply() → log + record

### 6. `ui/screens/menu/MenuScreen.kt`

- Add `onNavigateToServerReply: () -> Unit = {}` parameter
- Add case: `"Server" -> onNavigateToServerReply()`

### 7. `navigation/AppNavGraph.kt`

- Add import for `ServerReplyScreen`
- Add constant: `private const val ROUTE_SERVER_REPLY = "server_reply"`
- Add route in NavHost
- Wire in MenuScreen composable call

---

## Request / Response Contract

### POST to user's server

```json
{
  "app": "com.whatsapp",
  "sender": "John Doe",
  "message": "Hi, is this available?",
  "group_name": "",
  "phone": ""
}
```

Headers: `Content-Type: application/json` + optional custom header from config

### Expected response (HTTP 200)

```json
{
  "reply": "Yes, it is available!"
}
```

Non-200 or missing "reply" field → skip sending, log warning.

### Test Request (SEND TEST REQUEST button)

Same POST call but with dummy values:

```json
{
  "app": "test",
  "sender": "Test User",
  "message": "This is a test message",
  "group_name": "",
  "phone": ""
}
```

Show Snackbar with: success → `"Server replied: <reply text>"` | error → `"Test failed: <reason>"`

---

## Execution Checklist (do in order)

- [ ] Add columns to `AppSettingsEntity` + bump DB version to 15
- [ ] Add `setServerReplyConfig()` to `AppSettingsRepository`
- [ ] Add `@Named("Plain") OkHttpClient` to `NetworkModule`
- [ ] Create `ServerReplyViewModel.kt`
- [ ] Create `ServerReplyScreen.kt`
- [ ] Add `handleServerReply()` to `MessengerNotificationService` + inject plain client
- [ ] Update `MenuScreen` with `onNavigateToServerReply`
- [ ] Update `AppNavGraph` with route + wiring

## Important Notes

- Do NOT use the app backend `ApiService` for server reply calls — this is a direct call to the
  user's URL
- Use synchronous OkHttp `execute()` since the service runs on Dispatchers.IO
- Gson is already available — use `JsonParser.parseString(body).asJsonObject.get("reply").asString`
- Guard null/JsonNull when parsing the reply field
- AppSettingsEntity uses `Insert(REPLACE)` upsert — always do `dao.get() ?: AppSettingsEntity()`
  then `.copy()`
