# Current Task

Goal:
Implement Welcome Message module with timing-based auto-reply logic.

Current Scope:

- Welcome Message screen
- Timing configuration
- Welcome message editor
- Enable/disable feature
- Room persistence
- Navigation from Menu screen

Feature Overview:
Automatically send welcome message:

- on first message
- or after configured cooldown days

Requirements:

UI:

- Welcome Message screen
- Enable/disable toggle
- Change days action
- Welcome message preview
- Edit message action

Behavior:

- Send welcome message on first interaction
- Re-send only after configured cooldown days
- Persist timing configuration
- Persist welcome message
- Persist feature enabled state

Timing Logic:

- Configurable cooldown days
- Default timing support
- Track last welcome message timestamp per contact

Core Welcome Logic:

- Send welcome message if no previous welcome message record exists for contact
- If record exists:
    - calculate difference between current time and last welcome sent timestamp
    - send again only if configured cooldown days completed
- Do not send welcome message repeatedly within cooldown duration
- Update last welcome sent timestamp after successful auto-reply
- Maintain tracking separately per contact
- Logic must work independently per messaging app/contact

Database:
Create dedicated table/entity for:

- welcome message configuration
- contact timing tracking

Navigation:

- Add navigation from Menu screen
- Open dedicated Welcome Message screen
- Use full screen flow
- No bottom-sheet implementation

Files:

UI:

- WelcomeMessageScreen.kt
- EditWelcomeMessageScreen.kt

ViewModel:

- WelcomeMessageViewModel.kt

Database:

- WelcomeMessageEntity.kt
- WelcomeMessageContactEntity.kt
- WelcomeMessageDao.kt

Repository:

- WelcomeMessageRepository.kt

Expected Output:

- Working Welcome Message module
- Timing-based welcome logic
- Persistent Room storage
- Navigation from Menu screen

Important:

- MVP-only
- No backend sync
- No analytics
- No AI integration