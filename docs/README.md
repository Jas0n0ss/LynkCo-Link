# LynkCo-Link 文档

面向使用者和贡献者的文档索引。所有路径相对于仓库根目录。

## 使用

| 文档 | 内容 |
| --- | --- |
| [README](../README.md) | English: overview, supported cars, download and build |
| [README.zh-CN](../README.zh-CN.md) | 简体中文：项目简介、适配车型、下载与源码构建 |
| [EMULATOR-LYNK-OSN.md](EMULATOR-LYNK-OSN.md) | API 28 AVD、横屏 UI、车机输入和 Mock SDK 测试 |
| [INSTALL.md](INSTALL.md) | 车机安装、有线/无线连接、更新与故障排查 |
| [COMPATIBILITY.md](COMPATIBILITY.md) | 适配车型矩阵、功能适用性、风险和实车验证清单 |
| [PRIVACY.md](PRIVACY.md) | 本地数据、诊断报告、权限和更新检查的隐私说明 |
| [RELEASE-0.1.3.md](RELEASE-0.1.3.md) | 0.1.3 单 APK、领克首次设置与去 BYD 设置 |
| [RELEASE-0.1.1-PREVIEW.md](RELEASE-0.1.1-PREVIEW.md) | 首个预览版的变更、产物、验证和限制 |

## 开发

| 文档 | 内容 |
| --- | --- |
| [BUILD.md](BUILD.md) | 环境、模块、构建任务、认证资产、签名和 CI |
| [LYNK-ACCEPTANCE.md](LYNK-ACCEPTANCE.md) | 领克实车验收矩阵与取证脚本 |
| [TESTING.md](TESTING.md) | 自动化测试、构建验证与实车验收清单 |
| [DESIGN-REVIEW.md](DESIGN-REVIEW.md) | 发布前的设计评审结论与已处理问题 |
| [ADAPTATION-NOTES.md](ADAPTATION-NOTES.md) | 相对 DiPlay 的改造、LynkCo-DiPlay 整合和路线图 |
| [UPSTREAM-INTEGRATION.md](UPSTREAM-INTEGRATION.md) | 四个上游项目各取了什么、为什么不能整包合并 |
| [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) | 第三方代码、素材和许可 |
| [LICENSING.md](../LICENSING.md) | GPL-3.0 与 AGPL-3.0-only 文件的组合许可说明 |
| [AGENTS.md](../AGENTS.md) | 代码贡献规则与设置信息架构约束 |

## 参考

以下材料来自同平台兄弟项目 LynkCo-DiPlay，保留原文用于追溯实车证据；其中的包名、构建任务和路径描述的是该 fork，不是 LynkCo-Link 的当前实现。

- [LYNK_OS_N.md](reference/LYNK_OS_N.md)：2023 款领克 03 / OS N 2.0 的适配与实车记录（test1–test25）。
- [RELEASE-NOTES-LYNK-R1.md](reference/RELEASE-NOTES-LYNK-R1.md)：LynkCo-DiPlay R1 的发布说明和已知边界。

## 维护约定

- 用户文档放在 `docs/` 顶层，文件名使用大写英文加连字符。
- 不再保留上游 DiPlay 的 BYD 专属文档、逐版本发布说明和静态站点；上游历史通过 GitHub 链接追溯。
- 新增或移动设置项时，先读 [AGENTS.md](../AGENTS.md) 的分类规则，再更新 `AdaptiveSettingsUiTest` 和全部语言资源。
- 发布新版本时同步更新 `CHANGELOG.md`、`docs/TESTING.md` 的验证记录和对应的发布说明。
