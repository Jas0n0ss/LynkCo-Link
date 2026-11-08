# OSNPlay

**CarPlay for Lynk & Co OSN head units.**

[![Android checks](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml/badge.svg)](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/Jas0n0ss/OSN-Play?label=release)](https://github.com/Jas0n0ss/OSN-Play/releases)

OSNPlay runs on the **head unit**. It needs no jailbreak, dongle or authentication server. The public APK includes authentication assets and can connect directly.

## Supported cars

| Car | Platform / OS | Status |
| --- | --- | --- |
| Lynk & Co 01 2023 | Snapdragon 8155 / OSN | Development target; awaiting real-car acceptance |
| Lynk & Co 03 2023 | OSN 2.0 / Android 9 | Same-platform reference: wired works, wireless not tuned |
| Other Lynk & Co OSN cars | OSN / Android 7.0+ | Unverified |

See [compatibility](docs/COMPATIBILITY.md).

## Download and install

Download one file:

```text
OSNPlay-<version>-lynk-release-standalone.apk
```

From [Releases](https://github.com/Jas0n0ss/OSN-Play/releases/latest), or:

```sh
gh release download --repo Jas0n0ss/OSN-Play
```

> The package name is `com.kugou.android.auto`, used only to pass the OSN Launcher3 whitelist. The app is OSNPlay and contains no Kugou features. Remove any real Kugou head-unit app before installing.

### Install

**USB drive**: copy the APK to a FAT32 drive, open it from the head-unit file manager, allow unknown sources and install.

**ADB**:

1. Open native Android settings (common entry: dial `2468789`).
2. About device → tap Build number 7 times → Developer options → enable USB debugging.
3. Connect to a computer:

```sh
adb devices
adb install -r OSNPlay-<version>-lynk-release-standalone.apk
```

Launch once after installing; Android stopped state keeps boot start disabled until then:

```sh
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.OSNPlayActivity
```

The first run goes straight to “Connection → Choose iPhone”; it does not ask for a DiLink version.

### No icon in the app list

OSNPlay uses a whitelisted Launcher3 package name, so it should appear. If it does not:

1. Reboot the head unit and check “All apps”.
2. Settings → Advanced → Launcher bubble; allow “Display over other apps”.
3. Or start it with ADB:

```sh
adb shell am start -n com.kugou.android.auto/com.shilapi.xcertplay.OSNPlayActivity
```

### Connect iPhone

- **Wired**: connect to the USB-A data port, open OSNPlay, tap Allow on the iPhone.
- **Wireless**: OSNPlay → Connection, choose car hotspot / Wi-Fi Direct / Existing Wi-Fi and pair.
- On failure: Settings → Diagnostics → Save diagnostic report.

## FAQ

| Problem | Fix |
| --- | --- |
| `adb: command not found` | Install Platform-Tools or run from its directory |
| `device unauthorized` | Allow USB debugging on the car; revoke and reconnect if needed |
| `INSTALL_FAILED_USER_RESTRICTED` | Enable USB install / unknown sources |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Uninstall the old `com.osnplay.lynk` or entry package first (settings are lost) |
| Installed but no icon | Reboot; enable the launcher bubble; or start with ADB |
| CarPlay does not connect | Unlock the iPhone, enable Bluetooth/Wi-Fi, export diagnostics |
| Real Kugou app installed | Package conflict; uninstall the Kugou head-unit app |

## Update and uninstall

Each update is a separate Release; only the two newest are kept. From the old `com.osnplay.lynk` package, uninstall before installing.

```sh
adb uninstall com.kugou.android.auto
```

Uninstalling removes settings and pairing records.

## Build from source

Requires JDK 17+, Android SDK Platform 37.2, Build-Tools 37.0.0 and NDK 28.2.13676358. Authentication assets and the release keystore are never committed.

See [build instructions](docs/BUILD.md).

## Docs

- [Install](docs/INSTALL.md) · [Build](docs/BUILD.md) · [Compatibility](docs/COMPATIBILITY.md)
- [Testing](docs/TESTING.md) · [Privacy](docs/PRIVACY.md) · [Security](SECURITY.md)
- [License](LICENSING.md) · [Third-party notices](docs/THIRD_PARTY_NOTICES.md)

## Sources and credits

OSNPlay is based on these public projects:

| Project | Role |
| --- | --- |
| [DiPlay](https://github.com/shihabal3amri/DiPlay) | Main baseline (0.2.22): CarPlay transport, audio, video and settings |
| [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay) | Lynk & Co OSN environment checks, diagnostics and head-unit work |
| [xcertplay](https://github.com/shilapi/xcertplay) | Receiver base and project structure behind DiPlay |
| [diplay-ecarx](https://github.com/jianyun8023/diplay-ecarx) | Lynk & Co 01 / ECARX platform permissions and real-car notes |

See [LICENSING.md](LICENSING.md) and [third-party notices](docs/THIRD_PARTY_NOTICES.md) for licenses and copyright.

## Disclaimer

- Not an Apple-certified product; not affiliated with or endorsed by Apple Inc. or Lynk & Co. CarPlay and its icon belong to Apple Inc.
- The APK contains an extractable experimental identity; future iOS updates may break it, and public distribution is at your own risk.
- Installing third-party APKs may affect the warranty; do it while parked.

## License

Based on DiPlay / xcertplay (GPL-3.0); some settings UI files are AGPL-3.0-only. See [LICENSING.md](LICENSING.md).

[简体中文](README.md)
