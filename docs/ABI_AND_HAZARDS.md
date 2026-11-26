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
  - `autismclient/modules/aD` (`PanicModeController`; referenced by `modules/al.java`)
  - `autismclient/modules/ae` (`ModuleCategory`; referenced by `modules/al.java`)
  - `autismclient/modules/am` (`RenderModuleState`; referenced by `modules/al.java`)
  - `autismclient/modules/ar` (`WorldEspRenderer`; referenced by `modules/al.java`)
  - `autismclient/util/macro/ak` (`ToggleModuleAction`, plus frozen inner enum `ak$Inner_StreamUtil`; referenced by `modules/al.java`)
  - `autismclient/util/L` (auth/session manager; referenced by `a/DataUtil`, `gui/d/JsonUtil`, `util/mm/guardian/impl/InjectionScan`, `LoginProof`)
  - `autismclient/util/RefQueue` (conservatively frozen: `/challenge` JAR-attestation helper called only by `util/L` and coupled to `autismclient/e/DataUtil`)
  - `autismclient/util/ValueSink` (keybind/mouse-button code helper; referenced by `modules/al.java`)
  - `autismclient/util/aJ` (safe `http`/`https` URL launcher; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/util/at` (input/camera active-state helper; referenced by `modules/al.java`)
  - `autismclient/util/bK` (performance profiler (`autism.perf`); referenced by `modules/al.java`)
  - `autismclient/util/bW` (proxy entry model, plus frozen inner enum `bW$Inner_StreamUtil`; referenced by `util/bX.java`)
  - `autismclient/util/bi` (HUD toast notification helper; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/util/cA` (virtual UI scaling & screen-metrics helper; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/util/cf` (runtime feature/activity bitmask tracker; referenced by `modules/al.java`)
  - `autismclient/b/DataUtil` (`AddonManager`; referenced by `modules/al.java`)
  - `autismclient/util/g/PkgCls_a` (`PacedTpController`; referenced by `modules/al.java`)
  - `autismclient/util/mm/IoWorker` (`ServerTimeAnchor`; referenced by `autismclient/a/*`)
  - `autismclient/util/mm/StateCache` (conservatively frozen: JWT public-key verifier tightly coupled to encrypted string helpers in `autismclient/e/DataUtil`)
  - `autismclient/util/mm/a/CachePool` (`LocalIdentityStore`; referenced by `autismclient/a/CachePool` & `guardian/impl/LoginProof`)
  - `autismclient/util/mm/a/DataUtil` (`LocalSecretSealer`; referenced by `autismclient/a/StreamUtil`)
  - `autismclient/gui/f/NetHelper` (`UiRect`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/PacketUtil` (`UiPrimitives`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/SyncManager` (`UiRenderContext`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/UiHelper` (`UiContextFactory`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/a/DataUtil` (`UiAssetIds`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/BufferHelper` (`CardSurfaceRenderer`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/CachePool` (`ButtonRenderer`, plus frozen inner enum `CachePool$Inner_DataUtil`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/CharEncoder` (`UiStyleTheme`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/k` (`FontDrawHelper`; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/l` (`TextTone` enum; referenced by `gui/d/JsonUtil.java`)
  - `autismclient/ai/aP` (`AiPlacementScreen`; referenced by `modules/al.java`)
- **Compile-time frozen enum constants** (directly referenced by untouched `.java` files; verified by `:compileJava`):
  - `autismclient/gui/f/b/CachePool$Inner_DataUtil` (referenced by `gui/d/JsonUtil.java`)
  - `autismclient/gui/f/b/l` (referenced by `gui/d/JsonUtil.java`)
  - `autismclient/util/bW$Inner_StreamUtil` (referenced by `util/bX.java`)
  - `autismclient/util/macro/ak$Inner_StreamUtil` (referenced by `modules/al.java`)
- Any other class/method/field directly referenced by untouched `.java` files is automatically enforced by `./gradle.sh compileJava` and must be added here if encountered.
- Runtime binary compatibility with untouched integrity bytecode and binary addons is preserved via `tools/ReverseRemapJar.java` + synced `mappings/disambig.tsv`.

## 2. Public Addon API (Frozen)
- `autismclient.api.*` (54 files) is already 100% readable and represents the external addon contract. Do not rename classes, methods, or fields in `autismclient.api.*`.

## 3. AI Subsystem (Deobfuscated in Phase 7; `worker.jar` Untouched)
- `autismclient.ai` (`1/232` obf — only ABI-pinned `aP`), `autismclient.ai.a` (`0/71` obf), and `autismclient.ai.b` (`0/34` obf) were deobfuscated in place across Phase 7 (`312` classes renamed; `308` matched verbatim to `source-contracts.json`).
- `src/main/resources/autism-ai/worker.jar` remains untouched.

## 4. Gson Serialization Lock (0 `@SerializedName` Annotations)
There are **zero** `@SerializedName` annotations in the codebase. Every field on these JSON-serialized types IS an on-disk config/cache key and **MUST NOT** be renamed without adding `@SerializedName("<original>")` + round-trip verification:
- `autismclient.util.AutismConfig` (and all nested classes: `HudElementState`, `ThemeColors`, `PluginScanCacheEntry`, `PayloadFilter`, etc.)
- `autismclient.util.AutismWaypoints` + `Waypoint`
- `autismclient.util.ServerPluginScanCache.CacheFile`
- `autismclient.util.AutismPacketPreset` (used by `util/PacketPresetManager.java`)
- `autismclient.util.AutismProfile` (used by `util/AutismProfileManager.java`)
- `autismclient.util.oresim.AutismOreSimSeedStore`
- `autismclient.modules.ScaffoldFailureRecorder` (formerly `aO`)
- `autismclient.ai.{AiSettings, AiToolSettings, AiMemoryStore.Memory, AiRegions.Region, AiCatalog.Entry}` and `autismclient.api.ai.{AiCapabilityDescriptor, AiObservation}`

## 5. `Enum.name()` & `Enum.valueOf()` Serialization
- `mappings/enum-names.tsv` stores the exact original runtime `Enum.name()` strings (`2,190` constants across `413` enums).
- Renaming an enum constant in source is **only** permitted when renaming it to its exact string in `mappings/enum-names.tsv` AND syncing its `F` row in `mappings/disambig.tsv`.
- `MacroActionType.MULTI` and inert `util/macro/MultiAction.java` remain intact for readable legacy Multi blocks (`tools/checks/LegacyMultiCheck.java`).

## 6. Java `record` Component Names
- `mappings/record-names.tsv` (`1,124` entries) is validated strictly by `ReverseRemapJar.restoreRecordNames` (throws `IllegalStateException` on component count mismatch).
- Never alter record component order/count during pure deobfuscation; `record-names.tsv` is keyed by `origName`, so in-place record class renames only update `mappings/disambig.tsv` (update `record-names.tsv` only if component names themselves change).

## 7. Internal Reflection Lock (`MacroAction` Public Fields)
- `autismclient.util.macro.MacroAction` reflects `"listenDuringPreviousAction"`.
- `autismclient.util.macro.DynamicBindingManager` reflects `action.getClass().getField(binding.getKey())` for dynamic macro variable bindings, matching `ActionFieldSchema` keys in `gui/macro/editor/ActionFieldRegistry.java`.
- **Rule:** Never rename public fields on any `MacroAction` subclass (they are already readable schema keys).

## 8. String-Referenced Metadata & Checks
Keep these files in lockstep with any class/package rename:
- `src/main/resources/fabric.mod.json` (`AutismClientMod`, `AutismModMenuIntegration`, `"accessWidener": "autism.accesswidener"`)
- `src/main/resources/autism.accesswidener` (`accessWidener v2 official` for MC 26.3 runtime)
- `src/main/resources/autism.mixins.json` (`autismclient.mixin.*`, `AutismMixinPlugin`)
- `src/main/resources/META-INF/services/autismclient.util.mm.guardian.Guardian` (frozen)
- `src/main/resources/assets/autismclient/ai/source-contracts.json`
- `tools/checks/*Check.java` (44 test files — many use package-private access!)

## 9. Mandatory Verification Chain & Adversarial Convergence Gate
See [GEMINI.md](../GEMINI.md) (Sections 3–4) for the authoritative workflow. Every deobfuscation batch MUST pass all 5 gates and independent adversarial review before pushing:
1. **Clean Build & Tool Env:** `./gradle.sh --offline clean assemble prepareToolEnv` (`0` errors; verifies untouched `.java` files compile cleanly and `ReverseRemapJar` succeeds)
2. **Bytecode & Resource Equivalence:** `VerifyBytecodeEquiv` vs pre-wave baseline JAR (`DIFFS=0`, `RES_DIFFS=0`, `ONLY_BASE=0`, `ONLY_CAND=0`, expected `EXCLUDED_IDENTICAL`)
3. **Regression Checks:** `AUTISM_TOOL_ENV_PREPARED=1 bash tools/checks/run.sh` (`44/44 PASS`)
4. **MC 26.3 Mixin Audit:** `AUTISM_TOOL_ENV_PREPARED=1 SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh` (`3/3` profiles PASS)
5. **Mapping, Ledger, Link & Boundary Integrity:** `mappings/disambig.tsv` + `docs/DEOBFUSCATION_LEDGER.tsv` validation, packaged-JAR/class ASM link audit (`BROKEN_REFS=0`), and `git diff --name-only` check (`0` excluded/frozen paths touched)
6. **Independent Adversarial Review to Convergence:** Fresh read-only reviewer inspecting all commits until convergence with `0 CRITICAL`, `0 HIGH`, and `0 MEDIUM` issues.
