# Current Task

Goal:
Implement Notes module for saving and sharing text notes.

Current Scope:

- Notes listing screen
- Create/Edit note screen
- Share note functionality
- Multiple selection support
- Delete functionality
- Room persistence
- Navigation from Menu screen

Feature Overview:
User can:

- create notes
- edit notes
- share notes to external apps
- delete notes
- select multiple notes

Requirements:

UI:

- Notes listing screen using grid layout
- Add note action
- Create/Edit note screen
- Share action
- Multiple selection mode
- Delete action for selected notes

Behavior:

- Create note
- Update note
- Delete single note
- Delete multiple selected notes
- Share note text using Android share intent
- Long press enables selection mode
- Multiple notes can be selected

Database:
Create dedicated table/entity for:

- notes

Navigation:

- Add navigation from Menu screen
- Open dedicated full screen Notes flow
- No bottom-sheet implementation

Files:

UI:

- NotesScreen.kt
- CreateEditNoteScreen.kt

ViewModel:

- NotesViewModel.kt

Database:

- NoteEntity.kt
- NoteDao.kt

Repository:

- NotesRepository.kt

Expected Output:

- Working Notes module
- Grid-based note listing
- Share integration
- Multi-select delete support
- Persistent Room storage

Important:

- MVP-only
- No backend sync
- No cloud sync
- No rich text editor
- No folders/tags