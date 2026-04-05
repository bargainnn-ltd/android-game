#!/usr/bin/env bash
# Measure cold start via `am start -W` (repeat N times). Requires adb, unlocked device, app installed.
set -euo pipefail
PACKAGE="${1:-com.application.eatbts}"
ACTIVITY="${2:-com.application.eatbts/.MainActivity}"
RUNS="${3:-5}"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb not found in PATH" >&2
  exit 1
fi

echo "Stopping $PACKAGE (ignore errors if not running)"
adb shell am force-stop "$PACKAGE" 2>/dev/null || true

for i in $(seq 1 "$RUNS"); do
  echo "--- run $i / $RUNS ---"
  adb shell am start -W -n "$ACTIVITY" | sed -n '1,12p'
  adb shell am force-stop "$PACKAGE" 2>/dev/null || true
  sleep 1
done

echo "Collect TotalTime lines above for comparison across builds/devices."
