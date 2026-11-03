# 构建与 CI

本文覆盖 OSNPlay 的本地构建、认证资产、签名、构建产物和 GitHub Actions。所有命令都在仓库根目录（包含 `gradlew`）执行。

## 环境要求

| 项 | 要求 |
| --- | --- |
| JDK | 17+；CI 与推荐值为 25 |
| Gradle | 9.5.0（使用仓库自带 wrapper，勿另装） |
| Android SDK | Platform 37.2（`platforms;android-37.2`）、Build-Tools 37.0.0、Platform-Tools |
| NDK | 28.2.13676358（`shared` 的原生音频模块需要） |
| 环境变量 | `ANDROID_HOME`，或在根目录创建不提交的 `local.properties`：`sdk.dir=/path/to/sdk` |

工具链版本见 `gradle/libs.versions.toml`：AGP 9.3.0 / Kotlin 2.2.10 / Compose BOM 2026.02.01。

> **SDK 平台命名注意**：37 平台使用 `platforms;android-37.2` 这种带小版本的分号命名；裸 `platforms;android-37` 不存在。CI 已按此配置。

## 模块

| 模块 | 作用 |
| --- | --- |
| `:mobile` | 车机主应用，产出 APK |
| `:common` | 设置、首页、诊断和车机 UI |
| `:shared` | CarPlay 传输、音频、视频和协议实现 |

仓库只保留这三个模块；上游的 `home`、`maphost`、`automotive` 示例已移除，避免构建与文档噪音。

## 本地构建

```sh
# 单元测试
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest

# lint
./gradlew :mobile:lintStandardDebug

# 开发包（无认证身份）
./gradlew :mobile:assembleStandardDebug :mobile:assembleLynkDebug

# 预览包（debug 签名，独立 `.preview` 包名）
./gradlew :mobile:assembleStandardPreview :mobile:assembleLynkPreview
```

产物：

| 任务 | APK 路径 | 包名 |
| --- | --- | --- |
| `assembleStandardDebug` | `mobile/build/outputs/apk/standard/debug/mobile-standard-debug.apk` | `com.osnplay.debug` |
| `assembleLynkDebug` | `mobile/build/outputs/apk/lynk/debug/mobile-lynk-debug.apk` | `com.osnplay.lynk.debug` |
| `assembleStandardPreview` | `mobile/build/outputs/apk/standard/preview/mobile-standard-preview.apk` | `com.osnplay.preview` |
| `assembleLynkPreview` | `mobile/build/outputs/apk/lynk/preview/mobile-lynk-preview.apk` | `com.osnplay.lynk.preview` |

## 认证资产与 standalone 车机包

真实 CarPlay 连接需要配件认证资产，**认证文件不进入 Git，也不进入默认公开 CI 产物**。资产目录结构：

```text
<dir>/offline-mfi/identity.pk8
<dir>/offline-mfi/certificate.p7b
```

显式提供后构建 standalone 包：

```sh
DIPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets ./gradlew :mobile:assembleStandaloneDebug
# 或带认证身份的 preview 包（包名仍为 .preview）
DIPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets ./gradlew :mobile:assembleStandalonePreview
# 领克 A/B 版（debug 签名）
DIPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets ./gradlew :mobile:assembleStandaloneLynkPreview

# 正式签名版（需同时提供 ANDROID_KEYSTORE_* 环境变量）
DIPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets ./gradlew :mobile:assembleStandaloneStandardRelease
DIPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets ./gradlew :mobile:assembleStandaloneLynkRelease
```

- 只有显式传入的资产目录会被打包，并且**只对 `assembleStandalone*` 任务生效**；其他 debug / preview / release 构建即使设置了该变量也会忽略并给出警告，避免凭据误入公开包。
- `rejectBundledCredentials` 会拒绝其他 `.pk8` / `.p7b` / `.p12` / `.jks` 等凭据文件混入。
- 构建使用 `com.osnplay.debug`（standard debug）；正式签名版请使用 release 流程。
- 认证身份为公开固件恢复的实验性身份，不是 Apple 新颁发的 MFi 身份；持有者应自行评估分发风险。

## 签名

- **debug / preview**：使用 Android debug keystore，可直接安装。preview 使用独立的 `.preview` 包名，因而可以与正式版并存，也不会因 debug 证书与正式证书不同而阻塞将来升级。preview **不是**正式发布签名。
- **release**：需要以下环境变量；未配置时 release 构建会因找不到 keystore 失败，这是有意的保护。

```sh
export ANDROID_KEYSTORE_PATH=/absolute/path/release.jks
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS='...'
export ANDROID_KEY_PASSWORD='...'
./gradlew :mobile:assembleStandardRelease :mobile:assembleLynkRelease
```

请离线备份签名密钥；丢失后无法覆盖升级已安装的正式版。

## CI

`.github/workflows/android.yml` 在 push、pull request 和手动触发时运行：

1. `scripts/check_public_tree.py`：拒绝凭据容器和私钥块进入公开源码树。
2. 单元测试：`:shared:testDebugUnitTest`、`:common:testDebugUnitTest`。
3. lint：`:mobile:lintStandardDebug`。
4. 构建：`:mobile:assembleStandardDebug`、`:mobile:assembleLynkDebug`、`:mobile:assembleStandardPreview`、`:mobile:assembleLynkPreview`。
5. 断言 preview APK 中不含 `offline-mfi` / `.pk8` / `.p7b`。
6. 用临时 keystore 验证 `:mobile:assembleStandardRelease` 的签名路径。
7. 上传 APK、单元测试报告和 lint 报告为 artifact。

`.github/workflows/release.yml` 在推送 `v*` 标签时先跑单元测试和 lint，再构建预览 APK、断言公开包无认证资产、生成 `SHA256SUMS.txt` 并创建 GitHub Pre-release。若仓库配置了 `DIPLAY_AUTH_ASSETS_BASE64` secret，可额外构建带认证资产的 `-standalone.apk`；该包包含可提取的认证材料，发布说明必须明确风险。未配置时只发布源码预览包。

## 常见问题

| 现象 | 处理 |
| --- | --- |
| `SDK location not found` | 设置 `ANDROID_HOME` 或 `local.properties` 的 `sdk.dir` |
| 找不到 `platforms;android-37.2` | 用 `sdkmanager "platforms;android-37.2" "build-tools;37.0.0" "ndk;28.2.13676358"` 安装 |
| 首次构建很慢 / `Calculating task graph` 停滞 | 首次需下载依赖；可分步执行并使用 `--no-daemon` 规避 daemon 锁 |
| 单元测试 OOM | `common` 测试已设 2g 堆；关闭其他 Gradle 进程后重试 |
| release 构建报 keystore 缺失 | 按上文配置四个签名环境变量；预览构建请用 `assemble*Preview` |

## 验证记录

发布前验证结果记录在 [TESTING.md](TESTING.md) 与 [RELEASE-0.1.1-PREVIEW.md](RELEASE-0.1.1-PREVIEW.md)。
