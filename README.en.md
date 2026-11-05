# OSNPlay

**An independent CarPlay receiver for Lynk & Co OSN head units.**

[![Android checks](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml/badge.svg)](https://github.com/Jas0n0ss/OSN-Play/actions/workflows/android.yml)
[![Latest preview](https://img.shields.io/github/v/release/Jas0n0ss/OSN-Play?include_prereleases&label=preview)](https://github.com/Jas0n0ss/OSN-Play/releases)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0%20%2F%20AGPL--3.0-blue.svg)](LICENSE)

OSNPlay is a derivative of [DiPlay](https://github.com/shihabal3amri/DiPlay) (GPL-3.0, 0.2.22 baseline) adapted to the Lynk & Co OSN platform. It also integrates the Lynk-specific work from [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay): build editions, environment checks, connection diagnostics and vehicle notes.

- Installs on the **head unit**, not the iPhone. No jailbreak, dongle or authentication server is required.
- Application ID `com.osnplay`; it can coexist with the original DiPlay APK.
- Primary target: **Lynk & Co 01 2023 (Snapdragon 8155 / OSN)**. Wired CarPlay has been confirmed on the same platform in a 2023 Lynk & Co 03.

## Supported vehicles

| Vehicle | Platform / system | OSNPlay status | Notes |
| --- | --- | --- | --- |
| Lynk & Co 01 2023 | Snapdragon 8155 / OSN | 🎯 Target, vehicle validation pending | Resolution, hotspot and key behaviour must be checked on the car |
| Lynk & Co 03 2023 | OS N 2.0 / Android 9 (API 28) | 🧪 Same-platform reference | LynkCo-DiPlay R1 confirmed wired CarPlay; use the USB-A data port, USB-C is charge-only; wireless did not work |
| Other Lynk & Co OSN models | OSN / Android 7.0+ (API 24+) | ⚪ Untested | The APK installs, but connection, audio and display behaviour need per-vehicle testing |

OSNPlay's own vehicle acceptance is still in progress. “Same-platform reference” is evidence from a sibling fork, not an acceptance result for OSNPlay on your firmware. See [compatibility](docs/COMPATIBILITY.md).

## Features

- **Wired and wireless CarPlay**: USB, car hotspot, Wi-Fi Direct and Existing Wi-Fi / Same LAN; authentication stays local.
- **Environment check**: a read-only readiness report for app, USB and wireless in Settings → Diagnostics, with shortcuts to the relevant fix.
- **Connection diagnostics**: retains the last fatal stack and exports USB descriptors, network, Bluetooth, hotspot and media-runtime snapshots. Reports leave the unit only when you share them.
- **Display tuning**: resolution, picture size, frame rate, day/night, system bars and split-screen areas.
- **Audio and keys**: music buffering, navigation volume, optional steering-wheel media/Siri keys (some features require ADB or an accessibility service).
- **Upstream features**: auto-connect, start with the car, hotspot automation and diagnostic export from DiPlay 0.2.22.
- **OSN launcher compatibility**: adaptive icon, boot notification and home-screen bubble for the stock Launcher3 package whitelist that can hide sideloaded apps.

BYD-firmware-only features (HUD navigation, cluster cards, ambient lighting, BYD settings entry) are out of scope for OSN; the code remains only for upstream compatibility.

## Download and install

Download the preview APK from [Releases](https://github.com/Jas0n0ss/OSN-Play/releases) and follow the [installation guide](docs/INSTALL.md).

| Package | applicationId | Purpose |
| --- | --- | --- |
| `OSNPlay-<version>-standard.apk` | `com.osnplay` (release) / `com.osnplay.preview` (preview) | Main package |
| `OSNPlay-<version>-lynk.apk` | `com.osnplay.lynk` (release) / `com.osnplay.lynk.preview` (preview) | Lynk A/B package; coexists with the main package |

> **The default preview APK has no CarPlay accessory identity.** Public source and CI builds contain application code only. They install, show the interface and let you inspect settings, but **cannot complete a real CarPlay connection**. For in-car use, build a standalone package with your own lawful `DIPLAY_AUTH_ASSETS_DIR` as described in [BUILD.md](docs/BUILD.md). This repository does not distribute accessory private keys.

## Build from source

Requires JDK 17+ (CI uses 25), Android SDK Platform 37.2, Build-Tools 37.0.0 and NDK 28.2.13676358.

```sh
# Unit tests + lint
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :mobile:lintStandardDebug

# Source APK (no accessory identity)
./gradlew :mobile:assembleStandardDebug :mobile:assembleLynkDebug

# Preview packages (debug-signed, separate `.preview` application ID)
./gradlew :mobile:assembleStandardPreview :mobile:assembleLynkPreview
```

See [docs/BUILD.md](docs/BUILD.md) for the full environment, signing, standalone and CI instructions.

## Versions and build variants

| Build type | Application ID | Example version | Signing | Purpose |
| --- | --- | --- | --- | --- |
| `standardDebug` | `com.osnplay.debug` | `0.1.2-debug` | debug | Development |
| `lynkDebug` | `com.osnplay.lynk.debug` | `0.1.2-lynk-debug` | debug | Lynk development |
| `standardPreview` | `com.osnplay.preview` | `0.1.2-preview` | debug | Public preview / UI review (separate from stable) |
| `lynkPreview` | `com.osnplay.lynk.preview` | `0.1.2-lynk-preview` | debug | Lynk public preview (separate from stable) |
| `standardRelease` | `com.osnplay` | `0.1.2` | release | Signed release |
| `lynkRelease` | `com.osnplay.lynk` | `0.1.2-lynk` | release | Signed Lynk release |

`standard` / `lynk` are product flavors (distribution editions); `debug` / `preview` / `release` are build types. CI runs unit tests, lint, all four debug/preview variants and a release-signing check on every push and pull request, then uploads the APKs and reports.

## Documentation

- [Documentation index](docs/README.md)
- [Build and CI](docs/BUILD.md) · [Install](docs/INSTALL.md) · [Compatibility](docs/COMPATIBILITY.md)
- [Adaptation notes and roadmap](docs/ADAPTATION-NOTES.md) · [Testing](docs/TESTING.md)
- [Design review](docs/DESIGN-REVIEW.md) · [Preview release notes](docs/RELEASE-0.1.1-PREVIEW.md)
- [Privacy](docs/PRIVACY.md) · [Security](SECURITY.md) · [Third-party notices](docs/THIRD_PARTY_NOTICES.md)
- [简体中文 README](README.md)

## Important notices

- **Not an Apple-certified product.** This project is not affiliated with or endorsed by Apple Inc. or Lynk & Co. CarPlay and its icon belong to Apple Inc.
- The accessory identity is experimental and was recovered from public firmware; it is not a newly issued MFi identity. Future iOS updates may reject it, and its suitability for public distribution is not established.
- Installing third-party APKs on a head unit may affect the warranty or violate the manufacturer's terms. Assess the risk yourself.
- Do not operate non-driving features while driving. Install, debug and capture logs while parked.

## License and credits

- Based on [DiPlay](https://github.com/shihabal3amri/DiPlay) / [xcertplay](https://github.com/shilapi/xcertplay) under **GPL-3.0**. Four settings-UI files are **AGPL-3.0-only**, so the repository is a combined GPL/AGPL work; see [LICENSING.md](LICENSING.md) and [LICENSE](LICENSE).
- The settings UI adapts [DiAuto](https://github.com/wycomco/diauto) (AGPL-3.0); license texts are in `docs/licenses/`.
- Thanks to [LynkCo-DiPlay](https://github.com/soBigRice/LynkCo-DiPlay) for the Lynk OSN notes and diagnostics work.
- Dependency and asset notices are in [docs/THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md).
