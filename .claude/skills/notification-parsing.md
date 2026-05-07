# Notification Parsing Skill

Goal:
Generate scalable notification parsing logic for multiple messaging applications.

Supported Applications:
- WhatsApp
- WhatsApp Business
- Telegram
- Messenger
- Messenger Lite
- Instagram
- Twitter/X
- LinkedIn
- Signal
- Meta Business Suite
- Viber

Parsing Requirements:
- Detect source application
- Extract sender name
- Extract message text
- Ignore unsupported notifications
- Ignore summary/grouped notifications when required
- Handle Android 13+ behavior safely

Architecture Rules:
- Use app-specific parser handlers
- Avoid application hardcoding in core logic
- Use reusable parser interfaces
- Keep parser logic modular

Recommended Structure:
- NotificationParser
- AppNotificationParser interface
- App-specific parser implementations

Expected Output:
- ParsedMessage model
- Source app identifier
- Sender information
- Message body
- Timestamp if available

Important:
- Notification structures vary per application
- Notification formats may change after updates
- Defensive parsing is required

Code Expectations:
- Null-safe parsing
- Structured logging
- Fallback extraction handling
- Clean parser separation

Preferred Strategy:
1. Try RemoteInput reply support detection
2. Fallback to Accessibility automation when unavailable
