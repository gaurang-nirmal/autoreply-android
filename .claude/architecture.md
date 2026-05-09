# Mobile Architecture

Architecture:

- MVVM
- Repository Pattern
- Single Activity Architecture
- Jetpack Compose Navigation

Layers:

1. UI Layer
2. ViewModel Layer
3. Domain Layer
4. Repository Layer
5. Data Layer

Core Android Components:

- Accessibility Service
- Notification Listener Service
- Room Database
- WorkManager
- Hilt DI

Folder Structure:

app/
├── ui/
├── viewmodel/
├── domain/
├── data/
├── repository/
├── service/
├── database/
├── model/
├── parser/
├── automation/
├── supportedapps/
└── utils/

Architecture Goal:
Keep implementation scalable and app-agnostic.

Notification Flow:
Incoming Notification
→ Notification Listener
→ App Detection
→ Message Parser
→ Rule Engine
→ Reply Execution

Reply Execution Priority:

1. Notification direct reply (RemoteInput)
2. Accessibility automation fallback

Recommended Structure:

supportedapps/
├── whatsapp/
├── telegram/
├── messenger/
├── instagram/
└── common/

Each app module should contain:

- Notification parser
- Automation handler
- Package identifiers
- Selector strategy
