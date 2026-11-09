# 下一步 TODO：实车验收与适配

当前版本：**LynkCo-Link 0.3.4**  
目标车型：**领克 01 / 03，骁龙 8155，OSN 2.0**  
状态：模拟器验证完成，等待实车证据。

标记说明：

- **[USER]**：必须由车主/测试人员在实车上完成。
- **[DEV]**：收到证据后由开发完成。
- **驻车测试**：所有操作必须在车辆静止、驻车挡完成。

---

## A. 用户现在要做的事

### A1. 准备环境

需要：

- 领克 01 或 03，8155 / OSN 2.0
- iPhone，已更新到当前 iOS
- USB-A 数据线（能传数据，不是仅充电线）
- 车机已开启开发者选项和 USB 调试
- 电脑已安装 `adb`

在电脑上确认连接：

```sh
adb devices
```

应看到车机设备。若有多台设备，后续命令加：

```sh
adb -s <设备序列号> ...
```

先记录：

```text
车型/年款：
OSN / Android / 固件：
中控分辨率：
iPhone / iOS：
APK 版本：
```

### A2. 安装 APK

下载：

```text
LynkCo-Link-0.3.4-general-signed.apk
```

安装：

```sh
adb install -r LynkCo-Link-0.3.4-general-signed.apk
```

首次启动必须手动拉起：

```sh
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.LynkCoLinkActivity
```

如果提示签名冲突，先卸载旧包；卸载会清空设置：

```sh
adb uninstall com.kugou.android.auto
```

### A3. 权限和自检

打开：

```text
设置 → 诊断
```

依次执行：

1. 允许蓝牙、定位、通知等权限。
2. 允许“显示在其他应用上层”。
3. 允许“修改系统设置”。
4. 打开“使用情况访问”。
5. 点击 **Run head-unit checks**。
6. 截图保存检查结果。
7. 点击 **Export debug logs (ZIP)**，保存 ZIP。

重点记录检查结果中的：

```text
lynk_model
display
cluster
audio_output
wheel_keys
carplay_assets
```

### A4. 连接测试（每项单独记录）

按顺序测试，每项至少 10 分钟：

#### 1. USB 有线

1. 用数据线连接 iPhone。
2. iPhone 解锁，点“允许”。
3. 等待 CarPlay 画面。
4. 播放音乐、导航、Siri、拨一通电话。

记录：

```text
是否首次成功：
画面是否正常：
声音是否正常：
触摸是否偏移：
断开重连次数：
失败现象：
```

#### 2. 车机热点

1. 车机打开热点，优先 5 GHz。
2. App 选择 Built-in car hotspot。
3. 按提示保存热点信息。
4. iPhone 加入热点。
5. 等待 CarPlay。

#### 3. Wi-Fi Direct

1. App 选择 Wi-Fi Direct。
2. 等待建组。
3. 连接 iPhone。

#### 4. Same LAN

1. 车机和 iPhone 连接同一路由器。
2. 关闭路由器客户端隔离。
3. 选择 Existing Wi-Fi / Same LAN。

每种方式都记录：

```text
连接耗时：
是否自动重连：
视频是否卡顿：
音频是否断续：
10 分钟内是否掉线：
```

### A5. 音频、麦克风、Siri、通话

在 USB 或稳定无线连接下逐项测试：

| 项目 | 操作 | 记录 |
| --- | --- | --- |
| 音乐 | 播放 3 首不同歌曲 | 有无爆音/断音 |
| 导航 | 导航播报时播放音乐 | 音乐是否自动压低 |
| Siri | 长按方向盘 Siri 键，说“打开导航” | 是否唤醒、是否听清 |
| 麦克风 | 用 CarPlay 打电话 | 对方是否能听清 |
| 通话 | 接听、挂断、通话中切媒体 | 是否恢复音乐 |
| 微信语音 | 发一条语音 | 是否有声音/回声 |
| 音量 | 方向盘和车机音量键 | 是否同步 |

### A6. 方向盘按键采集

先在电脑运行：

```sh
adb shell getevent -lt > wheel-keys.txt
```

然后逐个按键，每次间隔 2 秒：

```text
Siri 短按
Siri 长按
播放/暂停
上一曲
下一曲
电话接听
电话挂断
音量 +
音量 -
模式键
```

完成后按 `Ctrl+C` 停止。

如果 `getevent` 没有权限，改用：

```text
设置 → 诊断 → Export debug logs (ZIP)
```

并在反馈中写明每个按键的实际反应。

### A7. 权限拒绝测试

逐项关闭权限，再重新打开 App：

- 蓝牙
- 定位
- 通知
- 悬浮窗
- 修改系统设置
- 使用情况访问

每项记录：

```text
是否崩溃：
是否有明确提示：
是否能进入设置修复：
是否反复弹窗：
```

### A8. 熄火、后台、OTA

1. CarPlay 运行中按 Home 回桌面，再切回 App。
2. 熄火，等待 30 秒，重新上电。
3. 强制停止 App：

```sh
adb shell am force-stop com.kugou.android.auto
```

4. 重新启动，确认是否自动恢复。
5. 如果车机有 OTA，更新后再测一次安装和启动。

### A9. 提交材料

请把以下文件发回：

```text
1. 本页 A4-A8 的填写结果
2. head-unit checks 截图
3. LynkCo-Link-vehicle-report-*.zip
4. LynkCo-Link-evidence-*.tar.gz
5. wheel-keys.txt（如有）
6. 故障截图/录屏
```

`acceptance-*.tar.gz` 不是 APK 内的文件，而是电脑通过 adb 采集的系统证据。
最省事的方式是一次生成合并包：

```sh
./scripts/lynk-collect.sh <设备序列号>
```

它会：

1. 采集 adb 系统证据（`acceptance-*.tar.gz`）
2. 拉取 APK 内最新的车辆报告 ZIP
3. 合并成 `LynkCo-Link-evidence-*.tar.gz`

如果提示 “No in-app vehicle report was found”，先执行：

```text
设置 → 诊断 → Collect vehicle report (ZIP)
```

然后再运行脚本。释放版无法通过 `run-as` 读取应用目录，脚本使用
`adb pull /sdcard/Android/data/com.kugou.android.auto/files/debug-logs/`

提交前删除：

- 热点密码
- VIN
- 电话号码
- 含个人信息的截图

---

## B. 开发接下来要做的事

### B1. 收到首批证据后

- [ ] 解析 `acceptance-*.tar.gz`、debug ZIP、wheel-keys。
- [ ] 确认真实分辨率、密度、音频设备、副屏和键值。
- [ ] 更新 `LynkCompatibility` 的车型证据。
- [ ] 把真实键值补进方向盘映射。
- [ ] 修复首批 USB/无线/音频问题。

### B2. 连接稳定性

- [ ] USB 首次授权和重连。
- [ ] 车机热点 5 GHz。
- [ ] Wi-Fi Direct 建组。
- [ ] Same LAN 发现。
- [ ] 熄火/休眠/后台恢复。
- [ ] 60 分钟稳定性测试。

### B3. 音频与通话

- [ ] 音乐/导航混音。
- [ ] 麦克风上行。
- [ ] Siri。
- [ ] 通话接听/挂断。
- [ ] 通话后媒体恢复。
- [ ] 回声/爆音/延迟修复。

### B4. 权限与生命周期

- [ ] 权限拒绝不崩溃。
- [ ] 权限恢复后可继续使用。
- [ ] 开机自启。
- [ ] 后台被杀恢复。
- [ ] OTA 后白名单和设置保留。

### B5. 真实车辆 SDK

- [ ] 获取领克/ECARX HVAC Binder 或厂商 SDK。
- [ ] 实现真实 `LynkVehicleSdk`：空调、媒体。
- [ ] 属性不存在/权限拒绝降级。
- [ ] 实车验证温度和风量。

> 没有厂商接口时不会伪造 HVAC 控制。

### B6. 仪表投射

- [ ] 确认副屏 displayId、分辨率和 DIM 服务。
- [ ] 接入 CarPlay 仪表画面。
- [ ] 实车确认仪表不被原厂界面遮挡。

### B7. 发布前

- [ ] 全量单元测试和 Lint。
- [ ] CI 全绿。
- [ ] 更新检查和 SHA256。
- [ ] 英文 release note。
- [ ] 保留所有历史 Release。

---

## C. 完成标准

只有以下全部满足，才能称为“实车适配完成”：

- [ ] 领克 01 8155 有线 + 至少一种无线稳定 60 分钟。
- [ ] 领克 03 无线路径完成或明确记录不支持。
- [ ] 音乐、导航、Siri、通话、麦克风正常。
- [ ] 方向盘按键全部正确且无卡键。
- [ ] 权限拒绝、熄火重启、后台恢复不崩溃。
- [ ] 升级/OTA 不丢设置。
- [ ] 调试日志可导出且已脱敏。
- [ ] CI 和 Release 资产校验通过。
