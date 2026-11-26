package autismclient.util.macro;

import autismclient.util.bo;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class RaceAction implements MacroAction {
   public String label = "Race";
   public MacroAction triggerAction = new DelayAction(0);
   public int bodyCount = 0;
   public int timeoutMs = 10000;
   public String conditionType = RaceAction.Inner_DataUtil.WAIT_PACKET.name();
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
      tag.putString("conditionType", this.conditionType == null ? RaceAction.Inner_DataUtil.WAIT_PACKET.name() : this.conditionType);
      tag.putString("actionType", this.actionType == null ? StreamGate.PACKET_CLICK.name() : this.actionType);
      tag.put("raceSteps", i.init(this.read()));
      tag.put("conditionTypes", i.init(this.init_List_2()));
      tag.put("actionTypes", i.init(this.write()));
      MacroAction trigger = this.triggerAction == null ? new DelayAction(0) : this.triggerAction;
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
      this.conditionType = read_Str(tag.getStringOr("conditionType", tag.getStringOr("triggerType", RaceAction.Inner_DataUtil.WAIT_PACKET.name())));
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

      this.conditionTypes.replaceAll(RaceAction::read_Str);
      if (this.raceSteps.isEmpty()) {
         for (String type : this.conditionTypes) {
            this.raceSteps.add("CONDITION:" + read_Str(type));
         }

         for (String type : this.actionTypes) {
            this.raceSteps.add("ACTION:" + write(type));
         }
      } else {
         this.raceSteps.replaceAll(RaceAction::load_Str);
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
         this.triggerAction = new DelayAction(0);
      }
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public boolean load_Z() {
      return this.load;
   }

   public PacketClickAction init_z() {
      if (this.init_bo_StreamUtil == null) {
         return null;
      }

      PacketClickAction action = new PacketClickAction(this.init_bo_StreamUtil, this.init_I, false);
      this.init_bo_StreamUtil = null;
      return action;
   }

   public void init_V_3() {
      this.init_Z = false;
      this.load = false;
      this.init_bo_StreamUtil = null;
   }

   public static boolean init_Z(MacroAction action) {
      return action instanceof DelayAction
         || action instanceof WaitForPacketAction
         || action instanceof WaitForHealthAction
         || action instanceof WaitForBlockAction
         || action instanceof WaitForGuiAction
         || action instanceof WaitForCooldownAction
         || action instanceof WaitPosAction
         || action instanceof WaitForChatAction
         || action instanceof WaitForEntityAction
         || action instanceof WaitForSlotChangeAction
         || action instanceof WaitForSoundAction
         || action instanceof WaitPacketMatchAction
         || action instanceof WaitInventoryPredicateAction
         || action instanceof WaitDurabilityAction
         || action instanceof WaitFreeSlotsAction
         || action instanceof WaitMovementAction
         || action instanceof WaitForLanStepAction
         || action instanceof WaitForMacroStepAction
         || action instanceof TickSyncAction
         || action instanceof RevisionSyncAction
         || action instanceof ServerTickSyncAction
         || action instanceof WaitForWorldChangeAction
         || action instanceof WaitForPositionDeltaAction
         || action instanceof WaitForTeleportAction
         || action instanceof WaitGamemodeChangeAction;
   }

   public static boolean load(MacroAction action) {
      if (action == null) {
         return false;
      } else {
         return init_Z(action)
            ? false
            : !(action instanceof RaceAction)
               && !(action instanceof ReportAction)
               && !(action instanceof MultiAction)
               && !(action instanceof RepeatAction)
               && !(action instanceof DisconnectAction)
               && !(action instanceof JoinAction)
               && !(action instanceof GoToAction)
               && !(action instanceof MoveAction);
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
            var10000 = new PacketClickAction();
            break;
         case SEND_CHAT:
            var10000 = new SendChatAction();
            break;
         case PAYLOAD:
            var10000 = new PayloadAction();
            break;
         case CLOSE_GUI:
            var10000 = new CloseGuiAction();
            break;
         case CLICK:
            var10000 = new ClickAction();
            break;
         case USE_ITEM:
            var10000 = new UseItemAction();
            break;
         case ITEM:
            var10000 = new ItemAction();
            break;
         case PICK_UP_ALL:
            var10000 = new PickUpAllAction();
            break;
         case XCARRY:
            var10000 = new XCarryAction();
            break;
         case DROP:
            var10000 = new DropAction();
            break;
         case SELECT_SLOT:
            var10000 = new SelectSlotAction();
            break;
         case SWAP_SLOTS:
            var10000 = new SwapSlotsAction();
            break;
         case SEND_PACKET:
            var10000 = new SendPacketAction();
            break;
         case INVENTORY:
            var10000 = new InventoryAction();
            break;
         case RESTORE_GUI:
            var10000 = new RestoreGuiAction();
            break;
         case SAVE_GUI:
            var10000 = new SaveGuiAction();
            break;
         case DESYNC:
            var10000 = new DesyncAction();
            break;
         case NBT_BOOK:
            var10000 = new NbtBookAction();
            break;
         case PAY:
            var10000 = new PayAction();
            break;
         case TRADE:
            var10000 = new TradeAction();
            break;
         case INSTA_BREAK:
            var10000 = new InstaBreakAction();
            break;
         case BREAK:
            var10000 = new BreakAction();
            break;
         case TOGGLE_MODULE:
            var10000 = new ak();
            break;
         case START_MACRO:
            var10000 = new StartMacroAction();
            break;
         case TOOLS:
            var10000 = new ToolsAction();
            break;
         case STOP_MACRO:
            var10000 = new StopMacroAction();
            break;
         case SNEAK:
            var10000 = new SneakAction();
            break;
         case JUMP:
            var10000 = new JumpAction();
            break;
         case SPRINT:
            var10000 = new SprintAction();
            break;
         case PACKET_GATE:
            var10000 = new PacketGateAction();
            break;
         case PACKET_BURST:
            var10000 = new PacketBurstAction();
            break;
         case CONTAINER_CLICK_SEQUENCE:
            var10000 = new ContainerClickSequenceAction();
            break;
         case ASSERT:
            var10000 = new AssertAction();
            break;
         case USE_ITEM_PHASE:
            var10000 = new UseItemPhaseAction();
            break;
         case SEND_COMMAND_PACKET:
            var10000 = new SendCommandPacketAction();
            break;
         case SIGN_EDIT:
            var10000 = new SignEditAction();
            break;
         case MACRO_VARIABLES:
            var10000 = new MacroVariablesAction();
            break;
         case FAKE_GAMEMODE:
            var10000 = new FakeGamemodeAction();
            break;
         case BUNDLE_DUPE_V2:
            var10000 = new BundleDupeV2Action();
            break;
         case VCLIP:
            var10000 = new VClipAction();
            break;
         case HCLIP:
            var10000 = new HClipAction();
            break;
         case TP:
            var10000 = new PacedTpAction();
            break;
         case MINE:
            var10000 = new MineAction();
            break;
         default:
            var10000 = new PacketClickAction();
      }

      MacroAction action = var10000;
      return load(action) ? action : new PacketClickAction();
   }

   public static RaceAction.Inner_DataUtil init_L_DataUtil(MacroAction action) {
      if (action instanceof WaitForGuiAction) {
         return RaceAction.Inner_DataUtil.WAIT_GUI;
      } else if (action instanceof WaitForChatAction) {
         return RaceAction.Inner_DataUtil.WAIT_CHAT;
      } else if (action instanceof WaitForPacketAction) {
         return RaceAction.Inner_DataUtil.WAIT_PACKET;
      } else if (action instanceof WaitForHealthAction) {
         return RaceAction.Inner_DataUtil.WAIT_HEALTH;
      } else if (action instanceof WaitForSlotChangeAction) {
         return RaceAction.Inner_DataUtil.WAIT_SLOT_CHANGE;
      } else if (action instanceof WaitForBlockAction) {
         return RaceAction.Inner_DataUtil.WAIT_BLOCK;
      } else if (action instanceof WaitForEntityAction) {
         return RaceAction.Inner_DataUtil.WAIT_ENTITY;
      } else if (action instanceof WaitForCooldownAction) {
         return RaceAction.Inner_DataUtil.WAIT_COOLDOWN;
      } else if (action instanceof WaitForSoundAction) {
         return RaceAction.Inner_DataUtil.WAIT_SOUND;
      } else if (action instanceof WaitPacketMatchAction) {
         return RaceAction.Inner_DataUtil.WAIT_PACKET_MATCH;
      } else if (action instanceof WaitInventoryPredicateAction) {
         return RaceAction.Inner_DataUtil.WAIT_INVENTORY_PREDICATE;
      } else if (action instanceof WaitDurabilityAction) {
         return RaceAction.Inner_DataUtil.WAIT_DURABILITY;
      } else if (action instanceof WaitFreeSlotsAction) {
         return RaceAction.Inner_DataUtil.WAIT_FREE_SLOTS;
      } else if (action instanceof WaitMovementAction) {
         return RaceAction.Inner_DataUtil.WAIT_MOVEMENT;
      } else if (action instanceof WaitForLanStepAction) {
         return RaceAction.Inner_DataUtil.WAIT_LAN_STEP;
      } else if (action instanceof WaitForMacroStepAction) {
         return RaceAction.Inner_DataUtil.WAIT_MACRO_STEP;
      } else if (action instanceof WaitGamemodeChangeAction) {
         return RaceAction.Inner_DataUtil.WAIT_GAMEMODE_CHANGE;
      } else if (action instanceof WaitPosAction || action instanceof WaitForWorldChangeAction || action instanceof WaitForPositionDeltaAction || action instanceof WaitForTeleportAction) {
         return RaceAction.Inner_DataUtil.WAIT_MOVEMENT;
      } else if (action instanceof TickSyncAction) {
         return RaceAction.Inner_DataUtil.TICK_SYNC;
      } else if (action instanceof RevisionSyncAction) {
         return RaceAction.Inner_DataUtil.REVISION_SYNC;
      } else {
         return action instanceof ServerTickSyncAction ? RaceAction.Inner_DataUtil.SERVER_TICK_SYNC : RaceAction.Inner_DataUtil.DELAY;
      }
   }

   public static MacroAction read_MacroAction(String typeName) {
      return init(init_L_DataUtil(typeName));
   }

   private static MacroAction init(RaceAction.Inner_DataUtil type) {
      MacroAction var10000;
      switch (type) {
         case DELAY:
            var10000 = new DelayAction(0);
            break;
         case WAIT_GUI:
            var10000 = new WaitForGuiAction();
            break;
         case WAIT_CHAT:
            var10000 = new WaitForChatAction();
            break;
         case WAIT_PACKET:
            var10000 = new WaitForPacketAction();
            break;
         case WAIT_HEALTH:
            var10000 = new WaitForHealthAction();
            break;
         case WAIT_SLOT_CHANGE:
            var10000 = new WaitForSlotChangeAction();
            break;
         case WAIT_BLOCK:
            var10000 = new WaitForBlockAction();
            break;
         case WAIT_ENTITY:
            var10000 = new WaitForEntityAction();
            break;
         case WAIT_COOLDOWN:
            var10000 = new WaitForCooldownAction();
            break;
         case WAIT_POS:
            var10000 = new WaitPosAction();
            break;
         case WAIT_SOUND:
            var10000 = new WaitForSoundAction();
            break;
         case WAIT_PACKET_MATCH:
            var10000 = new WaitPacketMatchAction();
            break;
         case WAIT_INVENTORY_PREDICATE:
            var10000 = new WaitInventoryPredicateAction();
            break;
         case WAIT_DURABILITY:
            var10000 = new WaitDurabilityAction();
            break;
         case WAIT_FREE_SLOTS:
            var10000 = new WaitFreeSlotsAction();
            break;
         case WAIT_MOVEMENT:
            var10000 = new WaitMovementAction();
            break;
         case WAIT_LAN_STEP:
            var10000 = new WaitForLanStepAction();
            break;
         case WAIT_MACRO_STEP:
            var10000 = new WaitForMacroStepAction();
            break;
         case TICK_SYNC:
            var10000 = new TickSyncAction();
            break;
         case REVISION_SYNC:
            var10000 = new RevisionSyncAction();
            break;
         case SERVER_TICK_SYNC:
            var10000 = new ServerTickSyncAction();
            break;
         case WAIT_WORLD_CHANGE:
            var10000 = new WaitForWorldChangeAction();
            break;
         case WAIT_POSITION_DELTA:
            var10000 = new WaitForPositionDeltaAction();
            break;
         case WAIT_TELEPORT:
            var10000 = new WaitForTeleportAction();
            break;
         case WAIT_GAMEMODE_CHANGE:
            var10000 = new WaitGamemodeChangeAction();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static MacroAction init(CompoundTag tag) {
      RaceAction.Inner_DataUtil type = init_L_DataUtil(tag.getStringOr("triggerType", RaceAction.Inner_DataUtil.DELAY.name()));
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
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_POSITION_DELTA.name());
            flat.putDouble("distance", tag.getDoubleOr("positionTolerance", 5.0));
            break;
         case "WORLD_CHANGE":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_WORLD_CHANGE.name());
            break;
         case "GUI_OPEN_BY_TITLE":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_GUI.name());
            flat.putString("waitMode", "OPEN");
            flat.putString("guiTitle", tag.getStringOr("guiTitleSubstr", ""));
            break;
         case "GUI_CLOSE_BY_TITLE":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_GUI.name());
            flat.putString("waitMode", "CLOSE");
            flat.putString("guiTitle", tag.getStringOr("guiTitleSubstr", ""));
            break;
         case "TELEPORT_PACKET":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_PACKET.name());
            flat.putString("packetName", "S2C:ClientboundPlayerPositionPacket");
            break;
         case "CHAT_MATCH":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_CHAT.name());
            flat.putString("pattern", tag.getStringOr("chatPattern", ""));
            flat.putBoolean("useRegex", tag.getBooleanOr("chatUseRegex", false));
            break;
         case "S2C_PACKET_CLASS":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_PACKET.name());
            flat.putString("packetName", "S2C:" + tag.getStringOr("s2cPacketClassName", ""));
            break;
         case "HEALTH_BELOW":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_HEALTH.name());
            flat.putDouble("healthThreshold", tag.getDoubleOr("healthBelowThreshold", 10.0));
            flat.putString("comparison", "Drops Below");
            break;
         case "SLOT_CHANGE":
         case "ITEM_FOUND":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_SLOT_CHANGE.name());
            break;
         case "BLOCK_STATE":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_BLOCK.name());
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
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_ENTITY.name());
            init(tag, flat, "entityNearIds", "entityIds");
            flat.putDouble("radius", tag.getDoubleOr("entityNearRadius", 8.0));
            break;
         case "COOLDOWN_READY":
            flat.putString("triggerType", RaceAction.Inner_DataUtil.WAIT_COOLDOWN.name());
            flat.putString("itemName", tag.getStringOr("cooldownItemName", ""));
            break;
         default:
            flat.putString("triggerType", RaceAction.Inner_DataUtil.DELAY.name());
            flat.putInt("delayMs", tag.getIntOr("manualDelayMs", 0));
      }

      return init(flat);
   }

   private static void init_V_CompoundTag_MacroAction(CompoundTag tag, MacroAction action) {
      RaceAction.Inner_DataUtil type = init_L_DataUtil(action);
      tag.putString("triggerType", type.name());
      CompoundTag src = autismclient.util.aM.init_CompoundTag_MacroAction(action);

      for (String key : src.keySet()) {
         if (!"type".equals(key) && !tag.contains(key)) {
            tag.put(key, src.get(key));
         }
      }

      if (action instanceof WaitForBlockAction block) {
         tag.putInt("blockX", block.blockPos == null ? 0 : block.blockPos.getX());
         tag.putInt("blockY", block.blockPos == null ? 0 : block.blockPos.getY());
         tag.putInt("blockZ", block.blockPos == null ? 0 : block.blockPos.getZ());
      }

      if (action instanceof WaitForEntityAction entity) {
         tag.putString("entityCheckMode", entity.checkMode == null ? "NEAR_PLAYER" : entity.checkMode.name());
         tag.putDouble("entityX", entity.f_x_D);
         tag.putDouble("entityY", entity.f_y_D);
         tag.putDouble("entityZ", entity.f_z_D);
      }
   }

   private static RaceAction.Inner_DataUtil init_L_DataUtil(String raw) {
      try {
         return RaceAction.Inner_DataUtil.valueOf(raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ignored) {
         return RaceAction.Inner_DataUtil.DELAY;
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
            var10000 = RaceAction.Inner_DataUtil.WAIT_MOVEMENT.name();
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
            RaceAction.Inner_DataUtil.valueOf(condition);
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
            if (action instanceof RaceAction || action instanceof ReportAction) {
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
