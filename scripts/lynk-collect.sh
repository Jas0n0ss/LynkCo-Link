#!/usr/bin/env bash
# Collect the in-app vehicle report and the PC-side adb evidence into one archive.
# Usage: scripts/lynk-collect.sh [adb-serial]
set -u
SERIAL="${1:-}"
if [ -n "$SERIAL" ]; then ADB=(adb -s "$SERIAL"); else ADB=(adb); fi
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="LynkCo-Link-evidence-${STAMP}"
mkdir -p "$OUT"

echo "Collecting adb evidence..."
./scripts/lynk-acceptance.sh "${SERIAL:-}" || true
LATEST_TAR="$(ls -t acceptance-*.tar.gz 2>/dev/null | head -1 || true)"
if [ -n "$LATEST_TAR" ]; then
  tar -xzf "$LATEST_TAR" -C "$OUT"
  cp "$LATEST_TAR" "$OUT/acceptance-source.tar.gz"
fi

echo "Pulling the latest in-app vehicle report..."
APP_DIR="/sdcard/Android/data/com.kugou.android.auto/files/debug-logs"
LATEST_ZIP="$("${ADB[@]}" shell ls -t "$APP_DIR"/*.zip 2>/dev/null | tr -d '\r' | head -1 || true)"
if [ -n "$LATEST_ZIP" ]; then
  mkdir -p "$OUT/app-report"
  "${ADB[@]}" pull "$LATEST_ZIP" "$OUT/app-report/" >/dev/null 2>&1 || true
  printf '%s\n' "$LATEST_ZIP" > "$OUT/app-report-path.txt"
else
  cat >"$OUT/app-report-missing.txt" <<TXT
No in-app vehicle report was found.

Open Settings -> Diagnostics -> Collect vehicle report (ZIP), then run this script again.
The app report path is:
$APP_DIR
TXT
fi

cat >"$OUT/README.txt" <<TXT
LynkCo-Link combined evidence
=============================

acceptance-*/   PC-side adb evidence: build, display, audio, Bluetooth, Wi-Fi,
                permissions, activities, logcat, screenshot and UI hierarchy.
app-report/     Latest in-app vehicle report zip: UI tree, app logcat, getprop,
                dumpsys where permitted, screenshot, session logs and checks.

Generated: $(date -Iseconds)
Serial: ${SERIAL:-default}
TXT

tar -czf "$OUT.tar.gz" "$OUT"
echo "Wrote $OUT.tar.gz"
