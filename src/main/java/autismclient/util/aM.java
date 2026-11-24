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

         if (!AutismLiteVariant.enabled() && autismclient.util.g.ValueSink.init_Z()) {
            autismclient.util.g.PacketUtil multi = autismclient.util.g.PacketUtil.load_PacketUtil();
            if (multi != null && multi.load_Z()) {
               autismclient.util.g.PacketUtil.Inner_CachePool result = multi.load(this, Set.of());
               bi.init("Run on POV: " + result.init_Str() + ".", -11013497);
               return -1L;
            } else {
               bi.write("The POV Multi session is no longer active.");
               return -1L;
            }
         } else {
            return MacroExecutor.init_J_aM(this);
         }
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
            if (type == autismclient.util.macro.StreamGate.sync) {
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
         case init:
            var10000 = new autismclient.util.macro.EventBus();
            break;
         case load:
            var10000 = new autismclient.util.macro.x();
            break;
         case read:
            var10000 = new autismclient.util.macro.z();
            break;
         case write:
            var10000 = new autismclient.util.macro.aB();
            break;
         case update:
            var10000 = new autismclient.util.macro.ay();
            break;
         case sync:
         case table:
            var10000 = new autismclient.util.macro.aD();
            break;
         case flush:
            var10000 = new autismclient.util.macro.at();
            break;
         case close:
            var10000 = new autismclient.util.macro.ax();
            break;
         case open:
            var10000 = new autismclient.util.macro.UiHelper();
            break;
         case poll:
            var10000 = new autismclient.util.macro.NetHelper();
            break;
         case push:
            var10000 = new autismclient.util.macro.S();
            break;
         case pop:
            var10000 = new autismclient.util.macro.an();
            break;
         case tick:
            var10000 = new autismclient.util.macro.RefQueue();
            break;
         case run:
            var10000 = new autismclient.util.macro.Y();
            break;
         case get:
            var10000 = new autismclient.util.macro.SyncManager();
            break;
         case set:
            var10000 = new autismclient.util.macro.am();
            break;
         case put:
            var10000 = new autismclient.util.macro.V();
            break;
         case add:
            var10000 = new autismclient.util.macro.aQ();
            break;
         case clear:
            var10000 = new autismclient.util.macro.PacketUtil();
            break;
         case reset:
            var10000 = new autismclient.util.macro.MapStore();
            break;
         case start:
            var10000 = new autismclient.util.macro.I();
            break;
         case stop:
            var10000 = new autismclient.util.macro.aj();
            break;
         case next:
            var10000 = new autismclient.util.macro.Q();
            break;
         case prev:
            var10000 = new autismclient.util.macro.aa();
            break;
         case valid:
            var10000 = new autismclient.util.macro.JsonUtil();
            break;
         case size:
            var10000 = new autismclient.util.macro.ai();
            break;
         case data:
            var10000 = new autismclient.util.macro.av();
            break;
         case value:
            var10000 = new autismclient.util.macro.CharEncoder();
            break;
         case state:
            var10000 = new autismclient.util.macro.aN();
            break;
         case queue:
            var10000 = new autismclient.util.macro.H();
            break;
         case buffer:
            var10000 = new autismclient.util.macro.ByteReader();
            break;
         case f_X_StreamGate:
            var10000 = new autismclient.util.macro.ClassCache();
            break;
         case entry:
            var10000 = new autismclient.util.macro.ak();
            break;
         case node:
            var10000 = new autismclient.util.macro.af();
            break;
         case cache:
            var10000 = new autismclient.util.macro.al();
            break;
         case task:
            var10000 = new autismclient.util.macro.ag();
            break;
         case pool:
            var10000 = new autismclient.util.macro.ad();
            break;
         case index:
            var10000 = new autismclient.util.macro.NodeWalker();
            break;
         case count:
            var10000 = new autismclient.util.macro.ae();
            break;
         case flags:
            var10000 = new autismclient.util.macro.s();
            break;
         case level:
            var10000 = new autismclient.util.macro.ValueSink();
            break;
         case frame:
            var10000 = new autismclient.util.macro.N();
            break;
         case block:
            var10000 = new autismclient.util.macro.au();
            break;
         case chunk:
            var10000 = new autismclient.util.macro.aw();
            break;
         case store:
            var10000 = new autismclient.util.macro.v();
            break;
         case drain:
            var10000 = new autismclient.util.macro.PathHelper();
            break;
         case fill:
            var10000 = new autismclient.util.macro.TextFormat();
            break;
         case mark:
            var10000 = new autismclient.util.macro.P();
            break;
         case scan:
            var10000 = new autismclient.util.macro.U();
            break;
         case parse:
            var10000 = new autismclient.util.macro.Z();
            break;
         case bind:
            var10000 = new autismclient.util.macro.FileCache();
            break;
         case f_a_StreamGate:
            var10000 = new autismclient.util.macro.ListPool();
            break;
         case f_b_StreamGate:
            var10000 = new autismclient.util.macro.ah();
            break;
         case f_c_StreamGate:
            var10000 = new autismclient.util.macro.aE();
            break;
         case f_d_StreamGate:
            var10000 = new autismclient.util.macro.q();
            break;
         case f_e_StreamGate:
            var10000 = new autismclient.util.macro.MethodUtil();
            break;
         case f_f_StreamGate:
            var10000 = new autismclient.util.macro.CachePool();
            break;
         case f_g_StreamGate:
            var10000 = new autismclient.util.macro.G();
            break;
         case f_i_StreamGate:
            var10000 = new autismclient.util.macro.u();
            break;
         case f_h_StreamGate:
            var10000 = new autismclient.util.macro.W();
            break;
         case f_j_StreamGate:
            var10000 = new autismclient.util.macro.az();
            break;
         case f_k_StreamGate:
            var10000 = new autismclient.util.macro.aA();
            break;
         case f_l_StreamGate:
            var10000 = new autismclient.util.macro.aG();
            break;
         case f_m_StreamGate:
            var10000 = new autismclient.util.macro.aC();
            break;
         case f_n_StreamGate:
            var10000 = new autismclient.util.macro.aF();
            break;
         case f_o_StreamGate:
            var10000 = new autismclient.util.macro.aI();
            break;
         case f_p_StreamGate:
            var10000 = new autismclient.util.macro.aL();
            break;
         case f_q_StreamGate:
            var10000 = new autismclient.util.macro.L();
            break;
         case f_r_StreamGate:
            var10000 = new autismclient.util.macro.O();
            break;
         case f_s_StreamGate:
            var10000 = new autismclient.util.macro.ap();
            break;
         case f_t_StreamGate:
            var10000 = new autismclient.util.macro.TokenStore();
            break;
         case f_u_StreamGate:
            var10000 = new autismclient.util.macro.w();
            break;
         case f_v_StreamGate:
            var10000 = new autismclient.util.macro.B();
            break;
         case f_B_StreamGate:
            var10000 = new autismclient.util.macro.HashUtil();
            break;
         case f_w_StreamGate:
            var10000 = new autismclient.util.macro.y();
            break;
         case f_x_StreamGate:
            var10000 = new autismclient.util.macro.StateCache();
            break;
         case f_y_StreamGate:
            var10000 = new autismclient.util.macro.DataUtil();
            break;
         case f_z_StreamGate:
            var10000 = new autismclient.util.macro.ao();
            break;
         case f_A_StreamGate:
            var10000 = new autismclient.util.macro.X();
            break;
         case f_C_StreamGate:
            var10000 = new autismclient.util.macro.aM();
            break;
         case f_D_StreamGate:
            var10000 = new autismclient.util.macro.aK();
            break;
         case f_E_StreamGate:
            var10000 = new autismclient.util.macro.ar();
            break;
         case f_F_StreamGate:
            var10000 = new autismclient.util.macro.aH();
            break;
         case f_G_StreamGate:
            var10000 = new autismclient.util.macro.as();
            break;
         case f_H_StreamGate:
            var10000 = new autismclient.util.macro.aJ();
            break;
         case f_I_StreamGate:
            var10000 = new autismclient.util.macro.StreamUtil();
            break;
         case f_J_StreamGate:
            var10000 = new autismclient.util.macro.PoolEntry();
            break;
         case f_K_StreamGate:
            var10000 = new autismclient.util.macro.o();
            break;
         case f_L_StreamGate:
            var10000 = new autismclient.util.macro.TaskQueue();
            break;
         case f_M_StreamGate:
            var10000 = new autismclient.util.macro.IoWorker();
            break;
         case f_N_StreamGate:
            var10000 = new autismclient.util.macro.BufferHelper();
            break;
         case f_O_StreamGate:
            var10000 = new autismclient.util.macro.R();
            break;
         case f_P_StreamGate:
            var10000 = new autismclient.util.macro.K();
            break;
         case f_Q_StreamGate:
            var10000 = new autismclient.util.macro.ac();
            break;
         case f_R_StreamGate:
            var10000 = new autismclient.util.macro.RingBuffer();
            break;
         case f_S_StreamGate:
            var10000 = new autismclient.util.macro.T();
            break;
         case f_T_StreamGate:
            var10000 = new autismclient.util.macro.FieldCache();
            break;
         case f_U_StreamGate:
            var10000 = new autismclient.util.macro.TableIndex();
            break;
         case f_V_StreamGate:
            var10000 = new autismclient.util.macro.KeyHolder();
            break;
         case f_W_StreamGate:
            var10000 = new autismclient.util.macro.t();
            break;
         case f_Y_StreamGate:
            var10000 = new autismclient.util.macro.aq();
            break;
         case f_Z_StreamGate:
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
