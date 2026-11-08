# OSNPlay

**在领克（Lynk & Co）OSN 车机上使用 CarPlay。**

[![Android checks](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml/badge.svg)](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/Jas0n0ss/OSN-Play?label=release)](https://github.com/Jas0n0ss/OSN-Play/releases)
[![License](https://img.shields.io/badge/license-GPL--3.0%20%2F%20AGPL--3.0-blue.svg)](LICENSING.md)

OSNPlay 安装在**车机**上，不需要越狱 iPhone、外设或认证服务器。公开 Release 的 APK 已内置认证资产，安装后可直接连接。

## 适配车型

| 车型 | 平台 / 系统 | 状态 |
| --- | --- | --- |
| 领克 01 2023 款 | 骁龙 8155 / OSN | 开发目标，待实车验收 |
| 领克 03 2023 款 | OSN 2.0 / Android 9 | 同平台参考：有线可用，无线未调通 |
| 其他领克 OSN 车型 | OSN / Android 7.0+ | 未验证 |

详见 [兼容性说明](docs/COMPATIBILITY.md)。

## 下载与安装

只下载一个文件：

```text
OSNPlay-<version>-lynk-release-standalone.apk
```

从 [Releases](https://github.com/Jas0n0ss/OSN-Play/releases/latest) 下载，或：

```sh
gh release download --repo Jas0n0ss/OSN-Play
```

> 包名是 `com.kugou.android.auto`，只用于通过 OSN 桌面白名单。应用本身是 OSNPlay，不含酷狗功能。安装前确认车机没有真正的酷狗车机版。

### 准备

- 车停稳，准备好 iPhone。
- 一根支持数据传输的 USB 线，或 FAT32 U 盘。
- 可选：电脑安装 [Android Platform-Tools](https://developer.android.com/tools/releases/platform-tools)。

### 安装

**U 盘**：把 APK 拷到 U 盘，插车机 USB 口，用文件管理器打开安装。

**ADB**：

1. 车机进入原生 Android 设置（常见入口：拨号输入 `2468789`）。
2. 关于设备 → 连续点击版本号 7 次 → 开发者选项 → 打开 USB 调试。
3. 连接电脑，执行：

```sh
adb devices
adb install -r OSNPlay-<version>-lynk-release-standalone.apk
```

安装后首次必须手动打开一次，否则 Android stopped state 会让开机自启失效：

```sh
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.OSNPlayActivity
```

首次打开会直接进入“连接 → 选择 iPhone”，不会询问 DiLink 版本。

### 桌面没有图标

OSNPlay 已使用原厂 Launcher3 白名单包名，正常会出现在应用列表。如果仍没有：

1. 重启车机，再找“所有应用 / 应用管理”。
2. 设置 → 高级 → 桌面悬浮球，允许“显示在其他应用上层”。
3. 或用 ADB 启动：

```sh
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.OSNPlayActivity
```

### 连接 iPhone

- **有线**：数据线接车机 USB-A 数据口，打开 OSNPlay，iPhone 点“允许”。
- **无线**：OSNPlay → 连接设置，选择车机热点 / Wi-Fi Direct / Existing Wi-Fi，按提示配对。
- 失败时：设置 → 诊断 → 保存诊断报告。

## 常见问题

| 问题 | 处理 |
| --- | --- |
| `adb: command not found` | 安装 Platform-Tools，或在 Platform-Tools 目录执行 |
| `device unauthorized` | 车机上允许 USB 调试；仍失败就撤销授权后重连 |
| `INSTALL_FAILED_USER_RESTRICTED` | 开发者选项打开“USB 安装/未知来源” |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 先卸载旧的 `com.osnplay.lynk` 或旧入口包，再安装（会丢设置） |
| 装完桌面找不到 | 重启车机；或开启桌面悬浮球；或 ADB 启动 |
| 无法连接 CarPlay | 确认 iPhone 已解锁、蓝牙/Wi-Fi 已开，并导出诊断报告 |
| 有真正的酷狗车机版 | 两者包名冲突，先卸载酷狗车机版 |

## 更新与卸载

新版本会作为独立 Release 发布，保留最近两个版本。覆盖安装前确认包名和签名一致；从旧的 `com.osnplay.lynk` 升级需要先卸载。

```sh
adb uninstall com.kugou.android.auto
```

卸载会清除设置和配对记录。

## 从源码构建

需要 JDK 17+、Android SDK Platform 37.2、Build-Tools 37.0.0、NDK 28.2.13676358。认证资产和 release keystore 不进入仓库。

完整步骤见 [构建说明](docs/BUILD.md)。

## 文档

- [安装](docs/INSTALL.md) · [构建](docs/BUILD.md) · [兼容性](docs/COMPATIBILITY.md)
- [测试](docs/TESTING.md) · [隐私](docs/PRIVACY.md) · [安全](SECURITY.md)
- [许可](LICENSING.md) · [第三方声明](docs/THIRD_PARTY_NOTICES.md)

## 声明

- 非 Apple 认证产品，与 Apple Inc.、领克汽车无隶属或背书关系；CarPlay 图标归 Apple Inc.。
- APK 含可提取的实验性认证身份，未来 iOS 更新可能失效，公开分发风险自担。
- 在车机安装第三方 APK 可能影响质保；请在驻车状态操作。

## 许可

基于 DiPlay / xcertplay（GPL-3.0）二次开发，部分设置 UI 为 AGPL-3.0-only，整体为 GPL/AGPL 组合作品。详见 [LICENSING.md](LICENSING.md)。

[English](README.en.md)
