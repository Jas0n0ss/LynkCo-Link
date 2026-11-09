# LynkCo-Link 0.3.6 — Lynk&co

- Apple HIG-inspired UI review and fixes.
- Body text is now 17sp, supporting text 16sp and minimum labels 14sp for head-unit viewing distance.
- Replaced the stale "DiLink version" copy with OSN.
- The home status chip now says "Not paired" until an iPhone is chosen, then "Ready for <iPhone>".
- Reduced secondary button weight and removed the low-value home version label.
- Added `docs/DESIGN-REVIEW-APPLE.md`, including the Liquid Glass rationale for Android 9.

Install:

```sh
adb install -r LynkCo-Link-0.3.6-lynk-release-standalone.apk
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```
