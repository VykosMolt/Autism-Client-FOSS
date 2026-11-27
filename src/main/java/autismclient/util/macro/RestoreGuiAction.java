package autismclient.util.macro;

import autismclient.util.bi;
import autismclient.util.AutismSessionState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class RestoreGuiAction implements MacroAction, GuiWaitAware {
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = true;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      AutismSessionState shared = AutismSessionState.getInstance();
      if (shared.getStoredGuiScreen() != null && shared.getStoredContainerMenu() != null) {
         mc.gui.setScreen(shared.getStoredGuiScreen());
         if (mc.player != null) {
            mc.player.containerMenu = shared.getStoredContainerMenu();
         }

         bi.init("GUI restored.", -13248397);
      } else {
         bi.write("No stored GUI.");
      }
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String getGuiName() {
      return "";
   }

   @Override
   public void setGuiName(String n) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.RESTORE_GUI;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "RESTORE_GUI");
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, true);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      return "Restore GUI" + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public String getIcon() {
      return "R";
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
