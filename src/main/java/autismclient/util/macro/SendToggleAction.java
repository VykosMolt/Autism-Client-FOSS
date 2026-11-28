package autismclient.util.macro;

import autismclient.util.AutismSessionState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SendToggleAction implements MacroAction {
   public SendToggleAction.ToggleMode mode = SendToggleAction.ToggleMode.ENABLE;
   private boolean enabled = true;

   public void init() {
      this.mode = SendToggleAction.ToggleMode.values()[(this.mode.ordinal() + 1) % SendToggleAction.ToggleMode.values().length];
   }

   public void load() {
      this.mode = SendToggleAction.ToggleMode.values()[(this.mode.ordinal() - 1 + SendToggleAction.ToggleMode.values().length) % SendToggleAction.ToggleMode.values().length];
   }

   @Override
   public void execute(Minecraft mc) {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setSendPacketsEnabled(this.mode == SendToggleAction.ToggleMode.ENABLE);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SEND_TOGGLE;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SEND_TOGGLE");
      tag.putString("mode", this.mode.name());
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = SendToggleAction.ToggleMode.valueOf(tag.getStringOr("mode", "ENABLE"));
         } catch (IllegalArgumentException ignored) {
            this.mode = SendToggleAction.ToggleMode.ENABLE;
         }
      }

      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      return this.mode == SendToggleAction.ToggleMode.ENABLE ? "Send Pkts: ON" : "Send Pkts: OFF";
   }

   @Override
   public String getIcon() {
      return "T";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   public enum ToggleMode {
      ENABLE,
      DISABLE;
   }
}
