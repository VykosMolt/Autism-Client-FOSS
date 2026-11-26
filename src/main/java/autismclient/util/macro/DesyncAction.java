package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class DesyncAction implements MacroAction {
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      autismclient.util.GuiDesyncHelper.read(mc, false);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.DESYNC;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "DESYNC");
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      return "Desync";
   }

   @Override
   public String getIcon() {
      return "~";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }
}
