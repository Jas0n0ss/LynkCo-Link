# LynkCo-Link 0.3.4 — Lynk&co

- Added **Run head-unit checks** under Settings → Diagnostics.
- Checks cover Lynk model/platform, runtime permissions, display, optional cluster display, audio outputs, wheel-key service, CarPlay authentication assets, language and build.
- Results are included in the diagnostic report and debug-log ZIP.
- Added `scripts/lynk-acceptance.sh` and a real-car acceptance checklist.

Install:

```sh
adb install -r LynkCo-Link-0.3.4-general-signed.apk
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```
