# ABI Boundaries & Silent-Breakage Hazards

Every rule below MUST be respected by all deobfuscation tooling and manual refactors. None of these failures are reliably caught by `javac` alone.

## 1. Hard-Frozen Integrity, Auth & Attestation Paths
**NEVER read, inspect, rename, move, or modify:**
- `precompiled/integrity-classes.jar`
- `stubs/integrity-stubs.jar`
- `reference/integrity/`
- `src/main/java/autismclient/e/**`
- `src/main/java/autismclient/sys/**`
- `src/main/java/autismclient/obf/**`
- `src/main/java/autismclient/a/**` (class-hash verification)
- `src/main/java/autismclient/util/mm/guardian/**` (`OfficialGuardian`, `AutismJarAttest`, `InjectionScan`, `LoginProof`)
- `src/main/java/autismclient/modules/al.java`
- `src/main/java/autismclient/util/AutismLiteVariant.java`
- `src/main/java/autismclient/util/bX.java`
- `src/main/java/autismclient/gui/a/DataUtil.java`
- `src/main/java/autismclient/gui/d/JsonUtil.java`

### Compile-Time & Runtime ABI Pins
- `autismclient/util/Q.java` — empty no-op stub required by untouched `modules/al.java`.
- `autismclient/util/mm/a/StreamUtil.java` — hashing helpers called by frozen `autismclient/a/CachePool`.
- **Compile-time frozen classes** (directly referenced by untouched integrity/attestation files; verified by `:compileJava`):
  - `autismclient/modules/f` (multi-module registration container; referenced by `modules/al.java`)
  - `autismclient/modules/as` (`NameCensorModule`; referenced by `modules/al.java`)
  - `autismclient/util/macro/ak` (`ToggleModuleAction`, plus frozen inner enum `ak$Inner_StreamUtil`; referenced by `modules/al.java`)
  - `autismclient/b/DataUtil` (`AddonManager`; referenced by `modules/al.java`)
  - `autismclient/util/g/PkgCls_a` (`PacedTpController`; referenced by `modules/al.java`)
  - `autismclient/util/mm/IoWorker` (`ServerTimeAnchor`; referenced by `autismclient/a/*`)
  - `autismclient/util/mm/StateCache` (JWT public-key verifier calling `autismclient/e/DataUtil`)
  - `autismclient/util/mm/a/CachePool` (`LocalIdentityStore`; referenced by `autismclient/a/CachePool` & `guardian/impl/LoginProof`)
  - `autismclient/util/mm/a/DataUtil` (`LocalSecretSealer`; referenced by `autismclient/a/StreamUtil`)
- **Compile-time frozen enum constants** (directly referenced by untouched `.java` files; verified by `:compileJava`):
  - `autismclient/gui/f/b/CachePool$Inner_DataUtil` (referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/l` (referenced by `gui/d/JsonUtil.java`)
  - `autismclient/util/bW$Inner_StreamUtil` (referenced by `util/bX.java`)
  - `autismclient/util/macro/ak$Inner_StreamUtil` (referenced by `modules/al.java`)
- Any other class/method/field directly referenced by untouched `.java` files is automatically enforced by `./gradle.sh compileJava` and must be added here if encountered.
- Runtime binary compatibility with untouched integrity bytecode and binary addons is preserved via `tools/ReverseRemapJar.java` + synced `mappings/disambig.tsv`.

## 2. Public Addon API (Frozen)
- `autismclient.api.*` (54 files) is already 100% readable and represents the external addon contract. Do not rename classes, methods, or fields in `autismclient.api.*`.

## 3. AI Subsystem (Deferred)
- `autismclient.ai`, `autismclient.ai.a`, `autismclient.ai.b`, and `src/main/resources/autism-ai/worker.jar` are intentionally untouched during non-AI deobfuscation.

## 4. Gson Serialization Lock (0 `@SerializedName` Annotations)
There are **zero** `@SerializedName` annotations in the codebase. Every field on these JSON-serialized types IS an on-disk config/cache key and **MUST NOT** be renamed without adding `@SerializedName("<original>")` + round-trip verification:
- `autismclient.util.AutismConfig` (and all nested classes: `HudElementState`, `ThemeColors`, `PluginScanCacheEntry`, `PayloadFilter`, etc.)
- `autismclient.util.AutismWaypoints` + `Waypoint`
- `autismclient.util.ServerPluginScanCache.CacheFile`
- `autismclient.util.AutismPacketPreset` (used by `util/bT.java`)
- `autismclient.util.AutismProfile` (used by `util/bV.java`)
- `autismclient.util.oresim.AutismOreSimSeedStore`
- `autismclient.modules.aO`
- `autismclient.ai.{AiSettings, AiToolSettings, AiMemoryStore.Memory, AiRegions.Region}` (AI deferred)

## 5. `Enum.name()` & `Enum.valueOf()` Serialization
- `mappings/enum-names.tsv` stores the exact original runtime `Enum.name()` strings (`2,190` constants across `413` enums).
- Renaming an enum constant in source is **only** permitted when renaming it to its exact string in `mappings/enum-names.tsv` AND syncing its `F` row in `mappings/disambig.tsv`.
- `StreamGate.MULTI` and inert `util/macro/MultiAction.java` remain intact for readable legacy Multi blocks (`tools/checks/LegacyMultiCheck.java`).

## 6. Java `record` Component Names
- `mappings/record-names.tsv` (`1,124` entries) is validated strictly by `ReverseRemapJar.restoreRecordNames` (throws `IllegalStateException` on component count mismatch).
- Never alter record component order/count during pure deobfuscation; sync `mappings/disambig.tsv` + `mappings/record-names.tsv` whenever renaming a record class or component.

## 7. Internal Reflection Lock (`MacroAction` Public Fields)
- `autismclient.util.macro.MacroAction` reflects `"listenDuringPreviousAction"`.
- `autismclient.util.macro.d` reflects `action.getClass().getField(binding.getKey())` for dynamic macro variable bindings, matching `ActionFieldSchema` keys in `gui/macro/editor/DataUtil.java`.
- **Rule:** Never rename public fields on any `MacroAction` subclass (they are already readable schema keys).

## 8. String-Referenced Metadata & Checks
Keep these files in lockstep with any class/package rename:
- `src/main/resources/fabric.mod.json` (`AutismClientMod`, `AutismModMenuIntegration`)
- `src/main/resources/autism.mixins.json` (`autismclient.mixin.*`, `AutismMixinPlugin`)
- `src/main/resources/META-INF/services/autismclient.util.mm.guardian.Guardian` (frozen)
- `src/main/resources/assets/autismclient/ai/source-contracts.json`
- `tools/checks/*Check.java` (44 test files — many use package-private access!)

## 9. Mandatory Verification Gate After Every Batch
Every deobfuscation batch MUST pass all 4 gates before proceeding:
1. `./gradle.sh clean assemble` (0 errors; verifies untouched `.java` files compile cleanly and `ReverseRemapJar` succeeds)
2. `bash tools/checks/run.sh` (all 44 regression checks PASS)
3. `SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh` (all 3 Mixin audit profiles PASS)
4. Packaged-JAR ASM link audit (`BROKEN_REFS=0`) + bytecode equivalence check (`VerifyBytecodeEquiv`) + `git status` check (`0` frozen files touched)
