package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class JsonUtil implements MacroAction {
   public String guiName = "";
   public String itemName = "";
   public ArrayUtil itemTarget = new ArrayUtil();
   public int targetSlot = -1;
   public boolean useItemFilter = false;
   public boolean sendPacket = true;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.gui.screen() != null) {
         boolean matchGui = true;
         if (!this.guiName.isEmpty()) {
            j.Inner_CachePool gui = n.init_j_CachePool_Str_Minecraft(this.guiName, mc);
            if (!gui.init_Z()) {
               return;
            }

            matchGui = mc.gui.screen().getTitle().getString().toLowerCase().contains(gui.init_Str().toLowerCase());
         }

         boolean matchItem = true;
         ArrayUtil target = this.init().init_ArrayUtil_Minecraft(mc);
         if (!this.useItemFilter || target != null) {
            if (this.useItemFilter && (target.init_Z() || target.update())) {
               matchItem = false;
               if (mc.player.containerMenu != null) {
                  int effectiveSlot = target.init_Z() ? target.init_I : this.targetSlot;
                  int resolvedTargetSlot = autismclient.util.aw.read_I(mc, effectiveSlot);
                  if (resolvedTargetSlot >= 0 && resolvedTargetSlot < mc.player.containerMenu.slots.size()) {
                     ItemStack stack = mc.player.containerMenu.slots.get(resolvedTargetSlot).getItem();
                     int visibleSlot = autismclient.util.aw.init_I(mc, resolvedTargetSlot);
                     if (!stack.isEmpty() && target.init_Z_ItemStack_I(stack, visibleSlot)) {
                        matchItem = true;
                     }
                  } else if (effectiveSlot == -1) {
                     for (Slot slot : mc.player.containerMenu.slots) {
                        ItemStack stack = slot.getItem();
                        int visibleSlot = autismclient.util.aw.init_I(mc, slot.index);
                        if (!stack.isEmpty() && target.init_Z_ItemStack_I(stack, visibleSlot)) {
                           matchItem = true;
                           break;
                        }
                     }
                  }
               }
            }

            if (matchGui && matchItem) {
               autismclient.util.af.init(mc, this.sendPacket, false);
            }
         }
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.CLOSE_GUI;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "CLOSE_GUI");
      tag.putString("guiName", this.guiName);
      ArrayUtil target = this.init();
      if (target.init_Z() || target.update()) {
         tag.put("itemName", target.init_CompoundTag());
      }

      tag.putBoolean("useItemFilter", this.useItemFilter);
      tag.putBoolean("sendPacket", this.sendPacket);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      this.itemTarget = tag.getCompound("itemName").map(ArrayUtil::init_ArrayUtil_CompoundTag).orElseGet(() -> {
         int legacySlot = tag.contains("targetSlot") ? tag.getIntOr("targetSlot", -1) : -1;
         String legacyName = tag.getStringOr("itemName", "");
         if (legacySlot >= 0 && !legacyName.isBlank()) {
            return ArrayUtil.update("#" + legacySlot + "|" + legacyName);
         } else {
            return legacySlot >= 0 ? ArrayUtil.init_ArrayUtil_I(legacySlot) : ArrayUtil.update(legacyName);
         }
      });
      this.itemName = this.itemTarget.write_Str();
      this.targetSlot = this.itemTarget.init_Z() ? this.itemTarget.init_I : -1;
      if (tag.contains("useItemFilter")) {
         this.useItemFilter = tag.getBooleanOr("useItemFilter", false);
      } else {
         this.useItemFilter = this.itemTarget.init_Z() || this.itemTarget.update();
      }

      if (tag.contains("sendPacket")) {
         this.sendPacket = tag.getBooleanOr("sendPacket", true);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String base = !this.guiName.isEmpty() ? "Close GUI: " + this.guiName : "Close GUI";
      return this.sendPacket ? base : base + " (desync)";
   }

   @Override
   public String getIcon() {
      return "X";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   private ArrayUtil init() {
      if (this.itemTarget == null || !this.itemTarget.init_Z() && !this.itemTarget.update()) {
         if (this.targetSlot >= 0 && !this.itemName.isBlank()) {
            this.itemTarget = ArrayUtil.update("#" + this.targetSlot + "|" + this.itemName);
         } else if (this.targetSlot >= 0) {
            this.itemTarget = ArrayUtil.init_ArrayUtil_I(this.targetSlot);
         } else {
            this.itemTarget = ArrayUtil.update(this.itemName);
         }

         return this.itemTarget;
      } else {
         return this.itemTarget;
      }
   }
}
