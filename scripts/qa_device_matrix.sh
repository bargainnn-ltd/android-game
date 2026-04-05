#!/usr/bin/env bash
# QA device matrix: unit tests + optional connected / Gradle Managed Devices (GMD).
# GMD tasks download system images on first run and require sufficient disk space.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "== Unit tests (JVM) =="
./gradlew :app:testDebugUnitTest --console=plain

echo "== Debug assemble =="
./gradlew :app:assembleDebug --console=plain

if [[ "${RUN_CONNECTED_TESTS:-}" == "1" ]]; then
  echo "== Connected instrumented tests (device/emulator required) =="
  ./gradlew :app:connectedDebugAndroidTest --console=plain
else
  echo "Skip connected tests (set RUN_CONNECTED_TESTS=1 with a device online)."
fi

if [[ "${RUN_GMD_TESTS:-}" == "1" ]]; then
  echo "== Gradle Managed Devices (API 26 + 34) — first run may download images =="
  ./gradlew :app:pixel2Api26DebugAndroidTest :app:pixel6Api34DebugAndroidTest --console=plain
else
  echo "Skip GMD (set RUN_GMD_TESTS=1 to run pixel2Api26 + pixel6Api34 instrumented tests)."
fi

echo "Done."
echo ""
echo "Manual QA (not automated here): matchmaking with airplane mode; TalkBack focus order; Accessibility Scanner contrast."
