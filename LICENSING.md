# 许可说明

OSNPlay 是 GPL-3.0 与 AGPL-3.0 代码的组合作品。仓库根目录的 [LICENSE](LICENSE) 是 GNU GPL v3 全文；另有少量文件在文件头标注了不同的许可。

## 组件许可

| 范围 | 许可 | 说明 |
| --- | --- | --- |
| `mobile`、`shared`、`common` 的其余代码 | GPL-3.0 | 基于 DiPlay / xcertplay，见根目录 `LICENSE` |
| `common/src/main/java/com/shilapi/xcertplay/DiPlayActivity.kt` | AGPL-3.0-only | 设置 UI 的配色、布局与文案改编自 DiAuto |
| `common/src/main/java/com/shilapi/xcertplay/AppLocale.kt` | AGPL-3.0-only | 同上 |
| `common/src/main/java/com/shilapi/xcertplay/InterfaceSize.kt` | AGPL-3.0-only | 同上 |
| `common/src/main/java/com/shilapi/xcertplay/SettingsLayoutPreferences.kt` | AGPL-3.0-only | 同上 |
| `docs/licenses/DiAuto-AGPL-3.0.txt` | AGPL-3.0 | DiAuto 许可全文 |
| 第三方依赖与素材 | 各自许可 | 见 [docs/THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md) |

## 组合与分发

GNU GPL v3 第 13 节允许把 GPL-3.0 作品与 AGPL-3.0 作品组合成一个作品并分发（AGPL v3 第 13 节则是网络交互条款）。组合作品中：

- AGPL-3.0-only 的文件继续受 AGPL-3.0 约束；
- 其余部分继续受 GPL-3.0 约束。

因此本仓库**不是**一个可以简单标为“纯 GPL-3.0”的代码库。如果你再分发源码或 APK，必须同时满足 GPL-3.0 和 AGPL-3.0 的要求，保留版权与许可声明，并向接收者提供对应源码。

如果你需要纯 GPL-3.0 的分发，必须先用独立实现替换上表中的四个 AGPL 文件；仅删除文件头 SPDX 标识不构成重新许可。

## 网络使用

AGPL 第 13 条要求：如果你修改本作品并通过网络向用户提供服务，必须向这些用户提供对应源码。OSNPlay 是安装在车机上的本地应用，不运营网络服务；但如果你把它改造成网络服务，请自行履行 AGPL 义务。

## 上游与致谢

- [DiPlay](https://github.com/shihabal3amri/DiPlay)（GPL-3.0）
- [xcertplay](https://github.com/shilapi/xcertplay)（GPL-3.0）
- [DiAuto](https://github.com/wycomco/diauto)（AGPL-3.0）
- [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay)（GPL-3.0 同源）

原始版权与许可声明保留在对应源文件和 `docs/licenses/` 中。
