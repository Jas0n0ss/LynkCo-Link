# LynkCo-Link 0.3.5 — Lynk&co

- One-click **Collect vehicle report (ZIP)** in Settings → Diagnostics.
- Includes build, display, audio, USB/input, Bluetooth/Wi-Fi, permissions, UI tree, getprop/dumpsys, diagnostic report, logcat, crash buffer, session logs and a screenshot.
- Secrets, identifiers and password fields are redacted.
- Designed for per-model head-unit adaptation.

Install:

```sh
adb install -r LynkCo-Link-0.3.5-lynk-release-standalone.apk
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```
