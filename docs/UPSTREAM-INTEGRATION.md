# 四个上游项目的整合说明

OSNPlay 的目标车机是 **领克 01 2023 / 骁龙 8155 / OSN**。四个参考项目面向的车机代际不同，不能整包合并；本页说明每个项目取了什么、为什么取，以及哪些没有并入。

| 项目 | 目标平台 | 本仓库采用的部分 | 没有直接并入的部分 |
| --- | --- | --- | --- |
| [DiPlay](https://github.com/shihabal3amri/DiPlay) | 通用 Android 车机，0.2.22 | CarPlay 传输、音频、视频、设置体系；桌面悬浮球；开机启动；Launcher 元数据 | BYD 固件专属功能 |
| [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay) | 领克 OS N 2.0 / Android 9 / 8155 同代 | 环境检测、连接诊断、`lynk` 构建约定、领克自适应图标做法、03 实车结论 | 基于 0.2.12 的深度会话/音频重构，未逐文件移植 |
| [xcertplay](https://github.com/shilapi/xcertplay) | DiPlay 的底座，Android Automotive 取向 | 工程结构、Manifest/Launcher 组织方式、部分兼容层 | Android Automotive 专属的 `automotive` 模块和 Car App 服务（本仓库已移除） |
| [diplay-ecarx](https://github.com/jianyun8023/diplay-ecarx) | 领克 01 2021 / ECARX 753 / 骁龙 820A | 无 ADB 运行思路、平台权限清单、1920×1080 车机画像、01 实车经验 | 平台签名、ECARX DIM/媒体 Binder、0.2.12 基线代码；与 8155/OSN 不同代，不能直接套用 |

## 为什么以 DiPlay 0.2.22 + LynkCo-DiPlay 为主

- OSNPlay 是 DiPlay 0.2.22 的 fork，代码结构、设置和诊断基础与 0.2.22 一致。
- LynkCo-DiPlay 同样面向领克 OSN，但其代码基线是 0.2.12；它的环境检测/诊断模块相对独立，适合增量移植。
- `diplay-ecarx` 面向 2021 款 01 的 ECARX 753 / 820A，使用**车机平台签名**和 ECARX 私有接口；2023 款 01 是 8155 / OSN，签名和接口都不同。直接合并会破坏构建，也不能获得平台权限。

## 桌面不显示 OSNPlay 的根因

领克 OSN 原厂 **Launcher3 使用包名白名单**显示第三方应用。APK 安装成功、`pm list packages` 能看到，并不等于首页/Dock 会出现图标。参考：[OSN 安装软件说明](https://zcteo.github.io/blog/others/014)（其中单列“系统启动器显示白名单 Launcher3”）、[小八桌面教程](https://www.xiaohongshu.com/discovery/item/69085c6f000000000303a5a3)。

这不是 Manifest 缺 `LAUNCHER` 类别的问题。四个参考项目的 launcher activity 都使用标准 `MAIN` + `LAUNCHER`，与本仓库一致；`diplay-ecarx` 能在 2021 款 01 上出现，主要依赖其平台签名和该代车机的桌面策略，而不是多写了一个 category。

## 本版本的缓解措施

- 自适应图标 + PNG 回退，显式 activity `label`/`icon`，兼容只读取 mipmap/图标的桌面。
- 增加 `LEANBACK_LAUNCHER` 入口，兼容只索引 leanback 类别的车机/TV 桌面。
- `lynk` 版本默认开机自动启动，并在开机时发布“打开 OSNPlay”通知。
- `lynk` 版本默认开启桌面悬浮球；首次运行提示授予“显示在其他应用上层”。授权后即使原厂桌面不列图标，也能从悬浮球打开。
- 文档给出 `am start`、第三方启动器（小八启动器/应用页）、白名单修改的取舍。

## 仍需车机侧处理的部分

- 要在**原厂 Launcher3 首页**出现图标，只能：使用替代启动器/应用页、通过 ADB 启动，或修改系统白名单（root/工程模式）。普通 APK 无法绕过。
- 不建议把包名改成白名单里的其他应用（例如伪装成车机自带应用）：会与原厂应用冲突，也可能被签名校验拦截。
- `lynk` 包默认自动启动/悬浮球是应用侧最稳妥的入口；如果车机禁止后台启动，开机通知和 `am start` 仍是后备方案。
