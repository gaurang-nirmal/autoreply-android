# Compose UI Skill

Goal:
Generate clean modern Jetpack Compose UI following MVVM architecture.

UI Expectations:

- Material 3
- Clean spacing
- Reusable composables
- Responsive layouts
- Production-friendly structure

Rules:

- Prefer stateless composables
- Hoist state to ViewModel
- Keep composables small and reusable
- Avoid monolithic screens
- Use remember only when necessary

Architecture:

- UI should not contain business logic
- ViewModel handles state/events
- Use StateFlow
- Avoid LiveData

Design Expectations:

- Minimal modern UI
- Proper loading states
- Proper error states
- Proper empty states

Important:

- MVP-first implementation
- Avoid UI overengineering
- Focus on usability and clarity
