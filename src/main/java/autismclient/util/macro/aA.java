package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class aA implements MacroAction {
   public String macroName = "";
   public int step = 1;
   public aA.Inner_DataUtil mode = aA.Inner_DataUtil.COMPLETED_STEP;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("macroName", this.macroName == null ? "" : this.macroName);
      tag.putInt("step", Math.max(1, this.step));
      tag.putString("mode", this.mode.name());
      tag.putInt("timeoutMs", Math.max(0, this.timeoutMs));
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.macroName = tag.getStringOr("macroName", "");
      this.step = Math.max(1, tag.getIntOr("step", 1));

      try {
         this.mode = aA.Inner_DataUtil.valueOf(tag.getStringOr("mode", aA.Inner_DataUtil.COMPLETED_STEP.name()));
      } catch (Exception ignored) {
         this.mode = aA.Inner_DataUtil.COMPLETED_STEP;
      }

      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 0));
      this.init = tag.getBooleanOr("enabled", true);
      p.load(tag, this);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.WAIT_MACRO_STEP;
   }

   @Override
   public String getDisplayName() {
      String name = this.macroName != null && !this.macroName.isBlank() ? this.macroName : "macro";
      return this.mode == aA.Inner_DataUtil.FINISHED ? "Wait: " + name + " finished" : "Wait: " + name + " step " + this.step;
   }

   @Override
   public String getIcon() {
      return "WMS";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init = enabled;
   }

   public enum Inner_DataUtil {
      STARTED_STEP,
      COMPLETED_STEP,
      FINISHED;
   }
}
