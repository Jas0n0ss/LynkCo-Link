#!/usr/bin/env bash
# Collect evidence from a real Lynk & Co head unit for acceptance.
# Usage: scripts/lynk-acceptance.sh [adb-serial]
set -u
SERIAL="${1:-}"
if [ -n "$SERIAL" ]; then ADB=(adb -s "$SERIAL"); else ADB=(adb); fi
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="acceptance-${STAMP}"
mkdir -p "$OUT"
run() { "$@" >>"$OUT/summary.txt" 2>&1 || true; }

{
  echo "LynkCo-Link acceptance evidence"
  echo "captured: $(date -Iseconds)"
  echo "serial: ${SERIAL:-default}"
} >"$OUT/summary.txt"

run "${ADB[@]}" shell getprop ro.product.manufacturer
run "${ADB[@]}" shell getprop ro.product.model
run "${ADB[@]}" shell getprop ro.build.version.release
run "${ADB[@]}" shell getprop ro.build.version.sdk
run "${ADB[@]}" shell getprop ro.build.display.id
run "${ADB[@]}" shell getprop persist.sys.locale
run "${ADB[@]}" shell wm size
run "${ADB[@]}" shell wm density
run "${ADB[@]}" shell dumpsys package com.kugou.android.auto
run "${ADB[@]}" shell dumpsys display
run "${ADB[@]}" shell dumpsys audio
run "${ADB[@]}" shell dumpsys media_session
run "${ADB[@]}" shell dumpsys bluetooth_manager
run "${ADB[@]}" shell dumpsys wifi
run "${ADB[@]}" shell dumpsys activity activities
run "${ADB[@]}" shell dumpsys window displays
run "${ADB[@]}" shell appops get com.kugou.android.auto
run "${ADB[@]}" shell pm list packages | grep -E 'kugou|lynkco|carplay' || true
run "${ADB[@]}" shell settings get secure enabled_accessibility_services
run "${ADB[@]}" shell settings get secure enabled_notification_listeners

"${ADB[@]}" exec-out screencap -p >"$OUT/screen.png" 2>/dev/null || true
"${ADB[@]}" shell uiautomator dump /sdcard/window.xml >/dev/null 2>&1 || true
"${ADB[@]}" shell cat /sdcard/window.xml >"$OUT/window.xml" 2>/dev/null || true
"${ADB[@]}" logcat -d -v threadtime >"$OUT/logcat.txt" 2>/dev/null || true
tar -czf "$OUT.tar.gz" "$OUT"
echo "Wrote $OUT.tar.gz"
