# OSNPlay

**面向领克（Lynk & Co）OSN 车机的独立 CarPlay 接收端。**

[![Android checks](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml/badge.svg)](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml)
[![Latest preview](https://img.shields.io/github/v/release/Jas0n0ss/OSN-Play?include_prereleases&label=preview)](https://github.com/Jas0n0ss/OSN-Play/releases)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0%20%2F%20AGPL--3.0-blue.svg)](LICENSE)

OSNPlay 是 [DiPlay](https://github.com/shihabal3amri/DiPlay)（GPL-3.0，0.2.22 基线）面向领克 OSN 车机平台的二创项目，并整合了 [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay) 的领克适配成果（构建风味、环境检测、连接诊断与实车记录）。

- 安装在**车机**上，不越狱 iPhone、不需要外设或认证服务器。
- 包名 `com.osnplay`，可与原版 DiPlay 及领克测试包并存安装。
- 当前开发目标：**领克 01 2023 款（骁龙 8155 / OSN）**；同平台已有 2023 款领克 03 的有线实测记录。

## 适配车型

| 车型 | 平台 / 系统 | OSNPlay 状态 | 说明 |
| --- | --- | --- | --- |
| 领克 01 2023 款 | 骁龙 8155 / OSN | 🎯 开发目标，待实车验证 | 默认目标；分辨率、热点和按键行为需按实车确认 |
| 领克 03 2023 款 | OS N 2.0 / Android 9（API 28） | 🧪 同平台参考验证 | 同平台 LynkCo-DiPlay R1 确认有线可用；USB-A 为数据口，USB-C 仅充电；无线未调通 |
| 领克 OSN 系列其他车型 | OSN / Android 7.0+（API 24+） | ⚪ 未验证 | APK 可安装，但连接、音频和显示行为需逐车实测 |

OSNPlay 自身的实车验收仍在进行中。上表中的“参考验证”来自同平台兄弟项目，不等同于 OSNPlay 在当前固件上的验收结论。逐项证据、风险和实车清单见 [兼容性说明](docs/COMPATIBILITY.md)。

## 功能

- **有线 / 无线 CarPlay**：USB、车机热点、Wi-Fi Direct、Existing Wi-Fi / Same LAN；认证在本地完成。
- **环境检测**：设置 → 诊断中只读检查应用、USB、无线三组就绪状态，并提供对应修复入口。
- **连接诊断**：保留最近一次致命栈，导出 USB 描述符、网络、蓝牙、热点和媒体运行时快照；报告仅在你主动分享时离开车机。
- **显示适配**：分辨率、画面尺寸、帧率、日夜模式、系统栏和分屏区域。
- **音频与按键**：音乐缓冲、导航音量、可选的方向盘媒体键 / Siri 键（部分功能需要 ADB 或无障碍服务）。
- **上游能力**：自动连接、开机启动、车机热点自动化、诊断报告等 DiPlay 0.2.22 能力。

比亚迪固件专属能力（HUD 导航、仪表卡片、氛围灯、BYD 设置入口等）不在 OSN 的适配范围内，相关代码仅为兼容上游而保留。

## 下载与安装

从 [Releases](https://github.com/Jas0n0ss/OSN-Play/releases) 下载预览版 APK，按 [安装指南](docs/INSTALL.md) 安装到车机。

| 包 | applicationId | 用途 |
| --- | --- | --- |
| `OSNPlay-<version>-standard.apk` | `com.osnplay` | 主包，日常使用 |
| `OSNPlay-<version>-lynk.apk` | `com.osnplay.lynk` | 领克 A/B 测试包，可与主包并存 |

> **默认预览包不含 CarPlay 配件认证身份。** 公开源码和 CI 构建只包含应用代码，可安装、浏览界面和验证设置，但**无法完成真实 CarPlay 连接**。需要实车连接时，请按 [构建说明](docs/BUILD.md) 用你自己合法持有的 `DIPLAY_AUTH_ASSETS_DIR` 构建 standalone 包。仓库不会分发认证私钥。

## 从源码构建

需要 JDK 17+（CI 使用 25）、Android SDK Platform 37.2、Build-Tools 37.0.0 和 NDK 28.2.13676358。

```sh
# 单元测试 + lint
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :mobile:lintStandardDebug

# 源码 APK（无认证身份）
./gradlew :mobile:assembleStandardDebug :mobile:assembleLynkDebug

# 预览包（debug 签名，独立 `.preview` 包名）
./gradlew :mobile:assembleStandardPreview :mobile:assembleLynkPreview
```

完整环境、签名、standalone 构建和 CI 说明见 [docs/BUILD.md](docs/BUILD.md)。

## 版本与构建

| 构建类型 | 包名 | 版本示例 | 签名 | 用途 |
| --- | --- | --- | --- | --- |
| `standardDebug` | `com.osnplay.debug` | `0.1.1-debug` | debug | 开发调试 |
| `lynkDebug` | `com.osnplay.lynk.debug` | `0.1.1-lynk-debug` | debug | 领克开发调试 |
| `standardPreview` | `com.osnplay.preview` | `0.1.1-preview` | debug | 公开预览 / UI 验收（与正式版独立） |
| `lynkPreview` | `com.osnplay.lynk.preview` | `0.1.1-lynk-preview` | debug | 领克公开预览（与正式版独立） |
| `standardRelease` | `com.osnplay` | `0.1.1` | release | 正式签名发布 |
| `lynkRelease` | `com.osnplay.lynk` | `0.1.1-lynk` | release | 领克正式签名发布 |

`standard` / `lynk` 是产品风味（distribution edition），`debug` / `preview` / `release` 是构建类型。CI 在每次 push 和 PR 上运行单元测试、lint、debug / preview 四个变体构建和 release 签名验证，并上传 APK 与测试报告。

## 文档

- [文档索引](docs/README.md)
- [构建与 CI](docs/BUILD.md) · [安装指南](docs/INSTALL.md) · [兼容性矩阵](docs/COMPATIBILITY.md)
- [改造说明与路线图](docs/ADAPTATION-NOTES.md) · [测试与验收](docs/TESTING.md)
- [设计评审](docs/DESIGN-REVIEW.md) · [预览版发布说明](docs/RELEASE-0.1.1-PREVIEW.md)
- [隐私](docs/PRIVACY.md) · [安全](SECURITY.md) · [第三方声明](docs/THIRD_PARTY_NOTICES.md)
- [English README](README.en.md)

## 重要声明

- **非 Apple 认证产品。** 本项目与 Apple Inc.、领克汽车无隶属或背书关系。CarPlay 与相关图标归 Apple Inc. 所有。
- 认证身份来自公开固件恢复的实验性配件身份，并非 Apple 新颁发的 MFi 身份；未来 iOS 更新后可能失效，公开分发的适宜性未获保证。
- 在车机上安装第三方 APK 可能影响质保或违反车厂用户协议；请自行评估风险。
- 驾驶时请勿操作与驾驶无关的功能。安装、调试和抓取日志请在驻车状态下进行。

## 许可与致谢

- 基于 [DiPlay](https://github.com/shihabal3amri/DiPlay) / [xcertplay](https://github.com/shilapi/xcertplay)（GPL-3.0）二次开发；仓库以 **GPL-3.0** 为主，设置 UI 的四个文件为 **AGPL-3.0-only**，整体为两者组成的作品，详见 [LICENSING.md](LICENSING.md) 与 [LICENSE](LICENSE)。
- 设置界面改编自 [DiAuto](https://github.com/wycomco/diauto)（AGPL-3.0），许可文本见 `docs/licenses/`。
- 感谢 [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay) 提供的领克 OSN 适配经验与诊断模块。
- 依赖和素材许可见 [docs/THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md)。
