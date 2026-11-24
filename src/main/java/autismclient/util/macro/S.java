package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class S implements MacroAction {
   public float yaw;
   public float pitch;
   public boolean smooth;
   public int smoothness = 6;
   public boolean waitForCompletion = true;

   public S() {
   }

   public S(float yaw, float pitch, boolean smooth, int smoothness) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.smooth = smooth;
      this.smoothness = init_I(smoothness);
   }

   public static int init_I(int value) {
      return Math.max(1, Math.min(10, value));
   }

   public static double init_D(int smoothness) {
      double minStep = 0.5;
      double maxStep = 9.05;
      double normalized = (10.0 - init_I(smoothness)) / 9.0;
      return minStep * Math.pow(maxStep / minStep, normalized);
   }

   public double init() {
      return init_D(this.smoothness);
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && !this.smooth) {
         mc.player.setYRot(this.yaw);
         mc.player.setXRot(this.pitch);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.push;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putFloat("yaw", this.yaw);
      tag.putFloat("pitch", this.pitch);
      tag.putBoolean("smooth", this.smooth);
      tag.putInt("smoothness", init_I(this.smoothness));
      tag.putBoolean("waitForCompletion", this.waitForCompletion);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("yaw")) {
         this.yaw = tag.getFloatOr("yaw", 0.0F);
      }

      if (tag.contains("pitch")) {
         this.pitch = tag.getFloatOr("pitch", 0.0F);
      }

      if (tag.contains("smooth")) {
         this.smooth = tag.getBooleanOr("smooth", false);
      }

      if (tag.contains("smoothness")) {
         this.smoothness = init_I(tag.getIntOr("smoothness", 6));
      } else {
         this.smoothness = 6;
      }

      if (tag.contains("waitForCompletion")) {
         this.waitForCompletion = tag.getBooleanOr("waitForCompletion", true);
      } else {
         this.waitForCompletion = true;
      }
   }

   @Override
   public String getDisplayName() {
      String suffix = this.smooth ? " (Smooth)" : "";
      if (!this.waitForCompletion) {
         suffix = suffix + " [NoWait]";
      }

      return String.format("Rot %.1f / %.1f%s", this.yaw, this.pitch, suffix);
   }

   @Override
   public String getIcon() {
      return "R";
   }
}
