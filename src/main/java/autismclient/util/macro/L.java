package autismclient.util.macro;

import autismclient.util.bo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class L implements MacroAction {
   public String label = "Race";
   public MacroAction triggerAction = new EventBus(0);
   public int bodyCount = 0;
   public int timeoutMs = 10000;
   public String conditionType = L.Inner_DataUtil.write.name();
   public String actionType = StreamGate.read.name();
   public ArrayList<String> raceSteps = new ArrayList<>();
   public ArrayList<String> conditionTypes = new ArrayList<>();
   public ArrayList<String> actionTypes = new ArrayList<>();
   private transient boolean init_Z = false;
   private transient boolean load = false;
   private transient bo.Inner_StreamUtil init_bo_StreamUtil = null;
   private transient int init_I = 1;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("label", this.label == null ? "" : this.label);
      tag.putInt("bodyCount", Math.max(0, this.bodyCount));
      tag.putInt("timeoutMs", Math.max(0, this.timeoutMs));
      tag.putString("conditionType", this.conditionType == null ? L.Inner_DataUtil.write.name() : this.conditionType);
      tag.putString("actionType", this.actionType == null ? StreamGate.read.name() : this.actionType);
      tag.put("raceSteps", i.init(this.read()));
      tag.put("conditionTypes", i.init(this.init_List_2()));
      tag.put("actionTypes", i.init(this.write()));
      MacroAction trigger = this.triggerAction == null ? new EventBus(0) : this.triggerAction;
      if (!this.init_List_2().isEmpty()) {
         tag.put("triggerAction", trigger.toTag());
         init_V_CompoundTag_MacroAction(tag, trigger);
      }

      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.label = tag.getStringOr("label", "Race");
      this.bodyCount = Math.max(0, tag.getIntOr("bodyCount", 0));
      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 10000));
      this.conditionType = read_Str(tag.getStringOr("conditionType", tag.getStringOr("triggerType", L.Inner_DataUtil.write.name())));
      this.actionType = tag.getStringOr("actionType", StreamGate.read.name());
      this.raceSteps = i.init(tag.getList("raceSteps").orElse(new ListTag()));
      this.raceSteps.removeIf(s -> s == null || s.isBlank());
      boolean hasConditionTypes = tag.contains("conditionTypes");
      this.conditionTypes = i.init(tag.getList("conditionTypes").orElse(new ListTag()));
      this.conditionTypes.removeIf(s -> s == null || s.isBlank());
      if (!hasConditionTypes && this.conditionType != null && !this.conditionType.isBlank()) {
         this.conditionTypes.add(this.conditionType);
      }

      this.actionTypes = i.init(tag.getList("actionTypes").orElse(new ListTag()));
      if (this.actionTypes.isEmpty() && this.actionType != null && !this.actionType.isBlank()) {
         this.actionTypes.add(this.actionType);
      }

      this.actionTypes.removeIf(s -> s == null || s.isBlank());
      if (this.actionTypes.isEmpty()) {
         this.actionTypes.add(StreamGate.read.name());
      }

      this.conditionTypes.replaceAll(L::read_Str);
      if (this.raceSteps.isEmpty()) {
         for (String type : this.conditionTypes) {
            this.raceSteps.add("CONDITION:" + read_Str(type));
         }

         for (String type : this.actionTypes) {
            this.raceSteps.add("ACTION:" + write(type));
         }
      } else {
         this.raceSteps.replaceAll(L::load_Str);
         this.raceSteps.removeIf(s -> s == null || s.isBlank());
         this.load_V();
         if (this.actionTypes.isEmpty()) {
            this.actionTypes.add(StreamGate.read.name());
         }
      }

      this.conditionType = this.conditionTypes.isEmpty() ? "" : this.conditionTypes.get(0);
      this.actionType = this.actionTypes.get(0);
      MacroAction nested = tag.getCompound("triggerAction").map(autismclient.util.aM::init_MacroAction_CompoundTag).orElse(null);
      if (init_Z(nested)) {
         this.triggerAction = nested;
      }

      if (tag.contains("triggerType")) {
         this.triggerAction = init(tag);
      } else if (tag.contains("trigger")) {
         this.triggerAction = load(tag);
         this.init_Z = true;
         this.load = tag.getBooleanOr("useNextAction", false);
         this.init_I = Math.max(1, tag.getIntOr("times", 1));
         this.init_bo_StreamUtil = tag.getCompound("target").map(bo.Inner_StreamUtil::init_bo_StreamUtil_CompoundTag).orElse(null);
      }

      if (!init_Z(this.triggerAction)) {
         this.triggerAction = new EventBus(0);
      }
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public boolean load_Z() {
      return this.load;
   }

   public z init_z() {
      if (this.init_bo_StreamUtil == null) {
         return null;
      }

      z action = new z(this.init_bo_StreamUtil, this.init_I, false);
      this.init_bo_StreamUtil = null;
      return action;
   }

   public void init_V_3() {
      this.init_Z = false;
      this.load = false;
      this.init_bo_StreamUtil = null;
   }

   public static boolean init_Z(MacroAction action) {
      return action instanceof EventBus
         || action instanceof aB
         || action instanceof ay
         || action instanceof at
         || action instanceof ax
         || action instanceof av
         || action instanceof aN
         || action instanceof au
         || action instanceof aw
         || action instanceof aD
         || action instanceof aE
         || action instanceof aM
         || action instanceof aK
         || action instanceof ar
         || action instanceof aH
         || action instanceof aL
         || action instanceof az
         || action instanceof aA
         || action instanceof aj
         || action instanceof Q
         || action instanceof aa
         || action instanceof aG
         || action instanceof aC
         || action instanceof aF
         || action instanceof aI;
   }

   public static boolean load(MacroAction action) {
      if (action == null) {
         return false;
      } else {
         return init_Z(action)
            ? false
            : !(action instanceof L)
               && !(action instanceof O)
               && !(action instanceof t)
               && !(action instanceof N)
               && !(action instanceof ByteReader)
               && !(action instanceof ClassCache)
               && !(action instanceof CharEncoder)
               && !(action instanceof s);
      }
   }

   public MacroAction init_MacroAction() {
      return init(init_L_DataUtil(this.conditionType));
   }

   public List<String> init_List_2() {
      ArrayList<String> out = new ArrayList<>();
      if (this.conditionTypes != null) {
         for (String type : this.conditionTypes) {
            String canonical = read_Str(type);
            if (canonical != null && !canonical.isBlank()) {
               out.add(canonical);
            }
         }
      }

      return out;
   }

   public List<MacroAction> load_List() {
      ArrayList<MacroAction> out = new ArrayList<>();

      for (String typeName : this.init_List_2()) {
         out.add(read_MacroAction(typeName));
      }

      return out;
   }

   public MacroAction load_MacroAction() {
      return load_MacroAction(this.actionType);
   }

   public List<String> read() {
      ArrayList<String> out = new ArrayList<>();
      if (this.raceSteps != null) {
         for (String step : this.raceSteps) {
            String canonical = load_Str(step);
            if (!canonical.isBlank()) {
               out.add(canonical);
            }
         }
      }

      if (out.isEmpty()) {
         for (String type : this.init_List_2()) {
            out.add("CONDITION:" + type);
         }

         for (String type : this.write()) {
            out.add("ACTION:" + type);
         }
      }

      return out;
   }

   public List<String> write() {
      ArrayList<String> out = new ArrayList<>();
      if (this.actionTypes != null) {
         for (String type : this.actionTypes) {
            if (type != null && !type.isBlank()) {
               out.add(type.trim().toUpperCase(Locale.ROOT));
            }
         }
      }

      if (out.isEmpty()) {
         out.add(this.actionType != null && !this.actionType.isBlank() ? this.actionType.trim().toUpperCase(Locale.ROOT) : StreamGate.read.name());
      }

      return out;
   }

   public List<MacroAction> update() {
      ArrayList<MacroAction> out = new ArrayList<>();

      for (String typeName : this.write()) {
         out.add(load_MacroAction(typeName));
      }

      return out;
   }

   public static boolean init_Z_2(String step) {
      return step != null && step.trim().toUpperCase(Locale.ROOT).startsWith("CONDITION:");
   }

   public static boolean load_Z(String step) {
      return step != null && step.trim().toUpperCase(Locale.ROOT).startsWith("ACTION:");
   }

   public static String init_Str_2(String step) {
      if (step == null) {
         return "";
      }

      int idx = step.indexOf(58);
      return (idx >= 0 ? step.substring(idx + 1) : step).trim().toUpperCase(Locale.ROOT);
   }

   public static MacroAction init_MacroAction(String step) {
      return init_Z_2(step) ? read_MacroAction(init_Str_2(step)) : load_MacroAction(init_Str_2(step));
   }

   public static MacroAction load_MacroAction(String typeName) {
      StreamGate type;
      try {
         type = StreamGate.valueOf(typeName == null ? "" : typeName.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         type = StreamGate.read;
      }

      MacroAction var10000;
      switch (type) {
         case read:
            var10000 = new z();
            break;
         case f_h_StreamGate:
            var10000 = new W();
            break;
         case queue:
            var10000 = new H();
            break;
         case valid:
            var10000 = new JsonUtil();
            break;
         case poll:
            var10000 = new NetHelper();
            break;
         case pop:
            var10000 = new an();
            break;
         case reset:
            var10000 = new MapStore();
            break;
         case start:
            var10000 = new I();
            break;
         case add:
            var10000 = new aQ();
            break;
         case clear:
            var10000 = new PacketUtil();
            break;
         case put:
            var10000 = new V();
            break;
         case size:
            var10000 = new ai();
            break;
         case run:
            var10000 = new Y();
            break;
         case tick:
            var10000 = new RefQueue();
            break;
         case mark:
            var10000 = new P();
            break;
         case scan:
            var10000 = new U();
            break;
         case fill:
            var10000 = new TextFormat();
            break;
         case f_i_StreamGate:
            var10000 = new u();
            break;
         case f_g_StreamGate:
            var10000 = new G();
            break;
         case set:
            var10000 = new am();
            break;
         case f_e_StreamGate:
            var10000 = new MethodUtil();
            break;
         case f_f_StreamGate:
            var10000 = new CachePool();
            break;
         case entry:
            var10000 = new ak();
            break;
         case node:
            var10000 = new af();
            break;
         case cache:
            var10000 = new al();
            break;
         case task:
            var10000 = new ag();
            break;
         case pool:
            var10000 = new ad();
            break;
         case index:
            var10000 = new NodeWalker();
            break;
         case count:
            var10000 = new ae();
            break;
         case f_v_StreamGate:
            var10000 = new B();
            break;
         case f_w_StreamGate:
            var10000 = new y();
            break;
         case f_x_StreamGate:
            var10000 = new StateCache();
            break;
         case f_y_StreamGate:
            var10000 = new DataUtil();
            break;
         case f_z_StreamGate:
            var10000 = new ao();
            break;
         case f_A_StreamGate:
            var10000 = new X();
            break;
         case f_Q_StreamGate:
            var10000 = new ac();
            break;
         case f_K_StreamGate:
            var10000 = new o();
            break;
         case f_M_StreamGate:
            var10000 = new IoWorker();
            break;
         case f_N_StreamGate:
            var10000 = new BufferHelper();
            break;
         case f_s_StreamGate:
            var10000 = new ap();
            break;
         case f_t_StreamGate:
            var10000 = new TokenStore();
            break;
         case f_u_StreamGate:
            var10000 = new w();
            break;
         case f_d_StreamGate:
            var10000 = new q();
            break;
         default:
            var10000 = new z();
      }

      MacroAction action = var10000;
      return load(action) ? action : new z();
   }

   public static L.Inner_DataUtil init_L_DataUtil(MacroAction action) {
      if (action instanceof ax) {
         return L.Inner_DataUtil.load;
      } else if (action instanceof au) {
         return L.Inner_DataUtil.read;
      } else if (action instanceof aB) {
         return L.Inner_DataUtil.write;
      } else if (action instanceof ay) {
         return L.Inner_DataUtil.update;
      } else if (action instanceof aD) {
         return L.Inner_DataUtil.sync;
      } else if (action instanceof at) {
         return L.Inner_DataUtil.flush;
      } else if (action instanceof aw) {
         return L.Inner_DataUtil.close;
      } else if (action instanceof av) {
         return L.Inner_DataUtil.open;
      } else if (action instanceof aE) {
         return L.Inner_DataUtil.push;
      } else if (action instanceof aM) {
         return L.Inner_DataUtil.pop;
      } else if (action instanceof aK) {
         return L.Inner_DataUtil.tick;
      } else if (action instanceof ar) {
         return L.Inner_DataUtil.run;
      } else if (action instanceof aH) {
         return L.Inner_DataUtil.get;
      } else if (action instanceof aL) {
         return L.Inner_DataUtil.set;
      } else if (action instanceof az) {
         return L.Inner_DataUtil.put;
      } else if (action instanceof aA) {
         return L.Inner_DataUtil.add;
      } else if (action instanceof aI) {
         return L.Inner_DataUtil.valid;
      } else if (action instanceof aN || action instanceof aG || action instanceof aC || action instanceof aF) {
         return L.Inner_DataUtil.set;
      } else if (action instanceof aj) {
         return L.Inner_DataUtil.clear;
      } else if (action instanceof Q) {
         return L.Inner_DataUtil.reset;
      } else {
         return action instanceof aa ? L.Inner_DataUtil.start : L.Inner_DataUtil.init;
      }
   }

   public static MacroAction read_MacroAction(String typeName) {
      return init(init_L_DataUtil(typeName));
   }

   private static MacroAction init(L.Inner_DataUtil type) {
      MacroAction var10000;
      switch (type) {
         case init:
            var10000 = new EventBus(0);
            break;
         case load:
            var10000 = new ax();
            break;
         case read:
            var10000 = new au();
            break;
         case write:
            var10000 = new aB();
            break;
         case update:
            var10000 = new ay();
            break;
         case sync:
            var10000 = new aD();
            break;
         case flush:
            var10000 = new at();
            break;
         case close:
            var10000 = new aw();
            break;
         case open:
            var10000 = new av();
            break;
         case poll:
            var10000 = new aN();
            break;
         case push:
            var10000 = new aE();
            break;
         case pop:
            var10000 = new aM();
            break;
         case tick:
            var10000 = new aK();
            break;
         case run:
            var10000 = new ar();
            break;
         case get:
            var10000 = new aH();
            break;
         case set:
            var10000 = new aL();
            break;
         case put:
            var10000 = new az();
            break;
         case add:
            var10000 = new aA();
            break;
         case clear:
            var10000 = new aj();
            break;
         case reset:
            var10000 = new Q();
            break;
         case start:
            var10000 = new aa();
            break;
         case stop:
            var10000 = new aG();
            break;
         case next:
            var10000 = new aC();
            break;
         case prev:
            var10000 = new aF();
            break;
         case valid:
            var10000 = new aI();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static MacroAction init(CompoundTag tag) {
      L.Inner_DataUtil type = init_L_DataUtil(tag.getStringOr("triggerType", L.Inner_DataUtil.init.name()));
      MacroAction action = init(type);
      CompoundTag actionTag = autismclient.util.aM.init_CompoundTag_MacroAction(action);
      switch (type) {
         case init:
            actionTag.putBoolean("useTicks", tag.getBooleanOr("useTicks", false));
            actionTag.putInt("delayMs", tag.getIntOr("delayMs", 0));
            actionTag.putInt("delayTicks", tag.getIntOr("delayTicks", 1));
            break;
         case load:
            actionTag.putString("waitMode", tag.getStringOr("waitMode", "OPEN"));
            actionTag.putString("guiTitle", tag.getStringOr("guiTitle", ""));
            break;
         case read:
            actionTag.putString("pattern", tag.getStringOr("pattern", ""));
            actionTag.putBoolean("useRegex", tag.getBooleanOr("useRegex", false));
            actionTag.putInt("fuzzyPercent", tag.getIntOr("fuzzyPercent", 100));
            break;
         case write:
            actionTag.putString("packetName", tag.getStringOr("packetName", ""));
            init(tag, actionTag, "packetNames");
            break;
         case update:
            actionTag.putFloat("healthThreshold", (float)tag.getDoubleOr("healthThreshold", 20.0));
            actionTag.putString("comparison", tag.getStringOr("comparison", "Drops Below"));
         case sync:
         case pop:
         case tick:
         case run:
         case get:
         case set:
         case put:
         case add:
         case clear:
         case reset:
         case start:
         case stop:
         default:
            break;
         case flush:
            actionTag.putString("checkMode", tag.getStringOr("checkMode", "AT_POSITION"));
            actionTag.putString("waitBehavior", tag.getStringOr("waitBehavior", "PRESENT"));
            actionTag.putBoolean("anyBlock", tag.getBooleanOr("anyBlock", false));
            init(tag, actionTag, "blockIds");
            actionTag.putInt("x", tag.getIntOr("blockX", tag.getIntOr("x", 0)));
            actionTag.putInt("y", tag.getIntOr("blockY", tag.getIntOr("y", 0)));
            actionTag.putInt("z", tag.getIntOr("blockZ", tag.getIntOr("z", 0)));
            actionTag.putDouble("searchRadius", tag.getDoubleOr("searchRadius", 6.0));
            break;
         case close:
            actionTag.putString("checkMode", tag.getStringOr("entityCheckMode", "RADIUS"));
            init(tag, actionTag, "entityIds");
            actionTag.putBoolean("centerOnPlayer", tag.getBooleanOr("centerOnPlayer", true));
            actionTag.putDouble("radius", tag.getDoubleOr("radius", 8.0));
            actionTag.putDouble("x", tag.getDoubleOr("entityX", 0.0));
            actionTag.putDouble("y", tag.getDoubleOr("entityY", 0.0));
            actionTag.putDouble("z", tag.getDoubleOr("entityZ", 0.0));
            break;
         case open:
            actionTag.putString("itemName", tag.getStringOr("itemName", ""));
            actionTag.putBoolean("checkMainHand", tag.getBooleanOr("checkMainHand", true));
            break;
         case poll:
            actionTag.putDouble("x", tag.getDoubleOr("x", 0.0));
            actionTag.putDouble("y", tag.getDoubleOr("y", 0.0));
            actionTag.putDouble("z", tag.getDoubleOr("z", 0.0));
            actionTag.putDouble("leeway", tag.getDoubleOr("leeway", 1.0));
            actionTag.putBoolean("checkRotation", tag.getBooleanOr("checkRotation", false));
            actionTag.putFloat("yaw", (float)tag.getDoubleOr("yaw", 0.0));
            actionTag.putFloat("pitch", (float)tag.getDoubleOr("pitch", 0.0));
            actionTag.putFloat("rotLeeway", (float)tag.getDoubleOr("rotLeeway", 5.0));
            break;
         case push:
            init(tag, actionTag, "soundIds");
            actionTag.putBoolean("checkDistance", tag.getBooleanOr("checkDistance", false));
            actionTag.putDouble("maxDistance", tag.getDoubleOr("maxDistance", 16.0));
            break;
         case next:
            actionTag.putDouble("distance", tag.getDoubleOr("distance", 5.0));
            actionTag.putBoolean("horizontalOnly", tag.getBooleanOr("horizontalOnly", false));
      }

      action.fromTag(actionTag);
      return action;
   }

   private static MacroAction load(CompoundTag tag) {
      String legacy = tag.getStringOr("trigger", "MANUAL_MS").toUpperCase(Locale.ROOT);
      CompoundTag flat = new CompoundTag();
      switch (legacy) {
         case "POSITION_JUMP":
            flat.putString("triggerType", L.Inner_DataUtil.next.name());
            flat.putDouble("distance", tag.getDoubleOr("positionTolerance", 5.0));
            break;
         case "WORLD_CHANGE":
            flat.putString("triggerType", L.Inner_DataUtil.stop.name());
            break;
         case "GUI_OPEN_BY_TITLE":
            flat.putString("triggerType", L.Inner_DataUtil.load.name());
            flat.putString("waitMode", "OPEN");
            flat.putString("guiTitle", tag.getStringOr("guiTitleSubstr", ""));
            break;
         case "GUI_CLOSE_BY_TITLE":
            flat.putString("triggerType", L.Inner_DataUtil.load.name());
            flat.putString("waitMode", "CLOSE");
            flat.putString("guiTitle", tag.getStringOr("guiTitleSubstr", ""));
            break;
         case "TELEPORT_PACKET":
            flat.putString("triggerType", L.Inner_DataUtil.write.name());
            flat.putString("packetName", "S2C:ClientboundPlayerPositionPacket");
            break;
         case "CHAT_MATCH":
            flat.putString("triggerType", L.Inner_DataUtil.read.name());
            flat.putString("pattern", tag.getStringOr("chatPattern", ""));
            flat.putBoolean("useRegex", tag.getBooleanOr("chatUseRegex", false));
            break;
         case "S2C_PACKET_CLASS":
            flat.putString("triggerType", L.Inner_DataUtil.write.name());
            flat.putString("packetName", "S2C:" + tag.getStringOr("s2cPacketClassName", ""));
            break;
         case "HEALTH_BELOW":
            flat.putString("triggerType", L.Inner_DataUtil.update.name());
            flat.putDouble("healthThreshold", tag.getDoubleOr("healthBelowThreshold", 10.0));
            flat.putString("comparison", "Drops Below");
            break;
         case "SLOT_CHANGE":
         case "ITEM_FOUND":
            flat.putString("triggerType", L.Inner_DataUtil.sync.name());
            break;
         case "BLOCK_STATE":
            flat.putString("triggerType", L.Inner_DataUtil.flush.name());
            ListTag blockIds = new ListTag();
            String blockId = tag.getStringOr("blockId", "");
            if (!blockId.isBlank()) {
               blockIds.add(StringTag.valueOf(blockId));
            }

            flat.put("blockIds", blockIds);
            flat.putInt("blockX", tag.getIntOr("blockX", 0));
            flat.putInt("blockY", tag.getIntOr("blockY", 0));
            flat.putInt("blockZ", tag.getIntOr("blockZ", 0));
            break;
         case "ENTITY_NEAR":
            flat.putString("triggerType", L.Inner_DataUtil.close.name());
            init(tag, flat, "entityNearIds", "entityIds");
            flat.putDouble("radius", tag.getDoubleOr("entityNearRadius", 8.0));
            break;
         case "COOLDOWN_READY":
            flat.putString("triggerType", L.Inner_DataUtil.open.name());
            flat.putString("itemName", tag.getStringOr("cooldownItemName", ""));
            break;
         default:
            flat.putString("triggerType", L.Inner_DataUtil.init.name());
            flat.putInt("delayMs", tag.getIntOr("manualDelayMs", 0));
      }

      return init(flat);
   }

   private static void init_V_CompoundTag_MacroAction(CompoundTag tag, MacroAction action) {
      L.Inner_DataUtil type = init_L_DataUtil(action);
      tag.putString("triggerType", type.name());
      CompoundTag src = autismclient.util.aM.init_CompoundTag_MacroAction(action);

      for (String key : src.keySet()) {
         if (!"type".equals(key) && !tag.contains(key)) {
            tag.put(key, src.get(key));
         }
      }

      if (action instanceof at block) {
         tag.putInt("blockX", block.blockPos == null ? 0 : block.blockPos.getX());
         tag.putInt("blockY", block.blockPos == null ? 0 : block.blockPos.getY());
         tag.putInt("blockZ", block.blockPos == null ? 0 : block.blockPos.getZ());
      }

      if (action instanceof aw entity) {
         tag.putString("entityCheckMode", entity.checkMode == null ? "NEAR_PLAYER" : entity.checkMode.name());
         tag.putDouble("entityX", entity.f_x_D);
         tag.putDouble("entityY", entity.f_y_D);
         tag.putDouble("entityZ", entity.f_z_D);
      }
   }

   private static L.Inner_DataUtil init_L_DataUtil(String raw) {
      try {
         return L.Inner_DataUtil.valueOf(raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return L.Inner_DataUtil.init;
      }
   }

   private static String read_Str(String raw) {
      String key = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
      String var10000;
      switch (key) {
         case "WAIT_POS":
         case "WAIT_WORLD_CHANGE":
         case "WAIT_POSITION_DELTA":
         case "WAIT_TELEPORT":
            var10000 = L.Inner_DataUtil.set.name();
            break;
         default:
            var10000 = key;
      }

      return var10000;
   }

   private static String write(String raw) {
      return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
   }

   public static String load_Str(String raw) {
      if (raw == null) {
         return "";
      }

      String text = raw.trim();
      if (text.isBlank()) {
         return "";
      }

      int idx = text.indexOf(58);
      String role = idx >= 0 ? text.substring(0, idx).trim().toUpperCase(Locale.ROOT) : "";
      String type = idx >= 0 ? text.substring(idx + 1).trim() : text;
      if ("CONDITION".equals(role) || "COND".equals(role)) {
         return "CONDITION:" + read_Str(type);
      }

      if (!"ACTION".equals(role) && !"ACT".equals(role)) {
         String condition = read_Str(type);

         try {
            L.Inner_DataUtil.valueOf(condition);
            return "CONDITION:" + condition;
         } catch (IllegalArgumentException ignored) {
            return "ACTION:" + write(type);
         }
      } else {
         return "ACTION:" + write(type);
      }
   }

   public void load_V() {
      this.conditionTypes = new ArrayList<>();
      this.actionTypes = new ArrayList<>();

      for (String step : this.read()) {
         String type = init_Str_2(step);
         if (init_Z_2(step)) {
            this.conditionTypes.add(read_Str(type));
         } else if (load_Z(step)) {
            this.actionTypes.add(write(type));
         }
      }
   }

   private static void init(CompoundTag from, CompoundTag to, String key) {
      init(from, to, key, key);
   }

   private static void init(CompoundTag from, CompoundTag to, String fromKey, String toKey) {
      ListTag out = new ListTag();
      from.getList(fromKey).ifPresent(list -> {
         for (int i = 0; i < list.size(); i++) {
            list.getString(i).ifPresent(s -> {
               if (!s.isBlank()) {
                  out.add(StringTag.valueOf(s));
               }
            });
         }
      });
      to.put(toKey, out);
   }

   public int init_I_List_I(List<MacroAction> actions, int headerIndex) {
      if (actions != null && headerIndex >= 0 && headerIndex < actions.size()) {
         int max = Math.max(0, Math.min(this.bodyCount, actions.size() - headerIndex - 1));
         int count = 0;

         for (int i = headerIndex + 1; i < actions.size() && count < max; i++) {
            MacroAction action = actions.get(i);
            if (action instanceof L || action instanceof O) {
               break;
            }

            count++;
         }

         return count;
      } else {
         return 0;
      }
   }

   public int load(List<MacroAction> actions, int headerIndex) {
      int body = this.init_I_List_I(actions, headerIndex);
      int count = 0;

      for (int offset = 1; offset <= body && headerIndex + offset < actions.size(); offset++) {
         if (init_Z(actions.get(headerIndex + offset))) {
            count++;
         }
      }

      return count;
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_q_StreamGate;
   }

   @Override
   public String getDisplayName() {
      String name = this.label != null && !this.label.isBlank() ? this.label.trim() : "Race";
      List<String> conditions = this.init_List_2();
      String trigger = conditions.isEmpty() ? "instant" : (conditions.size() == 1 ? conditions.get(0) : conditions.size() + " conditions");
      List<String> actions = this.write();
      String action = actions.size() == 1 ? actions.get(0) : actions.size() + " actions";
      return name + " [" + trigger + " -> " + action + "]";
   }

   @Override
   public String getIcon() {
      return "Rce";
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick,
      run,
      get,
      set,
      put,
      add,
      clear,
      reset,
      start,
      stop,
      next,
      prev,
      valid;
   }
}
