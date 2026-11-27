package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SprintAction implements PacketOrderAware, MacroAction {
   public boolean sprint = true;
   private boolean init = true;
   public boolean persistent = false;
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;

   public SprintAction() {
   }

   public SprintAction(boolean sprint) {
      this.sprint = sprint;
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.options != null && mc.options.keySprint != null) {
         mc.options.keySprint.setDown(this.sprint);
      }

      if (mc.player != null) {
         mc.player.setSprinting(this.sprint);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putBoolean("sprint", this.sprint);
      tag.putBoolean("persistent", this.persistent);
      tag.putBoolean("enabled", this.init);
      PacketOrderAware.writePacketOrder(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("sprint")) {
         this.sprint = tag.getBooleanOr("sprint", true);
      }

      if (tag.contains("persistent")) {
         this.persistent = tag.getBooleanOr("persistent", false);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = PacketOrderAware.readPacketOrder(tag);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SPRINT;
   }

   @Override
   public PacketOrderMode getPacketOrder() {
      return this.packetOrder;
   }

   @Override
   public String getDisplayName() {
      return "Sprint: " + (this.sprint ? "ON" : "OFF") + (this.persistent ? " [P]" : "");
   }

   @Override
   public String getIcon() {
      return "SPR";
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
