# Current Task

Goal:
Implement common Reply Timing & Reply Limit module reusable across reply types.

Current Scope:

- Common reply timing screen
- Shared timing logic
- Shared reply limit logic
- Separate persistence per reply type
- Integration with:
    - Keyword Reply
    - Menu Reply
    - Spreadsheet Reply

Feature Overview:
Define:

- when auto-reply should trigger
- how frequently replies are allowed
- reply limit handling per chat/contact

Supported Reply Modes:

- Reply every time
- Reply and wait
- Reply after delay
- Reply once

Reply Limit:

- Enable/disable reply limit
- Max replies per contact/chat
- Reply limit tracking

Requirements:

UI:

- Common reusable timing screen
- Dynamic title based on reply type
- Reply mode selection
- Reply limit toggle
- Max replies configuration
- Reply limit list action

Behavior:

- Persist settings separately for each reply type
- Apply timing logic independently per module
- Apply reply limits independently per module/contact
- Track sent reply counts per contact/chat
- Support wait duration handling
- Support delay handling
- Support one-time reply handling

Reply Logic:

Reply Every Time:

- Send reply for every matching incoming message

Reply And Wait:

- Send reply once
- Wait configured duration before allowing next reply

Reply After Delay:

- Delay sending reply by configured duration

Reply Once:

- Send reply only once
- Do not send again until auto-reply restarted/reset

Reply Limit Logic:

- Track reply count per contact/chat
- Stop replying after max replies reached
- Maintain separate tracking per reply type

Database:
Create/update tables/entities for:

- reply timing configuration
- reply limit configuration
- reply count tracking

Navigation:

- Open from:
    - Keyword Reply
    - Menu Reply
    - Spreadsheet Reply
- Use dedicated full screen flow
- No bottom-sheet implementation

Files:

UI:

- ReplyTimingScreen.kt

ViewModel:

- ReplyTimingViewModel.kt

Database:

- ReplyTimingEntity.kt
- ReplyLimitTrackingEntity.kt
- ReplyTimingDao.kt

Repository:

- ReplyTimingRepository.kt

Expected Output:

- Reusable timing module
- Shared timing logic
- Separate configuration per reply type
- Working reply limit handling

Important:

- MVP-only
- Reuse architecture where possible
- Avoid duplicate timing implementations
- No backend integration