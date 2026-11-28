package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class WaitForTeleportAction implements CaptureOutputAction, MacroAction {
   public double minDistance = 0.0;
   public boolean horizontalOnly = false;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putDouble("minDistance", this.minDistance);
      tag.putBoolean("horizontalOnly", this.horizontalOnly);
      tag.putBoolean("enabled", this.init);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.minDistance = Math.max(0.0, tag.getDoubleOr("minDistance", 0.0));
      this.horizontalOnly = tag.getBooleanOr("horizontalOnly", false);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_TELEPORT;
   }

   @Override
   public String getDisplayName() {
      return this.minDistance <= 0.0 ? "Wait Teleport" : "Wait Teleport: " + String.format(Locale.ROOT, "%.1f", this.minDistance);
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
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public String getSaveAs() {
      return this.saveAs;
   }

   @Override
   public void setSaveAs(String name) {
      this.saveAs = name == null ? "" : name;
   }
}
