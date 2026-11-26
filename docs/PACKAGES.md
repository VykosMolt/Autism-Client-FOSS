# 59-Package Directory (AUTISM Client 5.8-26.3)

Baseline counts exclude hard-frozen integrity paths (`e/`, `sys/`, `obf/`, and the 5 untouched individual integrity files).

## Frozen / Stable Packages
| Package | Files | LOC | Status | Description |
|---|---:|---:|---|---|
| `autismclient.a` | 7 | 2,934 | **FROZEN** | Integrity / class-hash verification |
| `autismclient.util.mm.guardian` | 2 | 59 | **FROZEN** | `Guardian` `ServiceLoader` interface |
| `autismclient.util.mm.guardian.impl` | 5 | 468 | **FROZEN** | `OfficialGuardian`, `AutismJarAttest`, `InjectionScan`, `LoginProof` |
| `autismclient.api` (+ 6 subpackages) | 60 | 3,665 | **FROZEN (Public API)** | Stable unobfuscated addon API (`module`, `macro`, `custommenu`, `ai`, `event`, `hud`) |

## Already Readable Non-AI Packages
| Package | Files | LOC | Description |
|---|---:|---:|---|
| `autismclient` | 2 | 373 | `AutismClientMod`, `AutismClientAddon` |
| `autismclient.mixin` (+ 6 subpackages) | 253 | 14,422 | Mixins, accessors (`accessor`), anti-fingerprint (`security`), compat (`lithium`, `indigo`, `via`, `guardian`) |
| `autismclient.aggressivepvp` | 18 | 4,738 | Aggressive PvP subsystem (Crystal/AutoTotem/MoveFix/LookMath) |
| `autismclient.compat263` | 7 | 1,443 | MC 26.3 compatibility shims |
| `autismclient.render` | 1 | 274 | `AutismRenderTypes` |

## Obfuscated Non-AI Packages (Deobfuscation Targets)
| Current Package | Files (Obf/Tot) | LOC | Role | Planned Target Package |
|---|---:|---:|---|---|
| `autismclient.c` | 2 / 2 | 18 | Mixin duck interfaces (`DisconnectedScreen`, external screen buttons) | `autismclient.mixin.duck` |
| `autismclient.b` | 1 / 1 | 895 | Addon loader & registry (`[Addons]`) | `autismclient.addons` |
| `autismclient.commands` | 3 / 5 | 1,010 | Command base, registry, dispatcher, Brigadier helpers | `autismclient.commands` |
| `autismclient.commands.a` | 8 / 8 | 776 | Custom Brigadier `ArgumentType`s | `autismclient.commands.arguments` |
| `autismclient.commands.b` | 40 / 40 | 4,247 | 39 built-in commands + `SlotClickCommandHelper` (`CharEncoder`) | `autismclient.commands.impl` |
| `autismclient.compat` | 1 / 2 | 493 | `AutismModMenuIntegration` + settings screen (`DataUtil`) | `autismclient.compat` |
| `autismclient.compat.a` | 1 / 1 | 371 | ViaFabricPlus protocol bridge | `autismclient.compat.via` |
| `autismclient.g` | 33 / 33 | 8,684 | **AutismProtector** anti-fingerprint / pack / proxy isolation | `autismclient.protector` |
| `autismclient.modules` | 148 / 150 | 94,176 | 64 top-level `Module`s, `f.java` inner modules, ESP/combat/movement helpers | `autismclient.modules` |
| `autismclient.util.macro` | 135 / 137 | 44,725 | `MacroExecutor`, `MacroAction`, `StreamGate`, 101 action implementations | `autismclient.util.macro` |
| `autismclient.gui.macro.editor` | 4 / 8 | 19,372 | `ActionEditorOverlay`, `ActionFieldSchema`, field schema registry | `autismclient.gui.macro.editor` |
| `autismclient.gui.macro.editor.a` | 3 / 3 | 297 | Macro live-capture UI helpers | `autismclient.gui.macro.editor.capture` |
| `autismclient.gui` | 3 / 3 | 511 | Theme-applying overlay, custom inventory screen, custom loading overlay | `autismclient.gui` |
| `autismclient.gui.c` | 2 / 2 | 1,031 | Profile / server library browser & server icon cache | `autismclient.gui.profiles` |
| `autismclient.gui.d` | 30 / 30 | 18,710 | Full-screen GUI screens (`KeyHolder` subclasses) | `autismclient.gui.screens` |
| `autismclient.gui.e` | 10 / 10 | 2,553 | Custom UI icon & dynamic texture loaders | `autismclient.gui.assets` |
| `autismclient.gui.f` (+ `.a/.b/.c/.d`) | 107 / 107 | 17,638 | Custom UI toolkit (theme, widgets, layout, scrollbars, typography) | `autismclient.gui.ui.*` |
| `autismclient.util.oresim` | 7 / 8 | 1,841 | OreSim engine & seed store | `autismclient.util.oresim` |
| `autismclient.util.j.a` | 5 / 5 | 1,120 | OreSim `WorldGenLevel` region simulator | `autismclient.util.oresim.worldgen` |
| `autismclient.util.a` | 8 / 8 | 1,639 | Clicker / CPS pattern engine (`Normal`, `Jitter`, `Butterfly`, `Scroll`) | `autismclient.util.clicker` |
| `autismclient.util.b` | 4 / 4 | 536 | Built-in `CustomMenuAdapter`s (`minecraft:dialog`, configuration) | `autismclient.util.custommenu` |
| `autismclient.util.c` | 3 / 3 | 1,323 | Animated GIF decoder & dynamic tickable textures | `autismclient.util.gif` |
| `autismclient.util.d` | 2 / 2 | 1,081 | Macro / LAN sync binary framing | `autismclient.util.sync` |
| `autismclient.util.e` | 3 / 3 | 716 | Safe block/chunk/stop-condition evaluator | `autismclient.util.safety` |
| `autismclient.util.f` | 4 / 4 | 407 | AutoLogin / AuthMe detector & handler | `autismclient.util.autologin` |
| `autismclient.util.g` | 1 / 1 | 1,748 | Direct TP / travel controller (`PkgCls_a`) | `autismclient.util.travel` |
| `autismclient.util.h` | 4 / 4 | 785 | Villager auto-trade & enchanted book matcher | `autismclient.util.villager` |
| `autismclient.util.i` | 1 / 1 | 1,061 | Villager dupe driver | `autismclient.util.villagerdupe` |
| `autismclient.util.mm` + `.a` | 7 / 7 | 1,969 | Local prefs/identity store, `autism-seal.key`, DPAPI, crypto (`util.mm.a.StreamUtil` ABI-locked!) | `autismclient.util.storage` |
| `autismclient.util` | 222 / 256 | 116,268 | Overlays, config, waypoints, packet logger/inspector, aim/input/inventory/perf utils | `autismclient.util` |

## Deferred AI Packages (Untouched For Now)
| Current Package | Files (Obf/Tot) | LOC | Original Name in `source-contracts.json` |
|---|---:|---:|---|
| `autismclient.ai` | 220 / 232 | 150,429 | `autismclient.ai` (`249` contract entries) |
| `autismclient.ai.a` | 59 / 71 | 56,247 | `autismclient.ai.engine` (`61` contract entries) |
| `autismclient.ai.b` | 34 / 34 | 48,924 | `autismclient.ai.print` (`34` contract entries) |
