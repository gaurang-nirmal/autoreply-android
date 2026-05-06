# Coding Guidelines

General Rules:

- Prefer simple readable code
- Avoid overengineering
- Keep classes small and reusable
- Prefer composition over inheritance
- Build scalable multi-app architecture

Kotlin Rules:

- Use Kotlin idiomatic syntax
- Prefer data classes
- Use sealed classes for UI state
- Use extension functions where useful

Compose Rules:

- Use stateless composables where possible
- Hoist state to ViewModel
- Keep composables small
- Use Material 3

ViewModel Rules:

- Use StateFlow
- Avoid LiveData
- Keep business logic outside composables

Coroutine Rules:

- Use viewModelScope
- Avoid GlobalScope
- Prefer suspend functions

Repository Rules:

- Repository handles data source coordination
- ViewModel should not access database directly

Multi-App Support Rules:

- Avoid app-specific hardcoding
- Keep parser logic modular
- Keep accessibility handlers isolated per app
- Use interface-based automation handlers

Naming Rules:

- Use meaningful names
- Avoid abbreviations

Backend Rules:

- Use modular Express structure
- Keep controllers thin
- Business logic inside services
- Use Prisma ORM only

Accessibility Rules:

- Accessibility node matching should be resilient
- Avoid fragile UI assumptions
- Add fallback selector handling
- Log automation failures for debugging