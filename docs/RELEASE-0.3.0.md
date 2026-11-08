# LynkCo-Link 0.3.0 — Lynk&co

- 品牌从 OSNPlay 统一改为 **LynkCo-Link**。
- 应用名、类名、资源、文档和 CI 产物同步改名。
- 标准包 applicationId 改为 `com.lynkco.link`；交付包仍为 `com.kugou.android.auto`。
- 删除桌面悬浮球。
- 新增 Debug 车辆 Mock SDK：空调、媒体、Parked/Driving/Charging。

安装：

```sh
adb install -r LynkCo-Link-0.3.0-lynk-release-standalone.apk
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```
