# AUTISM Client 5.8 (MC 26.3) — Architecture Overview

AUTISM Client is a Fabric client mod targeting **Minecraft 26.3**, **Java 25**, **Fabric Loader ≥ 0.19.5**, and **Fabric API ≥ 0.165.0+26.3** (with optional compatibility hooks for Sodium `0.9.3-beta.*`, Iris `1.11.7`, Lithium `0.26.3`, ModMenu `21.0.1`, and ViaFabricPlus).

## Build & Packaging Architecture

```
src/main/java/ (Java 25, disambiguated / progressively deobfuscated)
   │
   ├─► :applyAccessWidener
   │      └─ Widens local MC client JAR (if cp26.3.txt present) or downloaded MC 26.3 client.jar
   │         against src/main/resources/autism.accesswidener -> .gradle/minecraft/26.3/client-aw.jar
   │
   ├─► :compileJava (JDK 25 --release 25)
   │      ├─ stubs/compat-stubs.jar
   │      ├─ stubs/integrity-stubs.jar
   │      ├─ src/main/resources/autism-ai/worker.jar
   │      └─ compileLocalCpJars (if cp26.3.txt) or minecraftWidenedJar + minecraftRuntimeLibs
   │         (MC 26.3, LWJGL 3.4.3, Fabric Loader + MixinExtras, Fabric API, Sodium, Iris, Lithium, ModMenu)
   │
   ├─► build/classes/java/main
   │      └─► tools/checks/run.sh (44 regression checks)
   │
   └─► :packageModJar (tools/ReverseRemapJar.java)
          ├─ Reads mappings/disambig.tsv (new -> orig)
          ├─ Restores runtime Enum.name() strings from mappings/enum-names.tsv
          ├─ Restores record ObjectMethods names from mappings/record-names.tsv
          ├─ Overlays untouched integrity classes from precompiled/integrity-classes.jar
          └─ Packages build/libs/AUTISM-Client-5.8-26.3.jar
                 └─► tools/verify-26.3.sh (3 full Mixin audit profiles)
```

### Verification Commands
```bash
# 1. Clean build, package, and prepare tool environment
./gradle.sh clean assemble prepareToolEnv

# 2. Regression checks (44 checks against build/classes/java/main)
AUTISM_TOOL_ENV_PREPARED=1 bash tools/checks/run.sh

# 3. Mixin audits (Sodium beta.5, Sodium+Iris, Vanilla Indigo+Lithium — audit mods resolved via Gradle/Maven/Modrinth)
AUTISM_TOOL_ENV_PREPARED=1 SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh
```

## High-Level Subsystem Map

1. **Bootstrap & Lifecycle (`autismclient`)**
   - `AutismClientMod` (`ClientModInitializer`): initializes config/stores, AutismProtector (`autismclient.g`), modules, commands, overlays, resource reload prewarmers, and client tick hooks.
   - `AutismClientAddon`: shared `LOG`, data folder (`config/autism`), and mod constants.
2. **Mixin Layer (`autismclient.mixin.*`, 252 files)**
   - Configured by `src/main/resources/autism.mixins.json` (`251` client mixins + `AutismMixinPlugin`) and `autism.accesswidener`.
   - Sub-layers: core MC hooks (`175`), `accessor.*` (`36`), `security.*` (`34` anti-fingerprint mixins), `lithium.*` (`2`), `indigo.*` (`1`), `via.*` (`3`), `guardian.*` (`1`), plus duck interfaces in `autismclient.c`.
3. **Module System (`autismclient.modules`, `autismclient.aggressivepvp`)**
   - `Module` base class & settings schema (`autismclient.api.module.*`).
   - `62` top-level `*Module` subclasses (`KillAuraModule`, `ClutchModule`, `AnchorAuraModule`, `TriggerBotModule`, etc.) + `Module` + `AiCheckpointHud` + ABI-pinned inner module container `modules/f.java` + ESP/render state snapshots (`modules/am.java`) + `aggressivepvp/*` (`18` files).
4. **Macro Engine & Editor (`autismclient.util.macro`, `autismclient.gui.macro.editor`)**
   - `MacroExecutor` runs sequences of `MacroAction` / `ContextMacroAction` implementations (`103` `StreamGate` enum constants, `102` concrete `*Action.java` subclasses + `MacroAction.java` + `MacroCondition.java` + ABI-pinned `ak.java` + addon actions via `MacroActionRegistry`).
   - Serialized as NBT (`util/aM.java`), dynamically bound via reflection on public `MacroAction` fields (`util/macro/d.java`), edited in `AutismMacroEditorOverlay` + `ActionEditorOverlay` (`ActionFieldSchema` in `gui/macro/editor/DataUtil.java`).
5. **Commands (`autismclient.commands`, `commands.a`, `commands.b`)**
   - Brigadier-based client command dispatcher (`commands.DataUtil`), `8` custom `ArgumentType`s (`commands.a`), and `39` built-in `*Command` classes + `SlotClickCommandHelper` (`commands.b`).
6. **UI, Overlays & Rendering (`autismclient.gui.*`, `autismclient.render`, `util/Autism*Overlay`)**
   - Custom virtual-scaled (`cA`) screen framework (`gui.d.KeyHolder`), `17` named `*Screen` GUIs (`LoginPasswordScreen`, `JoinMacroScreen`, `ForceOpPreviewScreen`, etc.) + `13` base/dynamic-title helpers in `gui.d`, reusable widget/theme/typography toolkit (`gui.f.*`, `107` files), dynamic texture/GIF loaders (`gui.e`, `util.c`), and `AutismRenderTypes`.
7. **AutismProtector Privacy Layer (`autismclient.g`, `mixin.security`)**
   - Anti-fingerprint isolation for brand/channels, resource packs, known packs, connections, and proxies.
8. **Utilities & Feature Engines (`autismclient.util.*`)**
   - Persistence (`AutismConfig`, `AutismWaypoints`, `ServerPluginScanCache`, `util.mm.*` local sealing/DPAPI).
   - Specialized engines: OreSim (`util.oresim`, `util.j.a`), clicker (`util.a`), custom menus (`util.b`), LAN/macro sync (`util.d`), AutoLogin (`util.f`), travel (`util.g`), villager trading/duping (`util.h`, `util.i`).
9. **Public Addon API (`autismclient.api.*`, `autismclient.b`)**
   - `54` stable unobfuscated API classes (`3,531` LOC) covering modules, settings, macros, HUD, events, custom menus, and AI extensions, loaded by `autismclient.b.DataUtil`.
10. **AI Assistant (`autismclient.ai`, `ai.a`, `ai.b`, `autism-ai/worker.jar`)**
    - Self-contained autonomous agent, catalog (`AiCatalog`), memory/regions, engine (`ai.a`), schematic printer (`ai.b`), and Rhino JS worker (`worker.jar`). Currently frozen during non-AI deobfuscation.

## Deobfuscation Pipeline & Progress

All source renames are executed mechanically via JDK 25's compiler AST (`com.sun.source.util.JavacTask` + `javax.lang.model`) while preserving 100% binary runtime ABI compatibility through `tools/ReverseRemapJar.java` and `mappings/disambig.tsv`:

- **`tools/deobf/AstBatchRenamer.java`** — type-resolved AST batch renamer covering `src/main/java` and `tools/checks/*Check.java`, enforcing all frozen/integrity boundaries ([ABI_AND_HAZARDS.md](./ABI_AND_HAZARDS.md)) and syncing `mappings/disambig.tsv` atomically.
- **`tools/deobf/VerifyBytecodeEquiv.java`** — strict class-by-class and resource equivalence verifier between baseline and candidate packaged JARs (validating classfile version, generic signatures, exceptions, `InnerClasses`, `NestHost`/`NestMembers`, `PermittedSubclasses`, deterministic recursive annotations + `annotationDefault`, `RecordComponents`, byte-identical resources, SHA-256 identity on all 50 excluded classes, and globally injective 1-to-1 bijection of javac synthetic `$SwitchMap$` fields).
- **Completed Phases:**
  - **Phase 1 (`44b8fe9`):** `1,336` non-AI, non-frozen enum constants restored to exact runtime names from `mappings/enum-names.tsv` (bit-for-bit SHA-256 identical packaged JAR).
  - **Phase 2 (`df79bd7`):** `222` Rosetta Stone non-AI classes renamed in place (`40` in `commands/b`, `62` in `modules`, `103` in `util/macro`, `17` in `gui/d`), 100% aligned with `source-contracts.json` (`4,074/4,074` classes + `94/94` resources semantically bytecode-identical).

## Related Docs
- [ABI & Silent-Breakage Hazards](./ABI_AND_HAZARDS.md)
- [59-Package Directory](./PACKAGES.md)
- [Deobfuscation Ledger](./DEOBFUSCATION_LEDGER.tsv)
- Subsystem Guides: [Commands](./subsystems/COMMANDS.md) · [Modules](./subsystems/MODULES.md) · [Macros](./subsystems/MACROS.md) · [GUI & Screens](./subsystems/GUI.md)
