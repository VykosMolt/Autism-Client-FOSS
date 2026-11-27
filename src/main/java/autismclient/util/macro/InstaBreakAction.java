package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class InstaBreakAction implements PacketOrderAware, AimTargetAction, MacroAction, GuiWaitAware {
   public BlockPos blockPos = BlockPos.ZERO;
   public Direction direction = Direction.UP;
   public int delayTicks = 0;
   public int times = 1;
   public boolean autoPickaxe = true;
   public boolean manualDirection = false;
   public boolean interact = false;
   public InteractTimingMode interactTiming = InteractTimingMode.WITH;
   public int interactCustomMs = 0;
   public boolean sneak = false;
   public String sneakMode = "Packet";
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;
   private boolean init = true;
   public boolean raycast = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public boolean isRaycast() {
      return this.raycast;
   }

   @Override
   public void setRaycast(boolean value) {
      this.raycast = value;
   }

   @Override
   public AimTargetAction.AimTarget getAimTarget(Minecraft mc) {
      return AimTargetAction.AimTarget.init(this.blockPos);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("x", this.blockPos.getX());
      tag.putInt("y", this.blockPos.getY());
      tag.putInt("z", this.blockPos.getZ());
      tag.putString("direction", this.direction.name());
      tag.putInt("delayTicks", this.delayTicks);
      tag.putInt("times", this.times);
      tag.putBoolean("autoPickaxe", this.autoPickaxe);
      tag.putBoolean("manualDirection", this.manualDirection);
      tag.putBoolean("interact", this.interact);
      tag.putString("interactTiming", this.interactTiming.name());
      tag.putInt("interactCustomMs", this.interactCustomMs);
      tag.putBoolean("sneak", this.sneak);
      tag.putBoolean("raycast", this.raycast);
      tag.putString("sneakMode", this.sneakMode);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("enabled", this.init);
      PacketOrderAware.writePacketOrder(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("x") && tag.contains("y") && tag.contains("z")) {
         this.blockPos = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
      }

      if (tag.contains("direction")) {
         try {
            this.direction = Direction.valueOf(tag.getStringOr("direction", "UP"));
         } catch (IllegalArgumentException ignored) {
            this.direction = Direction.UP;
         }
      }

      this.delayTicks = Math.max(0, tag.getIntOr("delayTicks", tag.getIntOr("delay", 0)));
      this.times = Math.max(0, tag.getIntOr("times", 1));
      this.autoPickaxe = tag.getBooleanOr("autoPickaxe", true);
      this.manualDirection = tag.getBooleanOr("manualDirection", false);
      this.interact = tag.getBooleanOr("interact", false);
      this.interactTiming = InteractTimingMode.init(tag.getStringOr("interactTiming", "WITH"), InteractTimingMode.WITH);
      this.interactCustomMs = Math.max(-5000, Math.min(5000, tag.getIntOr("interactCustomMs", 0)));
      this.sneak = tag.getBooleanOr("sneak", false);
      this.raycast = tag.getBooleanOr("raycast", false);
      this.sneakMode = tag.getStringOr("sneakMode", "Packet");
      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, false);
      this.guiName = tag.getStringOr("guiName", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = PacketOrderAware.readPacketOrder(tag);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.INSTA_BREAK;
   }

   @Override
   public String getDisplayName() {
      return "InstaBreak @ "
         + this.blockPos.getX()
         + ","
         + this.blockPos.getY()
         + ","
         + this.blockPos.getZ()
         + (this.times == 0 ? " ∞" : " x" + this.times)
         + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String getGuiName() {
      return this.guiName;
   }

   @Override
   public void setGuiName(String name) {
      this.guiName = name;
   }

   @Override
   public PacketOrderMode getPacketOrder() {
      return this.packetOrder;
   }

   @Override
   public String getIcon() {
      return "IB";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init = enabled;
   }
}
