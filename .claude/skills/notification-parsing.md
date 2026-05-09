# Notification Parsing Skill

Goal:
Generate scalable notification parsing logic for messaging applications.

Supported apps are defined in project-context.md

Requirements:

- Detect source application
- Extract sender name
- Extract message text
- Ignore unsupported notifications
- Ignore grouped/summary notifications where needed
- Handle Android 13+ safely

Architecture:

- Use app-specific parser handlers
- Keep parser logic modular
- Avoid app-specific hardcoding in shared logic

Recommended Structure:

- NotificationParser
- AppNotificationParser interface
- App-specific parser implementations

Preferred Reply Strategy:

1. Try RemoteInput direct reply first
2. Fallback to Accessibility automation

Code Expectations:

- Null-safe parsing
- Structured logging
- Fallback extraction handling
- Clean parser separation
