# Current Task

## Feature

Implement Default Messages section in Home/Dashboard screen.

Reference screenshots shared by user.

---

## Requirements

### Home Screen Integration

Add a new card section below advertisement banner.

Section title:

- Messages

Top-right actions:

1. Expand/Collapse icon
2. Overflow menu

Overflow menu options:

- Show Messages
- Clear All

Use application's existing modern design system.

---

## Default Messages List

Show predefined default auto-reply messages.

Examples:

- I am busy, text you later.
- I am driving, text you later.
- I am sleeping, text you later.
- Can't talk now.
- At the movie, text you later.
- At work, text you later.
- In a meeting, text you later.

Use Room database.

Create separate table/entity for default messages.

Fields:

- id
- message
- isDefault
- createdAt
- updatedAt

---

## Expand / Collapse

Expand icon behavior:

- Expanded:
  show message list
- Collapsed:
  hide message list

Persist state locally.

---

## Show Messages Action

Popup menu action:

- "Show Messages"

Behavior:

- toggles visibility of message list
- persist locally

---

## Clear All Action

Popup menu action:

- "Clear All"

Behavior:

- delete all non-default messages only
- preserve seeded default messages

Show confirmation dialog using application's custom dialog design system.

---

## Message Selection

On tapping any message:

- set selected message as active auto-reply text
- update AutoReplyConfig/Home auto-reply message state
- reflect immediately in Auto reply text section

---

## Persistence

Seed default messages once during first launch.

Use repository pattern + Room + MVVM.

---

## UI Requirements

Use:

- Jetpack Compose
- Material3
- existing application theme/colors/components

Do NOT:

- use legacy XML
- use bottom sheets
- redesign unrelated screens

---

## Important

Keep implementation modular and reusable.

Do not modify reply-engine logic.

Only integrate dashboard/default-message functionality.