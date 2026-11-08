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

仓库只保留这三个模块；上游的 `home`、`maphost`、`automotive` 示例已移除。

## 本地构建（只构建功能包）

本仓库只交付一个 APK：**`assembleStandaloneLynkRelease`**。它使用 OSN 白名单包名 `com.kugou.android.auto`，同时包含主程序和桌面入口，含认证资产。不再构建 preview / debug 变体。

```sh
export OSNPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets
export ANDROID_KEYSTORE_PATH=/absolute/path/to/release.jks
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS='...'
export ANDROID_KEY_PASSWORD='...'

./gradlew :mobile:assembleStandaloneLynkRelease
```

产物：

```text
mobile/build/outputs/apk/lynk/release/mobile-lynk-release.apk
```

包名 `com.kugou.android.auto`，版本 `0.1.3-lynk`，release 签名，含显式提供的认证资产。

## 认证资产与 standalone 车机包

真实 CarPlay 连接需要配件认证资产，**认证文件不进入 Git，也不进入默认公开 CI 产物**。资产目录结构：

```text
<dir>/offline-mfi/identity.pk8
<dir>/offline-mfi/certificate.p7b
```

显式提供资产和 release keystore 后构建唯一主程序包：

```sh
OSNPLAY_AUTH_ASSETS_DIR=/absolute/path/to/assets \
ANDROID_KEYSTORE_PATH=/absolute/path/to/release.jks \
ANDROID_KEYSTORE_PASSWORD='...' \
ANDROID_KEY_ALIAS='...' \
ANDROID_KEY_PASSWORD='...' \
  ./gradlew :mobile:assembleStandaloneLynkRelease
```

- 只有显式传入的资产目录会被打包，并且**只对 `assembleStandalone*` 任务生效**；其他构建即使设置了该变量也会忽略并给出警告。
- `rejectBundledCredentials` 会拒绝其他 `.pk8` / `.p7b` / `.p12` / `.jks` 等凭据文件混入。
- 认证身份为公开固件恢复的实验性身份，不是 Apple 新颁发的 MFi 身份；持有者应自行评估分发风险。

## 签名

只交付 release 签名包。需要以下环境变量；未配置时构建会因找不到 keystore 失败，这是有意的保护。

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
4. 用临时 keystore 构建 `:mobile:assembleLynkRelease`（源码包，不含认证资产）。
5. 断言构建产物不含 `offline-mfi` / `.pk8` / `.p7b`。
6. 上传 APK、单元测试报告和 lint 报告为 artifact。

`.github/workflows/release.yml` 只手动触发，并且**只构建** `:mobile:assembleStandaloneLynkRelease`。它要求以下 Actions Secrets：

- `OSNPLAY_AUTH_ASSETS_BASE64`：包含 `offline-mfi/identity.pk8` + `certificate.p7b` 的 zip（base64）
- `ANDROID_KEYSTORE_BASE64`：release keystore（base64）
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

产物为：

- `OSNPlay-<version>-lynk-release-standalone.apk`：主程序 + 白名单入口，含可提取的认证材料；
- `SHA256SUMS.txt`：APK 校验和。

发布前必须确认认证材料的分发范围。

## 发布节奏

- 每次更新递增 `versionCode` / `versionName`，创建新的 `vX.Y.Z` 标签和 Release，不覆盖旧 Release。
- Release 说明只写本版变更和安装命令，不重复完整文档。
- 发布工作流结束后只保留**最近两个 Release 和标签**，更旧的 Release、标签和资产自动删除。

## 常见问题

| 现象 | 处理 |
| --- | --- |
| `SDK location not found` | 设置 `ANDROID_HOME` 或 `local.properties` 的 `sdk.dir` |
| 找不到 `platforms;android-37.2` | 用 `sdkmanager "platforms;android-37.2" "build-tools;37.0.0" "ndk;28.2.13676358"` 安装 |
| 首次构建很慢 / `Calculating task graph` 停滞 | 首次需下载依赖；可分步执行并使用 `--no-daemon` 规避 daemon 锁 |
| 单元测试 OOM | `common` 测试已设 2g 堆；关闭其他 Gradle 进程后重试 |
| release 构建报 keystore 缺失 | 按上文配置四个签名环境变量；预览构建请用 `assemble*Preview` |

## 验证记录

发布前验证结果记录在 [TESTING.md](TESTING.md) 与 [RELEASE-0.1.3.md](RELEASE-0.1.3.md)。
