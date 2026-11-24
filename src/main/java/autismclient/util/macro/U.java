package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class U implements MacroAction {
   private boolean init = true;
   public boolean closeAfter = false;
   public boolean sendPacket = true;

   @Override
   public void execute(Minecraft mc) {
      if (autismclient.util.af.init(mc, true) && this.closeAfter) {
         autismclient.util.af.init(mc, this.sendPacket, false);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.scan;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SAVE_GUI");
      tag.putBoolean("enabled", this.init);
      tag.putBoolean("closeAfter", this.closeAfter);
      tag.putBoolean("sendPacket", this.sendPacket);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      if (tag.contains("closeAfter")) {
         this.closeAfter = tag.getBooleanOr("closeAfter", false);
      }

      if (tag.contains("sendPacket")) {
         this.sendPacket = tag.getBooleanOr("sendPacket", true);
      }
   }

   @Override
   public String getDisplayName() {
      if (!this.closeAfter) {
         return "Save GUI";
      } else {
         return this.sendPacket ? "Save GUI (close)" : "Save GUI (close, desync)";
      }
   }

   @Override
   public String getIcon() {
      return "S";
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
