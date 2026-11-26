package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class JumpAction implements MacroAction {
   public int durationTicks = 1;
   public boolean tap = true;
   private boolean init = true;

   public JumpAction() {
   }

   public JumpAction(int durationTicks, boolean tap) {
      this.durationTicks = durationTicks;
      this.tap = tap;
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.options != null && mc.options.keyJump != null) {
         mc.options.keyJump.setDown(true);
      }
   }

   public void init(Minecraft mc) {
      if (mc.options != null && mc.options.keyJump != null) {
         mc.options.keyJump.setDown(false);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("durationTicks", this.durationTicks);
      tag.putBoolean("tap", this.tap);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("durationTicks")) {
         this.durationTicks = tag.getIntOr("durationTicks", 1);
      }

      if (tag.contains("tap")) {
         this.tap = tag.getBooleanOr("tap", true);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.JUMP;
   }

   @Override
   public String getDisplayName() {
      return this.tap ? "Jump (tap)" : "Jump (hold " + this.durationTicks + "t)";
   }

   @Override
   public String getIcon() {
      return "J";
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
