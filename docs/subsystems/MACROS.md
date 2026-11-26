# Macro Engine & Editor (`autismclient.util.macro`, `autismclient.gui.macro.editor`)

AUTISM Client's macro system spans `138` files (`44,808` LOC) in `autismclient.util.macro` plus `11` files (`19,672` LOC) in `autismclient.gui.macro.editor*`, `AutismMacroEditorOverlay`, and `util/AutismMacro.java`. Phase 2 renamed all `102` non-frozen concrete `*Action` subclasses and `MacroCondition`, Phase 3A renamed `AutismMacro` and `ActionFieldRegistry`, and Phase 5A renamed all `32` non-pinned macro helpers (`1/138` obfuscated remaining — only compile-time ABI-pinned `ak.java`).

## Architecture & Hazards
- **Execution Engine:** `MacroExecutor` (`11,435` LOC) manages macro runs, variable scopes (`MacroVariableStore`, `MacroVariableContext`, `MacroVarValue`, `MacroTemplateResolver`), packet gates (`PacketGateManager`), ping spoof scheduling (`PingSpoofScheduler`), FPS overrides (`MacroFpsOverrideManager`), server tick phase tracking (`ServerTickPhaseTracker`), wait conditions (`MacroWaitRegistry`), resource locking (`MacroResourceClassifier`, `MacroResourceLock`, `RaceEligibilityChecker`), aim overrides (`MacroAimController`), and AI tool bridges (`MacroToolBridge`).
- **Action Contracts:**
  - `MacroAction` (`55` LOC) — standard built-in action interface (`execute(Minecraft)`, `toTag()`, `fromTag()`, `getType()`, `sanitizeForSharing()`).
  - `ContextMacroAction` (`autismclient.api.macro`) — context-driven async action interface implemented by `TradeAction`, `VillagerDupeAction`, and `WaitVillagerItemAction` (plus external addon actions via `MacroActionRegistry`).
  - **Capability Interfaces & Helpers:** `CaptureOutputAction`, `PacketOrderAware` (`PacketOrderMode`), `AimTargetAction`, `GuiWaitAware`, plus `InteractTimingMode`, `MacroItemTarget`, `CandidatePoolSelector`, `CustomMenuActionPlanner`, `GuiScreenMatcher`, `PatternCaptureMatcher`, `TpRoutePlanner`, `ClipCollisionHelper`, `MacroStepColorHelper`, `MacroNbtCodecHelper`, `ListenDuringPreviousSerializer`.
- **Type Enum (`MacroActionType`):** `103` enum constants (all restored to true runtime names in Phase 1). `MacroActionType.WAIT_ITEM` is a legacy alias deserialized into `WaitForSlotChangeAction` (`util/AutismMacro.java`). `MacroActionType.MULTI` deserializes into inert `MultiAction` so legacy Multi blocks stay readable without executing (`LegacyMultiCheck`). Unknown addon type IDs fall back to `MissingAddonAction` (`util/AutismMacro.java`).
- **Condition Tree (`MacroCondition`):** `839` LOC boolean expression tree (`GROUP` ALL/ANY + `LEAF` predicates for items, slots, durability, GUI, entities, health, variables, and AI tools), used by `IfAction`, `FlowAction`, and `BranchAction`.
- **Mandatory Locks (see [ABI_AND_HAZARDS.md](../ABI_AND_HAZARDS.md)):**
  - **Reflection Lock (H5):** `MacroAction` reflects `"listenDuringPreviousAction"`, and `DynamicBindingManager` reflects `action.getClass().getField(binding.getKey())` matching `ActionFieldSchema` keys in `gui/macro/editor/ActionFieldRegistry.java`. **Never rename public fields on any `MacroAction` subclass.**
  - **ABI Pin (H1):** `util/macro/ak.java` (`TOGGLE_MODULE`) and its inner enum `ak$Inner_StreamUtil` are directly referenced by untouched `modules/al.java` and remain compile-time frozen.
  - **Sharing Sanitization:** Actions with local scripts or paths (`PayloadAction`, `NbtBookAction`, `MissingAddonAction`) strip executable/path fields in `sanitizeForSharing()`.

## Built-In Macro Actions (`102` Renamed `*Action` Classes + `ak`)

### Control Flow, Timing & Meta (`18`)
| Class | `MacroActionType` | Role |
|---|---|---|
| `AssertAction` | `ASSERT` | Assert inventory/GUI/entity/connection state |
| `BranchAction` | `BRANCH` | Conditional branch |
| `CaptureValueAction` | `CAPTURE_VALUE` | Capture client-visible state into a macro variable |
| `DelayAction` | `DELAY` | Pause macro execution |
| `FinallyAction` | `FINALLY` | Cleanup block guaranteed to run on macro exit |
| `FlowAction` | `FLOW` | Conditional or unconditional jump (`Goto`) to a `LabelAction` |
| `IfAction` | `IF` | Structured `if` / `else` step block backed by `MacroCondition` |
| `LabelAction` | `LABEL` | Named jump target anchor for `FlowAction` |
| `MacroVariablesAction` | `MACRO_VARIABLES` | Set or modify macro variables |
| `MissingAddonAction` | *(none)* | Non-destructive placeholder preserving unknown addon action NBT |
| `MultiAction` | `MULTI` | Inert legacy Multi block placeholder (`LegacyMultiCheck`) |
| `RaceAction` | `RACE` | Race concurrent sub-steps / conditions |
| `RepeatAction` | `REPEAT` | Loop block |
| `ReportAction` | `REPORT` | Report/log status or variable snapshots |
| `ServerTickSyncAction` | `SERVER_TICK_SYNC` | Synchronize with server tick timing |
| `StartMacroAction` | `START_MACRO` | Start another macro |
| `StopMacroAction` | `STOP_MACRO` | Stop active macro execution |
| `TickSyncAction` | `TICK_SYNC` | Synchronize to client tick boundary |

### Inventory, Containers & Items (`17`)
| Class | `MacroActionType` | Role |
|---|---|---|
| `CloseGuiAction` | `CLOSE_GUI` | Close current container screen |
| `ContainerClickSequenceAction` | `CONTAINER_CLICK_SEQUENCE` | Execute a sequence of container slot clicks |
| `CraftAction` | `CRAFT` | Craft configured recipe/slots |
| `CustomMenuAction` | `CUSTOM_MENU` | Fill and submit custom server dialog / config menus |
| `DropAction` | `DROP` | Drop held item, inventory, or matching items/slots |
| `InventoryAction` | `INVENTORY` | General inventory manipulation |
| `InventoryAuditAction` | `INVENTORY_AUDIT` | Audit / verify inventory contents and counts |
| `ItemAction` | `ITEM` | Item click actions in GUI or inventory |
| `OpenContainerAction` | `OPEN_CONTAINER` | Open a target container block/entity |
| `PickUpAllAction` | `PICK_UP_ALL` | Pick up matching stack in container |
| `RestoreGuiAction` | `RESTORE_GUI` | Restore previously saved GUI state |
| `SaveGuiAction` | `SAVE_GUI` | Save current GUI state |
| `SelectSlotAction` | `SELECT_SLOT` | Select hotbar slot |
| `StoreItemAction` | `STORE_ITEM` | Move items into an open container |
| `SwapSlotsAction` | `SWAP_SLOTS` | Swap two inventory/container slots |
| `UseItemAction` | `USE_ITEM` | Use held item (optionally while sneaking or with GUI open) |
| `UseItemPhaseAction` | `USE_ITEM_PHASE` | Phased / held item use |

### Movement, World & Interaction (`19`)
| Class | `MacroActionType` | Role |
|---|---|---|
| `BreakAction` | `BREAK` | Break target block |
| `ClickAction` | `CLICK` | Simulate attack/use click |
| `GoToAction` | `GO_TO` | Move/walk toward target position |
| `HClipAction` | `HCLIP` | Horizontal clip through clear air |
| `InstaBreakAction` | `INSTA_BREAK` | Instant block break packet sequence |
| `InteractEntityAction` | `INTERACT_ENTITY` | Interact with a target entity |
| `JumpAction` | `JUMP` | Trigger jump input |
| `LookAtBlockAction` | `LOOK_AT_BLOCK` | Aim crosshair at a block |
| `MineAction` | `MINE` | Mine block with tool handling |
| `MoveAction` | `MOVE` | Directional movement input |
| `PacedTpAction` | `TP` | Ground-first HClip teleport controller (same engine as `%tp`) |
| `PlaceAction` | `PLACE` | Place block at target |
| `RotateAction` | `ROTATE` | Set yaw/pitch rotation |
| `SignEditAction` | `SIGN_EDIT` | Write lines to sign editor |
| `SneakAction` | `SNEAK` | Toggle/hold sneak |
| `SprintAction` | `SPRINT` | Toggle/hold sprint |
| `TradeAction` *(Context)* | `TRADE` | Execute bounded villager merchant trades |
| `VClipAction` | `VCLIP` | Vertical clip via movement packets |
| `VillagerDupeAction` *(Context)* | `VILLAGER_DUPE` | Shear raised trade items off villagers in reach |

### Packets, Network, Client & Dupes (`25`)
| Class | `MacroActionType` | Role |
|---|---|---|
| `BundleDupeV2Action` | `BUNDLE_DUPE_V2` | Bundle dupe packet sequence |
| `DelayPacketsAction` | `DELAY_PACKETS` | Buffer/delay outbound packets |
| `DesyncAction` | `DESYNC` | Trigger client/server state desync |
| `DisconnectAction` | `DISCONNECT` | Disconnect / auto-disconnect with follow-up safety guard (`KickDupeBlockingCheck`) |
| `EndPacketGateAction` | `END_PACKET_GATE` | Close an active `PacketGateAction` |
| `FakeGamemodeAction` | `FAKE_GAMEMODE` | Real (`F3+F4`) or client-only fake gamemode switch |
| `FpsAction` | `FPS` | Temporarily cap client max FPS |
| `JoinAction` | `JOIN` | Join server and continue when join timing condition is met |
| `NbtBookAction` | `NBT_BOOK` | Write NBT/book content from `<config>/autism/book-sources/` |
| `PacketAction` | `PACKET` | Construct/modify packet action |
| `PacketBurstAction` | `PACKET_BURST` | Send burst of packets |
| `PacketClickAction` | `PACKET_CLICK` | Direct container click packet |
| `PacketGateAction` | `PACKET_GATE` | Gate/intercept packets until `EndPacketGateAction` (`PacketGateCheck`) |
| `PayAction` | `PAY` | Send `/pay` commands (split or per-player) |
| `PayloadAction` | `PAYLOAD` | Send custom payload (sanitizes `javaSource` on share) |
| `RevisionSyncAction` | `REVISION_SYNC` | Sync container state revision ID |
| `RollbackAction` | `ROLLBACK` | Trigger rollback packet sequence |
| `SPingAction` | `SPING` | Temporary ping spoof latency window |
| `SendChatAction` | `SEND_CHAT` | Send chat message |
| `SendCommandPacketAction` | `SEND_COMMAND_PACKET` | Send command packet directly |
| `SendPacketAction` | `SEND_PACKET` | Send raw C2S packet |
| `SendToggleAction` | `SEND_TOGGLE` | Toggle packet send state |
| `ToolsAction` | `TOOLS` | Direct AI `game:` / `client:` tool invocation |
| `XCarryAction` | `XCARRY` | XCarry slot stash/restore (`1,677` LOC) |
| `ak` *(ABI-pinned)* | `TOGGLE_MODULE` | Enable/disable/toggle a client module |

### Wait / Event Triggers (`24`)
`WaitDurabilityAction`, `WaitEntityTargetAction`, `WaitForBlockAction`, `WaitForChatAction`, `WaitForCooldownAction`, `WaitForEntityAction`, `WaitForGuiAction`, `WaitForHealthAction`, `WaitForLanStepAction`, `WaitForMacroStepAction`, `WaitForPacketAction`, `WaitForPositionDeltaAction`, `WaitForSlotChangeAction`, `WaitForSoundAction`, `WaitForTeleportAction`, `WaitForWorldChangeAction`, `WaitFreeSlotsAction`, `WaitGamemodeChangeAction`, `WaitGuiTypeAction`, `WaitInventoryPredicateAction`, `WaitMovementAction`, `WaitPacketMatchAction`, `WaitPosAction`, `WaitVillagerItemAction` *(Context)*.
