package autismclient.util.macro;

import autismclient.util.bl;
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
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      bl.init(this, "macro");
   }

   public String init() {
      return init(this.f_x_D, this.relativeX)
         + " "
         + init(this.f_y_D, this.relativeY)
         + " "
         + init(this.f_z_D, this.relativeZ)
         + " "
         + init(this.maxPackets, 1, 100)
         + " "
         + init(this.pauseMs, 50, 10000);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putDouble("x", init(this.f_x_D));
      tag.putDouble("y", init(this.f_y_D));
      tag.putDouble("z", init(this.f_z_D));
      tag.putBoolean("relativeX", this.relativeX);
      tag.putBoolean("relativeY", this.relativeY);
      tag.putBoolean("relativeZ", this.relativeZ);
      tag.putInt("maxPackets", init(this.maxPackets, 1, 100));
      tag.putInt("pauseMs", init(this.pauseMs, 50, 10000));
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.f_x_D = init(tag.getDoubleOr("x", 0.0));
      this.f_y_D = init(tag.getDoubleOr("y", 0.0));
      this.f_z_D = init(tag.getDoubleOr("z", 0.0));
      this.relativeX = tag.getBooleanOr("relativeX", false);
      this.relativeY = tag.getBooleanOr("relativeY", false);
      this.relativeZ = tag.getBooleanOr("relativeZ", false);
      this.maxPackets = init(tag.getIntOr("maxPackets", 20), 1, 100);
      this.pauseMs = init(tag.getIntOr("pauseMs", 500), 50, 10000);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.TP;
   }

   @Override
   public String getDisplayName() {
      return "TP " + init(this.f_x_D, this.relativeX) + " " + init(this.f_y_D, this.relativeY) + " " + init(this.f_z_D, this.relativeZ);
   }

   @Override
   public String getIcon() {
      return "TP";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init = enabled;
   }

   private static String init(double value, boolean relative) {
      return (relative ? "~" : "") + String.format(Locale.ROOT, "%.2f", init(value));
   }

   private static double init(double value) {
      return Double.isFinite(value) ? value : 0.0;
   }

   private static int init(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }
}
