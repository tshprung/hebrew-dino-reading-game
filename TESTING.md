# Testing — Dino Reading Hebrew

## Unit tests (JVM, no device)

```bash
.\gradlew.bat test
```

Runs everything under `app/src/test/` (station registry, generators, catalogs, navigation state, view models, etc.).

### Season 2 gameplay regression

| Test class | What it guards |
|------------|----------------|
| `Season2LevelSessionMatrixTest` | Every playable S2 station (40 total) completes a full `LevelSession` run |
| `Season2LevelSessionEdgeCasesTest` | Wrong answers don't score; drag/rhyme/word-parts content rules; 8× repeat runs per station |
| `Season2ChapterValidatorTest` | Chapter asset/station readiness checks reject bad poster, catalog, and letter-pool data |

Batch/wiring audit tests read production `.kt` files via [`ProjectSource.read`](app/src/test/java/com/tal/hebrewdino/test/ProjectSource.kt) instead of duplicating file I/O helpers.

Memory-match stations (Ch2 st4, Ch7 st3) are excluded from `LevelSession` tests — they use a dedicated screen.

Manual QA on a device or emulator is used for UI and gameplay flows.

### Edge-to-edge (Android 15+)

`MainActivity` calls `enableImmersiveFullscreen()`, which uses `enableEdgeToEdge()` plus immersive system-bar hiding. Top/back controls use `topChromeInsetsPadding()`; settings uses `safeContentInsetsPadding()`. See `EdgeToEdgeSetupTest`.

