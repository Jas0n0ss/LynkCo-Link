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
- **车机适配**：分辨率、画面尺寸、帧率、日夜模式、音频缓冲、方向盘媒体键 / Siri 键；CarPlay 车辆按钮使用 Lynk & Co 标识。
- **OSN 桌面兼容**：CarPlay 图标、开机自启/通知、桌面悬浮球；原厂 Launcher3 白名单限制见 [安装指南](docs/INSTALL.md#安装成功但桌面不显示-osnplay)。
- BYD 固件专属功能（HUD、仪表卡片、氛围灯等）不在 OSN 适配范围内。

## 构建

需要 JDK 17+、Android SDK Platform 37.2、Build-Tools 37.0.0、NDK 28.2.13676358。

只构建功能包：

```sh
export OSNPLAY_AUTH_ASSETS_DIR=/path/to/auth
export ANDROID_KEYSTORE_PATH=/path/to/release.jks
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS='...'
export ANDROID_KEY_PASSWORD='...'

./gradlew :mobile:assembleStandaloneLynkRelease
./gradlew :launcher-entry:assembleRelease
```

产物：

```text
mobile/build/outputs/apk/lynk/release/mobile-lynk-release.apk
launcher-entry/build/outputs/apk/release/launcher-entry-release.apk
```  
包名 `com.osnplay.lynk`，版本 `0.1.2-lynk`，release 签名，含显式提供的认证资产。

源码/CI 构建不含认证身份，只能验证界面和设置；**真实 CarPlay 连接需要认证资产**。公开 Release 的 `0.1.2` 功能包已包含该资产；源码树本身不分发私钥。

## 安装（小白版）

公开 Release 提供两个 APK，作用不同：

| APK | 作用 | 是否必需 |
| --- | --- | --- |
| `OSNPlay-<version>-lynk-release-standalone.apk` | 主程序，含认证资产，负责 CarPlay 连接 | 必需 |
| `OSNPlay-Whitelist-Entry-1.0.apk` | 白名单入口，让原厂应用列表显示 OSNPlay，点击后打开主程序 | 原厂列表不显示图标时安装 |

> 旧 preview 包不能连接 CarPlay。主程序必须安装；白名单入口按需安装。

### 准备

- 车停稳，准备好 iPhone。
- APK 文件：`OSNPlay-0.1.2-lynk-release-standalone.apk`。
- 一根**公对公 USB 数据线**（电脑装 APK），或一个 FAT32 格式 U 盘（车机文件管理器装 APK）。
- 电脑安装 [Android Platform-Tools](https://developer.android.com/tools/releases/platform-tools)（含 `adb`）。

### 第一步：让车机允许安装

**方法 A：U 盘安装（固件允许时最简单）**

1. 把 APK 拷到 FAT32 U 盘；
2. U 盘插车机 USB 口，用车机文件管理器打开 APK；
3. 按提示允许“未知来源”，安装。

**方法 B：ADB 安装（OSN 更常用）**

1. 车机打开拨号，输入 `2468789`，看是否进入原生 Android 设置；
2. 进入 **关于平板电脑 / 关于设备**，连续点击 **版本号** 7 次；
3. 返回 **开发者选项**，打开 **USB 调试**；
4. 用公对公数据线连接车机 **USB-A 数据口**和电脑；
5. 电脑终端执行：

```sh
adb devices
```

车机会弹出“允许 USB 调试”，勾选“始终允许”并确定。

> 拨号 `2468789` 不生效时，可试输入法入口：任意输入框 → 输入法设置 → 字典 → 编辑自定义短语 → 返回，进入系统设置。不同 OSN 版本可能封堵入口。

### 第二步：安装 APK

```sh
# 必需：主程序
adb install -r OSNPlay-0.1.2-lynk-release-standalone.apk

# 可选：原厂应用列表不显示 OSNPlay 时安装入口
adb install -r OSNPlay-Whitelist-Entry-1.0.apk
```

> 入口包使用 `com.kugou.android.auto`（OSN Launcher3 白名单中的酷狗音乐车机版包名），显示名为 OSNPlay，点击后打开主程序。安装前检查冲突：
> `adb shell pm list packages | grep com.kugou.android.auto`。若已有真正的酷狗车机版，不要覆盖。

常见报错：

| 报错 | 处理 |
| --- | --- |
| `adb: command not found` | 安装 Platform-Tools，或在 Platform-Tools 目录里执行 `adb` |
| `device unauthorized` | 在车机上允许 USB 调试；不行就撤销 USB 调试授权后重连 |
| `INSTALL_FAILED_USER_RESTRICTED` | 开发者选项里打开“USB 安装/未知来源”；或执行 `adb shell settings put secure install_non_market_apps 1` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | 旧包签名不同，先 `adb uninstall com.osnplay.lynk` 再装（会丢设置） |

### 第三步：第一次打开

Android 新安装应用在第一次手动启动前处于 stopped state，开机自启不会生效，所以必须打开一次：

```sh
adb shell am start -n com.osnplay.lynk/com.shilapi.xcertplay.OSNPlayActivity
```

没有 ADB 时，安装完点安装器的“打开”，或用小八启动器/应用页打开一次。

### 第四步：桌面没有图标怎么办

领克 OSN 原厂 Launcher3 使用**包名白名单**，第三方 APK 可能不出现在应用列表。这不是安装失败。

1. 先重启车机，找“所有应用 / 应用管理”；
2. 仍没有：进入 **设置 → 高级 → 桌面悬浮球**，允许“显示在其他应用上层”，桌面会出现 OSNPlay 悬浮图标；
3. 或每次用 ADB 打开：

```sh
adb shell am start -n com.osnplay.lynk/com.shilapi.xcertplay.OSNPlayActivity
```

4. 安装 `OSNPlay-Whitelist-Entry-1.0.apk` 后，应用列表会出现 OSNPlay 入口，点击打开主程序；它借用了白名单包名，安装前检查冲突，详见 [安装指南](docs/INSTALL.md#安装成功但桌面不显示-osnplay)。
5. 想彻底显示在原厂首页，只能修改系统白名单（root/工程模式）或使用替代启动器；普通 APK 无法绕过。

### 第五步：连接 iPhone

- **有线**：数据线接车机 USB-A 数据口，打开 OSNPlay，iPhone 弹窗点“允许”；
- **无线**：OSNPlay → 连接设置，选择车机热点/Wi-Fi Direct/Existing Wi-Fi，按提示配对；
- 连接失败：**设置 → 诊断 → 保存诊断报告**，用于排查。

### 卸载

车机 **设置 → 应用 → OSNPlay → 卸载**，或：

```sh
adb uninstall com.osnplay.lynk
```

卸载会清除应用设置和配对记录。

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
