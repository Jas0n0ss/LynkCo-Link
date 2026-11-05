# 测试与验收

本文区分三类验证：宿主单元测试、构建/打包验证、实车验收。自动化测试通过**不能**证明真实 CarPlay 认证、无线稳定性或车机兼容性。

## 一键检查

```sh
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest \
  :mobile:lintStandardDebug \
  :mobile:assembleStandardDebug :mobile:assembleLynkDebug \
  :mobile:assembleStandardPreview :mobile:assembleLynkPreview
```

CI 使用同一组核心任务，并额外验证公开 APK 无认证资产和 release 签名路径，见 [.github/workflows/android.yml](../.github/workflows/android.yml)。

## 自动化与构建验证

记录日期：2026-11-05。本地环境：macOS arm64、Homebrew OpenJDK 26、Android SDK Platform 37.2 / Build-Tools 37.0.0 / NDK 28.2.13676358、Gradle 9.5.0。GitHub Actions 的历史运行（0.1.1）见 run `41648473187`、`41648516628`、`41652240197`；0.1.2 的验证在本地完成。

| 检查 | 结果 | 说明 |
| --- | --- | --- |
| `:shared:testDebugUnitTest` | ✅ 1798 通过 / 1 跳过 | 传输、音频、视频、协议与兼容层 |
| `:common:testDebugUnitTest` | ✅ 1761 通过 | 设置 UI、诊断、更新、备份与资源测试 |
| `:mobile:lintStandardDebug` | ✅ 0 阻断 | lint 报告见 `mobile/build/reports/` |
| `:mobile:assembleStandardDebug` | ✅ | `com.osnplay.debug` / `0.1.2-debug` |
| `:mobile:assembleLynkDebug` | ✅ | `com.osnplay.lynk.debug` / `0.1.2-lynk-debug` |
| `:mobile:assembleStandardPreview` | ✅ | `com.osnplay.preview` / `0.1.2-preview` |
| `:mobile:assembleLynkPreview` | ✅ | `com.osnplay.lynk.preview` / `0.1.2-lynk-preview` |
| 公开包认证资产检查 | ✅ 无 | 四个 debug / preview APK 均无 `offline-mfi` / `.pk8` / `.p7b` |
| release 签名路径 | ✅ | 用临时 keystore 构建 `:mobile:assembleStandardRelease` 成功 |
| standalone 凭据门禁 | ✅ | 单独调用会打包显式资产；与公开构建混合调用会被拒绝 |
| `scripts/check_public_tree.py` | ✅ | 源码树无凭据容器或私钥块 |
| `aapt2` launcher 检查 | ✅ | 同时存在 `launchable-activity` 与 `leanback-launchable-activity`，label 为 OSNPlay |
| `lynk` 资源默认值 | ✅ | auto-start / launcher bubble / simple flow 均为 true |
| 0.1.2 签名发布包 | ✅ | `com.osnplay.lynk` / `0.1.2-lynk` / versionCode 102，认证资产已打入 |

### 产物核对

```sh
# 包名与版本
aapt2 dump badging mobile/build/outputs/apk/standard/preview/mobile-standard-preview.apk | head -3
aapt2 dump badging mobile/build/outputs/apk/lynk/preview/mobile-lynk-preview.apk | head -3

# 确认没有认证资产
unzip -l mobile/build/outputs/apk/standard/preview/mobile-standard-preview.apk | grep -iE 'offline-mfi|\.pk8|\.p7b'
```

预期：

| APK | package | versionName | minSdk | targetSdk |
| --- | --- | --- | --- | --- |
| standard preview | `com.osnplay.preview` | `0.1.2-preview` | 24 | 37 |
| lynk preview | `com.osnplay.lynk.preview` | `0.1.2-lynk-preview` | 24 | 37 |

## 自动化测试覆盖

- **shared**：USB / NCM / iAP2 传输、蓝牙 RFCOMM、无线控制与地址选择、音频编解码与缓冲、视频解码/输出、H.264/HEVC 兼容、热点与 Wi-Fi Direct、诊断和崩溃记录。
- **common**：设置信息架构与搜索、紧凑/全屏/大字体布局、语言与翻译完整性、显示尺寸、诊断导出、更新检查、设置备份、环境检测和兼容层。
- **未覆盖**：真实 iPhone 认证、真实车机的 USB 控制器/音频路由/热点固件差异、驾驶场景和长期稳定性。

新增测试时：

- 设置项移动或新增分类，更新 `AdaptiveSettingsUiTest` 与 `SettingsLayoutPolicyTest`。
- `settings_*` 新字符串必须补齐所有 `values-xx`，否则 `SettingsTranslationsTest` 失败。
- 更新检查的 release 资产解析、诊断路径等行为变更需要同步单测。

## 发布前检查清单

- [ ] `CHANGELOG.md` 和发布说明已更新
- [ ] 版本号、`versionCode`、APK 包名与文档一致
- [ ] 全量 CI 通过，lint 无新增阻断
- [ ] 预览 APK 不含认证资产；standalone 包只在本地明确构建
- [ ] 预览 release 的 `SHA256SUMS.txt` 与 APK 对应
- [ ] README、文档索引和兼容性矩阵无死链
- [ ] 许可证与第三方声明未因新增文件而失效

## 实车验收

OSNPlay 自身尚未完成领克 01 2023 的实车验收。以下项目必须在驻车状态逐项记录：

| 项目 | 通过标准 |
| --- | --- |
| 安装 | 车机允许安装，应用可启动 |
| USB 有线 | 首次授权后稳定进入 CarPlay；音乐、导航、Siri、麦克风可用 |
| 车机热点 | iPhone 加入热点后自动/手动连接成功 |
| Wi-Fi Direct | 建组成功，画面与音频稳定 |
| Same LAN | 同一路由器下连接成功 |
| 显示 | 分辨率/尺寸/帧率与中控匹配，触摸位置准确 |
| 音频 | 音乐、导航提示、通话、微信语音无明显卡顿/回声 |
| 方向盘 | Siri/媒体键映射正确，无卡键 |
| 重启 | 熄火重启后自动启动行为符合设置 |
| 诊断 | 环境检测面板状态准确，报告可导出并脱敏 |

记录模板：

```text
车型/年款：
OSN / Android / 固件：
iPhone / iOS：
APK 版本与包名：
连接方式：
结果与复现步骤：
诊断报告：
```

## 已知限制

- 公开预览包无认证身份，不能完成真实连接。
- 领克 03 的无线路径在同平台 R1 中未调通。
- 领克 01 的分辨率、热点接口和方向盘键值尚未采集。
- 自动化测试无法替代真实 iPhone 与车机固件验证。
