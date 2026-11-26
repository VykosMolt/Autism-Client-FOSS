package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class aM {
   public String init_Str = "New Macro";
   public String load_Str = "";
   public String read = "";
   public boolean init_Z = false;
   public int init_I = -1;
   public int load_I = -1;
   public List<MacroAction> init_List = new ArrayList<>();
   private static final Set<String> init_Set;

   public aM() {
   }

   public aM(String name) {
      this.init_Str = name;
   }

   public aM init_aM() {
      return new aM().init_aM_CompoundTag(this.load_CompoundTag());
   }

   public aM init_aM_Str(String newName) {
      aM copy = this.init_aM();
      if (newName != null && !newName.isBlank()) {
         copy.init_Str = newName;
      }

      return copy;
   }

   public aM load_aM() {
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
            if (action instanceof autismclient.util.macro.Y) {
               ((autismclient.util.macro.Y)action).init_V_10();
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

   public aM init_aM_CompoundTag(CompoundTag tag) {
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

            autismclient.util.macro.StreamGate type = autismclient.util.macro.StreamGate.valueOf(typeName);
            MacroAction action = init_MacroAction_StreamGate(type);
            if (type == autismclient.util.macro.StreamGate.WAIT_ITEM) {
               ((autismclient.util.macro.aD)action).init(actionTag);
            } else {
               action.fromTag(actionTag);
            }

            d.init(action, actionTag);
            return action;
         } catch (IllegalArgumentException e) {
            return load(actionTag);
         } catch (Throwable t) {
            String typeName = actionTag.getStringOr("type", "");
            AutismClientAddon.LOG.warn("[Macros] Failed to deserialize action '{}'; preserving its data", typeName, t);
            return new autismclient.util.macro.r(typeName.isBlank() ? "invalid:action" : typeName, actionTag.copy());
         }
      } else {
         return null;
      }
   }

   public static CompoundTag init_CompoundTag_MacroAction(MacroAction action) {
      CompoundTag tag = action == null ? new CompoundTag() : action.toTag();
      d.load(action, tag);
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

   private static MacroAction init_MacroAction_StreamGate(autismclient.util.macro.StreamGate type) {
      MacroAction var10000;
      switch (type) {
         case DELAY:
            var10000 = new autismclient.util.macro.EventBus();
            break;
         case PACKET:
            var10000 = new autismclient.util.macro.x();
            break;
         case PACKET_CLICK:
            var10000 = new autismclient.util.macro.z();
            break;
         case WAIT_PACKET:
            var10000 = new autismclient.util.macro.aB();
            break;
         case WAIT_HEALTH:
            var10000 = new autismclient.util.macro.ay();
            break;
         case WAIT_ITEM:
         case WAIT_SLOT_CHANGE:
            var10000 = new autismclient.util.macro.aD();
            break;
         case WAIT_BLOCK:
            var10000 = new autismclient.util.macro.at();
            break;
         case WAIT_GUI:
            var10000 = new autismclient.util.macro.ax();
            break;
         case CUSTOM_MENU:
            var10000 = new autismclient.util.macro.UiHelper();
            break;
         case CLICK:
            var10000 = new autismclient.util.macro.NetHelper();
            break;
         case ROTATE:
            var10000 = new autismclient.util.macro.S();
            break;
         case USE_ITEM:
            var10000 = new autismclient.util.macro.an();
            break;
         case INVENTORY:
            var10000 = new autismclient.util.macro.RefQueue();
            break;
         case SEND_PACKET:
            var10000 = new autismclient.util.macro.Y();
            break;
         case CRAFT:
            var10000 = new autismclient.util.macro.SyncManager();
            break;
         case TRADE:
            var10000 = new autismclient.util.macro.am();
            break;
         case SELECT_SLOT:
            var10000 = new autismclient.util.macro.V();
            break;
         case XCARRY:
            var10000 = new autismclient.util.macro.aQ();
            break;
         case DROP:
            var10000 = new autismclient.util.macro.PacketUtil();
            break;
         case ITEM:
            var10000 = new autismclient.util.macro.MapStore();
            break;
         case PICK_UP_ALL:
            var10000 = new autismclient.util.macro.I();
            break;
         case TICK_SYNC:
            var10000 = new autismclient.util.macro.aj();
            break;
         case REVISION_SYNC:
            var10000 = new autismclient.util.macro.Q();
            break;
         case SERVER_TICK_SYNC:
            var10000 = new autismclient.util.macro.aa();
            break;
         case CLOSE_GUI:
            var10000 = new autismclient.util.macro.JsonUtil();
            break;
         case SWAP_SLOTS:
            var10000 = new autismclient.util.macro.ai();
            break;
         case WAIT_COOLDOWN:
            var10000 = new autismclient.util.macro.av();
            break;
         case GO_TO:
            var10000 = new autismclient.util.macro.CharEncoder();
            break;
         case WAIT_POS:
            var10000 = new autismclient.util.macro.aN();
            break;
         case PAYLOAD:
            var10000 = new autismclient.util.macro.H();
            break;
         case DISCONNECT:
            var10000 = new autismclient.util.macro.ByteReader();
            break;
         case JOIN:
            var10000 = new autismclient.util.macro.ClassCache();
            break;
         case TOGGLE_MODULE:
            var10000 = new autismclient.util.macro.ak();
            break;
         case START_MACRO:
            var10000 = new autismclient.util.macro.af();
            break;
         case TOOLS:
            var10000 = new autismclient.util.macro.al();
            break;
         case STOP_MACRO:
            var10000 = new autismclient.util.macro.ag();
            break;
         case SNEAK:
            var10000 = new autismclient.util.macro.ad();
            break;
         case JUMP:
            var10000 = new autismclient.util.macro.NodeWalker();
            break;
         case SPRINT:
            var10000 = new autismclient.util.macro.ae();
            break;
         case MOVE:
            var10000 = new autismclient.util.macro.s();
            break;
         case LOOK_AT_BLOCK:
            var10000 = new autismclient.util.macro.ValueSink();
            break;
         case REPEAT:
            var10000 = new autismclient.util.macro.N();
            break;
         case WAIT_CHAT:
            var10000 = new autismclient.util.macro.au();
            break;
         case WAIT_ENTITY:
            var10000 = new autismclient.util.macro.aw();
            break;
         case OPEN_CONTAINER:
            var10000 = new autismclient.util.macro.v();
            break;
         case INTERACT_ENTITY:
            var10000 = new autismclient.util.macro.PathHelper();
            break;
         case DESYNC:
            var10000 = new autismclient.util.macro.TextFormat();
            break;
         case RESTORE_GUI:
            var10000 = new autismclient.util.macro.P();
            break;
         case SAVE_GUI:
            var10000 = new autismclient.util.macro.U();
            break;
         case SEND_TOGGLE:
            var10000 = new autismclient.util.macro.Z();
            break;
         case DELAY_PACKETS:
            var10000 = new autismclient.util.macro.FileCache();
            break;
         case INVENTORY_AUDIT:
            var10000 = new autismclient.util.macro.ListPool();
            break;
         case STORE_ITEM:
            var10000 = new autismclient.util.macro.ah();
            break;
         case WAIT_SOUND:
            var10000 = new autismclient.util.macro.aE();
            break;
         case MINE:
            var10000 = new autismclient.util.macro.q();
            break;
         case INSTA_BREAK:
            var10000 = new autismclient.util.macro.MethodUtil();
            break;
         case BREAK:
            var10000 = new autismclient.util.macro.CachePool();
            break;
         case PAY:
            var10000 = new autismclient.util.macro.G();
            break;
         case NBT_BOOK:
            var10000 = new autismclient.util.macro.u();
            break;
         case SEND_CHAT:
            var10000 = new autismclient.util.macro.W();
            break;
         case WAIT_LAN_STEP:
            var10000 = new autismclient.util.macro.az();
            break;
         case WAIT_MACRO_STEP:
            var10000 = new autismclient.util.macro.aA();
            break;
         case WAIT_WORLD_CHANGE:
            var10000 = new autismclient.util.macro.aG();
            break;
         case WAIT_POSITION_DELTA:
            var10000 = new autismclient.util.macro.aC();
            break;
         case WAIT_TELEPORT:
            var10000 = new autismclient.util.macro.aF();
            break;
         case WAIT_GAMEMODE_CHANGE:
            var10000 = new autismclient.util.macro.aI();
            break;
         case WAIT_MOVEMENT:
            var10000 = new autismclient.util.macro.aL();
            break;
         case RACE:
            var10000 = new autismclient.util.macro.L();
            break;
         case REPORT:
            var10000 = new autismclient.util.macro.O();
            break;
         case VCLIP:
            var10000 = new autismclient.util.macro.ap();
            break;
         case HCLIP:
            var10000 = new autismclient.util.macro.TokenStore();
            break;
         case TP:
            var10000 = new autismclient.util.macro.w();
            break;
         case PACKET_GATE:
            var10000 = new autismclient.util.macro.B();
            break;
         case END_PACKET_GATE:
            var10000 = new autismclient.util.macro.HashUtil();
            break;
         case PACKET_BURST:
            var10000 = new autismclient.util.macro.y();
            break;
         case CONTAINER_CLICK_SEQUENCE:
            var10000 = new autismclient.util.macro.StateCache();
            break;
         case ASSERT:
            var10000 = new autismclient.util.macro.DataUtil();
            break;
         case USE_ITEM_PHASE:
            var10000 = new autismclient.util.macro.ao();
            break;
         case SEND_COMMAND_PACKET:
            var10000 = new autismclient.util.macro.X();
            break;
         case WAIT_PACKET_MATCH:
            var10000 = new autismclient.util.macro.aM();
            break;
         case WAIT_INVENTORY_PREDICATE:
            var10000 = new autismclient.util.macro.aK();
            break;
         case WAIT_DURABILITY:
            var10000 = new autismclient.util.macro.ar();
            break;
         case WAIT_FREE_SLOTS:
            var10000 = new autismclient.util.macro.aH();
            break;
         case WAIT_ENTITY_TARGET:
            var10000 = new autismclient.util.macro.as();
            break;
         case WAIT_GUI_TYPE:
            var10000 = new autismclient.util.macro.aJ();
            break;
         case BRANCH:
            var10000 = new autismclient.util.macro.StreamUtil();
            break;
         case FINALLY:
            var10000 = new autismclient.util.macro.PoolEntry();
            break;
         case MACRO_VARIABLES:
            var10000 = new autismclient.util.macro.o();
            break;
         case CAPTURE_VALUE:
            var10000 = new autismclient.util.macro.TaskQueue();
            break;
         case FAKE_GAMEMODE:
            var10000 = new autismclient.util.macro.IoWorker();
            break;
         case BUNDLE_DUPE_V2:
            var10000 = new autismclient.util.macro.BufferHelper();
            break;
         case ROLLBACK:
            var10000 = new autismclient.util.macro.R();
            break;
         case PLACE:
            var10000 = new autismclient.util.macro.K();
            break;
         case SIGN_EDIT:
            var10000 = new autismclient.util.macro.ac();
            break;
         case FPS:
            var10000 = new autismclient.util.macro.RingBuffer();
            break;
         case SPING:
            var10000 = new autismclient.util.macro.T();
            break;
         case IF:
            var10000 = new autismclient.util.macro.FieldCache();
            break;
         case FLOW:
            var10000 = new autismclient.util.macro.TableIndex();
            break;
         case LABEL:
            var10000 = new autismclient.util.macro.KeyHolder();
            break;
         case MULTI:
            var10000 = new autismclient.util.macro.t();
            break;
         case VILLAGER_DUPE:
            var10000 = new autismclient.util.macro.aq();
            break;
         case WAIT_VILLAGER_ITEM:
            var10000 = new autismclient.util.macro.aO();
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
               d.init(action, actionTag);
               return action;
            }
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[MacroActions] Addon action '{}' failed to deserialize; keeping as placeholder", typeName, t);
         }
      }

      return new autismclient.util.macro.r(typeName, actionTag.copy());
   }

   private static MacroAction init(String typeName, CompoundTag actionTag) {
      if ("WAIT_ENTITY_TARGET".equals(typeName)) {
         autismclient.util.macro.aw migrated = new autismclient.util.macro.aw();
         migrated.init(actionTag);
         return migrated;
      } else if ("WAIT_GUI_TYPE".equals(typeName)) {
         autismclient.util.macro.ax migrated = new autismclient.util.macro.ax();
         migrated.init(actionTag);
         return migrated;
      } else {
         return null;
      }
   }

   private void read() {
      if (this.init_List != null && !this.init_List.isEmpty()) {
         for (int i = 0; i < this.init_List.size(); i++) {
            if (this.init_List.get(i) instanceof autismclient.util.macro.L race && race.init_Z()) {
               int inserted = 0;
               autismclient.util.macro.z packetClick = race.init_z();
               if (packetClick != null) {
                  this.init_List.add(i + 1, packetClick);
                  inserted++;
               }

               if (race.load_Z()) {
                  for (int j = i + 1 + inserted; j < this.init_List.size(); j++) {
                     MacroAction candidate = this.init_List.get(j);
                     if (!autismclient.util.macro.L.load(candidate)) {
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
         aM that = (aM)o;
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

      for (autismclient.util.macro.StreamGate t : autismclient.util.macro.StreamGate.values()) {
         s.add(t.name());
      }

      init_Set = Set.copyOf(s);
   }
}
