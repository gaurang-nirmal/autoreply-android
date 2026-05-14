# Reply Engine Skill

## Current Reply Types (ReplyType enum)

```
CUSTOM      → handleKeywordReply() — sends settings.autoReplyMessage as-is
KEYWORD     → handleKeywordReply() — matches KeywordRuleEntity against message
SPREADSHEET → handleSpreadsheetReply() — matches SpreadsheetRuleEntity
MENU        → handleMenuReply() via MenuReplyEngine (state machine)
AI_REPLY    → handleAiReply() via ApiService.getAiReply() (app backend)
SERVER      → handleServerReply() via user-configured URL (OkHttp direct call)
```

## Adding a New Reply Type

1. Add enum value to `ReplyType.kt`
2. Add `when` branch in `MessengerNotificationService.onNotificationPosted()`
3. Implement `handleXxxReply()` private suspend fun in the service
4. Apply timing gate + record reply after successful send
5. Add config screen + DB columns if needed

## Timing Gate (mandatory for all handlers)

```kotlin
when (val decision = replyTimingEvaluator.evaluate(contactKey)) {
    is TimingDecision.Block → return
    is TimingDecision.Delay → delay(decision.seconds * 1_000L)
    is TimingDecision.Allow → { /* proceed */ }
}
// after successful send:
replyTimingEvaluator.recordReply(contactKey)
replyNotificationsRepository.insert(ReplyNotificationEntity(...))
```

## Tag Substitution (resolveReplyText)

Available in keyword + spreadsheet handlers:
`{name}`, `{first_name}`, `{last_name}`, `{date}`, `{time}`, `{message}`

## Timing Modes (ReplyTimingScreen — shared by all types, keyed by replyType string)

1. Reply Every Time — no gate
2. Reply And Wait — reply once, pause for N duration
3. Reply After Delay — delay N seconds before sending
4. Reply Once — one reply per contact session

## Architecture Rules

- Timing evaluation is centralized in `ReplyTimingEvaluator`
- No reply logic in ViewModels or UI layer
- Each reply type gets its own private handler in the service
- Welcome message always takes priority over the active reply type
- contactKey format: `"$packageName:${sender.trim().lowercase()}"`
