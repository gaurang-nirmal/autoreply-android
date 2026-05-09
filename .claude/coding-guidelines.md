# Coding Guidelines

General Rules:

- Prefer simple readable code
- Avoid overengineering
- Keep classes small and reusable
- Prefer composition over inheritance

Kotlin Rules:

- Use idiomatic Kotlin syntax
- Prefer data classes
- Use sealed classes for UI state
- Use extension functions where useful

Compose Rules:

- Prefer stateless composables
- Hoist state to ViewModel
- Use Material 3
- Keep composables small and reusable

ViewModel Rules:

- Use StateFlow
- Avoid LiveData
- Keep business logic outside composables

Coroutine Rules:

- Use viewModelScope
- Avoid GlobalScope
- Prefer suspend functions

Repository Rules:

- Repository handles data coordination
- ViewModel should not access database directly

Multi-App Rules:

- Avoid app-specific hardcoding
- Keep parser logic modular
- Keep automation handlers isolated per app
- Prefer interface-based handlers

Accessibility Rules:

- Use resilient node matching
- Avoid fragile selectors
- Add fallback selector handling
- Add structured logging for failures

Naming Rules:

- Use meaningful names
- Avoid unclear abbreviations
