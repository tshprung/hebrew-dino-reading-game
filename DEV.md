# Developer guide — Dino Reading Hebrew

Android game app (Kotlin, Jetpack Compose). Landscape-first children’s reading practice.

## Requirements

- Android Studio (recent stable) or JDK 17 + Android SDK
- Device or emulator for UI tests and manual QA

## Build and run

```bash
.\gradlew.bat assembleDebug
```

Install/run from Android Studio, or:

```bash
.\gradlew.bat installDebug
```

## Tests

See [TESTING.md](TESTING.md) for full detail.

| Command | What it runs |
|---------|----------------|
| `.\gradlew.bat test` | JVM unit tests (`app/src/test/`) |
| `.\gradlew.bat connectedDebugAndroidTest` | Instrumentation UI tests (device required) |

## Project layout (high level)

| Path | Role |
|------|------|
| `app/src/main/java/com/tal/hebrewdino/ui/AppNav.kt` | NavHost entry; wires graphs |
| `app/src/main/java/com/tal/hebrewdino/ui/AppNav*Graph.kt` | Route definitions by area |
| `app/src/main/java/com/tal/hebrewdino/ui/domain/` | Station plans, registry, generators, chapter config |
| `app/src/main/java/com/tal/hebrewdino/ui/screens/GameScreen.kt` | In-station gameplay orchestration |
| `app/src/main/java/com/tal/hebrewdino/ui/screens/*Actions.kt` | Per-game interaction handlers |
| `app/src/main/res/drawable-nodpi/` | PNG backgrounds, dino sprites, UI art |
| `app/src/main/assets/` | Audio (WAV) and other bundled media |

## Assets

- Large PNGs live in `drawable-nodpi/` (no density scaling).
- Opening hero: `opening_splash_art.png`
- Forest/journey backgrounds: `forest_bg_*`, `chapter*_*.png`
- Dino animation frames: `dino_talk_0..3`, `dino_idle`, etc.
- Third-party notes: [ASSETS.md](ASSETS.md)

## Navigation graphs

- **System**: opening, seasons, chapters map, settings, training v1
- **Chapters 1–3**: beach story, ch2/ch3 journeys, shared level/reward routes
- **Chapters 4–6**: same pattern with chapter-specific assets

State derived from `MainViewModel` → `AppNavHostState` (progress, unlocks, egg strip).

## UI conventions

- App RTL; some screens use `LayoutDirection.Ltr` for layout math (journey road, opening).
- Immersive fullscreen: `ImmersiveFullscreen.kt` + `topChromeInsetsPadding()` on top buttons.
- Station back button extra inset: `GameBackButtonExtraTopInset` in `GameScreenVisuals.kt`.

## Privacy / store

Public privacy policy README is for GitHub Pages; game package id is in `app/build.gradle.kts`.
