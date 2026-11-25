#!/usr/bin/env bash
# Runs tools/checks/*Check.java (or the given files) against build/classes/java/main. Build first: ./gradle.sh compileJava
set -uo pipefail
ROOT="${ROOT:-$(cd "$(dirname "$0")/../.." && pwd)}"
REAL_HOME="${REAL_HOME:-$HOME}"
JDK_DIR="${JAVA_HOME:-$(find "$REAL_HOME/.local/share/gradle/jdks" -maxdepth 1 -mindepth 1 -type d -name '*25*' 2>/dev/null | head -n 1 || true)}"
JDK="${JDK_DIR:+$JDK_DIR/bin}"
JDK="${JDK:-$(dirname "$(readlink -f "$(command -v javac)")")}"
GAME_CP="$(cat "$ROOT/cp26.3.txt")"
LWJGL="$(find "$REAL_HOME/.local/share/PrismLauncher/libraries/org/lwjgl" -name '*3.4.3.jar' ! -name '*natives*' | paste -sd:)"
JARS="$(find "$ROOT/libs" "$ROOT/src/main/resources/META-INF/jars" "$ROOT/build/compile-deps" -name '*.jar' 2>/dev/null | paste -sd:)"
LOADER="$REAL_HOME/.local/share/PrismLauncher/libraries/net/fabricmc"
CP="$ROOT/build/classes/java/main:$ROOT/src/main/resources:$GAME_CP:$LWJGL:$JARS:$LOADER/sponge-mixin/0.17.5+mixin.0.8.7/sponge-mixin-0.17.5+mixin.0.8.7.jar:$LOADER/fabric-loader/0.20.1/fabric-loader-0.20.1.jar:$ROOT/stubs/compat-stubs.jar"
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
