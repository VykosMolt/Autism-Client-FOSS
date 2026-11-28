package autismclient.util.macro;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class PacketGateAction implements MacroAction {
   public PacketGateAction.GateMode mode = PacketGateAction.GateMode.CANCEL;
   public PacketGateAction.GateScope scope = PacketGateAction.GateScope.END_MARKER;
   public PacketGateAction.GateDirection direction = PacketGateAction.GateDirection.ANY;
   public String gateId = "auto";
   public ArrayList<String> packetNames = new ArrayList<>();
   public PacketGateAction.DurationMode durationMode = PacketGateAction.DurationMode.UNTIL_DISABLED;
   public int durationValue = 0;
   public String untilPacketName = "";
   public String untilPacketField = "";
   public String untilPacketOperator = WaitPacketMatchAction.Inner_StreamUtil.EXISTS.name();
   public String untilPacketValue = "";
   public String untilGuiType = "ANY";
   public String untilGuiTitle = "";
   public String untilInventoryCondition = WaitInventoryPredicateAction.InventoryCondition.ITEM_EXISTS.name();
   public String untilInventoryItem = "";
   public int untilInventoryCount = 1;
   public int untilInventorySlot = 0;
   public boolean flushOnDisable = true;

   @Override
   public void execute(Minecraft mc) {
      PacketGateManager.init(this, MacroExecutor.getCurrentRunId());
   }

   ArrayList<String> init() {
      return new ArrayList<>(this.packetNames);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PACKET_GATE;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "PACKET_GATE");
      tag.putString("mode", this.mode.name());
      tag.putString("scope", this.scope == null ? PacketGateAction.GateScope.END_MARKER.name() : this.scope.name());
      tag.putString("direction", PacketGateAction.GateDirection.ANY.name());
      tag.putString("gateId", this.gateId);
      tag.put("packetNames", MacroNbtCodecHelper.toStringListTag(this.packetNames));
      tag.putString("durationMode", this.durationMode == null ? PacketGateAction.DurationMode.UNTIL_DISABLED.name() : this.durationMode.name());
      tag.putInt("durationValue", Math.max(0, this.durationValue));
      tag.putString("untilPacketName", this.untilPacketName == null ? "" : this.untilPacketName);
      tag.putString("untilPacketField", this.untilPacketField == null ? "" : this.untilPacketField);
      tag.putString("untilPacketOperator", this.untilPacketOperator == null ? WaitPacketMatchAction.Inner_StreamUtil.EXISTS.name() : this.untilPacketOperator);
      tag.putString("untilPacketValue", this.untilPacketValue == null ? "" : this.untilPacketValue);
      tag.putString("untilGuiType", this.untilGuiType == null ? "ANY" : this.untilGuiType);
      tag.putString("untilGuiTitle", this.untilGuiTitle == null ? "" : this.untilGuiTitle);
      tag.putString("untilInventoryCondition", this.untilInventoryCondition == null ? WaitInventoryPredicateAction.InventoryCondition.ITEM_EXISTS.name() : this.untilInventoryCondition);
      tag.putString("untilInventoryItem", this.untilInventoryItem == null ? "" : this.untilInventoryItem);
      tag.putInt("untilInventoryCount", Math.max(1, this.untilInventoryCount));
      tag.putInt("untilInventorySlot", Math.max(0, this.untilInventorySlot));
      tag.putBoolean("flushOnDisable", this.flushOnDisable);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = MacroNbtCodecHelper.parseEnum(PacketGateAction.GateMode.class, tag.getStringOr("mode", "CANCEL"), PacketGateAction.GateMode.CANCEL);
      String rawScope = tag.getStringOr("scope", "END_MARKER");
      if ("MACRO_RUN".equalsIgnoreCase(rawScope)) {
         rawScope = PacketGateAction.GateScope.MACRO_PASS.name();
      }

      this.scope = MacroNbtCodecHelper.parseEnum(PacketGateAction.GateScope.class, rawScope, PacketGateAction.GateScope.END_MARKER);
      this.direction = PacketGateAction.GateDirection.ANY;
      this.gateId = tag.getStringOr("gateId", "auto");
      this.packetNames = MacroNbtCodecHelper.fromStringListTag(tag.getList("packetNames").orElse(new ListTag()));
      this.durationMode = MacroNbtCodecHelper.parseEnum(PacketGateAction.DurationMode.class, tag.getStringOr("durationMode", "UNTIL_DISABLED"), PacketGateAction.DurationMode.UNTIL_DISABLED);
      this.durationValue = Math.max(0, tag.getIntOr("durationValue", 0));
      this.untilPacketName = tag.getStringOr("untilPacketName", "");
      this.untilPacketField = tag.getStringOr("untilPacketField", "");
      this.untilPacketOperator = tag.getStringOr("untilPacketOperator", WaitPacketMatchAction.Inner_StreamUtil.EXISTS.name());
      this.untilPacketValue = tag.getStringOr("untilPacketValue", "");
      this.untilGuiType = tag.getStringOr("untilGuiType", "ANY");
      this.untilGuiTitle = tag.getStringOr("untilGuiTitle", "");
      this.untilInventoryCondition = tag.getStringOr("untilInventoryCondition", WaitInventoryPredicateAction.InventoryCondition.ITEM_EXISTS.name());
      this.untilInventoryItem = tag.getStringOr("untilInventoryItem", "");
      this.untilInventoryCount = Math.max(1, tag.getIntOr("untilInventoryCount", 1));
      this.untilInventorySlot = Math.max(0, tag.getIntOr("untilInventorySlot", 0));
      this.flushOnDisable = tag.getBooleanOr("flushOnDisable", true);
   }

   @Override
   public String getDisplayName() {
      String target = this.packetNames.isEmpty() ? "packets" : String.join(", ", this.packetNames);
      String suffix = this.scope == PacketGateAction.GateScope.MACRO_PASS ? " (pass)" : "";
      String var10000;
      switch (this.mode) {
         case CANCEL:
            var10000 = "Gate cancel ANY " + target + suffix;
            break;
         case DELAY:
            var10000 = "Gate delay ANY " + target + suffix;
            break;
         case ALLOW_ONLY:
            var10000 = "Gate allow only ANY " + target + suffix;
            break;
         case DISABLE_GATE:
            var10000 = "Disable gate " + this.gateId;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public String getIcon() {
      return "G";
   }

   public enum GateScope {
      END_MARKER,
      MACRO_PASS;
   }

   public enum GateMode {
      CANCEL,
      DELAY,
      ALLOW_ONLY,
      DISABLE_GATE;
   }

   public enum GateDirection {
      C2S,
      S2C,
      ANY;
   }

   public enum DurationMode {
      UNTIL_DISABLED,
      TICKS,
      MS,
      UNTIL_PACKET,
      UNTIL_GUI,
      UNTIL_INVENTORY;
   }
}
