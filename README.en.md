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
- **OSN launcher compatibility**: adaptive icon, boot auto-start/notification and home-screen bubble; see the stock Launcher3 whitelist note in [INSTALL](docs/INSTALL.md).
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

Source/CI builds contain no accessory identity; they only verify UI/settings. A **real CarPlay connection needs your own lawful `DIPLAY_AUTH_ASSETS_DIR`**. This repository does not distribute accessory private keys.

## Install and launcher

Follow [INSTALL](docs/INSTALL.md). Notes:

- Launch once manually after a fresh install (Android stopped state); boot auto-start/notification then works.
- The stock OSN Launcher3 uses a package whitelist, so a sideloaded APK may not appear in the app list; use ADB `am start`, an alternative launcher or the home-screen bubble.
- The stock home icon can only be solved by the system whitelist or an alternative launcher; a normal APK cannot bypass it.

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
