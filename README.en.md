# OSNPlay

**An independent CarPlay receiver for Lynk & Co OSN head units.**

[![Android checks](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml/badge.svg)](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/Jas0n0ss/OSN-Play?label=release)](https://github.com/Jas0n0ss/OSN-Play/releases)
[![License](https://img.shields.io/badge/license-GPL--3.0%20%2F%20AGPL--3.0-blue.svg)](LICENSING.md)

OSNPlay is based on [DiPlay](https://github.com/shihabal3amri/DiPlay) 0.2.22 and integrates the Lynk work from [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay), [xcertplay](https://github.com/shilapi/xcertplay) and [diplay-ecarx](https://github.com/jianyun8023/diplay-ecarx). It runs on the **head unit**, not the iPhone; no jailbreak, dongle or authentication server is required.

## Supported vehicles

| Vehicle | Platform / system | Status |
| --- | --- | --- |
| Lynk & Co 01 2023 | Snapdragon 8155 / OSN | 🎯 Target, OSNPlay vehicle acceptance pending |
| Lynk & Co 03 2023 | OS N 2.0 / Android 9 | 🧪 Same-platform reference: wired works, wireless not working |
| Other Lynk & Co OSN models | OSN / Android 7.0+ | ⚪ Untested |

“Reference” results come from the sibling LynkCo-DiPlay project, not from OSNPlay vehicle acceptance. See [compatibility](docs/COMPATIBILITY.md).

## Features

- **Wired / wireless CarPlay**: USB, car hotspot, Wi-Fi Direct and Existing Wi-Fi; local authentication.
- **Environment check and diagnostics**: app/USB/wireless readiness in Settings → Diagnostics, plus redacted report export.
- **Head-unit tuning**: resolution, picture size, frame rate, day/night, audio buffering, steering-wheel media/Siri keys.
- **OSN launcher compatibility**: CarPlay icon, boot auto-start/notification and home-screen bubble; see the stock Launcher3 whitelist note in [INSTALL](docs/INSTALL.md).
- BYD-only features (HUD, cluster cards, ambient lighting) are out of scope.

## Build

Requires JDK 17+, Android SDK Platform 37.2, Build-Tools 37.0.0 and NDK 28.2.13676358.

Build only the functional package:

```sh
export DIPLAY_AUTH_ASSETS_DIR=/path/to/auth
export ANDROID_KEYSTORE_PATH=/path/to/release.jks
export ANDROID_KEYSTORE_PASSWORD='...'
export ANDROID_KEY_ALIAS='...'
export ANDROID_KEY_PASSWORD='...'

./gradlew :mobile:assembleStandaloneLynkRelease
```

Output: `mobile/build/outputs/apk/lynk/release/mobile-lynk-release.apk`  
Application ID `com.osnplay.lynk`, version `0.1.2-lynk`, release-signed, with the explicitly supplied authentication assets.

Source/CI builds contain no accessory identity; they only verify UI/settings. A **real CarPlay connection needs authentication assets**. The public `0.1.2` release package includes them; the source tree itself does not distribute the private key.

## Install (beginner)

> Install only **`OSNPlay-<version>-lynk-release-standalone.apk`**. The public `0.1.2` release provides the functional package with authentication assets; older preview APKs cannot connect to CarPlay.

### Prepare

- Park the car and have the iPhone ready.
- APK file: `OSNPlay-0.1.2-lynk-release-standalone.apk`.
- A **USB-A data cable** (PC install) or a FAT32 USB drive (head-unit file manager).
- [Android Platform-Tools](https://developer.android.com/tools/releases/platform-tools) on the PC (includes `adb`).

### Step 1: allow installation

**A. USB drive (if the firmware allows it)**

1. Copy the APK to a FAT32 USB drive.
2. Open it from the head-unit file manager and allow “Unknown sources”.

**B. ADB (usual on OSN)**

1. Open the car dialer and enter `2468789`; if it opens native Android Settings, continue.
2. Tap **About tablet/device → Build number** seven times.
3. Open **Developer options → USB debugging**.
4. Connect the head-unit **USB-A data port** to the PC with a data cable.
5. Run:

```sh
adb devices
```

Allow the USB-debugging prompt on the car.

> If `2468789` does nothing, try the input-method route: text field → input-method settings → Dictionary → Edit custom phrases → back. Menu availability varies by OSN firmware.

### Step 2: install

```sh
adb install -r OSNPlay-0.1.2-lynk-release-standalone.apk
```

| Error | Fix |
| --- | --- |
| `adb: command not found` | Install Platform-Tools or run `adb` from its folder |
| `device unauthorized` | Allow USB debugging on the car; revoke authorizations and reconnect if needed |
| `INSTALL_FAILED_USER_RESTRICTED` | Enable “USB install/Unknown sources” in Developer options, or run `adb shell settings put secure install_non_market_apps 1` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Different signing key: `adb uninstall com.osnplay.lynk`, then install again (settings are lost) |

### Step 3: first launch

A newly installed app is in Android's stopped state; boot auto-start will not run until it is launched once:

```sh
adb shell am start -n com.osnplay.lynk/com.shilapi.xcertplay.DiPlayActivity
```

Without ADB, tap **Open** in the installer or open OSNPlay once from an alternative launcher.

### Step 4: no icon in the app list

The stock OSN Launcher3 uses a **package whitelist**, so sideloaded apps may not appear. This is not an installation failure.

1. Reboot the head unit and check “All apps / App management”.
2. If it is still missing, open **Settings → Advanced → Home-screen bubble**, allow “Display over other apps”, then tap the bubble on the home screen.
3. Or launch it from ADB:

```sh
adb shell am start -n com.osnplay.lynk/com.shilapi.xcertplay.DiPlayActivity
```

4. A stock home-screen icon requires modifying the system whitelist (root/engineering mode) or using an alternative launcher; a normal APK cannot bypass it.

### Step 5: connect the iPhone

- **Wired**: connect the iPhone to the USB-A data port, open OSNPlay, and allow CarPlay on the iPhone.
- **Wireless**: open Connection setup and choose car hotspot, Wi-Fi Direct or Existing Wi-Fi.
- On failure: **Settings → Diagnostics → Save diagnostic report**.

### Uninstall

**Settings → Apps → OSNPlay → Uninstall**, or:

```sh
adb uninstall com.osnplay.lynk
```

Uninstalling removes app settings and pairing records.

## Documentation

- [Build and CI](docs/BUILD.md) · [Install](docs/INSTALL.md) · [Compatibility](docs/COMPATIBILITY.md)
- [Testing](docs/TESTING.md) · [Upstream integration](docs/UPSTREAM-INTEGRATION.md)
- [Privacy](docs/PRIVACY.md) · [Security](SECURITY.md) · [Licensing](LICENSING.md) · [Third-party notices](docs/THIRD_PARTY_NOTICES.md)

## Notices

- **Not an Apple-certified product.** No affiliation with or endorsement by Apple Inc. or Lynk & Co. CarPlay and its icon belong to Apple Inc.
- The accessory identity is experimental, recovered from public firmware; future iOS updates may reject it, and public-distribution suitability is not established.
- Installing third-party APKs may affect the warranty or violate the manufacturer's terms. Install and debug while parked; use at your own risk.

## License and credits

- Based on DiPlay / xcertplay (GPL-3.0); four settings-UI files are AGPL-3.0-only, making the repository a combined GPL/AGPL work. See [LICENSING.md](LICENSING.md).
- Settings UI adapts [DiAuto](https://github.com/wycomco/diauto) (AGPL-3.0); thanks to LynkCo-DiPlay for the Lynk adaptation notes.
- Dependency and asset notices: [THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md).

[简体中文](README.md)
