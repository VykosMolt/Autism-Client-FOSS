package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class af implements MacroAction {
   public String macroName = "";
   public boolean restartIfRunning = false;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      if (this.macroName != null && !this.macroName.isBlank()) {
         if (this.restartIfRunning && MacroExecutor.init_Z_Str(this.macroName)) {
            MacroExecutor.init_V_Str(this.macroName);
         } else if (MacroExecutor.init_Z_Str(this.macroName)) {
            return;
         }

         autismclient.util.aM macro = autismclient.util.aN.init_aN().init_aM_Str(this.macroName);
         if (macro != null) {
            macro.init_V();
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("macroName", this.macroName == null ? "" : this.macroName);
      tag.putBoolean("restartIfRunning", this.restartIfRunning);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.macroName = tag.getStringOr("macroName", "");
      this.restartIfRunning = tag.getBooleanOr("restartIfRunning", false);
      this.init = tag.getBooleanOr("enabled", true);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.node;
   }

   @Override
   public String getDisplayName() {
      return this.macroName != null && !this.macroName.isBlank() ? "Start: " + this.macroName : "Start Macro";
   }

   @Override
   public String getIcon() {
      return "RUN";
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
