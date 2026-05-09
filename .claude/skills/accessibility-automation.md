# Accessibility Automation Skill

Goal:
Generate reliable Android Accessibility Service automation.

Supported apps are defined in project-context.md

Preferred Reply Strategy:

1. Use notification direct reply whenever supported
2. Use Accessibility automation as fallback

Rules:

- Avoid fragile selectors
- Avoid index-based node matching
- Prefer text/id/content-description matching
- Add fallback selectors
- Handle null safely
- Add retry handling where needed
- Add structured logging

Automation Flow:

1. Open target application/chat
2. Find message input node
3. Focus input field
4. Insert reply text
5. Find send button
6. Perform click action

Code Expectations:

- Reusable helper methods
- Modular app-specific handlers
- Avoid hardcoded delays
- Prefer configurable timing
- Use coroutines where suitable

Debugging:

- Log selector failures
- Log unsupported UI states
- Log automation failures
