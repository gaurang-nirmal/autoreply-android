# Current Task

Goal:
Implement scalable notification parsing architecture for multiple supported applications.

Requirements:

- Detect notification source app
- Extract sender name
- Extract message text
- Support:
    - WhatsApp
    - Telegram
    - Messenger
- Create reusable parser interface
- Ignore unsupported notifications

Files:

- NotificationListenerService.kt
- NotificationParser.kt
- AppNotificationParser.kt
- SupportedApp.kt

Expected Output:

- Clean modular parser system
- App-specific parser handlers
- Logging for debugging

Important:

- Keep implementation scalable
- Avoid app-specific hardcoding
- MVP-focused implementation only