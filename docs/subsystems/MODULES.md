# Modules Subsystem (`autismclient.modules`, `autismclient.aggressivepvp`)

AUTISM Client's module system comprises `150` non-excluded files (`93,930` LOC) in `autismclient.modules` plus `18` unobfuscated files (`4,720` LOC) in `autismclient.aggressivepvp`. Phase 2 renamed all `62` non-frozen top-level `*Module` subclasses to exact `source-contracts.json` names, and Phase 5B renamed all `80` non-pinned helper classes (`6/150` obfuscated remaining — all `6` are compile-time ABI-pinned by untouched `modules/al.java`).

## Architecture & ABI Boundaries
- **Base Class:** `autismclient.modules.Module` (`760` LOC) implements setting storage, enable/disable hooks (`ScopedToggleOverride`), `preMovementTick`, `onInputFrame`, and persistence integration with `AutismConfig`.
- **Categories (`autismclient.modules.ae`, ABI-pinned):** `Combat` (`ae.init_ae`), `Movement` (`ae.load_ae`), `Player` (`ae.read`), `Misc` (`ae.write`), `Render` (`ae.update`), `Dupes` (`ae.sync`), plus dynamic addon categories via `ae.init(addonId, label)`.
- **Compile-Time ABI Pins (see [ABI_AND_HAZARDS.md](../ABI_AND_HAZARDS.md)):**
  - `modules/al.java` — hard-frozen untouched registry/bootstrap class.
  - `modules/f.java` (`8,595` LOC) — compile-time ABI-pinned container holding `36` built-in inner `Module` subclasses referenced by `modules/al.java`.
  - `modules/as.java` (`845` LOC, `NameCensor` / `name-censor`) — compile-time ABI-pinned class referenced by `modules/al.java`.
  - `modules/aD.java` (`206` LOC, panic/hide controller), `modules/ae.java` (`88` LOC, categories), `modules/am.java` (`1,560` LOC, render/ESP state snapshots), `modules/ar.java` (`2,130` LOC, world ESP renderer) — compile-time ABI-pinned classes referenced by `modules/al.java`.
  - `modules/ScaffoldFailureRecorder.java` (formerly `aO.java`) — Gson-serialized record fields locked (0 `@SerializedName` annotations).
- **Aggressive PvP (`autismclient.aggressivepvp`):** `18` readable classes implementing alternate Aggressive `CrystalAura` and `AutoTotem` controllers (`Versions`, `ActionWindow`, `LookMath`, `MoveFix`), wired into `CrystalAuraModule`, `AutoTotemModule`, and `AggressiveConnectionMixin` (verified by 7 `Aggressive*Check` tests).

## Top-Level `*Module` Subclasses (`62` Phase 2 Renamed + `1` ABI-Pinned `as`)

### Combat (`13`)
| Class | ID | Name | LOC | Description |
|---|---|---|---:|---|
| `AnchorAuraModule` | `anchor-aura` | AnchorAura | 2,522 | Blows targets up with respawn anchors |
| `AntiKnockbackModule` | `anti-knockback` | AntiKnockback | 222 | Jump reset and briefly counter enemy knockback |
| `AutoArmorModule` | `auto-armor` | AutoArmor | 552 | Equips your best armor |
| `AutoTrapModule` | `auto-trap` | AutoTrap | 1,279 | Traps enemies in blocks |
| `BacktrackModule` | `backtrack` | Backtrack | 1,036 | Briefly hold a retreating target's movement updates |
| `ClutchModule` | `clutch` | Clutch | 3,255 | Saves you from knockback falls |
| `CrystalAuraModule` | `crystal-aura` | CrystalAura | 2,980 | Places and detonates end crystals |
| `DangerModule` | `danger` | Danger | 1,165 | Predict nearby projectiles/pearls; optionally return fireballs |
| `KBDisplacementModule` | `kb-displacement` | KBDisplacement | 1,326 | Direct sprint and Knockback hits toward a qualifying fall |
| `KillAuraModule` | `kill-aura` | KillAura | 4,112 | Automatically attacks configured entities |
| `TotemHitModule` | `totem-hit` | TotemHit | 226 | Borrow a knockback sword for an attack made with a totem |
| `TriggerBotModule` | `trigger-bot` | TriggerBot | 517 | Attacks configured entities when crosshair is on them (package-private) |
| `WTappingModule` | `w-tapping` | W-tapping | 128 | Briefly release forward after a landed sprint hit to reset sprint |

### Movement (`13`)
| Class | ID | Name | LOC | Description |
|---|---|---|---:|---|
| `AirJumpModule` | `air-jump` | AirJump | 87 | Jump in mid air |
| `AirStuckModule` | `air-stuck` | AirStuck | 37 | Freeze position while allowing mouse look |
| `AntiWebModule` | `anti-web` | AntiWeb | 63 | Remove cobweb slowdown or change movement in webs |
| `BoatFlyModule` | `boat-fly` | BoatFly | 58 | Lets ridden vehicles fly |
| `EntityControlModule` | `entity-control` | EntityControl | 380 | Controls selected rideable entities |
| `NoClipModule` | `no-clip` | NoClip | 122 | Move through blocks |
| `NoJumpDelayModule` | `no-jump-delay` | NoJumpDelay | 13 | Remove cooldown between grounded jumps |
| `NoPushModule` | `no-push` | NoPush | 22 | Prevent blocks or entities from pushing you |
| `NoSlowModule` | `no-slow` | NoSlow | 77 | Remove selected item and movement slowdowns |
| `PhaseModule` | `phase` | Phase | 583 | Phase through blocks |
| `SafeWalkModule` | `safe-walk` | SafeWalk | 136 | Sneaks at ledges |
| `ScaffoldModule` | `scaffold` | Scaffold | 18,595 | Places blocks beneath you |
| `TridentBoostModule` | `trident-boost` | TridentBoost | 72 | Use a Riptide trident outside water or rain |

### Player (`13`)
| Class | ID | Name | LOC | Description |
|---|---|---|---:|---|
| `AirPlaceModule` | `air-place` | AirPlace | 219 | Places blocks in air |
| `AutoFarmModule` | `auto-farm` | AutoFarm | 2,898 | Harvests and replants crops |
| `AutoLoginModule` | `auto-login` | AutoLogin | 153 | Answers server login and register gates |
| `AutoSignModule` | `auto-sign` | AutoSign | 28 | Fills signs with configured text |
| `AutoTotemModule` | `auto-totem` | AutoTotem | 1,479 | Keeps a totem in offhand |
| `AutoTradeModule` | `auto-trade` | AutoTrade | 1,012 | Trade with villagers or reroll their profession and offers |
| `BedDefenderModule` | `bed-defender` | BedDefender | 1,899 | Walls in a bed with your hardest blocks |
| `BedNukerModule` | `bed-nuker` | BedNuker | 639 | Silently mine the quickest bed opening in reach |
| `GhostBlockModule` | `ghostblock` | GhostBlock | 631 | Client-side ghost blocks (place/remove/creative arming) |
| `HandNoClipModule` | `hand-no-clip` | HandNoClip | 88 | Ignore selected blocks when aiming manual interaction |
| `InventoryTweaksModule` | `inventory-tweaks` | InventoryTweaks | 771 | Inventory sorting, protection, auto-drop, container steal/dump |
| `SurroundModule` | `surround` | Surround | 784 | Places blocks around your feet |
| `TpClickModule` | `tp-click` | TpClick | 110 | Teleport where you click |

### Render (`13`)
| Class | ID | Name | LOC | Description |
|---|---|---|---:|---|
| `AspectRatioModule` | `aspect-ratio` | AspectRatio | 40 | Change the world projection's width |
| `BedEspModule` | `bed-esp` | BedESP | 634 | Beds, cover, and quickest way in |
| `CrosshairModule` | `crosshair` | Crosshair | 201 | Custom crosshair shapes and colors |
| `EndPortalModule` | `end-portal` | EndPortal | 458 | Portal frames and Eye of Ender navigation |
| `FreeLookModule` | `free-look` | FreeLook | 102 | Look around without rotating the player |
| `GifModule` | `gif` | GIF | 305 | Custom animated images on capes, shields, and HUD (stock SDL dialog) |
| `HoleEspModule` | `hole-esp` | HoleESP | 417 | Highlights safe holes |
| `NameTagsModule` | `nametags` | Nametags | 83 | Labels above entities |
| `NoRenderModule` | `no-render` | NoRender | 287 | Disables rendering of selected overlays, HUD, world features, entities |
| `TrajectoriesModule` | `trajectories` | Trajectories | 434 | Preview flight paths and impact points |
| `ViewmodelModule` | `viewmodel` | Viewmodel | 122 | Customize first-person hand position and animations |
| `WaypointsModule` | `waypoints` | Waypoints | 98 | Save and render named destinations |
| `WorldModule` | `world` | World | 174 | World render tweaks |

### Misc (`8` + `1` ABI-Pinned)
| Class | ID | Name | LOC | Description |
|---|---|---|---:|---|
| `AntiBotModule` | `antibot` | AntiBot | 109 | Ignores fake/bot players |
| `AntiHungerModule` | `anti-hunger` | AntiHunger | 56 | Prevents hunger from decreasing |
| `AntiVanishModule` | `anti-vanish` | AntiVanish | 865 | Discovers online players outside TAB |
| `BlinkModule` | `blink` | Blink | 74 | Suspends packets to/from server, released on disable |
| `GoldenLeverModule` | `golden-lever` | GoldenLever | 108 | Renames and recolors vanilla levers |
| `PayAllModule` | `pay-all` | PayAll | 179 | Pay every listed player |
| `PingSpoofModule` | `ping-spoof` | PingSpoof | 34 | Adds latency to ping by delaying keep-alives |
| `TeamsModule` | `teams` | Teams | 342 | Friends and team detection |
| `as` *(ABI-pinned)* | `name-censor` | NameCensor | 845 | Replaces player names with aliases |

### Dupes (`2`)
| Class | ID | Name | LOC | Description |
|---|---|---|---:|---|
| `LeafDupeModule` | `leaf-dupe` | Leaf Dupe | 1,520 | Bone meals a small dripleaf and re-breaks a whitelisted block |
| `VillagerDupeModule` | `villager-dupe` | VillagerDupe | 113 | Shears trade items off villagers |

## Inner Modules in ABI-Pinned `modules/f.java` (`36` Modules)
- **Combat (`2`):** `AimAssist` (`Inner_StreamUtil`), `AutoClicker` (`Inner_CachePool`).
- **Movement (`7`):** `Flight` (`Inner_FileCache`), `InvMove` (`Inner_TableIndex`), `NoFall` (`Inner_BitSetUtil`), `Parkour` (`Inner_MethodUtil`), `Sneak` (`Inner_PathHelper`), `Speed` (`Inner_ListPool`), `Sprint` (`Inner_MapStore`).
- **Player (`8`):** `AutoFish` (`Inner_BufferHelper`), `AutoTool` (`Inner_TaskQueue`), `FastBreak` (`Inner_RenderState`), `FastUse` (`Inner_EventBus`), `InstantRebreak` (`Inner_PoolEntry`), `NoInteract` (`Inner_CharEncoder`), `OffhandInteract` (`Inner_TokenStore`), `XCarry` (`Inner_NodeWalker`).
- **Render (`12`):** `BetterTooltips` (`Inner_NetHelper`), `BlockESP` (`Inner_JsonUtil`), `ESP` (`Inner_SyncManager`), `FakeCoords` (`Inner_UiHelper`), `FrameESP` (`Inner_TextFormat`), `Freecam` (`Inner_ByteReader`), `Fullbright` (`Inner_PacketUtil`), `ItemESP` (`Inner_RingBuffer`), `SpawnerESP` (`Inner_RefQueue`), `StorageESP` (`Inner_ArrayUtil`), `Tracers` (`Inner_ClassCache`), `Xray` (`Inner_KeyHolder`).
- **Misc (`7`):** `AdminTools` (`Inner_DataUtil`), `AutoReconnect` (`Inner_ConfigStore`), `BookBot` (`Inner_StateCache`), `HUD` (`Inner_IoWorker`), `PacketCanceller` (`Inner_FieldCache`), `PanicMode` (`Inner_HashUtil`), `Spam` (`Inner_TaskTimer`).

*(Also in `autismclient.modules`: `AiCheckpointHud` [`39` LOC], `80` Phase 5B named helper classes covering runtime orchestration [`ModuleRuntimeManager`], combat/movement physics [`AntiBotTracker`, `BlinkPacketManager`, `ClutchFallPhysics`, `ClutchBlockPlanner`, `ShieldHitWindowHelper`, `KnockbackThrowPlanner`, `TerrainDropGrid`, `WTapController`], Danger/Trajectories [`IncomingProjectileScanner`, `ProjectileBallistics`, `TrajectoryWorldRenderer`, `PearlLandingEvaluator`], Bed/ESP/Crosshair rendering [`BedCoverPathSolver`, `BedMiningEstimator`, `EspChunkWorker`, `EspVoxelMeshBuilder`, `NameTagsRenderer`, `CrosshairRenderer`, `FreecamController`, `NoRenderState`, `ViewmodelState`], plus the `4` ABI-pinned shared helpers `aD`, `ae`, `am`, and `ar`.)*
