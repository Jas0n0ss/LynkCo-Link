# 设计评审（0.1.1 预览版）

评审日期：2026-11-03。范围：构建与发布设计、设置/诊断 UI、公开安全边界、文档与仓库结构、许可。结论是“可以发布预览版”，但必须保留文末的已知限制。

## P0 — 发布前必须修复（已处理）

### 1. CI 仍引用已删除的模块

**问题**：删除 `home` / `maphost` / `automotive` 后，`.github/workflows/android.yml` 和 `AGENTS.md` 仍执行 `:home:*`、`:maphost:*`，Gradle 会报 `Project 'home' not found`。

**处理**：CI 与 AGENTS 改为三模块命令；构建产物路径改为 `mobile/build/outputs/apk/...`；新增 preview 构建验证。

### 2. 产品风味使 `lintDebug` 任务名歧义

**问题**：引入 `standard` / `lynk` 后，`:mobile:lintDebug` 同时匹配多个变体，Gradle 报 `task 'lintDebug' is ambiguous`。

**处理**：CI 与文档统一使用 `:mobile:lintStandardDebug`。`assemble*` 使用显式变体名，不再依赖聚合任务。

### 3. 认证资产可能误入公开预览包

**问题**：`DIPLAY_AUTH_ASSETS_DIR` 原先对所有变体生效。如果发布者环境里残留该变量，`assembleStandardPreview` 会把认证私钥打进公开包，而文档又声称预览包不含身份。

**处理**：`mobile/build.gradle.kts` 只在任务名包含 `Standalone` 时读取该变量；其他构建忽略并打印警告。若同一次调用同时请求 `assembleStandalone*` 和其他 assemble 任务，配置阶段直接失败，避免共享 `main` assets 把凭据带进公开变体。新增 `assembleStandalonePreview` 供发布者显式构建带身份的预览包。CI 与 release 工作流在发布前用 `unzip -l` 断言公开 APK 不含 `offline-mfi` / `.pk8` / `.p7b`。

### 4. 预览包与正式版共用 applicationId

**问题**：preview 使用 debug 签名和 `com.osnplay`，而正式版使用 release 签名和同一包名。将来安装正式版必须卸载预览版；不同 CI 运行生成的 debug 证书也不稳定，预览包之间无法可靠覆盖升级。

**处理**：preview 构建类型增加 `.preview` 后缀，标准预览为 `com.osnplay.preview`，领克预览为 `com.osnplay.lynk.preview`。预览版与正式版可并存，不再承诺跨证书覆盖升级；发布说明已写明不同构建可能需要卸载重装。

### 5. 更新检查指向上游 DiPlay

**问题**：`UpdateClient.RELEASES_URL` 仍指向 `shihabal3amri/DiPlay`，OSNPlay 用户会被提示安装上游 DiPlay APK；多 APK 资产时也可能选中 `lynk` 包。

**处理**：改为 `Jas0n0ss/OSN-Play`；`UpdateCatalog` 优先选择文件名含 `-standard` 的 APK，找不到时回退到第一个 APK。新增单测覆盖标准/领克双资产。

### 6. 公开文档对“是否含认证身份”自相矛盾

**问题**：README 说预览包不含身份，旧 SECURITY / THIRD_PARTY_NOTICES 说公开预览包内置身份。发布者与用户无法判断。

**处理**：统一为“公开源码、CI 与 preview 包不含身份；只有本地显式 `assembleStandalone*` 构建才可能包含”。SECURITY、THIRD_PARTY_NOTICES、README、BUILD 和发布说明使用同一表述。

### 7. 许可边界未说明

**问题**：根 `LICENSE` 是 GPL-3.0，但 `DiPlayActivity.kt`、`AppLocale.kt`、`InterfaceSize.kt`、`SettingsLayoutPreferences.kt` 标注 AGPL-3.0-only。仅写“整体 GPL-3.0”会误导再分发者。

**处理**：新增 [LICENSING.md](../LICENSING.md)，列明组件许可、GPLv3 §13 的组合关系、再分发义务和“纯 GPL 需替换四个文件”的边界；README 与文档索引已链接。

## P1 — 应在预览版修复（已处理）

### 8. 用户可见文案仍大量出现 DiPlay

**问题**：55 个资源/HTML 文件共 1000+ 处 `DiPlay`，包括首页、权限提示、诊断和设置。应用名已改 OSNPlay，但界面仍显示旧品牌。

**处理**：批量替换 `common` / `mobile` 资源中的 `DiPlay` 为 `OSNPlay`；保留代码类名、日志 tag、opaque action 字符串和上游署名。同步修改 iAP2 配件名、AirPlay 设备名、通知标题、导航小组件和更新 User-Agent。

### 9. 诊断报告路径与实际目录不一致

**问题**：字符串已改为 `Downloads/OSNPlay`，但 `DiagnosticExportStore` 仍写入 `Download/DiPlay`，FileProvider 路径也指向旧目录。

**处理**：代码、FileProvider XML、测试断言统一为 `Download/OSNPlay`。旧版本已导出的 `Download/DiPlay` 报告不会被删除，用户可手动迁移。

### 10. Debug 包名变更暴露了 BYD 调试残留

**问题**：debug 包名从 `com.osnplay.hudtest` 改为 `com.osnplay.debug`，`StandaloneHudDemoActivity` 仍硬编码旧包名；该 debug 变体还请求 BYD 专属权限并注册 BYD HUD 测试入口，对 OSN 目标没有价值。

**处理**：删除整个 debug-only BYD HUD 测试 harness（manifest、Activity、Receiver、Packet 辅助类），debug 变体不再请求 BYD 权限。

### 11. 构建变体语义混乱

**问题**：原配置把 `lynkDebug` / `lynkRelease` 当构建类型，发行版本与 debug/release 两个维度混在一起，任务名和文档难以理解。

**处理**：改为 `standard` / `lynk` 产品风味 × `debug` / `preview` / `release` 构建类型。preview 使用 debug 签名和 `.preview` 包名，专门用于预览发布并与正式版并存。

### 12. 文档与发布流程缺失

**问题**：README 引用了不存在的 `build.yml`、测试/设计/发布文档；没有 release 工作流；没有校验和。

**处理**：重写 README、文档索引、构建、安装、兼容性、测试、隐私、安全和发布说明；新增 `release.yml`，在 `v*` 标签构建 preview APK、生成 `SHA256SUMS.txt` 并创建 Pre-release。

## P2 — 记录但不阻塞预览

- **包命名空间仍是 `com.shilapi.xcertplay`**：全量改名风险高，按路线图分模块进行。
- **比亚迪专属代码仍保留**：删除会牵动大量引用和测试；文档标注不适用。
- **部分非中英文长文案**：已统一品牌词，但语义仍是上游通用描述，未逐语言重写。
- **CarPlay 图标**：当前按用户要求使用 Apple 的 CarPlay 图标；不同桌面的圆角遮罩效果需实车确认。
- **未做领克 01 实车验收**：预览版只能证明宿主测试和构建，不证明认证、无线和音频在车上的表现。
- **更新检查会把预发布版视为可用版本**：`UpdateVersion` 只比较版本号，不区分 prerelease；后续应增加 stable/preview 通道。
- **环境面板尚未纳入 `SettingsSection`**：已去掉重复的 page title、加入搜索索引，并用深色容器修复日间模式对比度；但它仍不是 `filteredSection`，`SettingsLayoutPolicyTest` 看不到它。后续应新增 `ENVIRONMENT` section 并改为卡片内渲染。
- **环境面板 7 个语言未翻译**：`env_*` 目前只有 en / ja / zh-CN / zh-TW；ar / es / it / ko / ru / tr / uk 回退英文。面板始终显示，后续补齐翻译。
- **`lynk` 风味已启用 `config_simple_connection_flow`**：通过 `mobile/src/lynk/res/values/config.xml` 打开 OS N 配置；仍需要在领克实车上验证 API 28 热点与位置豁免行为。
- **BYD HUD 图标仍随 APK 分发**：`byd-hud-icons` 使用 PolyForm Noncommercial 许可。BYD HUD 不在 OSN 目标内，后续应移除资源或替换为原创图标。
- **预览包 debug 证书每次 CI 运行可能不同**：`.preview` 包名避免了与正式版冲突，但预览包之间的覆盖升级仍不保证。

## 验证结论

- 构建、单元测试、lint 与四个 APK 产物通过。
- 预览 APK 已确认无认证资产。
- 没有进行真实 iPhone / 领克车机验收；发布说明和 README 已明确写出。

## 后续建议

1. 在领克 01 2023 上按 [COMPATIBILITY.md](COMPATIBILITY.md) 清单采集分辨率、USB、热点和方向盘键值。
2. 为 stable / preview 增加更新通道，避免稳定版用户被提示升级到预览包。
3. 评估将命名空间迁移到 `com.osnplay`，分模块提交并保持 CI 通过。
4. 正式发布前配置独立 release keystore，并把签名步骤写入发布 runbook。
5. 为环境检测面板补充 compact / 大字体 / RTL 截图测试。
