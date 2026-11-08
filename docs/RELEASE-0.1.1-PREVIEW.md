# LynkCo-Link 0.1.1 Preview

发布日期：2026-11-03
标签：`v0.1.1-preview`
类型：Pre-release（预览版）

这是 LynkCo-Link 的首个公开预览版，面向领克 OSN 车机（开发目标：领克 01 2023 / 骁龙 8155）。

## 下载

| 文件 | applicationId | 说明 |
| --- | --- | --- |
| `LynkCo-Link-0.1.1-preview-standard.apk` | `com.lynkco.link.preview` | 标准预览包，仅用于界面与设置验证 |
| `LynkCo-Link-0.1.1-preview-lynk.apk` | `com.lynkco.link.lynk.preview` | 领克 A/B 预览包，与标准包并存 |
| `LynkCo-Link-0.1.1-preview-standalone.apk` | `com.lynkco.link.preview` | 可选：仓库配置了认证资产 secret 时附加；**含可提取的认证材料，风险自担** |
| `SHA256SUMS.txt` | — | 下载后校验 |

> **重要：默认预览包不含 CarPlay 配件认证身份。** 可以安装、浏览界面、修改设置和导出诊断，但**无法完成真实 CarPlay 连接**。实车连接请使用自己合法持有的认证资产按 [BUILD.md](BUILD.md) 构建 standalone 包。可选的 `-standalone.apk` 由仓库 secret 构建，含可提取的认证材料，不应作为通用分发版本。

## 本版内容

- 基于 DiPlay 0.2.22 的领克 OSN 二创基线，包名 `com.lynkco.link`。
- 有线 / 无线 CarPlay、车机热点、Wi-Fi Direct、Same LAN 的源码能力（默认预览包无法完成认证）。
- 领克环境检测面板：应用 / USB / 无线三组只读状态与修复入口。
- 连接诊断：最近一次致命栈、USB 与无线环境快照、脱敏导出。
- `standard` / `lynk` 产品风味，`debug` / `preview` / `release` 构建类型。
- 文档重构：构建、安装、兼容性、测试、隐私、安全、设计评审。
- GitHub Actions：单元测试、lint、debug / preview 四个变体构建与 release 签名验证；标签发布 Pre-release。

## 验证

| 项 | 结果 |
| --- | --- |
| `:shared:testDebugUnitTest` | 1798 通过 / 1 跳过 |
| `:common:testDebugUnitTest` | 1760 通过 |
| `:mobile:lintStandardDebug` | 通过，0 阻断 |
| debug / preview 四个 APK | 构建成功 |
| 预览 APK 认证资产 | 无 |
| 源码树凭据检查 | 通过 |

详细记录见 [TESTING.md](TESTING.md)。所有验证均在宿主完成，**没有进行领克实车验收**。

## 安装

1. 从 Releases 下载 `standard` 或 `lynk` APK。
2. 用 U 盘/文件管理器或 ADB 安装到车机。
3. 打开 LynkCo-Link，按 [安装指南](INSTALL.md) 连接 iPhone。

预览包使用 `.preview` 独立包名，可与正式版同时安装；`standard` 与 `lynk` 是独立应用，设置不共享。预览包使用 CI debug 证书，不同构建之间可能无法覆盖升级，建议卸载后重装并重新配置。

## 已知限制

- 默认预览包无认证身份，不能完成真实 CarPlay 连接；可选的 standalone 包除外。
- 公开预览使用 debug 签名和 `.preview` 包名，不是正式发布签名；正式版需要独立 release keystore。
- 领克 01 2023 尚未完成 LynkCo-Link 实车验收；分辨率、热点接口和方向盘键值待采集。
- 同平台领克 03 的记录显示有线可用、无线未调通（来自 LynkCo-DiPlay R1）。
- 部分非中英文长文案仍沿用上游 DiPlay 的表述。
- 比亚迪专属功能在 OSN 上不适用。

## 校验

```sh
sha256sum -c SHA256SUMS.txt
```

## 反馈

请使用 GitHub Issue 模板，附车型/年款、OSN/Android/固件、iPhone/iOS、APK 版本、连接方式和脱敏后的诊断报告。不要上传热点密码、认证文件或含个人信息的截图。

## 许可

GPL-3.0 为主，设置 UI 的四个文件为 AGPL-3.0-only；详见 [LICENSING.md](../LICENSING.md)。
