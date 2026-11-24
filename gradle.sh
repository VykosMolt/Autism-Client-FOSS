#!/usr/bin/env bash
set -euo pipefail
export REAL_HOME="${REAL_HOME:-$HOME}"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$REAL_HOME/.local/share/gradle}"
if [[ -z "${JAVA_HOME:-}" ]]; then
  JDK_DIR="$(find "$GRADLE_USER_HOME/jdks" -maxdepth 1 -mindepth 1 -type d -name '*25*' -print -quit 2>/dev/null || true)"
  if [[ -n "$JDK_DIR" ]]; then
    export JAVA_HOME="$JDK_DIR"
  fi
fi
GRADLE_BIN="${GRADLE_BIN:-$(find "$GRADLE_USER_HOME/wrapper/dists" -maxdepth 6 -type f -path '*/bin/gradle' -print -quit 2>/dev/null || true)}"
if [[ -z "$GRADLE_BIN" ]]; then
  GRADLE_BIN="$(command -v gradle)"
fi
cd -- "$(dirname -- "$0")"
exec "$GRADLE_BIN" --offline --no-daemon "$@"
