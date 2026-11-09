# 安全与发布打包

## 凭据边界

LynkCo-Link 的公开源码树和 CI 产物**不包含** CarPlay 配件认证身份，也不包含 Android 发布签名密钥。

但当前公开 Release 的 `v0.1.2-preview` 已上传 **`LynkCo-Link-0.1.2-general-signed.apk`**，该 APK 包含可提取的认证材料。任何下载者都能提取 `identity.pk8` / `certificate.p7b`；这是公开实验身份，不是 Apple 为 LynkCo-Link 签发的 MFi 身份。

- `.gitignore` 与 CI 的 `scripts/check_public_tree.py` 会拒绝 `.pk8`、`.p7b`、`.p12`、`.jks`、`.keystore`、`.apk` 等凭据或分发文件进入源码树。
- 只有显式设置 `LYNKCO_LINK_AUTH_ASSETS_DIR` 时，两个指定文件（`offline-mfi/identity.pk8`、`offline-mfi/certificate.p7b`）才会被打包；`rejectBundledCredentials` 会拒绝其他凭据混入。
- Android APK 签名密钥只保留在发布者本地，通过环境变量注入，不进入仓库或 APK。

如果你自己构建了含认证身份的 standalone 包，**不要**把额外的 APK、资产目录、日志或签名文件上传到公开 Issue、Release 或网盘。当前公开 Release 是维护者明确选择发布的一份实验包。

## 发布包说明

当前发布页的 `LynkCo-Link-0.1.2-general-signed.apk` 使用 release 签名并包含认证材料，风险由使用者自行承担；`standardPreview` / `lynkPreview` 不再构建。任何持有该 APK 的人都能提取其中的认证材料。

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
