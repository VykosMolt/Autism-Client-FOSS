package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class GoToAction implements MacroAction {
   public double f_x_D = 0.0;
   public double f_y_D = 0.0;
   public double f_z_D = 0.0;
   private boolean init = true;
   public boolean waitForArrival = true;
   public double arrivalRadius = 2.0;
   public int timeoutMs = 60000;

   public GoToAction() {
   }

   public GoToAction(double x, double y, double z) {
      this.f_x_D = x;
      this.f_y_D = y;
      this.f_z_D = z;
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         Runnable start = () -> {
            try {
               MacroExecutor.trackAiOperation(
                  autismclient.ai.engine.AiGoto.travelToCompat(
                     new BlockPos((int)Math.floor(this.f_x_D), (int)Math.floor(this.f_y_D), (int)Math.floor(this.f_z_D)),
                     this.arrivalRadius,
                     false,
                     true,
                     true,
                     Math.max(1, this.timeoutMs / 1000)
                  )
               );
            } catch (Throwable t) {
               AutismClientMessaging.sendPrefixed("§cNavigation is not available: " + t.getMessage());
            }
         };
         if (mc.isSameThread()) {
            start.run();
         } else {
            mc.execute(start);
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putDouble("x", this.f_x_D);
      tag.putDouble("y", this.f_y_D);
      tag.putDouble("z", this.f_z_D);
      tag.putBoolean("waitForArrival", this.waitForArrival);
      tag.putDouble("arrivalRadius", this.arrivalRadius);
      tag.putInt("timeoutMs", this.timeoutMs);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("x")) {
         this.f_x_D = tag.getDoubleOr("x", 0.0);
      }

      if (tag.contains("y")) {
         this.f_y_D = tag.getDoubleOr("y", 0.0);
      }

      if (tag.contains("z")) {
         this.f_z_D = tag.getDoubleOr("z", 0.0);
      }

      if (tag.contains("waitForArrival")) {
         this.waitForArrival = tag.getBooleanOr("waitForArrival", true);
      }

      if (tag.contains("arrivalRadius")) {
         this.arrivalRadius = tag.getDoubleOr("arrivalRadius", 2.0);
      }

      if (tag.contains("timeoutMs")) {
         this.timeoutMs = tag.getIntOr("timeoutMs", 60000);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.GO_TO;
   }

   @Override
   public String getDisplayName() {
      return String.format("Go To (%.0f, %.0f, %.0f)%s", this.f_x_D, this.f_y_D, this.f_z_D, this.waitForArrival ? " [W]" : "");
   }

   @Override
   public String getIcon() {
      return "GO";
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
