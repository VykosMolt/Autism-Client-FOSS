# AUTISM Client 5.8 (MC 26.3) — Engineering & Verification Workflow (`GEMINI.md`)

**Core Principle:** Slow, meticulous, and empirically verified wins the race. Never guess behavior, never trust assumptions without checking bytecode/runtime/tests, and never leave silent breakages behind.

---

## 1. Absolute Excluded & Frozen Boundaries

**NEVER read, search, list, inspect, dump, rename, move, or modify:**
- `precompiled/integrity-classes.jar`, `stubs/integrity-stubs.jar`, `reference/integrity/`
- `src/main/java/autismclient/e/**`, `src/main/java/autismclient/sys/**`, `src/main/java/autismclient/obf/**`, `src/main/java/autismclient/a/**`, `src/main/java/autismclient/util/mm/guardian/**`
- `src/main/java/autismclient/modules/al.java`, `src/main/java/autismclient/util/AutismLiteVariant.java`, `src/main/java/autismclient/util/bX.java`, `src/main/java/autismclient/gui/a/DataUtil.java`, `src/main/java/autismclient/gui/d/JsonUtil.java`

**Additional Hard Locks (see `docs/ABI_AND_HAZARDS.md`):**
- **Public Addon API (`autismclient.api.*`):** Frozen external contract — do not rename classes, methods, or fields.
- **Compile-Time ABI-Pinned Classes & Enums:** Keep `AstBatchRenamer.FROZEN_SYMBOL_EXACT` / `FROZEN_ENUMS` and `docs/ABI_AND_HAZARDS.md` in exact 1-to-1 lockstep. Their own names cannot change (untouched frozen `.java` files reference them), though references *inside* them to other renamed classes are still updated.
- **Gson Serialization Lock:** Zero `@SerializedName` annotations exist. Never rename fields on Gson-serialized DTOs (`AutismConfig`, `AutismWaypoints`, `ServerPluginScanCache.CacheFile`, `AutismPacketPreset`, `AutismProfile`, `AutismOreSimSeedStore`, `ScaffoldFailureRecorder`, AI stores) without `@SerializedName` + round-trip verification.
- **Enum & Record Restoration Locks:** Source enum constants may only be renamed to exact runtime names in `mappings/enum-names.tsv` (synced in `mappings/disambig.tsv`). Never reorder/drop Java `record` components (`mappings/record-names.tsv` is strictly checked by `tools/ReverseRemapJar.java`).
- **Reflection Locks:** Never rename public fields on `MacroAction` subclasses (`DynamicBindingManager` reflects `getField(key)` matching `ActionFieldRegistry`). Always grep `src/main/java` (non-excluded), `src/main/resources/`, and `tools/checks/*Check.java` for `Class.forName`, `getDeclaredField`, `getDeclaredMethod`, `getResourceAsStream`, and Mixin target strings before renaming any class.

---

## 2. AST-Verified Deobfuscation & Change Protocol

1. **Read Project Context First:** Check `docs/ARCHITECTURE.md`, `docs/ABI_AND_HAZARDS.md`, `docs/PACKAGES.md`, and the relevant `docs/subsystems/*.md` before planning edits.
2. **Capture a Clean Baseline JAR:** Before starting a wave of renames, build cleanly (`./gradle.sh --offline clean assemble prepareToolEnv`) and copy `build/libs/AUTISM-Client-5.8-26.3.jar` to your scratch directory as `baseline.jar`.
3. **Inspect Every Target From Source:** Read every class's declaration, superclass/interfaces, constructors, string literals, fields, and callers before naming it. Prefer authoritative upstream names from `src/main/resources/assets/autismclient/ai/source-contracts.json` whenever available.
4. **Use Compiler-Resolved AST Renaming:**
   - Ensure compiled classes exist first (`./gradle.sh --offline compileJava prepareToolEnv`) — `tools/deobf/AstBatchRenamer.java` resolves frozen symbols from `build/classes/java/main` via `CHECK_CP`. Always rebuild `compileJava` after any `:clean` or failed compile before invoking `AstBatchRenamer`.
   - Apply renames via `AstBatchRenamer` (0 unmatched declarations, 0 warnings), sync any `Class.forName` / reflection strings discovered in step 3, and append every rename row to `docs/DEOBFUSCATION_LEDGER.tsv`.
5. **Fast Compile-Time ABI-Pin Discovery:**
   - When touching a package that may be referenced by an untouched frozen file, run `./gradle.sh --offline compileJava` first.
   - If errors occur strictly inside an untouched frozen file, reset cleanly (`git checkout -- . && git clean -fd src/`), add the exact referenced symbol(s) to `docs/ABI_AND_HAZARDS.md` and `AstBatchRenamer.FROZEN_SYMBOL_EXACT`, run `./gradle.sh --offline compileJava prepareToolEnv`, and re-apply cleanly without the pinned symbol(s).

---

## 3. Mandatory 5-Gate Verification Chain

Every code or deobfuscation batch **MUST** pass all 5 gates before committing:

1. **Clean Build & Packaging:**
   ```bash
   ./gradle.sh --offline clean assemble prepareToolEnv
   ```
   Must finish `BUILD SUCCESSFUL` with `0` errors (`4,073` remapped + `1` untouched Integrity + `94` resources).
2. **Bytecode & Resource Equivalence (Zero Semantic Drift):**
   ```bash
   source build/tool-env.sh
   "$JDK/java" -cp "<ast_bin>:$LOADER_CP" VerifyBytecodeEquiv <baseline.jar> build/libs/AUTISM-Client-5.8-26.3.jar
   ```
   For pure deobfuscation batches, MUST report `ONLY_BASE=0 ONLY_CAND=0 RES_DIFFS=0 DIFFS=0` and `EXCLUDED_IDENTICAL=50` (with globally injective `$SwitchMap$` bijections). For behavioral changes, verify exact intended diffs and run an ASM link audit (`BROKEN_REFS=0`) on both the packaged JAR and `build/classes/java/main`.
3. **Full Regression Suite (`44/44` Checks):**
   ```bash
   AUTISM_TOOL_ENV_PREPARED=1 bash tools/checks/run.sh
   ```
   Every check must print `PASS`.
4. **All 3 MC 26.3 Mixin Audit Profiles:**
   ```bash
   AUTISM_TOOL_ENV_PREPARED=1 SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh
   ```
   All 3 profiles must pass (Sodium beta.5, Sodium+Iris, Vanilla Indigo+Lithium).
5. **Mapping, Ledger, Docs & Boundary Integrity:**
   - Validate `mappings/disambig.tsv`: `0` malformed lines, `0` duplicate `C` keys, `0` stale pre-rename class names in `C` targets or `F`/`M` owners/descriptors.
   - Validate `docs/DEOBFUSCATION_LEDGER.tsv`: every applied row matches the live filesystem 1-to-1.
   - Confirm `git diff --name-only <base>..HEAD` touches **`0`** excluded/frozen paths.
   - Empirically re-measure all file counts/LOCs when updating docs (never estimate) and verify `0` broken relative markdown links across `README.md` and `docs/**/*.md`.

---

## 4. Adversarial Review Until Convergence (Mandatory Before Push)

Before pushing any wave of commits to `dev`:

1. **Launch an Independent Read-Only Adversarial Reviewer Subagent** across the entire unpushed commit range (`origin/dev..HEAD`), armed with strict read-only and excluded-path rules.
2. **Required Audit Scope:**
   - Independent bytecode equivalence check + side-by-side inspection of every `$SwitchMap$` bijection and any behavioral diff.
   - Independent ASM link & classfile metadata audit on **both** `build/libs/AUTISM-Client-5.8-26.3.jar` and `build/classes/java/main` (`BROKEN_REFS=0`, `InnerClasses`, `NestHost`/`NestMembers`, generic `Signature`, `BootstrapMethods`, `RecordComponents` vs `record-names.tsv`, `<clinit>` `Enum.name()` vs `enum-names.tsv`).
   - Exhaustive sweep for stale reflection strings (`Class.forName`, `getDeclaredField`, `getDeclaredMethod`, `getResourceAsStream`), Gson serialization drift, `tools/checks/` edits, `fabric.mod.json` / `autism.mixins.json` / `autism.accesswidener`, and runtime Knot verification when loader/mixin/access-widener config changes.
   - Naming accuracy & package completeness review across every renamed class.
   - Live verification of every doc count, LOC, and markdown link + confirmation of `0` excluded/frozen paths touched.
3. **Convergence Gate:**
   - **No push is permitted while any `CRITICAL`, `HIGH`, or `MEDIUM` issue remains open.**
   - Resolve every confirmed `CRITICAL`, `HIGH`, and `MEDIUM` issue (plus valid `LOW` correctness/doc precision items).
   - If any source code, resource, build script, or mapping file was modified to address review findings, **re-run the full 5-gate verification chain** AND launch **another independent adversarial review round** on the updated commits.
   - Repeat until an independent review converges cleanly with **`0 CRITICAL`, `0 HIGH`, and `0 MEDIUM` issues**.

---

## 5. Parallel Multi-Session & Git Hygiene

- **Worktree Isolation:** Two sessions MUST NEVER edit or build inside the same working directory (concurrent Gradle runs lock `.gradle/` and overwrite `build/`). Secondary sessions must create an isolated git worktree on a dedicated branch:
  ```bash
  git -C <main> worktree add -b <branch> <worktree-dir> dev
  [ -f <main>/cp26.3.txt ] && ln -s <main>/cp26.3.txt <worktree-dir>/cp26.3.txt
  ln -s <main>/precompiled <worktree-dir>/precompiled
  ln -s <main>/stubs/integrity-stubs.jar <worktree-dir>/stubs/integrity-stubs.jar
  [ -d <main>/libs ] && ln -s <main>/libs <worktree-dir>/libs
  ```
  Never symlink `.gradle/` or `build/`, and never replace `stubs/` (`stubs/compat-stubs.jar` is tracked).
- **Disjoint Ownership:** Parallel sessions must own strictly disjoint packages and preserve their `plan_*.tsv` files so branches merge or replay deterministically via `AstBatchRenamer`.
- **Clean Commits:** Keep commits focused (one per verified phase, plus a dedicated documentation sync commit). Never commit `build/`, `.gradle/`, local jars, or temporary scripts.
