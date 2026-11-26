# AUTISM Client 5.8 (MC 26.3) — Architecture Overview

AUTISM Client is a Fabric client mod targeting **Minecraft 26.3**, **Java 25**, **Fabric Loader ≥ 0.19.3**, and **Fabric API ≥ 0.165.0+26.3** (with optional compatibility hooks for Sodium `0.9.3-beta.*`, Iris `1.11.7`, Lithium `0.26.3`, ModMenu `21.0.1`, and ViaFabricPlus).

## Build & Packaging Architecture

```
src/main/java/ (Java 25, disambiguated / progressively deobfuscated)
   │
   ├─► :compileJava (JDK 25 --release 25)
   │      ├─ stubs/compat-stubs.jar
   │      ├─ stubs/integrity-stubs.jar
   │      ├─ src/main/resources/autism-ai/worker.jar
   │      └─ externalJars (MC 26.3, LWJGL 3.4.3, Fabric Loader + MixinExtras, Fabric API, Sodium, Iris, Lithium, ModMenu)
   │
   ├─► build/classes/java/main
   │      └─► tools/checks/run.sh (43 regression checks)
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
# 1. Clean build & package
MC_MODS_DIR="$HOME/.local/share/PrismLauncher/instances/Windfall/minecraft/mods" \
IRIS_MODS_DIR="$HOME/.local/share/PrismLauncher/instances/Autism 26.3/minecraft/mods" \
./gradle.sh clean assemble

# 2. Regression checks (43 checks against build/classes/java/main)
bash tools/checks/run.sh

# 3. Mixin audits (Sodium beta.5, Sodium+Iris, Vanilla Indigo+Lithium)
MC_MODS_DIR="$HOME/.local/share/PrismLauncher/instances/Windfall/minecraft/mods" \
IRIS_MODS_DIR="$HOME/.local/share/PrismLauncher/instances/Autism 26.3/minecraft/mods" \
SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh
```

## High-Level Subsystem Map

1. **Bootstrap & Lifecycle (`autismclient`)**
   - `AutismClientMod` (`ClientModInitializer`): initializes config/stores, AutismProtector (`autismclient.g`), modules, commands, overlays, resource reload prewarmers, and client tick hooks.
   - `AutismClientAddon`: shared `LOG`, data folder (`config/autism`), and mod constants.
2. **Mixin Layer (`autismclient.mixin.*`, 254 classes)**
   - Configured by `src/main/resources/autism.mixins.json` (`AutismMixinPlugin`) and `autism.accesswidener`.
   - Sub-layers: core MC hooks, `accessor.*` (36 accessors/invokers), `security.*` (34 anti-fingerprint mixins), `lithium.*`, `indigo.*`, `via.*`, `guardian.*`, plus duck interfaces in `autismclient.c`.
3. **Module System (`autismclient.modules`, `autismclient.aggressivepvp`)**
   - `Module` base class & settings schema (`autismclient.api.module.*`).
   - 64 top-level module classes + inner module definitions in `modules/f.java` + ESP/render state snapshots (`modules/am.java`) + combat/movement drivers (`aa.java` KillAura, `j.java` Clutch, `BufferHelper.java` AnchorAura, `aggressivepvp/*`).
4. **Macro Engine & Editor (`autismclient.util.macro`, `autismclient.gui.macro.editor`)**
   - `MacroExecutor` runs sequences of `MacroAction` implementations (101 built-in action types keyed by `StreamGate` enum + addon actions via `MacroActionRegistry`).
   - Serialized as NBT (`aM.java`), dynamically bound via reflection on public `MacroAction` fields (`util/macro/d.java`), edited in `AutismMacroEditorOverlay` + `ActionEditorOverlay` (`ActionFieldSchema` in `gui/macro/editor/DataUtil.java`).
5. **Commands (`autismclient.commands`, `commands.a`, `commands.b`)**
   - Brigadier-based client command dispatcher (`commands.DataUtil`), 8 custom `ArgumentType`s (`commands.a`), and 39 built-in commands (`commands.b`).
6. **UI, Overlays & Rendering (`autismclient.gui.*`, `autismclient.render`, `util/Autism*Overlay`)**
   - Custom virtual-scaled (`cA`) screen framework (`gui.d.KeyHolder`), 19+ full-screen GUIs (`gui.d`), reusable widget/theme/typography toolkit (`gui.f.*`, 107 classes), dynamic texture/GIF loaders (`gui.e`, `util.c`), and `AutismRenderTypes`.
7. **AutismProtector Privacy Layer (`autismclient.g`, `mixin.security`)**
   - Anti-fingerprint isolation for brand/channels, resource packs, known packs, connections, and proxies.
8. **Utilities & Feature Engines (`autismclient.util.*`)**
   - Persistence (`AutismConfig`, `AutismWaypoints`, `ServerPluginScanCache`, `util.mm.*` local sealing/DPAPI).
   - Specialized engines: OreSim (`util.oresim`, `util.j.a`), clicker (`util.a`), custom menus (`util.b`), LAN/macro sync (`util.d`), AutoLogin (`util.f`), travel (`util.g`), villager trading/duping (`util.h`, `util.i`).
9. **Public Addon API (`autismclient.api.*`, `autismclient.b`)**
   - 60 stable unobfuscated API classes covering modules, settings, macros, HUD, events, custom menus, and AI extensions, loaded by `autismclient.b.DataUtil`.
10. **AI Assistant (`autismclient.ai`, `ai.a`, `ai.b`, `autism-ai/worker.jar`)**
    - Self-contained autonomous agent, catalog (`AiCatalog`), memory/regions, engine (`ai.a`), schematic printer (`ai.b`), and Rhino JS worker (`worker.jar`). Currently frozen during non-AI deobfuscation.

## Related Docs
- [ABI & Silent-Breakage Hazards](./ABI_AND_HAZARDS.md)
- [59-Package Directory](./PACKAGES.md)
- [Deobfuscation Ledger](./DEOBFUSCATION_LEDGER.tsv)
