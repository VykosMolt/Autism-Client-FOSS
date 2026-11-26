package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class InstaBreakAction implements E, M, MacroAction, aP {
   public BlockPos blockPos = BlockPos.ZERO;
   public Direction direction = Direction.UP;
   public int delayTicks = 0;
   public int times = 1;
   public boolean autoPickaxe = true;
   public boolean manualDirection = false;
   public boolean interact = false;
   public TaskTimer interactTiming = TaskTimer.WITH;
   public int interactCustomMs = 0;
   public boolean sneak = false;
   public String sneakMode = "Packet";
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public volatile D packetOrder = D.INSTANT;
   private boolean init = true;
   public boolean raycast = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public boolean init_Z_3() {
      return this.raycast;
   }

   @Override
   public void init_V_Z(boolean value) {
      this.raycast = value;
   }

   @Override
   public M.Inner_DataUtil init_M_DataUtil_Minecraft(Minecraft mc) {
      return M.Inner_DataUtil.init(this.blockPos);
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
      E.init_V_CompoundTag_D(tag, this.packetOrder);
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
      this.interactTiming = TaskTimer.init(tag.getStringOr("interactTiming", "WITH"), TaskTimer.WITH);
      this.interactCustomMs = Math.max(-5000, Math.min(5000, tag.getIntOr("interactCustomMs", 0)));
      this.sneak = tag.getBooleanOr("sneak", false);
      this.raycast = tag.getBooleanOr("raycast", false);
      this.sneakMode = tag.getStringOr("sneakMode", "Packet");
      this.waitForGuiBefore = aP.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = aP.load_Z_CompoundTag_Z(tag, false);
      this.guiName = tag.getStringOr("guiName", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = E.init_D_CompoundTag(tag);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.INSTA_BREAK;
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
         + aP.init_Str_aP(this);
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return this.guiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.guiName = name;
   }

   @Override
   public D init_D() {
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
