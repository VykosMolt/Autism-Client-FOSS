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
   public String init_Str = "New Macro";
   public String load_Str = "";
   public String read = "";
   public boolean init_Z = false;
   public int init_I = -1;
   public int load_I = -1;
   public List<MacroAction> init_List = new ArrayList<>();
   private static final Set<String> init_Set;

   public AutismMacro() {
   }

   public AutismMacro(String name) {
      this.init_Str = name;
   }

   public AutismMacro init_aM() {
      return new AutismMacro().init_aM_CompoundTag(this.load_CompoundTag());
   }

   public AutismMacro init_aM_Str(String newName) {
      AutismMacro copy = this.init_aM();
      if (newName != null && !newName.isBlank()) {
         copy.init_Str = newName;
      }

      return copy;
   }

   public AutismMacro load_aM() {
      if (this.init_List != null) {
         for (MacroAction action : this.init_List) {
            if (action != null) {
               action.sanitizeForSharing();
            }
         }
      }

      return this;
   }

   public CompoundTag init_CompoundTag() {
      return this.init_aM().load_aM().load_CompoundTag();
   }

   public void init_V() {
      this.init_V_Z(true);
   }

   public void init_V_Z(boolean regenerate) {
      this.init_J_Z(regenerate);
   }

   public long init_J() {
      return this.init_J_Z(true);
   }

   public long init_J_Z(boolean regenerate) {
      if (this.init_List != null && !this.init_List.isEmpty()) {
         if (regenerate) {
            this.load_V();
         }

         return MacroExecutor.init_J_aM(this);
      } else {
         AutismClientMessaging.sendPrefixed("§cMacro has no actions!");
         return -1L;
      }
   }

   public void load_V() {
      if (this.init_List != null) {
         for (MacroAction action : this.init_List) {
            if (action instanceof autismclient.util.macro.SendPacketAction) {
               ((autismclient.util.macro.SendPacketAction)action).init_V_10();
            }
         }
      }
   }

   public CompoundTag load_CompoundTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("name", this.init_Str);
      tag.putString("description", this.load_Str);
      tag.putString("folder", cY.init_Str_Str(this.read));
      tag.putBoolean("loop", this.init_Z);
      tag.putInt("loopCount", this.init_I);
      tag.putInt("keyCode", this.load_I);
      ListTag actionsList = new ListTag();
      if (this.init_List != null) {
         for (MacroAction action : this.init_List) {
            if (action != null) {
               actionsList.add(init_CompoundTag_MacroAction(action));
            }
         }
      }

      tag.put("actions", actionsList);
      return tag;
   }

   public AutismMacro init_aM_CompoundTag(CompoundTag tag) {
      this.read = cY.init_Str_Str(tag.getStringOr("folder", ""));
      if (tag.contains("name")) {
         this.init_Str = tag.getStringOr("name", "");
      }

      if (tag.contains("description")) {
         this.load_Str = tag.getStringOr("description", "");
      }

      if (tag.contains("loop")) {
         this.init_Z = tag.getBooleanOr("loop", false);
      }

      if (tag.contains("loopCount")) {
         this.init_I = tag.getIntOr("loopCount", -1);
      }

      if (tag.contains("keyCode")) {
         this.load_I = tag.getIntOr("keyCode", -1);
      }

      if (tag.get("actions") instanceof ListTag actionsList) {
         List<MacroAction> loadedActions = new ArrayList<>();

         for (Tag element : actionsList) {
            if (element instanceof CompoundTag actionTag) {
               MacroAction action = init_MacroAction_CompoundTag(actionTag);
               if (action != null) {
                  loadedActions.add(action);
               }
            }
         }

         this.init_List = loadedActions;
         this.read();
      }

      return this;
   }

   public static MacroAction init_MacroAction_CompoundTag(CompoundTag actionTag) {
      if (actionTag != null && actionTag.contains("type")) {
         try {
            String typeName = actionTag.getStringOr("type", "");
            MacroAction migratedLegacy = init(typeName, actionTag);
            if (migratedLegacy != null) {
               return migratedLegacy;
            }

            autismclient.util.macro.MacroActionType type = autismclient.util.macro.MacroActionType.valueOf(typeName);
            MacroAction action = init_MacroAction_StreamGate(type);
            if (type == autismclient.util.macro.MacroActionType.WAIT_ITEM) {
               ((autismclient.util.macro.WaitForSlotChangeAction)action).init(actionTag);
            } else {
               action.fromTag(actionTag);
            }

            DynamicBindingManager.init(action, actionTag);
            return action;
         } catch (IllegalArgumentException e) {
            return load(actionTag);
         } catch (Throwable t) {
            String typeName = actionTag.getStringOr("type", "");
            AutismClientAddon.LOG.warn("[Macros] Failed to deserialize action '{}'; preserving its data", typeName, t);
            return new autismclient.util.macro.MissingAddonAction(typeName.isBlank() ? "invalid:action" : typeName, actionTag.copy());
         }
      } else {
         return null;
      }
   }

   public static CompoundTag init_CompoundTag_MacroAction(MacroAction action) {
      CompoundTag tag = action == null ? new CompoundTag() : action.toTag();
      DynamicBindingManager.load(action, tag);
      return tag;
   }

   public static boolean init_Z_Str(String type) {
      return type != null && init_Set.contains(type);
   }

   public static int init_I_CompoundTag(CompoundTag macroTag) {
      if (macroTag == null) {
         return 0;
      } else if (!(macroTag.get("actions") instanceof ListTag actionsList)) {
         return 0;
      } else {
         ListTag var7 = new ListTag();
         int dropped = 0;

         for (Tag element : actionsList) {
            if (element instanceof CompoundTag actionTag && init_Z_Str(actionTag.getStringOr("type", ""))) {
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

   private static MacroAction init_MacroAction_StreamGate(autismclient.util.macro.MacroActionType type) {
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

   private static MacroAction load(CompoundTag actionTag) {
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
               DynamicBindingManager.init(action, actionTag);
               return action;
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[MacroActions] Addon action '{}' failed to deserialize; keeping as placeholder", typeName, t);
         }
      }

      return new autismclient.util.macro.MissingAddonAction(typeName, actionTag.copy());
   }

   private static MacroAction init(String typeName, CompoundTag actionTag) {
      if ("WAIT_ENTITY_TARGET".equals(typeName)) {
         autismclient.util.macro.WaitForEntityAction migrated = new autismclient.util.macro.WaitForEntityAction();
         migrated.init(actionTag);
         return migrated;
      } else if ("WAIT_GUI_TYPE".equals(typeName)) {
         autismclient.util.macro.WaitForGuiAction migrated = new autismclient.util.macro.WaitForGuiAction();
         migrated.init(actionTag);
         return migrated;
      } else {
         return null;
      }
   }

   private void read() {
      if (this.init_List != null && !this.init_List.isEmpty()) {
         for (int i = 0; i < this.init_List.size(); i++) {
            if (this.init_List.get(i) instanceof autismclient.util.macro.RaceAction race && race.init_Z()) {
               int inserted = 0;
               autismclient.util.macro.PacketClickAction packetClick = race.init_z();
               if (packetClick != null) {
                  this.init_List.add(i + 1, packetClick);
                  inserted++;
               }

               if (race.load_Z()) {
                  for (int j = i + 1 + inserted; j < this.init_List.size(); j++) {
                     MacroAction candidate = this.init_List.get(j);
                     if (!autismclient.util.macro.RaceAction.load(candidate)) {
                        break;
                     }

                     inserted++;
                  }
               }

               race.bodyCount = inserted;
               race.init_V_3();
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
         return cZ.init(this.init_Str, that.init_Str);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(cZ.init(this.init_Str));
   }

   static {
      Set<String> s = new HashSet<>();

      for (autismclient.util.macro.MacroActionType t : autismclient.util.macro.MacroActionType.values()) {
         s.add(t.name());
      }

      init_Set = Set.copyOf(s);
   }
}
