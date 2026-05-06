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

Architecture Goal:
Keep implementation scalable and app-agnostic.

Notification Flow:
Incoming Notification
→ Notification Listener
→ App Detection
→ Message Parser
→ Rule Engine
→ Accessibility Automation
→ Send Reply

Recommended Design:

- Each supported app should have:
    - Notification parser
    - Accessibility automation handler
    - Package identifier
    - UI selector strategy

Suggested Structure:

supportedapps/
├── whatsapp/
├── telegram/
├── messenger/
├── instagram/
└── common/

Backend Architecture:

- Express.js
- Prisma ORM
- PostgreSQL
- JWT Authentication

API Structure:

- /auth
- /rules
- /settings
- /subscriptions
- /supported-apps
- /users