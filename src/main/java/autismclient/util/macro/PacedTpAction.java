package autismclient.util.macro;

import autismclient.util.PacedTpJobManager;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class PacedTpAction implements MacroAction {
   public double f_x_D;
   public double f_y_D;
   public double f_z_D;
   public boolean relativeX;
   public boolean relativeY;
   public boolean relativeZ;
   public int maxPackets = 20;
   public int pauseMs = 500;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      PacedTpJobManager.init(this, "macro");
   }

   public String init() {
      return formatCoord(this.f_x_D, this.relativeX)
         + " "
         + formatCoord(this.f_y_D, this.relativeY)
         + " "
         + formatCoord(this.f_z_D, this.relativeZ)
         + " "
         + clamp(this.maxPackets, 1, 100)
         + " "
         + clamp(this.pauseMs, 50, 10000);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putDouble("x", finiteOrZero(this.f_x_D));
      tag.putDouble("y", finiteOrZero(this.f_y_D));
      tag.putDouble("z", finiteOrZero(this.f_z_D));
      tag.putBoolean("relativeX", this.relativeX);
      tag.putBoolean("relativeY", this.relativeY);
      tag.putBoolean("relativeZ", this.relativeZ);
      tag.putInt("maxPackets", clamp(this.maxPackets, 1, 100));
      tag.putInt("pauseMs", clamp(this.pauseMs, 50, 10000));
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.f_x_D = finiteOrZero(tag.getDoubleOr("x", 0.0));
      this.f_y_D = finiteOrZero(tag.getDoubleOr("y", 0.0));
      this.f_z_D = finiteOrZero(tag.getDoubleOr("z", 0.0));
      this.relativeX = tag.getBooleanOr("relativeX", false);
      this.relativeY = tag.getBooleanOr("relativeY", false);
      this.relativeZ = tag.getBooleanOr("relativeZ", false);
      this.maxPackets = clamp(tag.getIntOr("maxPackets", 20), 1, 100);
      this.pauseMs = clamp(tag.getIntOr("pauseMs", 500), 50, 10000);
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.TP;
   }

   @Override
   public String getDisplayName() {
      return "TP " + formatCoord(this.f_x_D, this.relativeX) + " " + formatCoord(this.f_y_D, this.relativeY) + " " + formatCoord(this.f_z_D, this.relativeZ);
   }

   @Override
   public String getIcon() {
      return "TP";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   private static String formatCoord(double value, boolean relative) {
      return (relative ? "~" : "") + String.format(Locale.ROOT, "%.2f", finiteOrZero(value));
   }

   private static double finiteOrZero(double value) {
      return Double.isFinite(value) ? value : 0.0;
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }
}
