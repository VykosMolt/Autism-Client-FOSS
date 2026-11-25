#!/usr/bin/env bash
# Runs tools/checks/*Check.java (or the given files) against build/classes/java/main. Build first: ./gradle.sh compileJava
set -uo pipefail
ROOT="${ROOT:-$(cd "$(dirname "$0")/../.." && pwd)}"
REAL_HOME="${REAL_HOME:-$HOME}"
JDK_DIR="${JAVA_HOME:-$(find "$REAL_HOME/.gradle/jdks" "$REAL_HOME/.local/share/gradle/jdks" -maxdepth 1 -mindepth 1 -type d -name '*25*' 2>/dev/null | head -n 1 || true)}"
JDK="${JDK_DIR:+$JDK_DIR/bin}"
JDK="${JDK:-$(dirname "$(readlink -f "$(command -v javac)")")}"

ENV_FILE="$ROOT/build/tool-env.sh"
if [[ ! -f "$ENV_FILE" ]]; then
  "$ROOT/gradle.sh" prepareToolEnv >/dev/null || exit 1
fi
# shellcheck source=/dev/null
source "$ENV_FILE"

CP="$CHECK_CP"
fail=0
files=("$@")
[ ${#files[@]} -eq 0 ] && files=("$ROOT"/tools/checks/*Check.java)
for f in "${files[@]}"; do
  out="$(mktemp -d)"
  if "$JDK/javac" -nowarn -proc:none -cp "$CP" -d "$out" "$f" && \
     "$JDK/java" -cp "$out:$CP" "$(sed -n 's/^package \(.*\);/\1./p' "$f")$(basename "$f" .java)"; then :; else
    echo "FAILED: $(basename "$f")"; fail=1
  fi
  rm -rf "$out"
done
exit $fail
