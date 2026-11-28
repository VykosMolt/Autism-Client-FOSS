package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.DynamicBindingManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class AutismMacro {
   public String name = "New Macro";
   public String description = "";
   public String folder = "";
   public boolean loop = false;
   public int loopCount = -1;
   public int keyCode = -1;
   public List<MacroAction> actions = new ArrayList<>();
   private static final Set<String> BUILT_IN_ACTION_TYPES;

   public AutismMacro() {
   }

   public AutismMacro(String name) {
      this.name = name;
   }

   public AutismMacro copy() {
      return new AutismMacro().fromTag(this.toTag());
   }

   public AutismMacro copyWithName(String newName) {
      AutismMacro copy = this.copy();
      if (newName != null && !newName.isBlank()) {
         copy.name = newName;
      }

      return copy;
   }

   public AutismMacro sanitizeForSharing() {
      if (this.actions != null) {
         for (MacroAction action : this.actions) {
            if (action != null) {
               action.sanitizeForSharing();
            }
         }
      }

      return this;
   }

   public CompoundTag toSharedTag() {
      return this.copy().sanitizeForSharing().toTag();
   }

   public void execute() {
      this.execute(true);
   }

   public void execute(boolean regenerate) {
      this.executeWithRunId(regenerate);
   }

   public long executeWithRunId() {
      return this.executeWithRunId(true);
   }

   public long executeWithRunId(boolean regenerate) {
      if (this.actions != null && !this.actions.isEmpty()) {
         if (regenerate) {
            this.regeneratePackets();
         }

         return MacroExecutor.startMacro(this);
      } else {
         AutismClientMessaging.sendPrefixed("§cMacro has no actions!");
         return -1L;
      }
   }

   public void regeneratePackets() {
      if (this.actions != null) {
         for (MacroAction action : this.actions) {
            if (action instanceof autismclient.util.macro.SendPacketAction) {
               ((autismclient.util.macro.SendPacketAction)action).regeneratePackets();
            }
         }
      }
   }

   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("name", this.name);
      tag.putString("description", this.description);
      tag.putString("folder", MacroFolderLayout.sanitize(this.folder));
      tag.putBoolean("loop", this.loop);
      tag.putInt("loopCount", this.loopCount);
      tag.putInt("keyCode", this.keyCode);
      ListTag actionsList = new ListTag();
      if (this.actions != null) {
         for (MacroAction action : this.actions) {
            if (action != null) {
               actionsList.add(serializeAction(action));
            }
         }
      }

      tag.put("actions", actionsList);
      return tag;
   }

   public AutismMacro fromTag(CompoundTag tag) {
      this.folder = MacroFolderLayout.sanitize(tag.getStringOr("folder", ""));
      if (tag.contains("name")) {
         this.name = tag.getStringOr("name", "");
      }

      if (tag.contains("description")) {
         this.description = tag.getStringOr("description", "");
      }

      if (tag.contains("loop")) {
         this.loop = tag.getBooleanOr("loop", false);
      }

      if (tag.contains("loopCount")) {
         this.loopCount = tag.getIntOr("loopCount", -1);
      }

      if (tag.contains("keyCode")) {
         this.keyCode = tag.getIntOr("keyCode", -1);
      }

      if (tag.get("actions") instanceof ListTag actionsList) {
         List<MacroAction> loadedActions = new ArrayList<>();

         for (Tag element : actionsList) {
            if (element instanceof CompoundTag actionTag) {
               MacroAction action = deserializeAction(actionTag);
               if (action != null) {
                  loadedActions.add(action);
               }
            }
         }

         this.actions = loadedActions;
         this.migrateLegacyRaceActions();
      }

      return this;
   }

   public static MacroAction deserializeAction(CompoundTag actionTag) {
      if (actionTag != null && actionTag.contains("type")) {
         try {
            String typeName = actionTag.getStringOr("type", "");
            MacroAction migratedLegacy = migrateLegacyAction(typeName, actionTag);
            if (migratedLegacy != null) {
               return migratedLegacy;
            }

            autismclient.util.macro.MacroActionType type = autismclient.util.macro.MacroActionType.valueOf(typeName);
            MacroAction action = createBuiltInAction(type);
            if (type == autismclient.util.macro.MacroActionType.WAIT_ITEM) {
               ((autismclient.util.macro.WaitForSlotChangeAction)action).readTag(actionTag);
            } else {
               action.fromTag(actionTag);
            }

            DynamicBindingManager.capture(action, actionTag);
            return action;
         } catch (IllegalArgumentException e) {
            return deserializeAddonAction(actionTag);
         } catch (Throwable t) {
            String typeName = actionTag.getStringOr("type", "");
            AutismClientAddon.LOG.warn("[Macros] Failed to deserialize action '{}'; preserving its data", typeName, t);
            return new autismclient.util.macro.MissingAddonAction(typeName.isBlank() ? "invalid:action" : typeName, actionTag.copy());
         }
      } else {
         return null;
      }
   }

   public static CompoundTag serializeAction(MacroAction action) {
      CompoundTag tag = action == null ? new CompoundTag() : action.toTag();
      DynamicBindingManager.restore(action, tag);
      return tag;
   }

   public static boolean isBuiltInActionType(String type) {
      return type != null && BUILT_IN_ACTION_TYPES.contains(type);
   }

   public static int pruneUnknownActions(CompoundTag macroTag) {
      if (macroTag == null) {
         return 0;
      } else if (!(macroTag.get("actions") instanceof ListTag actionsList)) {
         return 0;
      } else {
         ListTag var7 = new ListTag();
         int dropped = 0;

         for (Tag element : actionsList) {
            if (element instanceof CompoundTag actionTag && isBuiltInActionType(actionTag.getStringOr("type", ""))) {
               var7.add(actionTag);
            } else {
               dropped++;
            }
         }

         if (dropped > 0) {
            macroTag.put("actions", var7);
         }

         return dropped;
      }
   }

   private static MacroAction createBuiltInAction(autismclient.util.macro.MacroActionType type) {
      MacroAction var10000;
      switch (type) {
         case DELAY:
            var10000 = new autismclient.util.macro.DelayAction();
            break;
         case PACKET:
            var10000 = new autismclient.util.macro.PacketAction();
            break;
         case PACKET_CLICK:
            var10000 = new autismclient.util.macro.PacketClickAction();
            break;
         case WAIT_PACKET:
            var10000 = new autismclient.util.macro.WaitForPacketAction();
            break;
         case WAIT_HEALTH:
            var10000 = new autismclient.util.macro.WaitForHealthAction();
            break;
         case WAIT_ITEM:
         case WAIT_SLOT_CHANGE:
            var10000 = new autismclient.util.macro.WaitForSlotChangeAction();
            break;
         case WAIT_BLOCK:
            var10000 = new autismclient.util.macro.WaitForBlockAction();
            break;
         case WAIT_GUI:
            var10000 = new autismclient.util.macro.WaitForGuiAction();
            break;
         case CUSTOM_MENU:
            var10000 = new autismclient.util.macro.CustomMenuAction();
            break;
         case CLICK:
            var10000 = new autismclient.util.macro.ClickAction();
            break;
         case ROTATE:
            var10000 = new autismclient.util.macro.RotateAction();
            break;
         case USE_ITEM:
            var10000 = new autismclient.util.macro.UseItemAction();
            break;
         case INVENTORY:
            var10000 = new autismclient.util.macro.InventoryAction();
            break;
         case SEND_PACKET:
            var10000 = new autismclient.util.macro.SendPacketAction();
            break;
         case CRAFT:
            var10000 = new autismclient.util.macro.CraftAction();
            break;
         case TRADE:
            var10000 = new autismclient.util.macro.TradeAction();
            break;
         case SELECT_SLOT:
            var10000 = new autismclient.util.macro.SelectSlotAction();
            break;
         case XCARRY:
            var10000 = new autismclient.util.macro.XCarryAction();
            break;
         case DROP:
            var10000 = new autismclient.util.macro.DropAction();
            break;
         case ITEM:
            var10000 = new autismclient.util.macro.ItemAction();
            break;
         case PICK_UP_ALL:
            var10000 = new autismclient.util.macro.PickUpAllAction();
            break;
         case TICK_SYNC:
            var10000 = new autismclient.util.macro.TickSyncAction();
            break;
         case REVISION_SYNC:
            var10000 = new autismclient.util.macro.RevisionSyncAction();
            break;
         case SERVER_TICK_SYNC:
            var10000 = new autismclient.util.macro.ServerTickSyncAction();
            break;
         case CLOSE_GUI:
            var10000 = new autismclient.util.macro.CloseGuiAction();
            break;
         case SWAP_SLOTS:
            var10000 = new autismclient.util.macro.SwapSlotsAction();
            break;
         case WAIT_COOLDOWN:
            var10000 = new autismclient.util.macro.WaitForCooldownAction();
            break;
         case GO_TO:
            var10000 = new autismclient.util.macro.GoToAction();
            break;
         case WAIT_POS:
            var10000 = new autismclient.util.macro.WaitPosAction();
            break;
         case PAYLOAD:
            var10000 = new autismclient.util.macro.PayloadAction();
            break;
         case DISCONNECT:
            var10000 = new autismclient.util.macro.DisconnectAction();
            break;
         case JOIN:
            var10000 = new autismclient.util.macro.JoinAction();
            break;
         case TOGGLE_MODULE:
            var10000 = new autismclient.util.macro.ak();
            break;
         case START_MACRO:
            var10000 = new autismclient.util.macro.StartMacroAction();
            break;
         case TOOLS:
            var10000 = new autismclient.util.macro.ToolsAction();
            break;
         case STOP_MACRO:
            var10000 = new autismclient.util.macro.StopMacroAction();
            break;
         case SNEAK:
            var10000 = new autismclient.util.macro.SneakAction();
            break;
         case JUMP:
            var10000 = new autismclient.util.macro.JumpAction();
            break;
         case SPRINT:
            var10000 = new autismclient.util.macro.SprintAction();
            break;
         case MOVE:
            var10000 = new autismclient.util.macro.MoveAction();
            break;
         case LOOK_AT_BLOCK:
            var10000 = new autismclient.util.macro.LookAtBlockAction();
            break;
         case REPEAT:
            var10000 = new autismclient.util.macro.RepeatAction();
            break;
         case WAIT_CHAT:
            var10000 = new autismclient.util.macro.WaitForChatAction();
            break;
         case WAIT_ENTITY:
            var10000 = new autismclient.util.macro.WaitForEntityAction();
            break;
         case OPEN_CONTAINER:
            var10000 = new autismclient.util.macro.OpenContainerAction();
            break;
         case INTERACT_ENTITY:
            var10000 = new autismclient.util.macro.InteractEntityAction();
            break;
         case DESYNC:
            var10000 = new autismclient.util.macro.DesyncAction();
            break;
         case RESTORE_GUI:
            var10000 = new autismclient.util.macro.RestoreGuiAction();
            break;
         case SAVE_GUI:
            var10000 = new autismclient.util.macro.SaveGuiAction();
            break;
         case SEND_TOGGLE:
            var10000 = new autismclient.util.macro.SendToggleAction();
            break;
         case DELAY_PACKETS:
            var10000 = new autismclient.util.macro.DelayPacketsAction();
            break;
         case INVENTORY_AUDIT:
            var10000 = new autismclient.util.macro.InventoryAuditAction();
            break;
         case STORE_ITEM:
            var10000 = new autismclient.util.macro.StoreItemAction();
            break;
         case WAIT_SOUND:
            var10000 = new autismclient.util.macro.WaitForSoundAction();
            break;
         case MINE:
            var10000 = new autismclient.util.macro.MineAction();
            break;
         case INSTA_BREAK:
            var10000 = new autismclient.util.macro.InstaBreakAction();
            break;
         case BREAK:
            var10000 = new autismclient.util.macro.BreakAction();
            break;
         case PAY:
            var10000 = new autismclient.util.macro.PayAction();
            break;
         case NBT_BOOK:
            var10000 = new autismclient.util.macro.NbtBookAction();
            break;
         case SEND_CHAT:
            var10000 = new autismclient.util.macro.SendChatAction();
            break;
         case WAIT_LAN_STEP:
            var10000 = new autismclient.util.macro.WaitForLanStepAction();
            break;
         case WAIT_MACRO_STEP:
            var10000 = new autismclient.util.macro.WaitForMacroStepAction();
            break;
         case WAIT_WORLD_CHANGE:
            var10000 = new autismclient.util.macro.WaitForWorldChangeAction();
            break;
         case WAIT_POSITION_DELTA:
            var10000 = new autismclient.util.macro.WaitForPositionDeltaAction();
            break;
         case WAIT_TELEPORT:
            var10000 = new autismclient.util.macro.WaitForTeleportAction();
            break;
         case WAIT_GAMEMODE_CHANGE:
            var10000 = new autismclient.util.macro.WaitGamemodeChangeAction();
            break;
         case WAIT_MOVEMENT:
            var10000 = new autismclient.util.macro.WaitMovementAction();
            break;
         case RACE:
            var10000 = new autismclient.util.macro.RaceAction();
            break;
         case REPORT:
            var10000 = new autismclient.util.macro.ReportAction();
            break;
         case VCLIP:
            var10000 = new autismclient.util.macro.VClipAction();
            break;
         case HCLIP:
            var10000 = new autismclient.util.macro.HClipAction();
            break;
         case TP:
            var10000 = new autismclient.util.macro.PacedTpAction();
            break;
         case PACKET_GATE:
            var10000 = new autismclient.util.macro.PacketGateAction();
            break;
         case END_PACKET_GATE:
            var10000 = new autismclient.util.macro.EndPacketGateAction();
            break;
         case PACKET_BURST:
            var10000 = new autismclient.util.macro.PacketBurstAction();
            break;
         case CONTAINER_CLICK_SEQUENCE:
            var10000 = new autismclient.util.macro.ContainerClickSequenceAction();
            break;
         case ASSERT:
            var10000 = new autismclient.util.macro.AssertAction();
            break;
         case USE_ITEM_PHASE:
            var10000 = new autismclient.util.macro.UseItemPhaseAction();
            break;
         case SEND_COMMAND_PACKET:
            var10000 = new autismclient.util.macro.SendCommandPacketAction();
            break;
         case WAIT_PACKET_MATCH:
            var10000 = new autismclient.util.macro.WaitPacketMatchAction();
            break;
         case WAIT_INVENTORY_PREDICATE:
            var10000 = new autismclient.util.macro.WaitInventoryPredicateAction();
            break;
         case WAIT_DURABILITY:
            var10000 = new autismclient.util.macro.WaitDurabilityAction();
            break;
         case WAIT_FREE_SLOTS:
            var10000 = new autismclient.util.macro.WaitFreeSlotsAction();
            break;
         case WAIT_ENTITY_TARGET:
            var10000 = new autismclient.util.macro.WaitEntityTargetAction();
            break;
         case WAIT_GUI_TYPE:
            var10000 = new autismclient.util.macro.WaitGuiTypeAction();
            break;
         case BRANCH:
            var10000 = new autismclient.util.macro.BranchAction();
            break;
         case FINALLY:
            var10000 = new autismclient.util.macro.FinallyAction();
            break;
         case MACRO_VARIABLES:
            var10000 = new autismclient.util.macro.MacroVariablesAction();
            break;
         case CAPTURE_VALUE:
            var10000 = new autismclient.util.macro.CaptureValueAction();
            break;
         case FAKE_GAMEMODE:
            var10000 = new autismclient.util.macro.FakeGamemodeAction();
            break;
         case BUNDLE_DUPE_V2:
            var10000 = new autismclient.util.macro.BundleDupeV2Action();
            break;
         case ROLLBACK:
            var10000 = new autismclient.util.macro.RollbackAction();
            break;
         case PLACE:
            var10000 = new autismclient.util.macro.PlaceAction();
            break;
         case SIGN_EDIT:
            var10000 = new autismclient.util.macro.SignEditAction();
            break;
         case FPS:
            var10000 = new autismclient.util.macro.FpsAction();
            break;
         case SPING:
            var10000 = new autismclient.util.macro.SPingAction();
            break;
         case IF:
            var10000 = new autismclient.util.macro.IfAction();
            break;
         case FLOW:
            var10000 = new autismclient.util.macro.FlowAction();
            break;
         case LABEL:
            var10000 = new autismclient.util.macro.LabelAction();
            break;
         case MULTI:
            var10000 = new autismclient.util.macro.MultiAction();
            break;
         case VILLAGER_DUPE:
            var10000 = new autismclient.util.macro.VillagerDupeAction();
            break;
         case WAIT_VILLAGER_ITEM:
            var10000 = new autismclient.util.macro.WaitVillagerItemAction();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static MacroAction deserializeAddonAction(CompoundTag actionTag) {
      if (actionTag == null) {
         return null;
      }

      String typeName = actionTag.getStringOr("type", "");
      if (typeName.isEmpty()) {
         return null;
      }

      Supplier<MacroAction> factory = MacroActionRegistry.factory(typeName);
      if (factory != null) {
         try {
            MacroAction action = factory.get();
            if (action != null) {
               action.fromTag(actionTag);
               DynamicBindingManager.capture(action, actionTag);
               return action;
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[MacroActions] Addon action '{}' failed to deserialize; keeping as placeholder", typeName, t);
         }
      }

      return new autismclient.util.macro.MissingAddonAction(typeName, actionTag.copy());
   }

   private static MacroAction migrateLegacyAction(String typeName, CompoundTag actionTag) {
      if ("WAIT_ENTITY_TARGET".equals(typeName)) {
         autismclient.util.macro.WaitForEntityAction migrated = new autismclient.util.macro.WaitForEntityAction();
         migrated.migrateFrom(actionTag);
         return migrated;
      } else if ("WAIT_GUI_TYPE".equals(typeName)) {
         autismclient.util.macro.WaitForGuiAction migrated = new autismclient.util.macro.WaitForGuiAction();
         migrated.migrateFrom(actionTag);
         return migrated;
      } else {
         return null;
      }
   }

   private void migrateLegacyRaceActions() {
      if (this.actions != null && !this.actions.isEmpty()) {
         for (int i = 0; i < this.actions.size(); i++) {
            if (this.actions.get(i) instanceof autismclient.util.macro.RaceAction race && race.needsLegacyMigration()) {
               int inserted = 0;
               autismclient.util.macro.PacketClickAction packetClick = race.takeLegacyPacketClick();
               if (packetClick != null) {
                  this.actions.add(i + 1, packetClick);
                  inserted++;
               }

               if (race.legacyUsesNextAction()) {
                  for (int j = i + 1 + inserted; j < this.actions.size(); j++) {
                     MacroAction candidate = this.actions.get(j);
                     if (!autismclient.util.macro.RaceAction.load(candidate)) {
                        break;
                     }

                     inserted++;
                  }
               }

               race.bodyCount = inserted;
               race.clearLegacyState();
               i += inserted;
            }
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AutismMacro that = (AutismMacro)o;
         return MacroNameNormalizer.sameName(this.name, that.name);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(MacroNameNormalizer.normalize(this.name));
   }

   static {
      Set<String> s = new HashSet<>();

      for (autismclient.util.macro.MacroActionType t : autismclient.util.macro.MacroActionType.values()) {
         s.add(t.name());
      }

      BUILT_IN_ACTION_TYPES = Set.copyOf(s);
   }
}
