package autismclient.util.macro;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class B implements MacroAction {
   public B.Inner_CachePool mode = B.Inner_CachePool.CANCEL;
   public B.Inner_BufferHelper scope = B.Inner_BufferHelper.END_MARKER;
   public B.Inner_DataUtil direction = B.Inner_DataUtil.ANY;
   public String gateId = "auto";
   public ArrayList<String> packetNames = new ArrayList<>();
   public B.Inner_StreamUtil durationMode = B.Inner_StreamUtil.UNTIL_DISABLED;
   public int durationValue = 0;
   public String untilPacketName = "";
   public String untilPacketField = "";
   public String untilPacketOperator = aM.Inner_StreamUtil.EXISTS.name();
   public String untilPacketValue = "";
   public String untilGuiType = "ANY";
   public String untilGuiTitle = "";
   public String untilInventoryCondition = aK.Inner_DataUtil.ITEM_EXISTS.name();
   public String untilInventoryItem = "";
   public int untilInventoryCount = 1;
   public int untilInventorySlot = 0;
   public boolean flushOnDisable = true;

   @Override
   public void execute(Minecraft mc) {
      C.init(this, MacroExecutor.load_J());
   }

   ArrayList<String> init() {
      return new ArrayList<>(this.packetNames);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.PACKET_GATE;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "PACKET_GATE");
      tag.putString("mode", this.mode.name());
      tag.putString("scope", this.scope == null ? B.Inner_BufferHelper.END_MARKER.name() : this.scope.name());
      tag.putString("direction", B.Inner_DataUtil.ANY.name());
      tag.putString("gateId", this.gateId);
      tag.put("packetNames", i.init(this.packetNames));
      tag.putString("durationMode", this.durationMode == null ? B.Inner_StreamUtil.UNTIL_DISABLED.name() : this.durationMode.name());
      tag.putInt("durationValue", Math.max(0, this.durationValue));
      tag.putString("untilPacketName", this.untilPacketName == null ? "" : this.untilPacketName);
      tag.putString("untilPacketField", this.untilPacketField == null ? "" : this.untilPacketField);
      tag.putString("untilPacketOperator", this.untilPacketOperator == null ? aM.Inner_StreamUtil.EXISTS.name() : this.untilPacketOperator);
      tag.putString("untilPacketValue", this.untilPacketValue == null ? "" : this.untilPacketValue);
      tag.putString("untilGuiType", this.untilGuiType == null ? "ANY" : this.untilGuiType);
      tag.putString("untilGuiTitle", this.untilGuiTitle == null ? "" : this.untilGuiTitle);
      tag.putString("untilInventoryCondition", this.untilInventoryCondition == null ? aK.Inner_DataUtil.ITEM_EXISTS.name() : this.untilInventoryCondition);
      tag.putString("untilInventoryItem", this.untilInventoryItem == null ? "" : this.untilInventoryItem);
      tag.putInt("untilInventoryCount", Math.max(1, this.untilInventoryCount));
      tag.putInt("untilInventorySlot", Math.max(0, this.untilInventorySlot));
      tag.putBoolean("flushOnDisable", this.flushOnDisable);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = i.init(B.Inner_CachePool.class, tag.getStringOr("mode", "CANCEL"), B.Inner_CachePool.CANCEL);
      String rawScope = tag.getStringOr("scope", "END_MARKER");
      if ("MACRO_RUN".equalsIgnoreCase(rawScope)) {
         rawScope = B.Inner_BufferHelper.MACRO_PASS.name();
      }

      this.scope = i.init(B.Inner_BufferHelper.class, rawScope, B.Inner_BufferHelper.END_MARKER);
      this.direction = B.Inner_DataUtil.ANY;
      this.gateId = tag.getStringOr("gateId", "auto");
      this.packetNames = i.init(tag.getList("packetNames").orElse(new ListTag()));
      this.durationMode = i.init(B.Inner_StreamUtil.class, tag.getStringOr("durationMode", "UNTIL_DISABLED"), B.Inner_StreamUtil.UNTIL_DISABLED);
      this.durationValue = Math.max(0, tag.getIntOr("durationValue", 0));
      this.untilPacketName = tag.getStringOr("untilPacketName", "");
      this.untilPacketField = tag.getStringOr("untilPacketField", "");
      this.untilPacketOperator = tag.getStringOr("untilPacketOperator", aM.Inner_StreamUtil.EXISTS.name());
      this.untilPacketValue = tag.getStringOr("untilPacketValue", "");
      this.untilGuiType = tag.getStringOr("untilGuiType", "ANY");
      this.untilGuiTitle = tag.getStringOr("untilGuiTitle", "");
      this.untilInventoryCondition = tag.getStringOr("untilInventoryCondition", aK.Inner_DataUtil.ITEM_EXISTS.name());
      this.untilInventoryItem = tag.getStringOr("untilInventoryItem", "");
      this.untilInventoryCount = Math.max(1, tag.getIntOr("untilInventoryCount", 1));
      this.untilInventorySlot = Math.max(0, tag.getIntOr("untilInventorySlot", 0));
      this.flushOnDisable = tag.getBooleanOr("flushOnDisable", true);
   }

   @Override
   public String getDisplayName() {
      String target = this.packetNames.isEmpty() ? "packets" : String.join(", ", this.packetNames);
      String suffix = this.scope == B.Inner_BufferHelper.MACRO_PASS ? " (pass)" : "";
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

   public enum Inner_BufferHelper {
      END_MARKER,
      MACRO_PASS;
   }

   public enum Inner_CachePool {
      CANCEL,
      DELAY,
      ALLOW_ONLY,
      DISABLE_GATE;
   }

   public enum Inner_DataUtil {
      C2S,
      S2C,
      ANY;
   }

   public enum Inner_StreamUtil {
      UNTIL_DISABLED,
      TICKS,
      MS,
      UNTIL_PACKET,
      UNTIL_GUI,
      UNTIL_INVENTORY;
   }
}
