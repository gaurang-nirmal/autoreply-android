# Reply Engine Rules

Goal:
Centralize auto-reply execution behavior.

Reply Types:

- Custom Message
- Keyword Reply
- Spreadsheet Reply
- Menu Reply
- AI Reply
- Server Reply

Execution Flow:

1. Detect active reply type
2. Load reply type configuration
3. Evaluate timing rules
4. Evaluate reply limits
5. Evaluate contact restrictions
6. Execute reply

Timing Modes:

1. Reply Every Time

- Reply immediately for every incoming message

2. Reply And Wait

- Reply once
- Pause replies for configured duration
- Resume after wait duration expires

3. Reply After Delay

- Delay reply execution
- Send after configured duration

4. Reply Once

- Reply only one time per chat/session
- Ignore further messages until reset

Reply Limits:

- Optional per-contact reply limit
- Stop replying after max replies reached

Reply Limit List:

- Maintain per-contact reply counters

Rules:

- Active reply type controls execution
- AutoReplyConfigScreen acts as centralized selector
- Shared Reply Time screen must be reused
- No duplicate timing implementations

Architecture:

- Keep timing evaluation centralized
- Avoid duplicated reply execution logic
- Prefer reusable evaluators/resolvers

Suggested Components:

- ActiveReplyConfigResolver
- ReplyTimingEvaluator
- ReplyLimitEvaluator