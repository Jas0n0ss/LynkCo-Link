# 更新日志

## 0.1.2 — 2026-11-05

### 新增

- 领克 OSN 桌面可见性适配：CarPlay 图标、Lynk & Co 车辆按钮图标、显式 activity label/icon、`LEANBACK_LAUNCHER` 入口。
- 运行时/资源清理：`DiPlay*` 类、资源键、action、日志标记和环境变量统一改为 `OSNPlay*`；APK 内不再包含旧品牌字符串。
- 首次设置改为“连接 → iPhone → 完成”，移除 DiLink 版本选择和比亚迪功能页。
- 删除 BYD 专属设置分区：BYD ADB、BYD 导航/HUD、仪表盘地图、氛围灯、导航滚轮；同步移除 BYD 通话键/HUD/仪表运行接线和对应测试。
- 领克版保留通用能力：连接、显示、音频、定位、车辆按键、CarPlay 界面、诊断、备份和高级媒体/显示选项。
- 合并为单 APK：`OSNPlay-0.1.2-lynk-release-standalone.apk` 使用 OSN 白名单包名 `com.kugou.android.auto`，同时包含主程序、桌面入口和认证资产；不再单独发布入口包。
- `lynk` 版本默认开启开机自动启动和桌面悬浮球；首次运行提示授予“显示在其他应用上层”。
- 开机时发布“打开 OSNPlay”通知，即使原厂 Launcher3 的包名白名单不显示图标也能进入应用。
- 文档补充 OSN 原厂桌面白名单的原因、替代启动器/ADB/悬浮球处理方式。

### 变更

- versionCode 102 / versionName `0.1.2`。

### 已知限制

- 原厂 Launcher3 的包名白名单无法由普通 APK 绕过；如需桌面图标，需使用替代启动器、ADB 启动，或修改系统白名单。

OSNPlay 使用独立版本线。上游 DiPlay 的逐版本变更见 [DiPlay Releases](https://github.com/shihabal3amri/DiPlay/releases)；本文件只记录 OSNPlay 的改动。

## 0.1.1 — 2026-11-03（预览版）

首个公开预览版。

### 新增

- 领克 OSN 环境检测面板：只读检查应用、USB、无线三组状态，并提供修复入口。
- 连接诊断增强：保留最近一次致命栈，诊断报告新增 USB / 无线环境快照。
- 领克 `lynk` 发行风味（`com.osnplay.lynk`），可与标准包并存安装。
- 新增 `preview` 构建类型：debug 签名、公开包名，用于预览发布。
- GitHub Actions：三模块单元测试、lint、debug / preview 双包构建；`v*` 标签自动创建 Pre-release。
- 文档重构：统一入口、构建、安装、兼容性、测试、隐私、安全和设计评审。

### 变更

- 版本号 0.1.0 / 100 → 0.1.1 / 101。
- 构建配置改为 `standard` / `lynk` 产品风味 × `debug` / `preview` / `release` 构建类型。
- 仓库只保留 `common`、`shared`、`mobile` 三个模块，移除上游示例模块。
- README 合并为简体中文主文件和英文版本；删除重复语言版本、BYD 专属文档、上游逐版本说明和静态站点。
- 公开构建默认不包含认证身份；只有显式 `assembleStandalone*` 任务才接受 `OSNPLAY_AUTH_ASSETS_DIR`。

### 已知限制

- 公开预览包不含 CarPlay 认证身份，无法完成真实连接。
- 领克 01 2023 尚未完成 OSNPlay 实车验收；同平台参考结论来自 LynkCo-DiPlay R1。
- 领克 03 的无线 CarPlay 在同平台记录中仍未调通。
- 部分非中英文长文案仍沿用上游 DiPlay 表述。

## 0.1.0 — 2026-11-03

- 基于 DiPlay 0.2.22（commit `615b32c`）建立领克 OSN 二创基线。
- 应用名、图标、包名 `com.osnplay`、iAP2 名称、热点 SSID 前缀等品牌位改为 OSNPlay。
- 首次完成 `:mobile:assembleDebug`、lint 和 1798 项 `shared` 单元测试验证。
- 引入领克构建风味与适配文档。

## 上游基线

| OSNPlay | DiPlay 基线 |
| --- | --- |
| 0.1.1 | 0.2.22（`615b32c`） |
| 0.1.0 | 0.2.22（`615b32c`） |
