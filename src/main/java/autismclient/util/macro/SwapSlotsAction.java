package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SwapSlotsAction implements MacroAction {
   public int fromSlot = -1;
   public int toSlot = -1;
   public String fromItemName = "";
   public String toItemName = "";
   public MacroItemTarget fromItemTarget = new MacroItemTarget();
   public MacroItemTarget toItemTarget = new MacroItemTarget();
   public boolean fromUseItemName = false;
   public boolean toUseItemName = false;
   public boolean useHandlerSlots = true;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null) {
         int resolvedFrom = this.fromSlot;
         int resolvedTo = this.toSlot;
         if (this.fromUseItemName) {
            MacroItemTarget target = this.init().init_ArrayUtil_Minecraft(mc);
            if (target == null) {
               return;
            }

            if (!target.init_Z() && !target.update()) {
               return;
            }

            resolvedFrom = autismclient.util.InventorySlotHelper.load(mc, target);
            if (resolvedFrom == -1) {
               return;
            }
         }

         if (this.toUseItemName) {
            MacroItemTarget target = this.load().init_ArrayUtil_Minecraft(mc);
            if (target == null) {
               return;
            }

            if (!target.init_Z() && !target.update()) {
               return;
            }

            resolvedTo = autismclient.util.InventorySlotHelper.load(mc, target);
            if (resolvedTo == -1) {
               return;
            }
         }

         int fromHandlerSlot = autismclient.util.InventorySlotHelper.read_I(mc, resolvedFrom);
         int toHandlerSlot = autismclient.util.InventorySlotHelper.read_I(mc, resolvedTo);
         if (fromHandlerSlot >= 0 && toHandlerSlot >= 0) {
            autismclient.util.InventorySlotHelper.write(mc, fromHandlerSlot, toHandlerSlot);
         }
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SWAP_SLOTS;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SWAP_SLOTS");
      tag.putInt("fromSlot", this.fromSlot);
      tag.putInt("toSlot", this.toSlot);
      MacroItemTarget fromTarget = this.init();
      MacroItemTarget toTarget = this.load();
      if (fromTarget.init_Z() || fromTarget.update()) {
         tag.put("fromItemName", fromTarget.init_CompoundTag());
      }

      if (toTarget.init_Z() || toTarget.update()) {
         tag.put("toItemName", toTarget.init_CompoundTag());
      }

      tag.putBoolean("fromUseItemName", this.fromUseItemName);
      tag.putBoolean("toUseItemName", this.toUseItemName);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.fromSlot = tag.getIntOr("fromSlot", -1);
      this.toSlot = tag.getIntOr("toSlot", -1);
      this.fromItemTarget = tag.getCompound("fromItemName")
         .map(MacroItemTarget::init_ArrayUtil_CompoundTag)
         .orElseGet(() -> MacroItemTarget.update(tag.getStringOr("fromItemName", "")));
      this.toItemTarget = tag.getCompound("toItemName")
         .map(MacroItemTarget::init_ArrayUtil_CompoundTag)
         .orElseGet(() -> MacroItemTarget.update(tag.getStringOr("toItemName", "")));
      this.fromItemName = this.fromItemTarget.write_Str();
      this.toItemName = this.toItemTarget.write_Str();
      this.fromUseItemName = tag.contains("fromUseItemName") ? tag.getBooleanOr("fromUseItemName", false) : !this.fromItemName.isEmpty();
      this.toUseItemName = tag.contains("toUseItemName") ? tag.getBooleanOr("toUseItemName", false) : !this.toItemName.isEmpty();
      this.useHandlerSlots = true;
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String from = this.fromUseItemName ? "\"" + this.init().load_Str() + "\"" : String.valueOf(this.fromSlot);
      String to = this.toUseItemName ? "\"" + this.load().load_Str() + "\"" : String.valueOf(this.toSlot);
      return "Swap " + from + " <-> " + to;
   }

   @Override
   public String getIcon() {
      return "SWP";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   private MacroItemTarget init() {
      if (this.fromItemTarget != null && (this.fromItemTarget.init_Z() || this.fromItemTarget.update())) {
         return this.fromItemTarget;
      }

      this.fromItemTarget = MacroItemTarget.update(this.fromItemName);
      return this.fromItemTarget;
   }

   private MacroItemTarget load() {
      if (this.toItemTarget != null && (this.toItemTarget.init_Z() || this.toItemTarget.update())) {
         return this.toItemTarget;
      }

      this.toItemTarget = MacroItemTarget.update(this.toItemName);
      return this.toItemTarget;
   }
}
