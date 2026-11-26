# 59-Package Directory (AUTISM Client 5.8-26.3)

Counts reflect the post-Phase-2 working tree and exclude hard-frozen integrity paths (`e/`, `sys/`, `obf/`, and the 5 untouched individual integrity files).

## Frozen / Stable Packages
| Package | Files | LOC | Status | Description |
|---|---:|---:|---|---|
| `autismclient.a` | 7 | 2,927 | **FROZEN** | Integrity / class-hash verification |
| `autismclient.util.mm.guardian` | 2 | 57 | **FROZEN** | `Guardian` `ServiceLoader` interface |
| `autismclient.util.mm.guardian.impl` | 5 | 463 | **FROZEN** | `OfficialGuardian`, `AutismJarAttest`, `InjectionScan`, `LoginProof` |
| `autismclient.api` (+ 6 subpackages) | 54 | 3,531 | **FROZEN (Public API)** | Stable unobfuscated addon API (`module`, `macro`, `custommenu`, `ai`, `event`, `hud`) |

## Already Readable Non-AI Packages
| Package | Files | LOC | Description |
|---|---:|---:|---|
| `autismclient` | 2 | 371 | `AutismClientMod`, `AutismClientAddon` |
| `autismclient.mixin` (+ 6 subpackages) | 252 | 14,163 | Mixins (`251` client entries in `autism.mixins.json` + `AutismMixinPlugin`), `accessor`, `security`, `lithium`, `indigo`, `via`, `guardian` |
| `autismclient.aggressivepvp` | 18 | 4,720 | Aggressive PvP subsystem (Crystal/AutoTotem/MoveFix/LookMath) |
| `autismclient.compat263` | 7 | 1,405 | MC 26.3 compatibility shims |
| `autismclient.render` | 1 | 273 | `AutismRenderTypes` |

## Non-AI Deobfuscation Target Packages (Post-Phase-2)
| Current Package | Files (Obf/Tot) | LOC | Role | Planned Target Package |
|---|---:|---:|---|---|
| `autismclient.c` | 2 / 2 | 16 | Mixin duck interfaces (`DisconnectedScreen`, external screen buttons) | `autismclient.mixin.duck` |
| `autismclient.b` | 1 / 1 | 894 | Addon loader & registry (`[Addons]`) | `autismclient.addons` |
| `autismclient.commands` | 3 / 5 | 1,005 | Command base, registry, dispatcher, Brigadier helpers | `autismclient.commands` |
| `autismclient.commands.a` | 8 / 8 | 768 | Custom Brigadier `ArgumentType`s | `autismclient.commands.arguments` |
| `autismclient.commands.b` | 0 / 40 | 4,207 | **Phase 2 complete:** `39` built-in `*Command`s + `SlotClickCommandHelper` | `autismclient.commands.impl` |
| `autismclient.compat` | 1 / 2 | 491 | `AutismModMenuIntegration` + settings screen (`DataUtil`) | `autismclient.compat` |
| `autismclient.compat.a` | 1 / 1 | 370 | ViaFabricPlus protocol bridge | `autismclient.compat.via` |
| `autismclient.g` | 33 / 33 | 8,651 | **AutismProtector** anti-fingerprint / pack / proxy isolation | `autismclient.protector` |
| `autismclient.modules` | 86 / 150 | 93,930 | **Phase 2:** `62` `*Module` subclasses + `Module` + `AiCheckpointHud`; remaining `86` helpers + ABI-pinned `f`/`as` | `autismclient.modules` |
| `autismclient.util.macro` | 33 / 138 | 44,808 | **Phase 2:** `102` concrete `*Action`s + `MacroAction` + `MacroCondition` + `MacroExecutor`; remaining `33` helpers + ABI-pinned `ak` | `autismclient.util.macro` |
| `autismclient.gui.macro.editor` | 4 / 8 | 19,378 | `ActionEditorOverlay`, `ActionFieldSchema`, field schema registry | `autismclient.gui.macro.editor` |
| `autismclient.gui.macro.editor.a` | 3 / 3 | 294 | Macro live-capture UI helpers | `autismclient.gui.macro.editor.capture` |
| `autismclient.gui` | 3 / 3 | 508 | Theme-applying overlay, custom inventory screen, custom loading overlay | `autismclient.gui` |
| `autismclient.gui.c` | 2 / 2 | 1,029 | Profile / server library browser & server icon cache | `autismclient.gui.profiles` |
| `autismclient.gui.d` | 13 / 30 | 18,714 | **Phase 2:** `17` named `*Screen` GUIs; remaining `13` base/dynamic-title screens & helpers | `autismclient.gui.screens` |
| `autismclient.gui.e` | 10 / 10 | 2,543 | Custom UI icon & dynamic texture loaders | `autismclient.gui.assets` |
| `autismclient.gui.f` (+ `.a/.b/.c/.d`) | 107 / 107 | 17,531 | Custom UI toolkit (theme, widgets, layout, scrollbars, typography) | `autismclient.gui.ui.*` |
| `autismclient.util.oresim` | 7 / 8 | 1,833 | OreSim engine & seed store | `autismclient.util.oresim` |
| `autismclient.util.j.a` | 5 / 5 | 1,115 | OreSim `WorldGenLevel` region simulator | `autismclient.util.oresim.worldgen` |
| `autismclient.util.a` | 8 / 8 | 1,631 | Clicker / CPS pattern engine (`Normal`, `Jitter`, `Butterfly`, `Scroll`) | `autismclient.util.clicker` |
| `autismclient.util.b` | 4 / 4 | 532 | Built-in `CustomMenuAdapter`s (`minecraft:dialog`, configuration) | `autismclient.util.custommenu` |
| `autismclient.util.c` | 3 / 3 | 1,320 | Animated GIF decoder & dynamic tickable textures | `autismclient.util.gif` |
| `autismclient.util.d` | 2 / 2 | 1,079 | Macro / LAN sync binary framing | `autismclient.util.sync` |
| `autismclient.util.e` | 3 / 3 | 713 | Safe block/chunk/stop-condition evaluator | `autismclient.util.safety` |
| `autismclient.util.f` | 4 / 4 | 403 | AutoLogin / AuthMe detector & handler | `autismclient.util.autologin` |
| `autismclient.util.g` | 1 / 1 | 1,747 | Direct TP / travel controller (`PkgCls_a`) | `autismclient.util.travel` |
| `autismclient.util.h` | 4 / 4 | 781 | Villager auto-trade & enchanted book matcher | `autismclient.util.villager` |
| `autismclient.util.i` | 1 / 1 | 1,060 | Villager dupe driver | `autismclient.util.villagerdupe` |
| `autismclient.util.mm` + `.a` | 7 / 7 | 1,962 | Local prefs/identity store, `autism-seal.key`, DPAPI, crypto (`util.mm.a.StreamUtil` ABI-locked!) | `autismclient.util.storage` |
| `autismclient.util` | 222 / 256 | 116,228 | Overlays, config, waypoints, packet logger/inspector, aim/input/inventory/perf utils | `autismclient.util` |

## Deferred AI Packages (Untouched For Now)
| Current Package | Files (Obf/Tot) | LOC | Original Name in `source-contracts.json` |
|---|---:|---:|---|
| `autismclient.ai` | 220 / 232 | 150,193 | `autismclient.ai` (`249` contract entries) |
| `autismclient.ai.a` | 59 / 71 | 56,176 | `autismclient.ai.engine` (`61` contract entries) |
| `autismclient.ai.b` | 34 / 34 | 48,890 | `autismclient.ai.print` (`34` contract entries) |
