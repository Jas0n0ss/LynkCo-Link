# 隐私与诊断

LynkCo-Link 使用本地认证和到 iPhone 的直连 USB / Wi-Fi，不需要账号，也没有远程认证服务。诊断数据不会自动上传；iPhone 上 CarPlay 应用自身的联网与隐私行为不受本项目控制。

## 车机本地数据

车机会在应用私有目录保存：

- 应用设置、首选连接方式和已选 iPhone；
- 配对数据与认证状态；
- 有界的运行日志、最近一次致命栈和环境快照；
- 你主动导出的诊断报告。

认证与配对材料不进入 Android 备份。卸载应用会删除应用私有数据；已经导出到“下载”或 U 盘的报告需要你手动删除。

## 诊断报告

只有你主动选择 **设置 → 诊断 → 保存诊断报告** 时才会生成报告。报告可能包含：

- 应用版本、Android/OSN/固件版本、设备型号；
- 显示尺寸、分辨率、帧率和 negotiated 参数；
- 连接阶段、USB 描述符、网络接口、Wi-Fi 频段/信道；
- 蓝牙名称、热点 SSID、媒体运行时状态；
- 最近一次崩溃栈（已去敏，不含异常消息）；
- 开启方向盘按键服务时，最近收到的按键码和 LynkCo-Link 的处理结果（不记录输入设备名，不记录文本输入内容）。

导出器会过滤协议载荷、凭据行和常见标识符，但**不能保证**识别所有厂商字符串。公开报告前请自行检查并移除热点密码、账号、车牌、地址等个人信息。

## 报告保存位置

- Android 10+：默认保存到 `Download/LynkCo-Link`（或系统文档选择器选择的位置）。
- 文档选择器或下载目录不可用时：保存到 `Android/data/<package>/files/diagnostic-reports/`，无需存储权限。
- 外部存储也不可用时：保存到应用私有存储。

每个回退位置最多保留最近 8 份导出。确认对话框会显示实际路径，并提供**查看**和**分享**；分享只授予所选文件的读权限。

| 包 | 用途 |
| --- | --- |
| `com.lynkco.link` | 标准正式版 |
| `com.lynkco.link.preview` | 标准预览版 |
| `com.kugou.android.auto` | 领克 A/B 正式版 |
| `com.kugou.android.auto.preview` | 领克 A/B 预览版 |
| `com.lynkco.link.debug` | 标准调试包 |
| `com.kugou.android.auto.debug` | 领克调试包 |

## 权限

- **麦克风**：Siri、通话和 App 内语音。
- **蓝牙 / 附近设备**：发现 iPhone 与无线传输。
- **Wi-Fi / 位置**：无线发现；Android 的 Wi-Fi 扫描通常要求定位权限。位置不会上传。
- **本地 VPN**：仅用于 USB 链路，不提供远程 VPN。
- **无障碍 / ADB（可选）**：方向盘按键、热点自动化、轮速/驻车视频等实验功能；不开启时不影响核心连接。
- **使用情况访问（可选，BYD 专属）**：仅在上游 BYD 仪表主题功能中启用；OSN 上不使用。

## 更新检查

LynkCo-Link 可检查 GitHub 公开 Release 列表获取版本信息。请求会带上版本号作为 User-Agent，GitHub 会看到车机 IP；不会下载或安装未经你确认的更新。可在“关于”中关闭后台检查。

## 网站与第三方

本仓库不托管统计脚本或广告。GitHub、GitHub Actions 和 Release 下载适用 GitHub 自身的隐私政策。第三方依赖与素材见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
