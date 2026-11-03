# 改造说明与路线图

本文记录 OSNPlay 相对上游 DiPlay 的改动、有意保留的部分、LynkCo-DiPlay 整合范围、版本策略和后续计划。

## 基线

| 项 | 值 |
| --- | --- |
| 上游项目 | [DiPlay](https://github.com/shihabal3amri/DiPlay)（基于 [xcertplay](https://github.com/shilapi/xcertplay)） |
| 上游版本 | 0.2.22（commit `615b32c`） |
| 上游许可 | GPL-3.0；设置 UI 沿袭 DiAuto，AGPL-3.0 |
| 本版本 | OSNPlay 0.1.1（versionCode 101） |
| 标准包名 | `com.osnplay` |
| 领克包名 | `com.osnplay.lynk` |
| 命名空间 | `com.shilapi.xcertplay`（有意保留，见下文） |

## 相对上游的改动

### 1. 工程与构建

- 根项目名 `xcertplay` → `OSNPlay`。
- 删除上游的 `home`、`maphost`、`automotive` 示例模块，只保留 `common`、`shared`、`mobile` 三个核心模块。
- 引入产品风味表示发行版本：
  - `standard` → `com.osnplay`
  - `lynk` → `com.osnplay.lynk`
- 构建类型只表示构建性质：
  - `debug` → 包名追加 `.debug`，版本追加 `-debug`
  - `preview` → debug 签名、独立 `.preview` 包名、版本追加 `-preview`，用于预览发布
  - `release` → 正式签名，版本不追加后缀
- 版本从 0.1.0 / 100 起步，0.1.1 为当前预览版。

### 2. 应用身份与品牌

- `applicationId` 从 `com.shihab.diplay` 改为 `com.osnplay`，可与原版 DiPlay 并存安装。
- 应用名、Launcher 图标、iAP2 配件名、AirPlay label、热点 SSID 前缀（`OSNPlay-XXXX`）、Telecom 账户标签、设置备份页等用户可见品牌位已改为 OSNPlay。
- 新增原创矢量图标 `ic_osnplay` / `ic_osnplay_round`。
- 英文与简体中文的 About / 致谢文案已改为 OSNPlay；其他语言的长文案仍部分沿用上游 DiPlay 表述，见路线图。

### 3. 领克 OSN 适配

- 新增 `lynk` 产品风味，作为领克实车 A/B 测试包。
- 整合 LynkCo-DiPlay 的环境检测与连接诊断增量：
  - `CarPlayEnvironmentCheck`：只读检查应用、USB、无线三组状态。
  - `EnvironmentCheckPanel`：诊断设置页面板，提供修复入口。
  - `ConnectionCrashLog`：保留最近一次致命栈，去敏后随报告导出。
  - `ConnectionEnvironmentSnapshot`：USB 描述符、网络/接口、蓝牙、热点、媒体运行时快照。
  - 诊断报告新增「Last retained fatal stack」「USB and wireless environment at export」两节。
- 新增资源 `strings_environment.xml` 与 `strings_lynk.xml`，并补齐 `values-ja`、`values-zh-rTW` 等语言。
- 兼容性矩阵和实车证据整理到 [COMPATIBILITY.md](COMPATIBILITY.md)。

### 4. 文档与仓库整理

- 根目录只保留一份简体中文 README 和一份英文 README；移除重复的 `README.zh-CN.md`、`README.zh-TW.md`。
- 文档从 `docs/lynk/` 提升到 `docs/` 顶层，新增文档索引、设计评审、预览发布说明。
- 删除上游 BYD 专属文档、逐版本发布说明、静态站点、截图和站点脚本。
- 仅保留 LynkCo-DiPlay 的两份原始参考材料在 `docs/reference/`。
- GitHub Issue 模板改为 OSNPlay 专用；CI 改为三模块，并新增 release 工作流。

## 有意保留

- **包命名空间 `com.shilapi.xcertplay` 保持不变。** Manifest、Kotlin 引用和 R 类都绑定该命名空间；全量改名必须分模块进行，每步以构建和单测通过为前提。
- **比亚迪专属功能代码不删除。** HUD、仪表卡片、氛围灯、摇杆键、BYD 设置入口等在 OSN 上不生效，但删除会牵动大量引用和测试；文档标注为“不适用”。
- **`BYD_SUPPORT_SPLIT_ACTIVITY` 等 meta-data 保留。** 对 OSN 无副作用，分屏行为不同。
- **`Theme.Xcertplay` 样式名保留。** 非用户可见。
- **`com.shihab.diplay.action.EMBED_MAP` 等 intent action 字符串保留。** 属 opaque 标识符，上游组件仍可能使用。

## LynkCo-DiPlay 整合边界

LynkCo-DiPlay 基于 DiPlay **0.2.12**，OSNPlay 基于 **0.2.22**。其深度改动绑定旧基座，不能整文件合并；当前只并入自包含的检测/诊断模块、构建约定和实车结论。

已并入：

- 环境检测、崩溃日志、环境快照和诊断报告扩展。
- `lynk` 发行风味与独立包名。
- 2023 款领克 03 的有线实测结论（USB-A 数据口、无线未调通）。

待移植（按价值排序，均需构建 + 单测通过）：

| 项目 | 说明 |
| --- | --- |
| USB 配置回读 / 驱动释放 | 领克 Android 9 的 USB 认领与 EBUSY 恢复；含 JNI，成本高 |
| 有线蓝牙媒体交接 | 有线连接时关闭已配对 A2DP Sink 并恢复 |
| 音频焦点协调 | 仅由 sink 请求焦点、延迟/拒绝处理 |
| 媒体资源生命周期 | worker 关闭屏障、同输出串行移交 |
| 领克面板 / 双入口 / Dock | 依赖其 DiPlayActivity 重构，需按 0.2.22 重写 |
| 领克热点 / 无线 | API 28 临时热点与 2.4 GHz 交接；其 R1 无线仍未通 |
| 平台画像 `HeadUnitProfile` | 领克资源与音频/蓝牙行为开关 |

冲突取舍：品牌（OSNPlay 名称、图标、包名）和文档体系以本地为准；LynkCo-DiPlay 的 Lynk 品牌不并入。

## 版本策略

- OSNPlay 使用独立版本线，versionCode 从 100 起。
- 每个 OSNPlay 版本记录对应的上游 DiPlay 基线，便于追溯。
- 上游重要修复按模块 cherry-pick，不直接合并且不覆盖本地品牌与领克适配。
- 预览版使用 `preview` 构建（debug 签名、独立 `.preview` 包名），正式版使用 `release` 构建和独立签名密钥。

## 路线图

### P0 — 已完成（0.1.1 预览）

- 品牌化 fork、三模块整理、领克构建风味。
- 环境检测与诊断模块落库。
- 文档重构、CI、预览发布流程。

### P1 — 领克 01 实车画像

- 确认 01 2023 的 Android/OSN 版本、分辨率、密度和 USB 口行为。
- 增加 1920×1080 等 OSN 显示预设。
- 验证车机热点、Wi-Fi Direct、Same LAN 三条无线路径。
- 采集并映射方向盘 Siri / 媒体键键值。
- 验证熄火重启后的自动启动。

### P2 — ADB 可选能力

- 轮速、驻车视频、热点自动化在 OSN 固件上的可行性探测。
- 仅在有证据的车型上开放，默认关闭并标注实验性。

### P3 — 工程卫生

- 分模块将命名空间改为 OSNPlay 命名空间，每步保持构建和测试通过。
- 清理其他语言中残留的 DiPlay 长文案。
- 生成 adaptive icon 各密度资源。

### P4 — 发布

- 维护实车验证清单和兼容性矩阵。
- 正式签名版发布 APK + 源码 tag + SHA-256。
- 建立稳定的 issue / 诊断报告反馈流程。
