package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;

public class InventoryAction implements MacroAction, GuiWaitAware {
   public InventoryAction.InventoryMode mode = InventoryAction.InventoryMode.OPEN;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = true;
   public String guiName = "";
   public boolean sendPacket = true;

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null) {
         if (this.mode == InventoryAction.InventoryMode.OPEN) {
            mc.execute(() -> mc.gui.setScreen(new InventoryScreen(mc.player)));
         } else {
            mc.execute(() -> autismclient.util.GuiDesyncHelper.init(mc, this.sendPacket, false));
         }
      }
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.mode == InventoryAction.InventoryMode.OPEN && this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.mode == InventoryAction.InventoryMode.OPEN && this.waitForGuiAfter;
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
   public void setGuiName(String name) {
      this.guiName = "";
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.INVENTORY;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "INVENTORY");
      tag.putString("mode", this.mode.name());
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("sendPacket", this.sendPacket);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = InventoryAction.InventoryMode.valueOf(tag.getStringOr("mode", "OPEN"));
         } catch (IllegalArgumentException ignored) {
            this.mode = InventoryAction.InventoryMode.OPEN;
         }
      }

      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, true);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      if (tag.contains("sendPacket")) {
         this.sendPacket = tag.getBooleanOr("sendPacket", true);
      }
   }

   @Override
   public String getDisplayName() {
      if (this.mode == InventoryAction.InventoryMode.OPEN) {
         return "Inv Open" + GuiWaitAware.waitSuffix(this);
      } else {
         return this.sendPacket ? "Inv Close" : "Inv Close (no pkt)";
      }
   }

   @Override
   public String getIcon() {
      return "I";
   }

   public enum InventoryMode {
      OPEN,
      CLOSE;
   }
}
