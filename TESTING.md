# Testing — Dino Reading Hebrew

## Unit tests (JVM, no device)

```bash
.\gradlew.bat test
```

Runs everything under `app/src/test/` (station registry, generators, catalogs, etc.).

## UI / instrumentation tests (device or emulator required)

1. Connect a device with USB debugging, or start an Android emulator.
2. Verify ADB sees it:

```bash
adb devices
```

3. Run:

```bash
.\gradlew.bat connectedDebugAndroidTest
```

Runs `app/src/androidTest/` — currently `OpeningAndSeasonsUiTest` (opening + seasons smoke tests).

### Android Studio

- Run the **`androidTest`** configuration for `OpeningAndSeasonsUiTest`, not plain **unit `test`**.
- Pick a device/emulator in the run target dropdown.

### Notes

- Opening screen tests use `enableMotion = false` and a frozen compose clock so infinite animations do not block the test runner.
- If tests hang, confirm a device is online and that you are not running `test` instead of `connectedDebugAndroidTest`.
