# LynkCo-Link 0.2.0 — Lynk&co

首个完成 BYD/DiLink 代码清理的版本。

- 删除 BYD/DiLink/FCE 后端、仪表、HUD、氛围灯和 ADB 仪表代码。
- 保留领克 01/02/03/05/06 适配范围，8155 / OSN 2.0 优先。
- CarPlay 主链路、连接、显示、音频、麦克风、方向盘和诊断保留。
- 日夜模式使用 Android 光线传感器。

安装：

```sh
adb install -r LynkCo-Link-0.2.0-lynk-release-standalone.apk
```
