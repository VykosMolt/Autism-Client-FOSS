package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class StopMacroAction implements MacroAction {
   public String macroName = "";
   public StopMacroAction.Inner_DataUtil target = StopMacroAction.Inner_DataUtil.SELF;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      if (this.target == StopMacroAction.Inner_DataUtil.ALL) {
         MacroExecutor.read_V();
      } else if (this.target == StopMacroAction.Inner_DataUtil.SELECTED && this.macroName != null && !this.macroName.isBlank()) {
         MacroExecutor.init_V_Str(this.macroName);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("target", this.target.name());
      tag.putString("macroName", this.macroName == null ? "" : this.macroName);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      try {
         this.target = StopMacroAction.Inner_DataUtil.valueOf(tag.getStringOr("target", StopMacroAction.Inner_DataUtil.SELF.name()));
      } catch (Exception ignored) {
         this.target = StopMacroAction.Inner_DataUtil.SELF;
      }

      this.macroName = tag.getStringOr("macroName", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.STOP_MACRO;
   }

   @Override
   public String getDisplayName() {
      if (this.target == StopMacroAction.Inner_DataUtil.SELECTED && this.macroName != null && !this.macroName.isBlank()) {
         return "Stop: " + this.macroName;
      } else {
         return this.target == StopMacroAction.Inner_DataUtil.ALL ? "Stop All Macros" : "Stop This Macro";
      }
   }

   @Override
   public String getIcon() {
      return "STP";
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
      SELF,
      SELECTED,
      ALL;
   }
}
