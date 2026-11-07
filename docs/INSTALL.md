# 安装指南（领克 OSN）

OSNPlay 安装在**车机**上，不是 iPhone。无需越狱、外设、Mac、Apple 账号或认证服务器。

## 前置条件

- 领克 OSN 车机，系统允许安装 APK。
- iPhone，iOS 版本支持 CarPlay。
- OSNPlay APK（[Releases](https://github.com/Jas0n0ss/OSN-Play/releases) 或自行构建）。
- 有线连接时使用支持数据传输的 USB 线；领克 03 的实测经验是接 **USB-A 数据口**，USB-C 仅充电。

## 选择 APK

| 包 | applicationId | 何时使用 |
| --- | --- | --- |
| `OSNPlay-<version>-standard.apk` | `com.osnplay`（正式）/ `com.osnplay.preview`（预览） | 标准包 |
| `OSNPlay-<version>-lynk.apk` | `com.osnplay.lynk`（正式）/ `com.osnplay.lynk.preview`（预览） | 领克 A/B 包，可与标准包并存 |
| standalone 自建包 | `com.osnplay.debug` / `com.osnplay.preview` | 含认证资产，可完成真实连接 |

> 公开预览包**不含认证身份**，安装后可以看界面、改设置、导出诊断，但无法完成 CarPlay 认证。实车连接请按 [构建说明](BUILD.md) 自行构建 standalone 包。

## 安装到车机

### 方式 A：U 盘 / 文件管理器（固件允许时）

1. 将 APK 拷入 FAT32/exFAT U 盘。
2. U 盘插入车机 USB 口，用系统文件管理器打开 APK。
3. 按提示允许“未知来源”。
4. 安装完成后在应用列表中找到 **OSNPlay**。

### 方式 B：ADB

1. 在车机开启 USB 调试或 Wi-Fi ADB（入口随固件不同，需按你的固件版本操作）。
2. 电脑连接车机：

   ```sh
   adb devices
   adb install -r OSNPlay-0.1.1-preview-standard.apk
   ```

3. 若系统拦截未知来源：

   ```sh
   adb shell settings put secure install_non_market_apps 1
   ```

> 如果 OSN 已关闭 ADB 或第三方安装通道，OSNPlay 无法绕过系统限制。

## 安装成功但桌面不显示 OSNPlay

这是领克 OSN 原厂 **Launcher3 的包名白名单**行为，不是 APK 安装失败。第三方 APK 即使安装成功，也可能只出现在“应用管理”里，不出现在首页或 Dock。社区教程也把“系统启动器显示白名单 Launcher3”单独列出，并提供了小八启动器、应用页等替代入口（[OSN 安装软件说明](https://zcteo.github.io/blog/others/014)、[小八桌面教程](https://www.xiaohongshu.com/discovery/item/69085c6f000000000303a5a3)）。

按以下顺序处理：

1. **先重启车机**，再到“所有应用 / 应用管理 / 更多应用”里找 **OSNPlay**。有些 OSN 版本只刷新桌面缓存后才会出现。
2. **用替代入口打开一次**：
   ```sh
   adb shell am start -n com.osnplay.lynk/com.shilapi.xcertplay.OSNPlayActivity
   ```
   或安装社区用的 **小八启动器 / 应用页 / 应用管家**，它们会列出所有已安装应用。
3. **使用本仓库的 lynk 包**：`lynk` 版本默认开启“开机后自动启动”，并在开机时发一条“打开 OSNPlay”通知；即使桌面没有图标，也能从通知或开机画面进入。**注意：Android 的 stopped state 会让新安装应用在第一次手动启动前收不到 `BOOT_COMPLETED`，所以首次安装后仍需用 ADB 或第三方启动器打开一次；之后开机自启/通知才会生效。**
4. **开启桌面悬浮球**：进入 **设置 → 高级 → 桌面悬浮球**，允许“显示在其他应用上层”。之后回到桌面会出现 OSNPlay 悬浮图标，点击即可打开。这个功能就是为“桌面只显示自己卡片”的车机准备的（上游 #650）。如果系统设置页没有悬浮窗入口，可用 ADB：
   ```sh
   adb shell appops set com.osnplay.lynk SYSTEM_ALERT_WINDOW allow
   ```
5. **修改白名单**：需要 root/工程模式修改 Launcher3 白名单，风险高，不建议普通用户操作；也不要通过改包名伪装成白名单应用，容易和原厂应用冲突。

如果 `am start` 提示 `Activity class does not exist`，说明包没有装进当前用户，按下面的 ADB 安装命令加 `--user 0` 重装。

## 有线连接

1. 驻车，用数据线将 iPhone 插入车机 USB-A 数据口。
2. 打开 OSNPlay；如提示 USB 权限，选择允许。
3. iPhone 弹出“允许使用 CarPlay”时点**允许**，并保持解锁。
4. 稍候进入 CarPlay 画面。

## 无线连接

| 方式 | 操作 |
| --- | --- |
| 车机热点 | 车机设置中打开热点；OSNPlay → 连接设置选择车机热点，iPhone 加入该热点后按提示配对 |
| Wi-Fi Direct | OSNPlay → 连接设置选择 Wi-Fi Direct；依赖固件，Android 7–9 与 10+ 路径不同 |
| Existing Wi-Fi / Same LAN | iPhone 与车机连同一外部路由器；在 OSNPlay 中选择该路径 |

无线失败时先确认：车机 Wi-Fi 已开、蓝牙已开、iPhone 已解锁、OSNPlay 有定位/附近设备权限。部分固件要求热点名使用拉丁字符。

## 更新与卸载

- 同包名、同签名的 APK 可直接覆盖安装，设置保留。
- 切换 standard / lynk 包等于安装两个独立应用，设置不共享。
- 卸载会删除应用私有设置与日志；已导出的诊断报告需手动删除。

## 故障排查

1. **连接失败**：查看 OSNPlay 首页提示；进入 **设置 → 诊断 → 保存诊断报告**，保留报告用于反馈。
2. **卡在准备 CarPlay**：尝试车机热点或 USB；确认热点名不是中文；重启 OSNPlay 后重试。
3. **有画面无声音**：检查音频输出、音乐缓冲和车机音量/音源；导出诊断报告。
4. **麦克风无声**：确认车机授予录音权限，iPhone 通话/微信语音时保持 CarPlay 前台。
5. **画面比例不对**：设置 → 显示，调整分辨率/尺寸；应用会提示重连生效。
6. **安装被拦截**：确认未知来源与 ADB 权限；部分固件需要工程模式。

反馈问题时请附：车型/年款、OSN 与 Android 版本、固件号、iPhone/iOS、连接方式、APK 版本、复现步骤和脱敏后的诊断报告。**不要上传热点密码或认证文件。**

## 风险提示

- 在车机上安装第三方 APK 可能影响质保或违反领克用户协议。
- OSNPlay 非 Apple 认证产品，未来 iOS 更新后可能失效。
- 驾驶时请勿操作；安装、调试和抓取日志请在驻车状态进行。
