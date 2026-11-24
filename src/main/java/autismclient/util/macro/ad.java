package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class ad implements E, MacroAction {
   public boolean sneak = true;
   private boolean init = true;
   public boolean persistent = false;
   public volatile D packetOrder = D.init;

   public ad() {
   }

   public ad(boolean sneak) {
      this.sneak = sneak;
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.options != null && mc.options.keyShift != null) {
         mc.options.keyShift.setDown(this.sneak);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putBoolean("sneak", this.sneak);
      tag.putBoolean("persistent", this.persistent);
      tag.putBoolean("enabled", this.init);
      E.init_V_CompoundTag_D(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("sneak")) {
         this.sneak = tag.getBooleanOr("sneak", true);
      }

      if (tag.contains("persistent")) {
         this.persistent = tag.getBooleanOr("persistent", false);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = E.init_D_CompoundTag(tag);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.pool;
   }

   @Override
   public D init_D() {
      return this.packetOrder;
   }

   @Override
   public String getDisplayName() {
      return "Sneak: " + (this.sneak ? "ON" : "OFF") + (this.persistent ? " [P]" : "");
   }

   @Override
   public String getIcon() {
      return "SNK";
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
