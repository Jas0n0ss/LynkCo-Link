# OSNPlay 0.1.2 Preview

发布日期：2026-11-07
标签：`v0.1.2-preview`
版本：`0.1.2-preview` / versionCode `102`
目标：23 款领克 01 / OSN 2.0 / 骁龙 8155（同平台 03 参考）
类型：Preview（预览版；GitHub Release 不勾选 Pre-release，以便在 Releases 侧栏和最新版本中显示）

本版重点解决 **领克 OSN 2.0 / 23 款领克 01** 上“ADB 安装成功但应用列表不显示图标”的可用性问题，并合并四个上游项目的相关经验。

## 下载

| 文件 | applicationId | 说明 |
| --- | --- | --- |
| `OSNPlay-0.1.2-preview-standard.apk` | `com.osnplay.preview` | 标准预览包，仅用于界面与设置验证 |
| `OSNPlay-0.1.2-preview-lynk.apk` | `com.osnplay.lynk.preview` | 领克预览包，含 lynk 默认入口策略 |
| `SHA256SUMS.txt` | — | 下载后校验 |

> **默认预览包不含 CarPlay 配件认证身份。** 可以安装、浏览界面、修改设置和导出诊断，但**无法完成真实 CarPlay 连接**。实车连接请使用自己合法持有的认证资产按 [BUILD.md](BUILD.md) 构建 standalone 包。仓库不分发认证私钥。

## 本版内容

### 桌面可见性

- 自适应图标 + 各密度 PNG 回退；Activity 显式声明 `label`/`icon`。
- 增加 `LEANBACK_LAUNCHER` 入口，兼容部分只索引 leanback 类别的车机/桌面。
- `lynk` 版本默认开启开机自动启动，并在 `BOOT_COMPLETED` 发布“打开 OSNPlay”通知。
- `lynk` 版本默认开启桌面悬浮球；首次运行提示授予“显示在其他应用上层”。
- 文档补充 OSN 原厂 Launcher3 包名白名单、ADB `am start`、小八启动器/应用页等替代入口。

### 上游整合

- DiPlay 0.2.22：主基线、桌面悬浮球、开机启动、Launcher 元数据。
- LynkCo-DiPlay：`lynk` 配置、环境检测/诊断、领克自适应图标做法、03 实车结论。
- xcertplay：工程结构、Manifest/Launcher 组织方式。
- diplay-ecarx：01/ECARX 无 ADB 运行思路、平台权限清单、1920×1080 车机画像（代码未直接合并）。

详见 [UPSTREAM-INTEGRATION.md](UPSTREAM-INTEGRATION.md)。

### 修复与变更

- 版本 `0.1.2` / versionCode 102。
- 修复新安装后通知/自启的入口问题；说明 Android stopped state 下首次必须手动启动一次。
- 增加 overlay 权限的 ADB 兜底命令。

## 验证

| 项 | 结果 |
| --- | --- |
| `:shared:testDebugUnitTest` | 1798 通过 / 1 跳过 |
| `:common:testDebugUnitTest` | 1761 通过 |
| `:mobile:lintStandardDebug` | 通过，0 阻断 |
| debug / preview 四变体 | 构建成功 |
| 公开预览 APK 认证资产 | 无 |
| 0.1.2 lynk standalone | `com.osnplay.lynk` / `0.1.2-lynk` / 102，含认证资产，release 签名 |
| Launcher 元数据 | `launchable-activity` + `leanback-launchable-activity` 均存在 |

详细记录见 [TESTING.md](TESTING.md)。

## 安装后首次启动

Android 的 stopped state 会让新安装应用在首次手动启动前收不到 `BOOT_COMPLETED`：

```sh
adb install -r OSNPlay-0.1.2-preview-lynk.apk
adb shell am start -n com.osnplay.lynk.preview/com.shilapi.xcertplay.DiPlayActivity
```

首次启动后，重启车机，`lynk` 包的开机自启、通知和悬浮球才会工作。

## 已知限制

- **原厂 Launcher3 的包名白名单无法由普通 APK 绕过。** 本版提供开机自启、通知、悬浮球、LEANBACK 和文档替代入口，但不能强制原厂首页显示图标。需要首页图标时使用小八启动器/应用页、ADB 启动，或修改系统白名单。
- 公开预览包无认证身份，不能完成真实 CarPlay 连接。
- 预览包使用 debug 签名和 `.preview` 包名，不是正式发布签名。
- 领克 01 2023 仍需实车验收；同平台 03 的结论来自 LynkCo-DiPlay R1。
- 部分非中英文长文案仍沿用上游 DiPlay 表述。

## 校验

```sh
sha256sum -c SHA256SUMS.txt
```

## 反馈

请附车型/年款、OSN/Android/固件、iPhone/iOS、APK 版本、安装方式和 `adb shell pm list packages | grep osnplay` 等脱敏信息。不要上传热点密码、认证文件或含个人信息的截图。
