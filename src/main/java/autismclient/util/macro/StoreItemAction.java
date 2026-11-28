package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StoreItemAction implements MacroAction {
   public StoreItemAction.TransferMode mode = StoreItemAction.TransferMode.STORE;
   public List<String> targetItems = new ArrayList<>();
   public List<MacroItemTarget> itemTargets = new ArrayList<>();
   public boolean persistent = false;
   public boolean closeAfter = false;
   public boolean allItems = false;
   public boolean closeSendPkt = true;
   public int delayTicks = 0;
   private boolean enabled = true;

   public void storeItems(Minecraft mc) {
      if (mc.player != null && mc.gameMode != null) {
         AbstractContainerMenu h = mc.player.containerMenu;
         if (h != mc.player.inventoryMenu) {
            for (int slotId : this.slotsToStore(mc)) {
               autismclient.util.ContainerClickExecutor.click(mc, slotId, 0, ContainerInput.QUICK_MOVE);
            }

            if (this.closeAfter && !this.persistent) {
               autismclient.util.GuiDesyncHelper.init(mc, this.closeSendPkt, false);
            }
         }
      }
   }

   @Override
   public void execute(Minecraft mc) {
      this.storeItems(mc);
   }

   public boolean init_Z_ItemStack(ItemStack stack) {
      return this.init(stack, -1);
   }

   public boolean init(ItemStack stack, int visibleSlot) {
      if (stack.isEmpty()) {
         return false;
      }

      if (this.allItems) {
         return true;
      }

      List<MacroItemTarget> targets = this.runtimeTargets(Minecraft.getInstance());
      if (targets.isEmpty()) {
         return false;
      }

      for (MacroItemTarget target : targets) {
         if (this.matchesTarget(stack, visibleSlot, target)) {
            return true;
         }
      }

      return false;
   }

   public List<Integer> slotsToStore(Minecraft mc) {
      List<Integer> slotsToMove = new ArrayList<>();
      if (mc != null && mc.player != null && mc.gameMode != null) {
         AbstractContainerMenu h = mc.player.containerMenu;
         if (h != null && h != mc.player.inventoryMenu) {
            for (int i = 0; i < h.slots.size(); i++) {
               Slot slot = h.slots.get(i);
               if (slot != null) {
                  boolean playerInventorySlot = autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot);
                  if ((this.mode != StoreItemAction.TransferMode.LOOT || !playerInventorySlot) && (this.mode != StoreItemAction.TransferMode.STORE || playerInventorySlot)) {
                     ItemStack stack = slot.getItem();
                     if (!stack.isEmpty()) {
                        int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, i);
                        if (this.init(stack, visibleSlot)) {
                           slotsToMove.add(i);
                        }
                     }
                  }
               }
            }

            return slotsToMove;
         } else {
            return slotsToMove;
         }
      } else {
         return slotsToMove;
      }
   }

   private boolean matchesTarget(ItemStack stack, int visibleSlot, MacroItemTarget target) {
      return target == null || !target.hasSlot() && !target.hasItemFilter() ? false : target.matches(stack, visibleSlot);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.STORE_ITEM;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "STORE_ITEM");
      tag.putString("mode", this.mode.name());
      tag.putBoolean("persistent", this.persistent);
      tag.putBoolean("closeAfter", this.closeAfter);
      tag.putBoolean("allItems", this.allItems);
      tag.putBoolean("closeSendPkt", this.closeSendPkt);
      tag.putInt("delayTicks", this.delayTicks);
      tag.put("targetItems", MacroItemTarget.toListTag(this.resolvedTargets()));
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = StoreItemAction.TransferMode.valueOf(tag.getStringOr("mode", "LOOT"));
         } catch (IllegalArgumentException ignored) {
            this.mode = StoreItemAction.TransferMode.LOOT;
         }
      }

      if (tag.contains("persistent")) {
         this.persistent = tag.getBooleanOr("persistent", false);
      }

      if (tag.contains("closeAfter")) {
         this.closeAfter = tag.getBooleanOr("closeAfter", false);
      }

      if (tag.contains("allItems")) {
         this.allItems = tag.getBooleanOr("allItems", false);
      }

      if (tag.contains("closeSendPkt")) {
         this.closeSendPkt = tag.getBooleanOr("closeSendPkt", true);
      }

      if (tag.contains("delayTicks")) {
         this.delayTicks = clampDelayTicks(tag.getIntOr("delayTicks", 0));
      }

      this.itemTargets.clear();
      this.targetItems.clear();
      if (tag.contains("targetItems")) {
         ListTag list = tag.getList("targetItems").orElse(new ListTag());
         this.itemTargets.addAll(MacroItemTarget.fromListTag(list));
      }

      this.syncTargetItems();
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String modeStr = this.mode == StoreItemAction.TransferMode.LOOT ? "Loot" : "Store";
      if (this.allItems) {
         return modeStr + " All" + (this.persistent ? " [Inf]" : "");
      }

      List<MacroItemTarget> targets = this.resolvedTargets();
      if (targets.isEmpty()) {
         return modeStr + " (nothing)";
      }

      String what = targets.size() == 1 ? formatTarget(targets.get(0).toSpecString()) : targets.size() + " items";
      return modeStr + " " + what + (this.persistent ? " [Inf]" : "");
   }

   public static String normalizeTarget(String raw) {
      if (raw == null) {
         return null;
      }

      String trimmed = raw.trim();
      if (trimmed.isEmpty()) {
         return null;
      }

      if (!trimmed.startsWith("#") && trimmed.matches("\\d+")) {
         trimmed = "#" + trimmed;
      }

      if (!trimmed.startsWith("#")) {
         return trimmed;
      }

      int separator = trimmed.indexOf(124);
      String slotRaw = separator >= 0 ? trimmed.substring(1, separator).trim() : trimmed.substring(1).trim();
      if (slotRaw.isEmpty()) {
         return null;
      }

      try {
         int slot = Integer.parseInt(slotRaw);
         if (slot < 0) {
            return null;
         }

         String itemPart = separator >= 0 && separator + 1 < trimmed.length() ? trimmed.substring(separator + 1).trim() : "";
         return itemPart.isEmpty() ? "#" + slot : "#" + slot + "|" + itemPart;
      } catch (NumberFormatException ignored) {
         return trimmed;
      }
   }

   public static String init_Str_Str(String itemName, String registryId, int visibleSlot) {
      String itemPart = registryId != null && !registryId.isBlank() ? registryId.trim() : (itemName != null ? itemName.trim() : "");
      if (!itemPart.isEmpty()) {
         return itemPart;
      } else {
         return visibleSlot >= 0 ? "#" + visibleSlot : null;
      }
   }

   public static String formatTarget(String entry) {
      if (entry != null && !entry.isBlank()) {
         MacroItemTarget target = MacroItemTarget.parse(entry);
         String label = target.resolvedDisplayName();
         if (target.hasSlot() && label.isBlank()) {
            return "#" + target.slot;
         } else {
            return target.hasSlot() ? target.slot + ": " + label : label;
         }
      } else {
         return "";
      }
   }

   private static int entrySlot(String entry) {
      if (entry != null && entry.startsWith("#")) {
         int separator = entry.indexOf(124);
         String slotRaw = separator >= 0 ? entry.substring(1, separator).trim() : entry.substring(1).trim();
         if (slotRaw.isEmpty()) {
            return -1;
         }

         try {
            int slot = Integer.parseInt(slotRaw);
            return slot >= 0 ? slot : -1;
         } catch (NumberFormatException ignored) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static String unusedEntryAfterSeparator(String entry) {
      if (entry != null && !entry.isBlank()) {
         int separator = entry.indexOf(124);
         return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
      } else {
         return "";
      }
   }

   @Override
   public String getIcon() {
      return this.mode == StoreItemAction.TransferMode.LOOT ? "LOT" : "STR";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   private List<MacroItemTarget> resolvedTargets() {
      if (!this.itemTargets.isEmpty()) {
         return this.itemTargets;
      }

      for (String target : this.targetItems) {
         MacroItemTarget parsed = MacroItemTarget.parse(target);
         if (parsed.hasSlot() || parsed.hasItemFilter()) {
            this.itemTargets.add(parsed);
         }
      }

      return this.itemTargets;
   }

   private List<MacroItemTarget> runtimeTargets(Minecraft mc) {
      List<MacroItemTarget> runtime = new ArrayList<>();

      for (MacroItemTarget target : this.resolvedTargets()) {
         if (target != null) {
            MacroItemTarget resolved = target.resolveTemplates(mc);
            if (resolved != null && (resolved.hasSlot() || resolved.hasItemFilter())) {
               runtime.add(resolved);
            }
         }
      }

      return runtime;
   }

   private void syncTargetItems() {
      this.targetItems.clear();

      for (MacroItemTarget target : this.itemTargets) {
         if (target != null) {
            String entry = target.toSpecString();
            if (!entry.isBlank()) {
               this.targetItems.add(entry);
            }
         }
      }
   }

   public static int clampDelayTicks(int value) {
      return Math.max(0, Math.min(200, value));
   }

   public enum TransferMode {
      LOOT,
      STORE;
   }
}
