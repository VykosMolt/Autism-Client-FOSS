# 59-Package Directory (AUTISM Client 5.8-26.3)

Counts reflect the post-Phase-6 working tree and exclude hard-frozen integrity paths (`e/`, `sys/`, `obf/`, and the 5 untouched individual integrity files).

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

## Non-AI Deobfuscation Target Packages (Post-Phase-6)
| Current Package | Files (Obf/Tot) | LOC | Role | Planned Target Package |
|---|---:|---:|---|---|
| `autismclient.c` | **0 / 2** | 16 | **Phase 3A complete:** `DisconnectedScreenDuck`, `ExternalButtonScreenDuck` | `autismclient.mixin.duck` |
| `autismclient.b` | 1 / 1 | 894 | Addon loader & registry (`DataUtil` — **ABI-pinned**, referenced by `modules/al.java`) | `autismclient.addons` |
| `autismclient.commands` | **0 / 5** | 1,005 | **Phase 3A complete:** `Command`, `AutismCommandSource`, `CommandManager`, `CommandSuggestions`, `RegistrySuggestions` | `autismclient.commands` |
| `autismclient.commands.a` | **0 / 8** | 768 | **Phase 3A complete:** All 8 custom Brigadier `*ArgumentType` classes | `autismclient.commands.arguments` |
| `autismclient.commands.b` | **0 / 40** | 4,207 | **Phase 2 complete:** `39` built-in `*Command`s + `SlotClickCommandHelper` | `autismclient.commands.impl` |
| `autismclient.compat` | **0 / 2** | 491 | **Phase 3A complete:** `AutismModMenuIntegration`, `AutismSettingsScreen` | `autismclient.compat` |
| `autismclient.compat.a` | **0 / 1** | 370 | **Phase 3A complete:** `ViaFabricPlusBridge` | `autismclient.compat.via` |
| `autismclient.g` | **0 / 33** | 8,651 | **Phase 3B complete:** All 33 **AutismProtector** anti-fingerprint / pack / proxy / compat classes | `autismclient.protector` |
| `autismclient.modules` | **6 / 150** | 93,930 | **Phase 2 + 5B complete:** `62` `*Module`s + `Module` + `AiCheckpointHud` + `80` helpers renamed; only `6` **ABI-pinned** classes remain (`f`, `as`, `aD`, `ae`, `am`, `ar`) | `autismclient.modules` |
| `autismclient.util.macro` | **1 / 138** | 44,808 | **Phase 2 + 5A complete:** `102` `*Action`s + `MacroAction` + `MacroCondition` + `MacroExecutor` + `32` helpers renamed; only **ABI-pinned** `ak` remains | `autismclient.util.macro` |
| `autismclient.gui.macro.editor` | 3 / 8 | 19,378 | **Phase 3A:** `ActionFieldRegistry` + `ActionEditorOverlay`, `ActionFieldSchema`, `FieldDef`, `RaceStepSelectorOverlay`; `3` helpers remain | `autismclient.gui.macro.editor` |
| `autismclient.gui.macro.editor.a` | 3 / 3 | 294 | Macro live-capture UI helpers | `autismclient.gui.macro.editor.capture` |
| `autismclient.gui` | 3 / 3 | 508 | Theme-applying overlay, custom inventory screen, custom loading overlay | `autismclient.gui` |
| `autismclient.gui.c` | 2 / 2 | 1,029 | Profile / server library browser & server icon cache | `autismclient.gui.profiles` |
| `autismclient.gui.d` | 13 / 30 | 18,714 | **Phase 2:** `17` named `*Screen` GUIs; remaining `13` base/dynamic-title screens & helpers | `autismclient.gui.screens` |
| `autismclient.gui.e` | 10 / 10 | 2,543 | Custom UI icon & dynamic texture loaders | `autismclient.gui.assets` |
| `autismclient.gui.f` (+ `.a/.b/.c/.d`) | 107 / 107 | 17,531 | Custom UI toolkit (theme, widgets, layout, scrollbars, typography) | `autismclient.gui.ui.*` |
| `autismclient.util.oresim` | **0 / 8** | 1,833 | **Phase 3A complete:** `AutismOreSimSeedStore`, `OreSimEngine`, `OreSimSeedParser`, `OreBlockCatalog`, `OreGhostModelCache`, `OreSimWorkerPool`, `NearestOreHeap`, `NearestChunkSelector` | `autismclient.util.oresim` |
| `autismclient.util.j.a` | **0 / 5** | 1,115 | **Phase 3A complete:** `OreSimRegionGenerator`, `SyntheticWorldGenLevel`, `VanillaWorldGenBootstrap`, `WorldGenThreadScope`, `PaleMossDecoratorShim` | `autismclient.util.oresim.worldgen` |
| `autismclient.util.a` | **0 / 8** | 1,631 | **Phase 3A complete:** Clicker / CPS pattern engine (`ClickPatternEngine`, `ClickProfile`, `ClickTechnique`, `ClickTimingMath`, `DoubleClickSampler`, `KeyClickOutput`, `ReactionDelayModel`, `SwingTimingModel`) | `autismclient.util.clicker` |
| `autismclient.util.b` | **0 / 4** | 532 | **Phase 3A complete:** `CustomMenuTracker`, `CustomMenuStateStore`, `DialogScreenBridge`, `VanillaDialogMenuAdapter` | `autismclient.util.custommenu` |
| `autismclient.util.c` | **0 / 3** | 1,320 | **Phase 3A complete:** `GifImageDecoder`, `GifTextureManager`, `GifRenderContext` | `autismclient.util.gif` |
| `autismclient.util.d` | **0 / 2** | 1,079 | **Phase 3A complete:** `LanSyncPacket`, `LanSyncPacketType` | `autismclient.util.sync` |
| `autismclient.util.e` | **0 / 3** | 713 | **Phase 3A complete:** Leaf Dupe engine (`LeafDupePacketTracker`, `LeafDupeRateLimiter`, `LeafDupeValidator`) | `autismclient.util.leafdupe` |
| `autismclient.util.f` | **0 / 4** | 403 | **Phase 3A complete:** AutoLogin engine (`AutoLoginBackend`, `AutoLoginDriver`, `AuthPromptParser`, `AutoLoginTimings`) | `autismclient.util.autologin` |
| `autismclient.util.g` | 1 / 1 | 1,747 | Direct TP / travel controller (`PkgCls_a` — **ABI-pinned**, referenced by `modules/al.java`) | `autismclient.util.travel` |
| `autismclient.util.h` | **0 / 4** | 781 | **Phase 3A complete:** Villager trade engine (`VillagerTradeSession`, `TradeOfferFilter`, `MerchantInteractHelper`, `BlockChangeAckTracker`) | `autismclient.util.villager` |
| `autismclient.util.i` | **0 / 1** | 1,060 | **Phase 3A complete:** `VillagerDupeDriver` | `autismclient.util.villagerdupe` |
| `autismclient.util.mm` + `.a` | 5 / 7 | 1,962 | **Phase 3A:** `LocalPrefsStore`, `WindowsDpapiBridge`; remaining `5` (`IoWorker`, `StateCache`, `a/CachePool`, `a/DataUtil`, `a/StreamUtil`) are **ABI-pinned/frozen** | `autismclient.util.storage` |
| `autismclient.util` | **11 / 256** | 116,228 | **Phase 3A + 6A–6D complete:** `211` non-pinned utility classes + `AutismMacro` renamed (packet/payload, combat/rotation, HUD/overlays/EndPortal, crafting/placement/LAN sync/session); only `11` **ABI-pinned/attestation** classes remain (`L`, `Q`, `RefQueue`, `ValueSink`, `aJ`, `at`, `bK`, `bW`, `bi`, `cA`, `cf`) | `autismclient.util` |

## Deferred AI Packages (Untouched For Now)
| Current Package | Files (Obf/Tot) | LOC | Original Name in `source-contracts.json` |
|---|---:|---:|---|
| `autismclient.ai` | 220 / 232 | 150,193 | `autismclient.ai` (`249` contract entries) |
| `autismclient.ai.a` | 59 / 71 | 56,176 | `autismclient.ai.engine` (`61` contract entries) |
| `autismclient.ai.b` | 34 / 34 | 48,890 | `autismclient.ai.print` (`34` contract entries) |
