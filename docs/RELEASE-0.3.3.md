# LynkCo-Link 0.3.3 — Lynk&co

- Added **Export debug logs (ZIP)** in Settings → Diagnostics.
- The zip contains the diagnostic report, this app's logcat, the Android crash buffer and retained session logs.
- Credentials, protocol payloads, IP/MAC addresses and long identifiers are redacted before writing.
- Nothing is uploaded automatically; share the zip from the save dialog.

Install:

```sh
adb install -r LynkCo-Link-0.3.3-lynk-release-standalone.apk
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```
