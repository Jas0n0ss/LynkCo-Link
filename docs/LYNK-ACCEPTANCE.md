# Lynk & Co OSN acceptance

This is the real-car gate for LynkCo-Link. Unit tests and the emulator cannot prove CarPlay authentication, audio routing, or OEM firmware behavior.

## Capture evidence

With USB debugging enabled and the phone connected:

```sh
scripts/lynk-acceptance.sh <adb-serial>
```

The archive contains device/build identity, display, audio, Bluetooth, Wi-Fi, package permissions, logcat, a screenshot and the UI hierarchy. Attach it to a GitHub issue with the manual results below.

To export the app's own redacted bundle:

```text
Settings → Diagnostics → Export debug logs (ZIP)
Settings → Diagnostics → Run head-unit checks
```

## Test matrix

| Area | Test | Pass condition |
| --- | --- | --- |
| Install | fresh install, upgrade, reboot | launches and keeps settings |
| USB | first authorization, 60 min session | video/audio stable, no ANR |
| Hotspot | 5 GHz WPA2, reconnect | iPhone joins and reconnects |
| Wi-Fi Direct | create group, reconnect | stable session |
| Same LAN | router with client isolation off | discovery and session work |
| Audio | music, navigation, call, Siri, WeChat | no echo, dropout or stuck focus |
| Microphone | call and voice assistant | both directions audible |
| Steering wheel | Siri, play/pause, next/previous, phone | correct action, no repeat/stuck key |
| Display | 1920×1080, 1920×720, day/night | no clipping, touch aligned |
| Lifecycle | ignition off/on, background, kill | recovers without reopening settings |
| Permissions | deny Bluetooth, location, overlay, usage | clear recovery, no crash |
| OTA | head-unit update then launch | whitelist and settings survive |
| Diagnostics | debug ZIP and validation page | complete, redacted, shareable |

## Known hardware risks

- Experimental CarPlay identity may break after an iOS update.
- OSN firmware differs in hotspot, ADB, split-screen and audio routing.
- HVAC APIs are not public; a vendor bridge is required.
- Cluster projection is only implemented for measured Geely panels.

Record the model, OSN/Android build, iPhone/iOS version, connection mode and the evidence archive with every result.
