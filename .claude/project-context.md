# Project Context

Application Type:
Android auto-reply application inspired by Whatauto.

Core Features:

- Auto reply to incoming messages
- Multi-application support
- Keyword-based rules
- Default replies
- Delay and cooldown logic
- Group filtering
- Contact filtering
- Schedule-based activation
- Backend sync

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

Main Flow:

1. Detect incoming notification
2. Identify source application
3. Extract sender and message
4. Match rule
5. Trigger Accessibility Service
6. Open corresponding app chat
7. Send automated reply

Subscription Logic:

- Only 2 applications can be enabled simultaneously in basic/free plan
- Premium subscription logic may unlock additional apps later

Important Constraints:

- Android only
- No unofficial messaging APIs
- Accessibility automation is core
- Accessibility flows differ per app
- Real-device testing required
- UI structures may change after app updates

MVP Priorities:

1. Stable notification detection
2. Stable auto reply
3. Multi-app architecture
4. Rule engine
5. Reliable accessibility automation

Non-MVP Features:

- AI chatbot
- Multi-device sync
- Cloud backup
- Advanced analytics
- Smart reply suggestions