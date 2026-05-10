# Current Task

Goal:
Integrate existing backup/restore implementation and add search support in Keyword Reply module.

Scope:

- Connect Backup action
- Connect Restore action
- Connect Clear All action
- Add keyword reply search feature
- Reuse existing backup/restore implementation

Requirements:

Topbar Actions:

- Search
- Backup
- Restore
- Clear All

Search:

- Search keyword replies
- Search by:
    - incoming keyword
    - reply message
- Real-time filtering

Behavior:

- Backup keyword reply records
- Restore keyword reply records
- Clear all keyword replies
- Show confirmation before clear all
- Filter listing while typing search query

UI:

- Match application theme
- Use reusable popup/dialog system
- Modern search UI behavior

Important:

- Reuse existing backup/restore implementation
- Avoid duplicate backup logic
- MVP-only