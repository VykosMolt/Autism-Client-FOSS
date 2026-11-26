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
            MacroItemTarget target = this.init().resolveTemplates(mc);
            if (target == null) {
               return;
            }

            if (!target.hasSlot() && !target.hasItemFilter()) {
               return;
            }

            resolvedFrom = autismclient.util.InventorySlotHelper.findVisibleSlot(mc, target);
            if (resolvedFrom == -1) {
               return;
            }
         }

         if (this.toUseItemName) {
            MacroItemTarget target = this.load().resolveTemplates(mc);
            if (target == null) {
               return;
            }

            if (!target.hasSlot() && !target.hasItemFilter()) {
               return;
            }

            resolvedTo = autismclient.util.InventorySlotHelper.findVisibleSlot(mc, target);
            if (resolvedTo == -1) {
               return;
            }
         }

         int fromHandlerSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, resolvedFrom);
         int toHandlerSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, resolvedTo);
         if (fromHandlerSlot >= 0 && toHandlerSlot >= 0) {
            autismclient.util.InventorySlotHelper.swapScreenSlots(mc, fromHandlerSlot, toHandlerSlot);
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
      if (fromTarget.hasSlot() || fromTarget.hasItemFilter()) {
         tag.put("fromItemName", fromTarget.toTag());
      }

      if (toTarget.hasSlot() || toTarget.hasItemFilter()) {
         tag.put("toItemName", toTarget.toTag());
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
         .map(MacroItemTarget::fromTag)
         .orElseGet(() -> MacroItemTarget.parse(tag.getStringOr("fromItemName", "")));
      this.toItemTarget = tag.getCompound("toItemName")
         .map(MacroItemTarget::fromTag)
         .orElseGet(() -> MacroItemTarget.parse(tag.getStringOr("toItemName", "")));
      this.fromItemName = this.fromItemTarget.toSpecString();
      this.toItemName = this.toItemTarget.toSpecString();
      this.fromUseItemName = tag.contains("fromUseItemName") ? tag.getBooleanOr("fromUseItemName", false) : !this.fromItemName.isEmpty();
      this.toUseItemName = tag.contains("toUseItemName") ? tag.getBooleanOr("toUseItemName", false) : !this.toItemName.isEmpty();
      this.useHandlerSlots = true;
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String from = this.fromUseItemName ? "\"" + this.init().formatSummary() + "\"" : String.valueOf(this.fromSlot);
      String to = this.toUseItemName ? "\"" + this.load().formatSummary() + "\"" : String.valueOf(this.toSlot);
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
      if (this.fromItemTarget != null && (this.fromItemTarget.hasSlot() || this.fromItemTarget.hasItemFilter())) {
         return this.fromItemTarget;
      }

      this.fromItemTarget = MacroItemTarget.parse(this.fromItemName);
      return this.fromItemTarget;
   }

   private MacroItemTarget load() {
      if (this.toItemTarget != null && (this.toItemTarget.hasSlot() || this.toItemTarget.hasItemFilter())) {
         return this.toItemTarget;
      }

      this.toItemTarget = MacroItemTarget.parse(this.toItemName);
      return this.toItemTarget;
   }
}
