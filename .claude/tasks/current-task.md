# Current Task

Goal:
Implement Home Auto Reply configuration flow and Follow-Up Message module.

Modules:

- Home screen auto-reply controls
- Auto reply configuration screen
- Follow-up message feature
- Reply type selection integration
- Shared reply timing integration

Features:

1. Home Screen
   Add/update:

- Auto reply enable/disable toggle
- Auto reply text card
- Sent messages statistics card

Behavior:

- Toggle enables/disables global auto reply
- Auto reply text card opens configuration screen
- Statistics card shows sent auto replies count

2. Auto Reply Configuration Screen

Features:

- Configure custom auto reply message
- Support reply tags:
    - name
    - first name
    - last name
    - date
    - time
    - message

Reply Type Options:

- Custom Message
- Keyword Reply
- Spreadsheet Reply
- Menu Reply
- AI Reply
- Server Reply

Behavior:

- Only one reply type selectable
- Persist selected reply type
- Persist custom message
- Support reply tag insertion

Navigation:

- Reply Time -> existing common Reply Timing screen
- Follow-Up Message -> Follow-Up screen

3. Follow-Up Message Module

Features:

- Enable/disable follow-up
- Configure follow-up message
- Support reply tags
- Define follow-up scope:
    - all reply messages
    - specific reply messages
    - exclude reply messages

Manage reply messages:

- Add/manage included messages
- Add/manage excluded messages

History:

- Follow-up history screen placeholder/navigation

Core Logic:

- Send follow-up only if recipient does not reply
- Delay configurable
- Follow-up linked with sent auto replies
- Maintain sent/scheduled follow-up history

Database:

Create/update entities for:

- home auto reply config
- selected reply type
- custom auto reply message
- follow-up settings
- follow-up history
- follow-up include/exclude mappings

Files:

UI:

- HomeScreen.kt
- AutoReplyConfigurationScreen.kt
- FollowUpMessageScreen.kt

ViewModel:

- HomeViewModel.kt
- AutoReplyConfigurationViewModel.kt
- FollowUpViewModel.kt

Database:

- AutoReplyConfigEntity.kt
- FollowUpConfigEntity.kt
- FollowUpHistoryEntity.kt

Repository:

- AutoReplyRepository.kt
- FollowUpRepository.kt

Requirements:

- Full screen implementation only
- No bottom sheets
- Reuse existing reply timing screen
- Reuse shared design system/components
- Maintain MVP-focused implementation
- No backend integration
- Keep implementation modular and reusable