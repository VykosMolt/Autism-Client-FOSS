# 61-Package Directory (AUTISM Client 5.8-26.3)

Counts reflect the post-Phase-8 working tree (`1,484 / 1,520` non-excluded files named; `365` classes consolidated out of `24` single-letter packages into semantic packages; all `36` remaining obfuscated top-level classes are compile-time ABI-pinned/attestation). The `1,520` non-excluded total (`625,298` LOC across `58` non-excluded packages) excludes hard-frozen integrity paths (`e/`, `sys/`, `obf/`, `a/`, `util/mm/guardian/`, and the `5` untouched individual integrity files; `a/` and `util/mm/guardian*` are listed separately in the Frozen section below, totaling `61` documented packages).

## Frozen / Stable Packages
| Package | Files | LOC | Status | Description |
|---|---:|---:|---|---|
| `autismclient.a` | 7 | 2,927 | **FROZEN** | Integrity / class-hash verification |
| `autismclient.util.mm.guardian` | 2 | 57 | **FROZEN** | `Guardian` `ServiceLoader` interface |
| `autismclient.util.mm.guardian.impl` | 5 | 463 | **FROZEN** | `OfficialGuardian`, `AutismJarAttest`, `InjectionScan`, `LoginProof` |
| `autismclient.api` (+ 6 subpackages) | 54 | 3,531 | **FROZEN (Public API)** | Stable unobfuscated addon API (`module`, `macro`, `custommenu`, `ai`, `event`, `hud`) |

## Core, Mixin & Already Readable Packages
| Package | Files | LOC | Description |
|---|---:|---:|---|
| `autismclient` | 2 | 371 | `AutismClientMod`, `AutismClientAddon` |
| `autismclient.mixin` (+ 7 subpackages) | 254 | 14,179 | Mixins (`251` client entries in `autism.mixins.json` + `AutismMixinPlugin`), `accessor` (`36`), `security` (`34`), `via` (`3`), `lithium` (`2`), `duck` (`2`, consolidated from `c/` in Phase 8A), `indigo` (`1`), `guardian` (`1`) |
| `autismclient.aggressivepvp` | 18 | 4,720 | Aggressive PvP subsystem (Crystal/AutoTotem/MoveFix/LookMath) |
| `autismclient.compat263` | 7 | 1,405 | MC 26.3 compatibility shims |
| `autismclient.render` | 1 | 273 | `AutismRenderTypes` |

## Deobfuscated & Consolidated Non-AI Packages (Post-Phase-8)
| Package | Files (Obf/Tot) | LOC | Role |
|---|---:|---:|---|
| `autismclient.commands` | **0 / 5** | 1,005 | **Phase 3A:** `Command`, `AutismCommandSource`, `CommandManager`, `CommandSuggestions`, `RegistrySuggestions` |
| `autismclient.commands.arguments` | **0 / 8** | 768 | **Phase 3A + 8A** (from `commands.a`): All 8 custom Brigadier `*ArgumentType` classes |
| `autismclient.commands.impl` | **0 / 40** | 4,207 | **Phase 2 + 8A** (from `commands.b`): `39` built-in `*Command`s + `SlotClickCommandHelper` |
| `autismclient.compat` | **0 / 2** | 491 | **Phase 3A:** `AutismModMenuIntegration`, `AutismSettingsScreen` |
| `autismclient.compat.via` | **0 / 1** | 370 | **Phase 3A + 8A** (from `compat.a`): `ViaFabricPlusBridge` |
| `autismclient.protector` | **0 / 33** | 8,651 | **Phase 3B + 8A** (from `g`): All 33 **AutismProtector** anti-fingerprint / pack / proxy / compat classes |
| `autismclient.modules` | **6 / 150** | 93,930 | **Phase 2 + 5B:** `62` `*Module`s + `Module` + `AiCheckpointHud` + `80` helpers renamed; only `6` **ABI-pinned** classes remain (`f`, `as`, `aD`, `ae`, `am`, `ar`) |
| `autismclient.util.macro` | **1 / 138** | 44,808 | **Phase 2 + 5A:** `102` `*Action`s + `MacroAction` + `MacroCondition` + `MacroExecutor` + `32` helpers renamed; only **ABI-pinned** `ak` remains |
| `autismclient.gui` | **0 / 3** | 508 | **Phase 4A:** `ThemeApplyProgressOverlay`, `AutismGhostInventoryScreen`, `AutismLoadingOverlay` |
| `autismclient.gui.macro.editor` | **0 / 8** | 19,378 | **Phase 3A + 4A:** `ActionEditorOverlay`, `ActionFieldRegistry`, `ActionFieldSchema`, `ToolsActionSchemaBuilder`, `FieldDef`, `FieldType`, `CaptureMode`, `RaceStepSelectorOverlay` |
| `autismclient.gui.macro.editor.capture` | **0 / 3** | 294 | **Phase 4A + 8C** (from `gui.macro.editor.a`): `EditorListRowHelper`, `CaptureButtonRenderer`, `ActiveCaptureSession` |
| `autismclient.gui.profiles` | **0 / 2** | 1,029 | **Phase 4A + 8C** (from `gui.c`): `ProfilesLibraryView`, `ProfileServerIconCache` |
| `autismclient.gui.screens` | **0 / 30** | 18,714 | **Phase 2 + 4A + 8C** (from `gui.d`): `17` named `*Screen` GUIs + `13` base/menu/editor/widget classes (hard-frozen `gui/d/JsonUtil.java` remains untouched in `gui/d/`) |
| `autismclient.gui.sdf` | **0 / 10** | 2,543 | **Phase 4A + 8C** (from `gui.e`): SDF/MSDF vector shape, icon, font atlas & subpixel scissor renderer |
| `autismclient.gui.ui` | **0 / 20** | 1,282 | **Phase 4B + 8C** (from `gui.f`): Core UI theme, metrics, animation, clip/alpha stacks & layer routing |
| `autismclient.gui.ui.widgets` | **0 / 48** | 7,199 | **Phase 4B + 8C** (from `gui.f.b`): Immediate-mode widget renderers, scrollbars, popups, sliders, inputs, toasts & frames |
| `autismclient.gui.ui.layout` | **0 / 24** | 3,376 | **Phase 4B + 8C** (from `gui.f.c`): Declarative layout tree nodes, containers & list/card helpers |
| `autismclient.gui.ui.menu` | **0 / 5** | 4,425 | **Phase 4B + 8C** (from `gui.f.d`): Module-menu & settings controllers |
| `autismclient.util` | **11 / 256** | 116,228 | **Phase 3A + 6A–6D:** `211` non-pinned utility classes + `AutismMacro` renamed; only `11` **ABI-pinned/attestation** classes remain (`L`, `Q`, `RefQueue`, `ValueSink`, `aJ`, `at`, `bK`, `bW`, `bi`, `cA`, `cf`) |
| `autismclient.util.oresim` | **0 / 8** | 1,833 | **Phase 3A:** `AutismOreSimSeedStore`, `OreSimEngine`, `OreSimSeedParser`, `OreBlockCatalog`, `OreGhostModelCache`, `OreSimWorkerPool`, `NearestOreHeap`, `NearestChunkSelector` |
| `autismclient.util.oresim.worldgen` | **0 / 5** | 1,115 | **Phase 3A + 8A** (from `util.j.a`): `OreSimRegionGenerator`, `SyntheticWorldGenLevel`, `VanillaWorldGenBootstrap`, `WorldGenThreadScope`, `PaleMossDecoratorShim` |
| `autismclient.util.clicker` | **0 / 8** | 1,631 | **Phase 3A + 8A** (from `util.a`): Clicker / CPS pattern engine (`ClickPatternEngine`, `ClickProfile`, `ClickTechnique`, `ClickTimingMath`, `DoubleClickSampler`, `KeyClickOutput`, `ReactionDelayModel`, `SwingTimingModel`) |
| `autismclient.util.custommenu` | **0 / 4** | 532 | **Phase 3A + 8A** (from `util.b`): `CustomMenuTracker`, `CustomMenuStateStore`, `DialogScreenBridge`, `VanillaDialogMenuAdapter` |
| `autismclient.util.gif` | **0 / 3** | 1,320 | **Phase 3A + 8A** (from `util.c`): `GifImageDecoder`, `GifTextureManager`, `GifRenderContext` |
| `autismclient.util.sync` | **0 / 2** | 1,079 | **Phase 3A + 8A** (from `util.d`): `LanSyncPacket`, `LanSyncPacketType` |
| `autismclient.util.leafdupe` | **0 / 3** | 713 | **Phase 3A + 8A** (from `util.e`): `LeafDupePacketTracker`, `LeafDupeRateLimiter`, `LeafDupeValidator` |
| `autismclient.util.autologin` | **0 / 4** | 403 | **Phase 3A + 8A** (from `util.f`): `AutoLoginBackend`, `AutoLoginDriver`, `AuthPromptParser`, `AutoLoginTimings` |
| `autismclient.util.villager` | **0 / 4** | 781 | **Phase 3A + 8A** (from `util.h`): `VillagerTradeSession`, `TradeOfferFilter`, `MerchantInteractHelper`, `BlockChangeAckTracker` |
| `autismclient.util.villagerdupe` | **0 / 1** | 1,060 | **Phase 3A + 8A** (from `util.i`): `VillagerDupeDriver` |

## ABI-Pinned Single-Letter & Storage Packages (Untouched ABI Anchors)
| Package | Files (Obf/Tot) | LOC | Role |
|---|---:|---:|---|
| `autismclient.b` | 1 / 1 | 894 | Addon loader & registry (`DataUtil` — **ABI-pinned**, referenced by `modules/al.java`) |
| `autismclient.util.g` | 1 / 1 | 1,747 | Direct TP / travel controller (`PkgCls_a` — **ABI-pinned**, referenced by `modules/al.java`) |
| `autismclient.util.mm` + `.a` | 5 / 7 | 1,962 | **Phase 3A:** `LocalPrefsStore`, `WindowsDpapiBridge`; remaining `5` (`IoWorker`, `StateCache`, `a/CachePool`, `a/DataUtil`, `a/StreamUtil`) are **ABI-pinned/frozen** |
| `autismclient.gui.f` (+ `.a/.b`) | 10 / 10 | 1,284 | **ABI-pinned** UI primitives referenced by untouched `gui/d/JsonUtil.java` (`NetHelper`, `PacketUtil`, `SyncManager`, `UiHelper`, `a/DataUtil`, `b/{BufferHelper,CachePool,CharEncoder,k,l}`) |

## Deobfuscated & Consolidated AI Packages (Phase 7 + Phase 8B Complete)
| Package | Files (Obf/Tot) | LOC | Role / Match to `source-contracts.json` |
|---|---:|---:|---|
| `autismclient.ai` | **1 / 232** | 150,193 | **Phase 7C–7E:** `219` renamed + `12` pre-existing readable; only **ABI-pinned** `aP` (`AiPlacementScreen`) remains (`249` contract entries) |
| `autismclient.ai.engine` | **0 / 71** | 56,176 | **Phase 7B + 8B** (from `ai.a`): `59` renamed + `12` pre-existing readable (`61` contract entries) |
| `autismclient.ai.print` | **0 / 34** | 48,890 | **Phase 7A + 8B** (from `ai.b`): `34/34` exact matches (`34` contract entries) |
