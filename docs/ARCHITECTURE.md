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
   - `AutismClientMod` (`ClientModInitializer`): initializes config/stores, AutismProtector (`autismclient.protector`), modules, commands, overlays, resource reload prewarmers, and client tick hooks.
   - `AutismClientAddon`: shared `LOG`, data folder (`config/autism`), and mod constants.
2. **Mixin Layer (`autismclient.mixin.*`, 254 files)**
   - Configured by `src/main/resources/autism.mixins.json` (`251` client mixins + `AutismMixinPlugin`) and `src/main/resources/autism.accesswidener` (`accessWidener v2 official`, wired in `fabric.mod.json`).
   - Sub-layers: `174` core MC hook mixins (+ `AutismMixinPlugin`), `accessor.*` (`36`), `security.*` (`34` anti-fingerprint mixins), `via.*` (`3`), `lithium.*` (`2`), `duck.*` (`2`: `DisconnectedScreenDuck`, `ExternalButtonScreenDuck`), `indigo.*` (`1`), `guardian.*` (`1`).
3. **Module System (`autismclient.modules`, `autismclient.aggressivepvp`)**
   - `Module` base class & settings schema (`autismclient.api.module.*`).
   - `62` top-level `*Module` subclasses (`KillAuraModule`, `ClutchModule`, `AnchorAuraModule`, `TriggerBotModule`, etc.) + `Module` + `AiCheckpointHud` + `80` named helpers (`ModuleRuntimeManager`, `BlinkPacketManager`, `AntiBotTracker`, `FreecamController`, `NoRenderState`, `NameTagsRenderer`, `ClutchFallPhysics`, `ClutchBlockPlanner`, etc.) + `6` ABI-pinned classes (`f`, `as`, `aD`, `ae`, `am`, `ar`) + `aggressivepvp/*` (`18` files).
4. **Macro Engine & Editor (`autismclient.util.macro`, `autismclient.gui.macro.editor.*`)**
   - `MacroExecutor` runs sequences of `MacroAction` / `ContextMacroAction` implementations (`103` `MacroActionType` enum constants, `102` concrete `*Action.java` subclasses + `MacroExecutor.java` + `MacroAction.java` + `MacroCondition.java` + `32` named runtime/variable/routing helpers + ABI-pinned `ak.java` + addon actions via `MacroActionRegistry`).
   - Serialized as NBT (`util/AutismMacro.java`), dynamically bound via reflection on public `MacroAction` fields (`DynamicBindingManager`), edited in `AutismMacroEditorOverlay` + `ActionEditorOverlay` (`ActionFieldSchema` in `gui/macro/editor/ActionFieldRegistry.java`, plus live-capture helpers in `gui/macro/editor/capture`).
5. **Commands (`autismclient.commands`, `commands.arguments`, `commands.impl`)**
   - Brigadier-based client command dispatcher (`CommandManager`, `CommandSuggestions`, `RegistrySuggestions`), `8` custom `*ArgumentType`s (`commands.arguments`), and `39` built-in `*Command` classes + `SlotClickCommandHelper` (`commands.impl`).
6. **UI, Overlays & Rendering (`autismclient.gui.*`, `autismclient.render`, `util/Autism*Overlay`)**
   - Custom virtual-scaled (`cA`) screen framework (`VirtualScaledScreen`), `30/30` named `gui.screens` screens/editors/widgets (`LoginPasswordScreen`, `JoinMacroScreen`, `AutismTitleScreen`, `NbtTextEditorScreen`, etc.), profile library (`gui.profiles`), SDF/MSDF vector/text engine (`gui.sdf`, `10/10`), ModMenu settings screen (`compat.AutismSettingsScreen`), reusable widget/theme/layout toolkit (`gui.ui.*`, `97` named classes + `10` ABI-pinned anchors in `gui.f*`), dynamic GIF loaders (`util.gif`), and `AutismRenderTypes` ([GUI.md](./subsystems/GUI.md)).
7. **AutismProtector Privacy Layer (`autismclient.protector`, `mixin.security`)**
   - `33` named classes in `autismclient.protector` + `34` `mixin.security.*` hooks providing anti-fingerprint isolation for brand/channels, resource packs, HTTP/proxy scoping, known packs, translations/keybinds, and registry compatibility ([PROTECTOR.md](./subsystems/PROTECTOR.md)).
8. **Utilities & Feature Engines (`autismclient.util.*`)**
   - **Core `autismclient.util` (`256` non-excluded files; `211` renamed in Phase 6A–6D, only `11` ABI-pinned/attestation remaining — [UTIL.md](./subsystems/UTIL.md)):** runtime session state (`AutismSessionState`), packet/payload inspection & wire capture (`PacketInspectorEngine`, `PacketTypeRegistry`, `PacketSchemaRegistry`, `PacketWireCaptureStore`, `PayloadChannelFilterManager`, `PayloadFieldCodec`, `PayloadJsonCodec`), combat/aim/rotation/input (`SilentRotationManager`, `CombatAimEngine`, `RotationStreamPlanner`, `MovementInputFixer`, `ExplosionDamageCalculator`, `BacktrackTrailHistory`), HUD/overlays/rendering/EndPortal (`AutismHudManager`, `OverlayManager`, `DraggableOverlayWindow`, `ThemePaletteManager`, `ThemeTextureRecolorer`, `StrongholdTriangulator`), persistence & macros (`AutismConfig`, `AutismWaypoints`, `AutismProfileManager`, `PacketPresetManager`, `ProxyListManager`, `MacroLibraryManager`, `LanSyncManager`, `JoinMacroController`), crafting & world helpers (`AutoCraftingEngine`, `BlockPlacementPlanner`, `GridWalkNavigator`, `AntiVanishRosterTracker`).
   - **Specialized subpackages (deobfuscated in Phase 3A & consolidated in Phase 8A):** OreSim (`util.oresim`, `util.oresim.worldgen`), clicker (`util.clicker`), custom menus (`util.custommenu`), GIF (`util.gif`), LAN sync packets (`util.sync`), Leaf Dupe (`util.leafdupe`), AutoLogin (`util.autologin`), villager trading/duping (`util.villager`, `util.villagerdupe`), storage (`util.mm.LocalPrefsStore`, `util.mm.a.WindowsDpapiBridge`, plus ABI-pinned crypto/sealing classes in `util.mm*`), plus ABI-pinned travel (`util.g.PkgCls_a`) and ViaFabricPlus bridge (`compat.via.ViaFabricPlusBridge`).
9. **Public Addon API (`autismclient.api.*`, `autismclient.b`)**
   - `54` stable unobfuscated API classes (`3,531` LOC) covering modules, settings, macros, HUD, events, custom menus, and AI extensions, loaded by ABI-pinned `autismclient.b.DataUtil`.
10. **AI Assistant (`autismclient.ai`, `ai.engine`, `ai.print`, `autism-ai/worker.jar`)**
    - Autonomous agent runtime (`AiService`, `AiTools`, `AiCatalog`, `AiBuilder`, `AiSpeedrun`, `AiNetherRun`), A* pathfinding/parkour engine (`ai.engine`: `AiPathfinder`, `AiGoto`, `AiBlockMine`, `AiJumpSolver`), Schematic Printer v2 (`ai.print`: `PrintCore`, `PrintAccess`, `PrintFlight`, `34/34`), and Rhino JS worker (`worker.jar`, untouched) — `336/337` named in Phase 7 and consolidated in Phase 8B (`1` ABI-pinned: `ai/aP.java`; [AI.md](./subsystems/AI.md)).

## Deobfuscation Pipeline & Progress

All source renames and package moves are executed mechanically via JDK 25's compiler AST (`com.sun.source.util.JavacTask` + `javax.lang.model`) while preserving 100% binary runtime ABI compatibility through `tools/ReverseRemapJar.java` and `mappings/disambig.tsv`:

- **`tools/deobf/AstBatchRenamer.java`** — type-resolved AST batch renamer and cross-package mover covering `src/main/java` and `tools/checks/*Check.java` (enum constants `E`, top-level renames/cross-package moves `C` with automatic split-package import synthesis & empty-dir cleanup, nested member class renames `N` including compact record constructors, and member renames: fields `F`, methods `M` with full override-family & arity-colliding overload-merge guards, and record components `R` with backing-field/accessor/canonical-ctor sync, plus `Trees.getScope` lexical shadowing guards), enforcing all frozen/integrity/Gson/MacroAction boundaries ([ABI_AND_HAZARDS.md](./ABI_AND_HAZARDS.md)) and syncing `mappings/disambig.tsv` atomically.
- **`tools/deobf/VerifyBytecodeEquiv.java`** — strict class-by-class and resource equivalence verifier between baseline and candidate packaged JARs (validating classfile version, generic signatures, exceptions, `InnerClasses`, `NestHost`/`NestMembers`, `PermittedSubclasses`, deterministic recursive annotations + `annotationDefault`, `RecordComponents`, byte-identical resources, SHA-256 identity on all 50 excluded classes, globally injective 1-to-1 bijection of javac synthetic `$SwitchMap$` fields, and strict 1-to-1 per-class bijection of javac private synthetic `lambda$<enclosing>$N` methods derived strictly from enclosing-method `invokedynamic`/callsite order).
- **Completed Phases:**
  - **Phase 1 (`44b8fe9`):** `1,336` non-AI, non-frozen enum constants restored to exact runtime names from `mappings/enum-names.tsv` (bit-for-bit SHA-256 identical packaged JAR).
  - **Phase 2 (`df79bd7`):** `222` Rosetta Stone non-AI classes renamed in place (`40` in `commands/b`, `62` in `modules`, `103` in `util/macro`, `17` in `gui/d`), 100% aligned with `source-contracts.json` (`4,074/4,074` classes + `94/94` resources semantically bytecode-identical).
  - **Phase 3A (`24eb317`):** `60` self-contained non-AI subsystem classes renamed in place (`c/`, `commands/`, `commands/a/`, `compat/`, `compat/a/`, `util/oresim/`, `util/j/a/`, `util/a..f/`, `util/h/`, `util/i/`, `util/mm*`, plus `AutismMacro` and `ActionFieldRegistry`), alongside 6 newly confirmed ABI pins in `AstBatchRenamer.FROZEN_SYMBOL_EXACT`.
  - **Phase 3B (`d81bac9`):** All `33/33` AutismProtector classes in `autismclient/g/` renamed in place (`4,074/4,074` classes + `94/94` resources semantically bytecode-identical vs pre-Phase-3 baseline `c19e1c4`).
  - **Phase 5A (`5fc7d1d`):** All `32` non-pinned helper classes in `autismclient/util/macro/` renamed in place (`MacroActionType`, `MacroWaitRegistry`, `DynamicBindingManager`, `MacroTemplateResolver`, `PacketGateManager`, `PingSpoofScheduler`, `TpRoutePlanner`, etc.), leaving only ABI-pinned `ak.java`.
  - **Phase 5B (`8526c70`):** All `80` non-pinned helper classes in `autismclient/modules/` renamed in place (`ModuleRuntimeManager`, `BlinkPacketManager`, `AntiBotTracker`, `FreecamController`, `NoRenderState`, `ClutchFallPhysics`, `ClutchBlockPlanner`, `NameTagsRenderer`, etc.), plus `4` newly confirmed ABI pins (`aD`, `ae`, `am`, `ar`); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical vs pre-Phase-5 baseline `f1348f0`.
  - **Phase 6A–6D (`7438285`, `7618bf1`, `4ae40ff`, `3a9c544`):** All `211` non-pinned classes in `autismclient/util/` renamed in place across 4 verified batches (`6A: 53` packet/payload/persistence, `6B: 59` combat/rotation/aim/input, `6C: 52` overlay/HUD/theme/render/EndPortal, `6D: 47` inventory/world/macro/session helpers), alongside `10` newly confirmed ABI-pinned/attestation entries in `AstBatchRenamer.FROZEN_SYMBOL_EXACT` (`L`, `RefQueue`, `ValueSink`, `aJ`, `at`, `bK`, `bW`, `bi`, `cA`, `cf`); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical vs pre-Phase-6 baseline `c1ea800`.
  - **Phase 4 Integrated (`9453081`, replayed from `deobf/gui-stack`):** All `131` non-pinned GUI classes (`34` in `gui`, `gui.c`, `gui.d`, `gui.e`, `gui.macro.editor*` + `97` UI toolkit classes in `gui.f.*`) + `10` GUI ABI pins (`gui/d/JsonUtil.java`).
  - **Phase 7 Integrated (`6ca4ba3`, replayed from `deobf/ai-stack`):** All `312` non-pinned AI classes (`34` in `ai.b`, `59` in `ai.a`, `219` in `ai`; `308` exact `source-contracts.json` matches) + `1` AI ABI pin (`ai/aP`), completing **100% of renameable top-level classes** across the codebase (`1,081` top-level classes + `1,336` enum constants deobfuscated; `36/1,520` ABI-pinned/attestation remaining; cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical vs pre-merge baseline `3eb36a0`).
  - **Phase 8A–8C (`2281c5c`, `248dab3`, `feba456`):** Atomic package consolidation of all `365` non-pinned classes across `24` single-letter obfuscated packages (`8A: 118` classes across `14` core/commands/protector/compat/util packages; `8B: 105` AI classes `ai.a` → `ai.engine` & `ai.b` → `ai.print` + `3` engine checks; `8C: 142` GUI classes across `8` packages into `gui.profiles`, `gui.screens`, `gui.sdf`, `gui.macro.editor.capture`, `gui.ui.*`, leaving only ABI-pinned/frozen anchors in `gui.d`/`gui.f*`); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical vs pre-Phase-8 baseline `fb2eb05`.
  - **Phase 9A–9C (`54e7008`, `ce68cfa`, `02947e5`):** Restored all `586` remaining AI enum constants across `102` AI enums (`9A`, completing `100%` [`1,922/1,922`] of non-frozen enum constants in `mappings/enum-names.tsv`) and deobfuscated `398` nested member `Inner_*` types (`9B: 177` high-fanout shared-hub types across `40` hubs; `9C: 221` nested types across the top `10` monoliths + synced reflection in `PluginScanFilterCheck`); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical vs pre-Phase-9 baseline `0414ed2`.
  - **Phase 10A–10B (`83adf9c`, `f70ece2`, `677f4ab`, `43564c6`, `e2c21a7a`):** Extended `AstBatchRenamer` with `F`/`M`/`R` member renaming + overload-merge, interface-aware `MacroAction`, and `Trees.getScope` lexical shadowing guards, and `VerifyBytecodeEquiv` with strict per-class `lambda$<enclosing>$N` bijection; deobfuscated `1,120` high-fanout members (`300` fields, `792` methods, `28` record components; `15,399` AST-verified token edits including `55` audit-cleanup edits) across `36` shared hubs (`10A: 439` members / `5,807` edits across `17` core/util/GUI hubs + `DraggableOverlayWindow` override; `10B: 681` members / `9,537` edits across `19` AI/macro/protector/module hubs including `20` check edits); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical (`DIFFS=0`, `EXCLUDED_IDENTICAL=50`, `LAMBDA_BIJECTIONS=59`) vs pre-Phase-10 baseline `4c859f8`.
  - **Phase 10C–10D (`af95773`, `5b98a2b`, `a91331c`):** Deobfuscated `2,495` additional high-fanout members (`818` fields, `1,620` methods, `57` record components; `19,395` AST-verified token edits including `83` audit-cleanup edits across `17` corrected member names) across `42` shared hubs (`74` owner classes): `10C` (`af95773`: `1,359` members — `430` fields, `872` methods, `57` record components; `9,805` AST edits including `20` check edits, plus `9` synced check reflection strings across `17` core/util/macro/GUI hubs: `AutismSessionState`, `MacroExecutor`, `MacroVariableContext`, `MacroVarValue`, `MacroTemplateResolver`, `ModuleRuntimeManager`, `BlinkPacketManager`, `NoRenderState`, `SilentRotationManager`, `ServerRotationTracker`, `InventorySlotArbiter`, `PacketTypeRegistry`, `OverlayScreenController`, `UiColorPalette`, `GuiIconAtlas`, `ThemedTextInputBox`, `ListenDuringPreviousSerializer`) and `10D` (`5b98a2b`: `1,136` members — `388` fields, `748` methods; `9,507` AST edits including `1` check edit across `25` AI/print/UI hubs: `LayoutContext`, `LayoutViewport`, `ScrollableListView`, `ScrollOffsetController`, `PanelSurfaceHelper`, `ListRowRenderer`, `PrintControls`, `PrintEnv`/`LivePrintEnv`, `PrintView`, `PrintNav`, `AiPlacer`, `AiRestock`, `AiWorldIdentity`, `AiPreparationReceipt`, `AiRunTrace`, `AiWorldScan`, `AiContainerTransactions`, `AiThreats`, `AiGuardLayer`, `AiUiKit`, `AiGoto`, `AiService`, plus remaining `AiPhysics`/`AiAim` constants); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical (`DIFFS=0`, `EXCLUDED_IDENTICAL=50`, `LAMBDA_BIJECTIONS=93`) vs pre-Phase-10C baseline `e2c21a7a`.
  - **Phase 10E–10F (`23d6dae`, `489a0c3`):** Deobfuscated `2,330` additional high-fanout members (`735` fields, `1,466` methods, `129` record components; `13,022` AST-verified token edits across `240` unique Java files) across `28` shared hubs (`118` owner classes across `44` top-level files): `10E` (`23d6dae`: `1,298` members — `333` fields, `851` methods, `114` record components; `7,576` AST edits across `158` Java files including `2` edits + `1` synced reflection string in `PayloadIdentityCheck` across `20` UI widget/layout and core util/payload hubs: `TextInputWidget`, `DropdownWidget`, `DropdownMenu`, `OverlayActionButton`, `ScrollListLayout`, `CollapsibleWindowNode`, `LayoutTreeHost`, `LayoutNode`, `LayoutContainer`, `EdgeInsets`, `PanelLayoutMath`, `HoverTransitionCache`, `FocusableInput`, `UiLayerComponent`, `BlockPlacementPlanner`, `AutismPayloadSupport`, `AutoCraftingEngine`, `PayloadChannelFilterManager`, `PayloadFieldCodec`, `ExplosionDamageCalculator`, plus propagated `LayoutNode`/`LayoutContainer`/`UiLayerComponent` override targets) and `10F` (`489a0c3`: `1,032` members — `402` fields, `615` methods, `15` record components; `5,446` AST edits across `96` Java files including `1` edit in `ChestMirrorCheck` across `8` AI engine and physics hubs: `AiPathfinder`, `AiBuilder`, `AiJumpSolver`, `AiBlockDigger`, `AiBreakBlock`, `AiNavDebug`, `AiViewState`, `ClutchFallPhysics`); cumulative `4,074/4,074` classes + `94/94` resources semantically bytecode-identical (`DIFFS=0`, `EXCLUDED_IDENTICAL=50`, `LAMBDA_BIJECTIONS=103`) vs pre-Phase-10E baseline `a91331c`.

## Related Docs
- [Engineering & Verification Workflow](../GEMINI.md)
- [ABI & Silent-Breakage Hazards](./ABI_AND_HAZARDS.md)
- [61-Package Directory](./PACKAGES.md)
- [Deobfuscation Ledger](./DEOBFUSCATION_LEDGER.tsv)
- Subsystem Guides: [Commands](./subsystems/COMMANDS.md) · [Modules](./subsystems/MODULES.md) · [Macros](./subsystems/MACROS.md) · [GUI & Screens](./subsystems/GUI.md) · [AutismProtector](./subsystems/PROTECTOR.md) · [Core Utilities](./subsystems/UTIL.md) · [AI Assistant](./subsystems/AI.md)
