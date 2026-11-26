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
   public String conditionType = L.Inner_DataUtil.WAIT_PACKET.name();
   public String actionType = StreamGate.PACKET_CLICK.name();
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
      tag.putString("conditionType", this.conditionType == null ? L.Inner_DataUtil.WAIT_PACKET.name() : this.conditionType);
      tag.putString("actionType", this.actionType == null ? StreamGate.PACKET_CLICK.name() : this.actionType);
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
      this.conditionType = read_Str(tag.getStringOr("conditionType", tag.getStringOr("triggerType", L.Inner_DataUtil.WAIT_PACKET.name())));
      this.actionType = tag.getStringOr("actionType", StreamGate.PACKET_CLICK.name());
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
         this.actionTypes.add(StreamGate.PACKET_CLICK.name());
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
            this.actionTypes.add(StreamGate.PACKET_CLICK.name());
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
         out.add(this.actionType != null && !this.actionType.isBlank() ? this.actionType.trim().toUpperCase(Locale.ROOT) : StreamGate.PACKET_CLICK.name());
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
         type = StreamGate.PACKET_CLICK;
      }

      MacroAction var10000;
      switch (type) {
         case PACKET_CLICK:
            var10000 = new z();
            break;
         case SEND_CHAT:
            var10000 = new W();
            break;
         case PAYLOAD:
            var10000 = new H();
            break;
         case CLOSE_GUI:
            var10000 = new JsonUtil();
            break;
         case CLICK:
            var10000 = new NetHelper();
            break;
         case USE_ITEM:
            var10000 = new an();
            break;
         case ITEM:
            var10000 = new MapStore();
            break;
         case PICK_UP_ALL:
            var10000 = new I();
            break;
         case XCARRY:
            var10000 = new aQ();
            break;
         case DROP:
            var10000 = new PacketUtil();
            break;
         case SELECT_SLOT:
            var10000 = new V();
            break;
         case SWAP_SLOTS:
            var10000 = new ai();
            break;
         case SEND_PACKET:
            var10000 = new Y();
            break;
         case INVENTORY:
            var10000 = new RefQueue();
            break;
         case RESTORE_GUI:
            var10000 = new P();
            break;
         case SAVE_GUI:
            var10000 = new U();
            break;
         case DESYNC:
            var10000 = new TextFormat();
            break;
         case NBT_BOOK:
            var10000 = new u();
            break;
         case PAY:
            var10000 = new G();
            break;
         case TRADE:
            var10000 = new am();
            break;
         case INSTA_BREAK:
            var10000 = new MethodUtil();
            break;
         case BREAK:
            var10000 = new CachePool();
            break;
         case TOGGLE_MODULE:
            var10000 = new ak();
            break;
         case START_MACRO:
            var10000 = new af();
            break;
         case TOOLS:
            var10000 = new al();
            break;
         case STOP_MACRO:
            var10000 = new ag();
            break;
         case SNEAK:
            var10000 = new ad();
            break;
         case JUMP:
            var10000 = new NodeWalker();
            break;
         case SPRINT:
            var10000 = new ae();
            break;
         case PACKET_GATE:
            var10000 = new B();
            break;
         case PACKET_BURST:
            var10000 = new y();
            break;
         case CONTAINER_CLICK_SEQUENCE:
            var10000 = new StateCache();
            break;
         case ASSERT:
            var10000 = new DataUtil();
            break;
         case USE_ITEM_PHASE:
            var10000 = new ao();
            break;
         case SEND_COMMAND_PACKET:
            var10000 = new X();
            break;
         case SIGN_EDIT:
            var10000 = new ac();
            break;
         case MACRO_VARIABLES:
            var10000 = new o();
            break;
         case FAKE_GAMEMODE:
            var10000 = new IoWorker();
            break;
         case BUNDLE_DUPE_V2:
            var10000 = new BufferHelper();
            break;
         case VCLIP:
            var10000 = new ap();
            break;
         case HCLIP:
            var10000 = new TokenStore();
            break;
         case TP:
            var10000 = new w();
            break;
         case MINE:
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
         return L.Inner_DataUtil.WAIT_GUI;
      } else if (action instanceof au) {
         return L.Inner_DataUtil.WAIT_CHAT;
      } else if (action instanceof aB) {
         return L.Inner_DataUtil.WAIT_PACKET;
      } else if (action instanceof ay) {
         return L.Inner_DataUtil.WAIT_HEALTH;
      } else if (action instanceof aD) {
         return L.Inner_DataUtil.WAIT_SLOT_CHANGE;
      } else if (action instanceof at) {
         return L.Inner_DataUtil.WAIT_BLOCK;
      } else if (action instanceof aw) {
         return L.Inner_DataUtil.WAIT_ENTITY;
      } else if (action instanceof av) {
         return L.Inner_DataUtil.WAIT_COOLDOWN;
      } else if (action instanceof aE) {
         return L.Inner_DataUtil.WAIT_SOUND;
      } else if (action instanceof aM) {
         return L.Inner_DataUtil.WAIT_PACKET_MATCH;
      } else if (action instanceof aK) {
         return L.Inner_DataUtil.WAIT_INVENTORY_PREDICATE;
      } else if (action instanceof ar) {
         return L.Inner_DataUtil.WAIT_DURABILITY;
      } else if (action instanceof aH) {
         return L.Inner_DataUtil.WAIT_FREE_SLOTS;
      } else if (action instanceof aL) {
         return L.Inner_DataUtil.WAIT_MOVEMENT;
      } else if (action instanceof az) {
         return L.Inner_DataUtil.WAIT_LAN_STEP;
      } else if (action instanceof aA) {
         return L.Inner_DataUtil.WAIT_MACRO_STEP;
      } else if (action instanceof aI) {
         return L.Inner_DataUtil.WAIT_GAMEMODE_CHANGE;
      } else if (action instanceof aN || action instanceof aG || action instanceof aC || action instanceof aF) {
         return L.Inner_DataUtil.WAIT_MOVEMENT;
      } else if (action instanceof aj) {
         return L.Inner_DataUtil.TICK_SYNC;
      } else if (action instanceof Q) {
         return L.Inner_DataUtil.REVISION_SYNC;
      } else {
         return action instanceof aa ? L.Inner_DataUtil.SERVER_TICK_SYNC : L.Inner_DataUtil.DELAY;
      }
   }

   public static MacroAction read_MacroAction(String typeName) {
      return init(init_L_DataUtil(typeName));
   }

   private static MacroAction init(L.Inner_DataUtil type) {
      MacroAction var10000;
      switch (type) {
         case DELAY:
            var10000 = new EventBus(0);
            break;
         case WAIT_GUI:
            var10000 = new ax();
            break;
         case WAIT_CHAT:
            var10000 = new au();
            break;
         case WAIT_PACKET:
            var10000 = new aB();
            break;
         case WAIT_HEALTH:
            var10000 = new ay();
            break;
         case WAIT_SLOT_CHANGE:
            var10000 = new aD();
            break;
         case WAIT_BLOCK:
            var10000 = new at();
            break;
         case WAIT_ENTITY:
            var10000 = new aw();
            break;
         case WAIT_COOLDOWN:
            var10000 = new av();
            break;
         case WAIT_POS:
            var10000 = new aN();
            break;
         case WAIT_SOUND:
            var10000 = new aE();
            break;
         case WAIT_PACKET_MATCH:
            var10000 = new aM();
            break;
         case WAIT_INVENTORY_PREDICATE:
            var10000 = new aK();
            break;
         case WAIT_DURABILITY:
            var10000 = new ar();
            break;
         case WAIT_FREE_SLOTS:
            var10000 = new aH();
            break;
         case WAIT_MOVEMENT:
            var10000 = new aL();
            break;
         case WAIT_LAN_STEP:
            var10000 = new az();
            break;
         case WAIT_MACRO_STEP:
            var10000 = new aA();
            break;
         case TICK_SYNC:
            var10000 = new aj();
            break;
         case REVISION_SYNC:
            var10000 = new Q();
            break;
         case SERVER_TICK_SYNC:
            var10000 = new aa();
            break;
         case WAIT_WORLD_CHANGE:
            var10000 = new aG();
            break;
         case WAIT_POSITION_DELTA:
            var10000 = new aC();
            break;
         case WAIT_TELEPORT:
            var10000 = new aF();
            break;
         case WAIT_GAMEMODE_CHANGE:
            var10000 = new aI();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static MacroAction init(CompoundTag tag) {
      L.Inner_DataUtil type = init_L_DataUtil(tag.getStringOr("triggerType", L.Inner_DataUtil.DELAY.name()));
      MacroAction action = init(type);
      CompoundTag actionTag = autismclient.util.aM.init_CompoundTag_MacroAction(action);
      switch (type) {
         case DELAY:
            actionTag.putBoolean("useTicks", tag.getBooleanOr("useTicks", false));
            actionTag.putInt("delayMs", tag.getIntOr("delayMs", 0));
            actionTag.putInt("delayTicks", tag.getIntOr("delayTicks", 1));
            break;
         case WAIT_GUI:
            actionTag.putString("waitMode", tag.getStringOr("waitMode", "OPEN"));
            actionTag.putString("guiTitle", tag.getStringOr("guiTitle", ""));
            break;
         case WAIT_CHAT:
            actionTag.putString("pattern", tag.getStringOr("pattern", ""));
            actionTag.putBoolean("useRegex", tag.getBooleanOr("useRegex", false));
            actionTag.putInt("fuzzyPercent", tag.getIntOr("fuzzyPercent", 100));
            break;
         case WAIT_PACKET:
            actionTag.putString("packetName", tag.getStringOr("packetName", ""));
            init(tag, actionTag, "packetNames");
            break;
         case WAIT_HEALTH:
            actionTag.putFloat("healthThreshold", (float)tag.getDoubleOr("healthThreshold", 20.0));
            actionTag.putString("comparison", tag.getStringOr("comparison", "Drops Below"));
         case WAIT_SLOT_CHANGE:
         case WAIT_PACKET_MATCH:
         case WAIT_INVENTORY_PREDICATE:
         case WAIT_DURABILITY:
         case WAIT_FREE_SLOTS:
         case WAIT_MOVEMENT:
         case WAIT_LAN_STEP:
         case WAIT_MACRO_STEP:
         case TICK_SYNC:
         case REVISION_SYNC:
         case SERVER_TICK_SYNC:
         case WAIT_WORLD_CHANGE:
         default:
            break;
         case WAIT_BLOCK:
            actionTag.putString("checkMode", tag.getStringOr("checkMode", "AT_POSITION"));
            actionTag.putString("waitBehavior", tag.getStringOr("waitBehavior", "PRESENT"));
            actionTag.putBoolean("anyBlock", tag.getBooleanOr("anyBlock", false));
            init(tag, actionTag, "blockIds");
            actionTag.putInt("x", tag.getIntOr("blockX", tag.getIntOr("x", 0)));
            actionTag.putInt("y", tag.getIntOr("blockY", tag.getIntOr("y", 0)));
            actionTag.putInt("z", tag.getIntOr("blockZ", tag.getIntOr("z", 0)));
            actionTag.putDouble("searchRadius", tag.getDoubleOr("searchRadius", 6.0));
            break;
         case WAIT_ENTITY:
            actionTag.putString("checkMode", tag.getStringOr("entityCheckMode", "RADIUS"));
            init(tag, actionTag, "entityIds");
            actionTag.putBoolean("centerOnPlayer", tag.getBooleanOr("centerOnPlayer", true));
            actionTag.putDouble("radius", tag.getDoubleOr("radius", 8.0));
            actionTag.putDouble("x", tag.getDoubleOr("entityX", 0.0));
            actionTag.putDouble("y", tag.getDoubleOr("entityY", 0.0));
            actionTag.putDouble("z", tag.getDoubleOr("entityZ", 0.0));
            break;
         case WAIT_COOLDOWN:
            actionTag.putString("itemName", tag.getStringOr("itemName", ""));
            actionTag.putBoolean("checkMainHand", tag.getBooleanOr("checkMainHand", true));
            break;
         case WAIT_POS:
            actionTag.putDouble("x", tag.getDoubleOr("x", 0.0));
            actionTag.putDouble("y", tag.getDoubleOr("y", 0.0));
            actionTag.putDouble("z", tag.getDoubleOr("z", 0.0));
            actionTag.putDouble("leeway", tag.getDoubleOr("leeway", 1.0));
            actionTag.putBoolean("checkRotation", tag.getBooleanOr("checkRotation", false));
            actionTag.putFloat("yaw", (float)tag.getDoubleOr("yaw", 0.0));
            actionTag.putFloat("pitch", (float)tag.getDoubleOr("pitch", 0.0));
            actionTag.putFloat("rotLeeway", (float)tag.getDoubleOr("rotLeeway", 5.0));
            break;
         case WAIT_SOUND:
            init(tag, actionTag, "soundIds");
            actionTag.putBoolean("checkDistance", tag.getBooleanOr("checkDistance", false));
            actionTag.putDouble("maxDistance", tag.getDoubleOr("maxDistance", 16.0));
            break;
         case WAIT_POSITION_DELTA:
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
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_POSITION_DELTA.name());
            flat.putDouble("distance", tag.getDoubleOr("positionTolerance", 5.0));
            break;
         case "WORLD_CHANGE":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_WORLD_CHANGE.name());
            break;
         case "GUI_OPEN_BY_TITLE":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_GUI.name());
            flat.putString("waitMode", "OPEN");
            flat.putString("guiTitle", tag.getStringOr("guiTitleSubstr", ""));
            break;
         case "GUI_CLOSE_BY_TITLE":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_GUI.name());
            flat.putString("waitMode", "CLOSE");
            flat.putString("guiTitle", tag.getStringOr("guiTitleSubstr", ""));
            break;
         case "TELEPORT_PACKET":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_PACKET.name());
            flat.putString("packetName", "S2C:ClientboundPlayerPositionPacket");
            break;
         case "CHAT_MATCH":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_CHAT.name());
            flat.putString("pattern", tag.getStringOr("chatPattern", ""));
            flat.putBoolean("useRegex", tag.getBooleanOr("chatUseRegex", false));
            break;
         case "S2C_PACKET_CLASS":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_PACKET.name());
            flat.putString("packetName", "S2C:" + tag.getStringOr("s2cPacketClassName", ""));
            break;
         case "HEALTH_BELOW":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_HEALTH.name());
            flat.putDouble("healthThreshold", tag.getDoubleOr("healthBelowThreshold", 10.0));
            flat.putString("comparison", "Drops Below");
            break;
         case "SLOT_CHANGE":
         case "ITEM_FOUND":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_SLOT_CHANGE.name());
            break;
         case "BLOCK_STATE":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_BLOCK.name());
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
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_ENTITY.name());
            init(tag, flat, "entityNearIds", "entityIds");
            flat.putDouble("radius", tag.getDoubleOr("entityNearRadius", 8.0));
            break;
         case "COOLDOWN_READY":
            flat.putString("triggerType", L.Inner_DataUtil.WAIT_COOLDOWN.name());
            flat.putString("itemName", tag.getStringOr("cooldownItemName", ""));
            break;
         default:
            flat.putString("triggerType", L.Inner_DataUtil.DELAY.name());
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
         return L.Inner_DataUtil.DELAY;
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
            var10000 = L.Inner_DataUtil.WAIT_MOVEMENT.name();
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
      return StreamGate.RACE;
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
      DELAY,
      WAIT_GUI,
      WAIT_CHAT,
      WAIT_PACKET,
      WAIT_HEALTH,
      WAIT_SLOT_CHANGE,
      WAIT_BLOCK,
      WAIT_ENTITY,
      WAIT_COOLDOWN,
      WAIT_POS,
      WAIT_SOUND,
      WAIT_PACKET_MATCH,
      WAIT_INVENTORY_PREDICATE,
      WAIT_DURABILITY,
      WAIT_FREE_SLOTS,
      WAIT_MOVEMENT,
      WAIT_LAN_STEP,
      WAIT_MACRO_STEP,
      TICK_SYNC,
      REVISION_SYNC,
      SERVER_TICK_SYNC,
      WAIT_WORLD_CHANGE,
      WAIT_POSITION_DELTA,
      WAIT_TELEPORT,
      WAIT_GAMEMODE_CHANGE;
   }
}
