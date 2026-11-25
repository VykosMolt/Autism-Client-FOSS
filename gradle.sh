#!/usr/bin/env bash
set -euo pipefail
export REAL_HOME="${REAL_HOME:-$HOME}"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$REAL_HOME/.gradle}"
if [[ -z "${JAVA_HOME:-}" ]]; then
  JDK_DIR="$(find "$GRADLE_USER_HOME/jdks" "$REAL_HOME/.local/share/gradle/jdks" -maxdepth 1 -mindepth 1 -type d -name '*25*' -print -quit 2>/dev/null || true)"
  if [[ -n "$JDK_DIR" ]]; then
    export JAVA_HOME="$JDK_DIR"
  fi
fi
GRADLE_BIN="${GRADLE_BIN:-$(command -v gradle 2>/dev/null || true)}"
if [[ -z "$GRADLE_BIN" ]]; then
  GRADLE_BIN="$(find "$GRADLE_USER_HOME/wrapper/dists" "$REAL_HOME/.local/share/gradle/wrapper/dists" -maxdepth 6 -type f -path '*/bin/gradle' -print -quit 2>/dev/null || true)"
fi
if [[ -z "$GRADLE_BIN" ]]; then
  echo "ERROR: gradle not found on PATH or in Gradle wrapper caches (set GRADLE_BIN or install Gradle)." >&2
  exit 1
fi
cd -- "$(dirname -- "$0")"
OFFLINE_FLAG=()
if [[ "${GRADLE_OFFLINE:-0}" == "1" ]]; then
  OFFLINE_FLAG=(--offline)
fi
exec "$GRADLE_BIN" "${OFFLINE_FLAG[@]}" --no-daemon "$@"
