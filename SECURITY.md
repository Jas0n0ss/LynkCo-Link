# 安全与发布打包

## 凭据边界

OSNPlay 的公开源码树和默认 CI 产物**不包含** CarPlay 配件认证身份，也不包含 Android 发布签名密钥。唯一的例外是维护者显式配置认证资产 secret 后构建的 `-standalone` 发布包。

- `.gitignore` 与 CI 的 `scripts/check_public_tree.py` 会拒绝 `.pk8`、`.p7b`、`.p12`、`.jks`、`.keystore`、`.apk` 等凭据或分发文件进入源码树。
- 只有显式设置 `DIPLAY_AUTH_ASSETS_DIR` 时，两个指定文件（`offline-mfi/identity.pk8`、`offline-mfi/certificate.p7b`）才会被打包；`rejectBundledCredentials` 会拒绝其他凭据混入。
- Android APK 签名密钥只保留在发布者本地，通过环境变量注入，不进入仓库或 APK。

如果你在本地构建了含认证身份的 standalone 包，**不要**把该 APK、资产目录、日志或签名文件上传到公开 Issue、Release 或网盘。

## 预览包说明

默认公开预览包（`standardPreview` / `lynkPreview`）使用 debug 签名且不含认证身份，只能用于界面与设置验证。若发布页额外提供 `-standalone.apk`，该包包含可提取的认证材料，风险由使用者自行承担。它不是正式发布签名，不能用于覆盖升级正式版。

认证身份的实验性质、来源和风险见 [第三方声明](docs/THIRD_PARTY_NOTICES.md) 与 README 的重要声明。任何持有 APK 的人都可以提取其中打包的认证材料；源码构建、混淆或移除历史都不能使共享密钥保密。

## 诊断报告

诊断报告可能包含设备型号、Android/固件版本、USB 描述符、网络接口、蓝牙名称、热点 SSID、应用设置和崩溃栈。导出器会过滤协议载荷、凭据行和常见标识符，但不能保证识别所有厂商字符串。公开反馈前请自行检查并移除：

- 热点密码、Wi-Fi 密码、Apple 账号或其他凭据；
- 车牌、地址、姓名等个人信息；
- 完整认证文件或配对记录。

## 漏洞报告

请使用 GitHub 的 **Private vulnerability reporting**（仓库 Security 标签页）报告敏感问题，不要在公开 Issue 中披露可利用细节。非敏感问题可使用 Issue 模板，并附脱敏后的诊断报告。

## 支持的版本

安全修复只针对最新预览版和最新正式版。旧预览包可能包含已修复的问题，请升级后再复现。
