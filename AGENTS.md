# Project Overview

We are building an Android auto-reply application inspired by:
https://play.google.com/store/apps/details?id=com.guibais.whatsauto

Main Goal:
Detect incoming messages from supported messaging/social applications and send automated replies
using Android Accessibility Service automation.

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

Subscription Logic:

- User can enable auto-reply for maximum 2 applications at a time in free/basic plan
- Additional application support may require subscription in future

Tech Stack:

- Kotlin
- Jetpack Compose
- MVVM
- Hilt
- Room DB
- Coroutines + Flow
- Node.js
- Express.js
- PostgreSQL
- Prisma ORM

Important:

- Accessibility Service is core
- Notification Listener is used for message detection
- Avoid unofficial APIs for messaging platforms
- Android only
- MVP first
- Real-device testing is critical

Coding Rules:

- Prefer small reusable classes
- Use repository pattern
- Use StateFlow
- Follow Compose best practices
- Keep architecture modular
- Avoid overengineering
- Prefer practical implementation over theoretical abstraction

Current Focus:

- Notification parsing
- Accessibility automation
- Rule engine
- Multi-app support architecture
- Backend integration