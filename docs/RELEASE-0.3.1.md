# LynkCo-Link 0.3.1 — Lynk&co

- Renamed the product, app label, classes, resources and CI artifacts to LynkCo-Link.
- Standard applicationId is now `com.lynkco.link`; the delivered whitelisted id remains `com.kugou.android.auto`.
- Added a one-time migration for legacy preference files.
- Internal resources, preference names, thread names and log tags now use lynkco_link.

Install:

```sh
adb install -r LynkCo-Link-0.3.1-lynk-release-standalone.apk
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```
