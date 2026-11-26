package autismclient.util.macro;

import autismclient.util.bi;
import autismclient.util.ck;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class RestoreGuiAction implements MacroAction, aP {
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = true;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      ck shared = ck.init_ck();
      if (shared.init_Screen() != null && shared.init_AbstractContainerM() != null) {
         mc.gui.setScreen(shared.init_Screen());
         if (mc.player != null) {
            mc.player.containerMenu = shared.init_AbstractContainerM();
         }

         bi.init("GUI restored.", -13248397);
      } else {
         bi.write("No stored GUI.");
      }
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return "";
   }

   @Override
   public void init_V_Str(String n) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.RESTORE_GUI;
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
      this.waitForGuiBefore = aP.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = aP.load_Z_CompoundTag_Z(tag, true);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      return "Restore GUI" + aP.init_Str_aP(this);
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
