package autismclient.util.macro;

import autismclient.util.ck;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SendToggleAction implements MacroAction {
   public SendToggleAction.Inner_DataUtil mode = SendToggleAction.Inner_DataUtil.ENABLE;
   private boolean init = true;

   public void init() {
      this.mode = SendToggleAction.Inner_DataUtil.values()[(this.mode.ordinal() + 1) % SendToggleAction.Inner_DataUtil.values().length];
   }

   public void load() {
      this.mode = SendToggleAction.Inner_DataUtil.values()[(this.mode.ordinal() - 1 + SendToggleAction.Inner_DataUtil.values().length) % SendToggleAction.Inner_DataUtil.values().length];
   }

   @Override
   public void execute(Minecraft mc) {
      ck shared = ck.init_ck();
      shared.read_V_Z(this.mode == SendToggleAction.Inner_DataUtil.ENABLE);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.SEND_TOGGLE;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SEND_TOGGLE");
      tag.putString("mode", this.mode.name());
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = SendToggleAction.Inner_DataUtil.valueOf(tag.getStringOr("mode", "ENABLE"));
         } catch (IllegalArgumentException ignored) {
            this.mode = SendToggleAction.Inner_DataUtil.ENABLE;
         }
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      return this.mode == SendToggleAction.Inner_DataUtil.ENABLE ? "Send Pkts: ON" : "Send Pkts: OFF";
   }

   @Override
   public String getIcon() {
      return "T";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      ENABLE,
      DISABLE;
   }
}
