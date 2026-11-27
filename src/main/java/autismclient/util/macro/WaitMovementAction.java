package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class WaitMovementAction implements MacroAction {
   public WaitMovementAction.Inner_DataUtil mode = WaitMovementAction.Inner_DataUtil.POSITION;
   public double f_x_D = 0.0;
   public double f_y_D = 0.0;
   public double f_z_D = 0.0;
   public double leeway = 1.0;
   public boolean checkRotation = false;
   public double yaw = 0.0;
   public double pitch = 0.0;
   public double rotLeeway = 5.0;
   public double distance = 5.0;
   public double minDistance = 5.0;
   public boolean horizontalOnly = false;
   public String targetDimension = "";
   public boolean listenDuringPreviousAction = false;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   public MacroAction toWaitAction() {
      MacroAction var10000;
      switch (this.mode) {
         case POSITION:
            var10000 = this.init_aN();
            break;
         case POSITION_DELTA:
            var10000 = this.init_aC();
            break;
         case WORLD_CHANGE:
            var10000 = this.init_aG();
            break;
         case TELEPORT:
            var10000 = this.init_aF();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public WaitPosAction init_aN() {
      WaitPosAction a = new WaitPosAction();
      a.f_x_D = this.f_x_D;
      a.f_y_D = this.f_y_D;
      a.f_z_D = this.f_z_D;
      a.leeway = this.leeway;
      a.checkRotation = this.checkRotation;
      a.yaw = (float)this.yaw;
      a.pitch = (float)this.pitch;
      a.rotLeeway = (float)this.rotLeeway;
      a.listenDuringPreviousAction = this.listenDuringPreviousAction;
      return a;
   }

   public WaitForPositionDeltaAction init_aC() {
      WaitForPositionDeltaAction a = new WaitForPositionDeltaAction();
      a.distance = this.distance;
      a.horizontalOnly = this.horizontalOnly;
      a.listenDuringPreviousAction = this.listenDuringPreviousAction;
      return a;
   }

   public WaitForWorldChangeAction init_aG() {
      WaitForWorldChangeAction a = new WaitForWorldChangeAction();
      a.targetDimension = this.targetDimension;
      a.listenDuringPreviousAction = this.listenDuringPreviousAction;
      return a;
   }

   public WaitForTeleportAction init_aF() {
      WaitForTeleportAction a = new WaitForTeleportAction();
      a.minDistance = this.minDistance;
      a.horizontalOnly = this.horizontalOnly;
      a.listenDuringPreviousAction = this.listenDuringPreviousAction;
      return a;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_MOVEMENT;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "WAIT_MOVEMENT");
      tag.putString("mode", this.mode.name());
      tag.putDouble("x", this.f_x_D);
      tag.putDouble("y", this.f_y_D);
      tag.putDouble("z", this.f_z_D);
      tag.putDouble("leeway", this.leeway);
      tag.putBoolean("checkRotation", this.checkRotation);
      tag.putDouble("yaw", this.yaw);
      tag.putDouble("pitch", this.pitch);
      tag.putDouble("rotLeeway", this.rotLeeway);
      tag.putDouble("distance", this.distance);
      tag.putDouble("minDistance", this.minDistance);
      tag.putBoolean("horizontalOnly", this.horizontalOnly);
      tag.putString("targetDimension", this.targetDimension == null ? "" : this.targetDimension);
      tag.putBoolean("enabled", this.init);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = MacroNbtCodecHelper.parseEnum(WaitMovementAction.Inner_DataUtil.class, tag.getStringOr("mode", "POSITION"), WaitMovementAction.Inner_DataUtil.POSITION);
      this.f_x_D = tag.getDoubleOr("x", 0.0);
      this.f_y_D = tag.getDoubleOr("y", 0.0);
      this.f_z_D = tag.getDoubleOr("z", 0.0);
      this.leeway = tag.getDoubleOr("leeway", 1.0);
      this.checkRotation = tag.getBooleanOr("checkRotation", false);
      this.yaw = tag.getDoubleOr("yaw", 0.0);
      this.pitch = tag.getDoubleOr("pitch", 0.0);
      this.rotLeeway = tag.getDoubleOr("rotLeeway", 5.0);
      this.distance = tag.getDoubleOr("distance", 5.0);
      this.minDistance = Math.max(0.0, tag.getDoubleOr("minDistance", 5.0));
      this.horizontalOnly = tag.getBooleanOr("horizontalOnly", false);
      this.targetDimension = tag.getStringOr("targetDimension", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.mode) {
         case POSITION:
            var10000 = "Wait Pos " + String.format(Locale.ROOT, "%.0f,%.0f,%.0f", this.f_x_D, this.f_y_D, this.f_z_D);
            break;
         case POSITION_DELTA:
            var10000 = "Wait Moved " + String.format(Locale.ROOT, "%.1f", this.distance);
            break;
         case WORLD_CHANGE:
            var10000 = this.targetDimension != null && !this.targetDimension.isBlank() ? "Wait World: " + this.targetDimension : "Wait World Change";
            break;
         case TELEPORT:
            var10000 = this.minDistance <= 0.0 ? "Wait Teleport" : "Wait Teleport " + String.format(Locale.ROOT, "%.1f", this.minDistance);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public String getIcon() {
      return "MV";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      POSITION,
      POSITION_DELTA,
      WORLD_CHANGE,
      TELEPORT;
   }
}
