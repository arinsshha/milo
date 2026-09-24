# Pocket Companion 🟠

Milo, a floating AI companion that remembers things, sets real reminders, tracks
spending and (soon) knows when *not* to bother you.

```
pocket-companion/
├── shared/       Kotlin Multiplatform: domain, Room DB, API client, Compose UI  (~reused on iOS)
├── androidApp/   Android shell: Activity, AlarmManager reminders, boot receiver
└── backend/      Spring Boot 4 + Gemini: turns messages into structured intents
```

## How a message flows

```
"remind me to pay emi on 5th every month"
   │
   ▼  ChatViewModel ──POST /api/v1/interpret──▶ Spring Boot ──▶ Gemini (JSON schema output)
                                                   │
                                          IntentValidator  (drops junk, flags doubts)
   ◀──────────── { reply, intents[] } ─────────────┘
   │
   ▼  IntentExecutor  (the ONLY thing that writes to Room / schedules alarms)
   ▼  Room + AlarmManager  →  notification on the 5th, re-booked for next month
```

The AI never acts directly. It proposes; deterministic Kotlin decides.

## Run the backend

Needs JDK 21 and a Gemini API key (https://aistudio.google.com/apikey).

```bash
cd backend
export GEMINI_API_KEY=your_key_here        # Windows: set GEMINI_API_KEY=your_key_here
./mvnw spring-boot:run                     # Windows: mvnw.cmd spring-boot:run
./mvnw test                                # validator tests
```

Try it:

```bash
curl -s localhost:8080/api/v1/interpret -H 'Content-Type: application/json' -d '{
  "message": "remind me to pay emi on 5th every month and I spent 250 on lunch",
  "now": "2026-09-24T14:10:00Z",
  "timezone": "Asia/Kolkata"
}'
```

## Run the Android app

Needs Android Studio (latest stable), JDK 17+, and the backend running.

1. Open the `pocket-companion` folder (not `backend/`) in Android Studio and let Gradle sync.
2. Run `androidApp` on an emulator. It talks to `http://10.0.2.2:8080` (your computer).
   On a real phone, change `API_BASE_URL` in `androidApp/build.gradle.kts` to your PC's LAN IP.
3. Tests: `./gradlew :shared:testAndroidHostTest`

## What works now

- Today screen (live from Room), chat with Milo, talking-mouth animation
- Reminders that actually fire, repeat monthly/weekly/daily, and survive reboots
- Memories (incl. temporary ones that expire), expenses in paise, undo on every action
- Yes/no confirmation when the AI is unsure; forgetting always asks
- Intervention engine (pure logic + tests), ready for the Focus feature

## Next up (build order)

| Weeks | Feature |
|-------|---------|
| 3–4   | Memory screen, reminder editing, onboarding + permission explainers |
| 5–6   | Floating overlay bubble (foreground service), voice in/out |
| 7–8   | UsageStats tracking, nudges wired to `InterventionEngine`, Focus screen |
| 9–10  | Money screen, categories, monthly report |
| later | Fraunces/Manrope fonts, auth + Postgres sync, iOS target |

## Before deploying the backend publicly

It has no auth or rate limiting yet. Anyone with the URL could spend your Gemini quota.
Add auth (e.g. Firebase/JWT) and a per-user rate limit first, and remove
`usesCleartextTraffic` from the Android manifest once it's behind HTTPS.
