# OSNPlay

**面向领克（Lynk & Co）OSN 车机的独立 CarPlay 接收端。**

[![Android checks](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml/badge.svg)](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/Jas0n0ss/OSN-Play?label=release)](https://github.com/Jas0n0ss/OSN-Play/releases)
[![License](https://img.shields.io/badge/license-GPL--3.0%20%2F%20AGPL--3.0-blue.svg)](LICENSING.md)

OSNPlay 基于 [DiPlay](https://github.com/shihabal3amri/DiPlay) 0.2.22，整合 [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay)、[xcertplay](https://github.com/shilapi/xcertplay) 和 [diplay-ecarx](https://github.com/jianyun8023/diplay-ecarx) 的领克适配经验。应用安装在**车机**上，不需要越狱 iPhone、外设或认证服务器。

## 适配车型

| 车型 | 平台 / 系统 | 状态 |
| --- | --- | --- |
| 领克 01 2023 款 | 骁龙 8155 / OSN | 🎯 开发目标，待 OSNPlay 实车验收 |
| 领克 03 2023 款 | OS N 2.0 / Android 9 | 🧪 同平台参考：有线可用，无线未调通 |
| 其他领克 OSN 车型 | OSN / Android 7.0+ | ⚪ 未验证 |

“参考”结论来自同平台 LynkCo-DiPlay，不等同于 OSNPlay 的实车验收。详见 [兼容性说明](docs/COMPATIBILITY.md)。

## 功能

- **有线 / 无线 CarPlay**：USB、车机热点、Wi-Fi Direct、Existing Wi-Fi，认证在本地完成。
- **环境检测与诊断**：设置 → 诊断中检查应用 / USB / 无线状态；可导出脱敏诊断报告。
- **车机适配**：分辨率、画面尺寸、帧率、日夜模式、音频缓冲、方向盘媒体键 / Siri 键。
- **OSN 桌面兼容**：自适应图标、开机自启/通知、桌面悬浮球；原厂 Launcher3 白名单限制见 [安装指南](docs/INSTALL.md#安装成功但桌面不显示-osnplay)。
- BYD 固件专属功能（HUD、仪表卡片、氛围灯等）不在 OSN 适配范围内。

## 构建

需要 JDK 17+、Android SDK Platform 37.2、Build-Tools 37.0.0、NDK 28.2.13676358。

只构建功能包：

```sh
export DIPLAY_AUTH_ASSETS_DIR=/path/to/auth
export ANDROID_KEYSTORE_PATH=/path/to/release.jks
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS='...'
export ANDROID_KEY_PASSWORD='...'

./gradlew :mobile:assembleStandaloneLynkRelease
```

产物：`mobile/build/outputs/apk/lynk/release/mobile-lynk-release.apk`  
包名 `com.osnplay.lynk`，版本 `0.1.2-lynk`，release 签名，含显式提供的认证资产。

源码/CI 构建不含认证身份，只能验证界面和设置；**真实 CarPlay 连接需要你自己合法持有的 `DIPLAY_AUTH_ASSETS_DIR`**。本仓库不分发认证私钥。

## 安装与桌面显示

按 [安装指南](docs/INSTALL.md) 安装。注意：

- 全新安装后需手动启动一次（Android stopped state），之后开机自启/通知才生效；
- 领克 OSN 原厂 Launcher3 使用包名白名单，第三方 APK 可能不出现在应用列表；可用 ADB `am start`、替代启动器或桌面悬浮球打开；
- 原厂首页图标只能通过系统白名单或替代启动器解决，普通 APK 无法绕过。

## 文档

- [构建与 CI](docs/BUILD.md) · [安装](docs/INSTALL.md) · [兼容性](docs/COMPATIBILITY.md)
- [测试与验收](docs/TESTING.md) · [四个上游项目整合](docs/UPSTREAM-INTEGRATION.md)
- [隐私](docs/PRIVACY.md) · [安全](SECURITY.md) · [许可](LICENSING.md) · [第三方声明](docs/THIRD_PARTY_NOTICES.md)

## 声明

- **非 Apple 认证产品**，与 Apple Inc.、领克汽车无隶属或背书关系；CarPlay 图标归 Apple Inc.。
- 认证身份为公开固件恢复的实验性身份，未来 iOS 更新可能失效；公开分发适宜性未获保证。
- 在车机安装第三方 APK 可能影响质保或违反车厂协议；安装、调试请在驻车状态进行，风险自担。

## 许可与致谢

- 基于 DiPlay / xcertplay（GPL-3.0）二次开发；设置 UI 的四个文件为 AGPL-3.0-only，整体为 GPL/AGPL 组合作品，详见 [LICENSING.md](LICENSING.md)。
- 设置界面改编自 [DiAuto](https://github.com/wycomco/diauto)（AGPL-3.0）；感谢 LynkCo-DiPlay 的领克适配经验。
- 第三方依赖与素材许可见 [THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md)。

[English](README.en.md)
