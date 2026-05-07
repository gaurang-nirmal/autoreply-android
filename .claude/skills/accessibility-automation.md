# Accessibility Automation Skill

Goal:
Generate reliable Android Accessibility Service automation for messaging applications.

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

Preferred Reply Strategy:
1. Use notification direct reply (RemoteInput) whenever supported
2. Use Accessibility automation as fallback

Accessibility Rules:
- Avoid fragile selectors
- Avoid index-based node matching
- Prefer text/id/content-description matching
- Add fallback selectors
- Handle null safely
- Add retry handling where necessary
- Add structured logging for failures

Automation Flow:
1. Open target application/chat
2. Find message input node
3. Focus input field
4. Insert reply text
5. Find send button
6. Perform click action
7. Verify send success if possible

Important:
- UI structures differ per app
- UI may change after app updates
- Real-device testing is mandatory
- Accessibility automation must remain modular

Code Expectations:
- Reusable helper methods
- Modular app-specific handlers
- Avoid hardcoded delays
- Prefer configurable timing
- Use coroutines where suitable

Debugging:
- Log node hierarchy when failures occur
- Log selector failures
- Log unsupported UI states