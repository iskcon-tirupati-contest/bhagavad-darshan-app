# Bhagavad Darshan Subscriptions

Android app for managing devotee subscriptions to **భాగవత దర్శనం / Bhagavad Darshan** (ISKCON monthly magazine).

## Phase 1 (now)

- Kotlin + Jetpack Compose
- Local Room database (works offline for collectors)
- Flyer pricing (with postage + gift books)
- Register, search, expiring list

## Phase 2 (later — same EC2 as iskconcontest.org)

- Node.js API + PostgreSQL
- Web self-subscribe via QR
- Admin push (FCM) for ending subscriptions

## Open in Android Studio

1. File → Open → `/home/radha/bhagavad-darshan-subs`
2. Wait for Gradle sync
3. Run on emulator or device

## Flyer plans

| Years | Magazine | Postage | Total | Gift books |
|------:|---------:|--------:|------:|-----------:|
| 1 | ₹450 | ₹84 | ₹534 | 1 |
| 2 | ₹900 | ₹168 | ₹1068 | 2 |
| 3 | ₹1450 | ₹252 | ₹1702 | 4 |
| 5 | ₹2400 | ₹420 | ₹2820 | 6 |
