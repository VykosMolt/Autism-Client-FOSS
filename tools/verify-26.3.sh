#!/usr/bin/env bash
set -euo pipefail

# Verifies the built AUTISM Client 5.8-26.3 JAR:
#   1. JarCheck against stock 26.3 JAR (optional; skip with SKIP_JAR_CHECK=1)
#   2. Full MC 26.3 Mixin Audit with Sodium 0.9.3-beta.5 + Lithium + Fabric API + ModMenu
#   3. Full MC 26.3 Mixin Audit with Sodium 0.9.3-beta.3 + Iris 1.11.7 + Lithium + Fabric API + ModMenu
#   4. Full MC 26.3 Mixin Audit without Sodium (checks vanilla FluidRenderer + Fabric Indigo mixins)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJ_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REAL_HOME="${REAL_HOME:-$HOME}"

JAR_PATH="${1:-$PROJ_DIR/build/libs/AUTISM-Client-5.8-26.3.jar}"
STOCK_JAR="${2:-$REAL_HOME/Downloads/AUTISM-Client-5.3-26.3.jar}"

CURRENT_WORK=""
cleanup() {
  if [[ -n "$CURRENT_WORK" && -d "$CURRENT_WORK" ]]; then
    rm -rf "$CURRENT_WORK"
  fi
}
trap cleanup EXIT INT TERM

if [[ ! -f "$JAR_PATH" ]]; then
  echo "ERROR: Built JAR not found at $JAR_PATH (run ./gradlew assemble first)" >&2
  exit 1
fi

ENV_FILE="$PROJ_DIR/build/tool-env.sh"
if [[ "${AUTISM_TOOL_ENV_PREPARED:-0}" != 1 || ! -f "$ENV_FILE" ]]; then
  "$PROJ_DIR/gradle.sh" -q prepareToolEnv
fi
# shellcheck source=/dev/null
source "$ENV_FILE"

if [[ "${SKIP_JAR_CHECK:-0}" != "1" ]]; then
  if [[ ! -f "$STOCK_JAR" ]]; then
    echo "ERROR: Stock 26.3 JAR not found at $STOCK_JAR (pass as arg 2 or set SKIP_JAR_CHECK=1)" >&2
    exit 1
  fi
  echo "== [1/4] Jar check against 26.3 stock =="
  "$JDK/java" -cp "$ASM_CP" "$PROJ_DIR/tools/JarCheck.java" "$STOCK_JAR" "$JAR_PATH"
fi

run_mixin_audit() {
  local label="$1"
  local with_sodium="$2"
  local work
  work="$(mktemp -d /tmp/autism-263-verify-XXXXXX)"
  CURRENT_WORK="$work"
  mkdir -p "$work/mods" "$work/classes"
  cp "$JAR_PATH" "$work/mods/AUTISM-Client-5.8-26.3.jar"
  cp "$MOD_FABRIC_API" "$MOD_LITHIUM" "$MOD_MODMENU" "$work/mods/"
  if [[ "$with_sodium" == "beta5" ]]; then
    cp "$MOD_SODIUM_BETA5" "$work/mods/"
  elif [[ "$with_sodium" == "iris" ]]; then
    cp "$MOD_SODIUM_IRIS" "$MOD_IRIS" "$work/mods/"
  fi

  cat > "$work/AuditRunner.java" <<'JAVA'
import java.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;

public final class AuditRunner {
    public static void main(String[] args) throws Exception {
        ClassLoader cl = new Knot(EnvType.CLIENT).init(new String[]{"--gameDir", args[0]});
        Thread.currentThread().setContextClassLoader(cl);
        Class.forName("net.minecraft.client.main.Main", false, cl);
        Class<?> envCls = cl.loadClass("org.spongepowered.asm.mixin.MixinEnvironment");
        Object transformer = envCls.getMethod("getActiveTransformer").invoke(envCls.getMethod("getCurrentEnvironment").invoke(null));
        List<?> configs = (List<?>) field(field(transformer, "processor"), "configs");
        var getTargets = cl.loadClass("org.spongepowered.asm.mixin.extensibility.IMixinConfig").getMethod("getTargets");
        Set<String> targets = new TreeSet<>();
        for (Object c : configs) {
            for (Object t : (Collection<?>) getTargets.invoke(c)) targets.add(t.toString());
        }
        int failed = 0;
        for (String t : targets) {
            try {
                Class.forName(t.replace('/', '.'), false, cl).getDeclaredMethods();
            } catch (Throwable e) {
                Throwable root = e;
                while (root.getCause() != null) root = root.getCause();
                failed++;
                System.out.println("MIXIN TARGET FAILED " + t + ": " + root);
            }
        }
        System.out.println(failed == 0
            ? "MIXIN AUDIT PASSED (" + configs.size() + " configs, " + targets.size() + " targets)"
            : "MIXIN AUDIT FAILED: " + failed + " of " + targets.size() + " targets");
        System.exit(failed == 0 && !targets.isEmpty() ? 0 : 1);
    }

    private static Object field(Object o, String name) throws Exception {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            try {
                var f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(o);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }
}
JAVA

  "$JDK/javac" -cp "$LOADER_CP:$GAME_CP" -d "$work/classes" "$work/AuditRunner.java"
  echo "== $label =="
  if ! ( cd "$work" && "$JDK/java" -cp "$LOADER_CP:$GAME_CP:$work/classes" AuditRunner "$work" > "$work/audit.log" 2>&1 ); then
    grep -E 'MIXIN|Exception|Error|Caused by' "$work/audit.log" | head -40
    rm -rf "$work"
    exit 1
  fi
  grep 'MIXIN AUDIT' "$work/audit.log"
  rm -rf "$work"
  CURRENT_WORK=""
}

run_mixin_audit "[2/4] Mixin audit (with Sodium 0.9.3-beta.5 + Lithium + Fabric API + ModMenu)" beta5
run_mixin_audit "[3/4] Mixin audit (with Sodium 0.9.3-beta.3 + Iris 1.11.7 + Lithium + Fabric API + ModMenu)" iris
run_mixin_audit "[4/4] Mixin audit (without Sodium: Indigo + vanilla FluidRenderer + Lithium)" none
echo "== ALL 26.3 VERIFICATION CHECKS PASSED =="
