package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class aN implements CacheEntry, MacroAction {
   public double f_x_D = 0.0;
   public double f_y_D = 0.0;
   public double f_z_D = 0.0;
   public double leeway = 1.0;
   public boolean checkRotation = false;
   public float yaw = 0.0F;
   public float pitch = 0.0F;
   public float rotLeeway = 5.0F;
   private boolean init = true;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";

   public aN() {
   }

   public aN(double x, double y, double z, double leeway) {
      this.f_x_D = x;
      this.f_y_D = y;
      this.f_z_D = z;
      this.leeway = leeway;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putDouble("x", this.f_x_D);
      tag.putDouble("y", this.f_y_D);
      tag.putDouble("z", this.f_z_D);
      tag.putDouble("leeway", this.leeway);
      tag.putBoolean("checkRotation", this.checkRotation);
      tag.putFloat("yaw", this.yaw);
      tag.putFloat("pitch", this.pitch);
      tag.putFloat("rotLeeway", this.rotLeeway);
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
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

      if (tag.contains("leeway")) {
         this.leeway = tag.getDoubleOr("leeway", 1.0);
      }

      if (tag.contains("checkRotation")) {
         this.checkRotation = tag.getBooleanOr("checkRotation", false);
      }

      if (tag.contains("yaw")) {
         this.yaw = tag.getFloatOr("yaw", 0.0F);
      }

      if (tag.contains("pitch")) {
         this.pitch = tag.getFloatOr("pitch", 0.0F);
      }

      if (tag.contains("rotLeeway")) {
         this.rotLeeway = tag.getFloatOr("rotLeeway", 5.0F);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public StreamGate getType() {
      return StreamGate.state;
   }

   @Override
   public String getDisplayName() {
      return this.checkRotation
         ? String.format("Wait Pos & Rot (%.0f, %.0f, %.0f)", this.f_x_D, this.f_y_D, this.f_z_D)
         : String.format("Wait Pos (%.0f, %.0f, %.0f) +/-%.1f", this.f_x_D, this.f_y_D, this.f_z_D, this.leeway);
   }

   @Override
   public String getIcon() {
      return "LOC";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }
}
