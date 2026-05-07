# Compose UI Skill

Goal:
Generate clean modern Jetpack Compose UI following MVVM architecture.

UI Expectations:
- Material 3
- Clean spacing
- Reusable composables
- Responsive layouts
- Production-friendly structure

Compose Rules:
- Prefer stateless composables
- Hoist state to ViewModel
- Keep composables small and reusable
- Avoid large monolithic screens
- Use remember only when necessary

Architecture Rules:
- UI should not contain business logic
- ViewModel handles state and events
- Use StateFlow for UI state
- Avoid LiveData

Preferred Screen Structure:
- Screen composable
- Content composable
- Reusable components
- ViewModel state/event handling

Design Expectations:
- Minimal modern UI
- Simple navigation
- Proper loading states
- Proper empty states
- Proper error states

Important:
- MVP-first implementation
- Avoid overengineering UI
- Focus on usability and clarity

Common Screens:
- Dashboard
- Rule Management
- Supported Apps Selection
- Schedule Settings
- Subscription Screen
- Accessibility Permission Screen
- Notification Access Screen

Code Expectations:
- Use Material 3 components
- Use Compose Navigation
- Use sealed UI states
- Use immutable state models
- Keep previews when useful
