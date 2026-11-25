#!/usr/bin/env bash
# Runs the given checks or every *Check.java. Build first with ./gradlew compileJava.
set -euo pipefail
ROOT="${ROOT:-$(cd "$(dirname "$0")/../.." && pwd)}"

ENV_FILE="$ROOT/build/tool-env.sh"
if [[ "${AUTISM_TOOL_ENV_PREPARED:-0}" != 1 || ! -f "$ENV_FILE" ]]; then
  "$ROOT/gradle.sh" -q prepareToolEnv
fi
# shellcheck source=/dev/null
source "$ENV_FILE"

fail=0
files=("$@")
if (( ${#files[@]} == 0 )); then
  files=("$ROOT"/tools/checks/*Check.java)
fi
for f in "${files[@]}"; do
  out="$(mktemp -d)"
  if ! {
    "$JDK/javac" -nowarn -proc:none -cp "$CHECK_CP" -d "$out" "$f" &&
      "$JDK/java" -cp "$out:$CHECK_CP" "$(sed -n 's/^package \(.*\);/\1./p' "$f")$(basename "$f" .java)"
  }; then
    echo "FAILED: $(basename "$f")"
    fail=1
  fi
  rm -rf "$out"
done
exit $fail
