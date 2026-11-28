package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DropAction implements PacketOrderAware, MacroAction, GuiWaitAware {
   public DropAction.Inner_DataUtil mode = DropAction.Inner_DataUtil.TIMES;
   public int dropCount = 1;
   public List<String> itemNames = new ArrayList<>();
   public List<MacroItemTarget> itemTargets = new ArrayList<>();
   public List<Integer> itemCounts = new ArrayList<>();
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public boolean useHandlerSlots = true;
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;
   private boolean init = true;

   public int getItemCount(int index) {
      if (index >= 0 && index < this.itemCounts.size()) {
         return Math.max(0, this.itemCounts.get(index));
      } else {
         return this.mode == DropAction.Inner_DataUtil.ALL ? 0 : Math.max(1, this.dropCount);
      }
   }

   public boolean hasTargetFilter() {
      for (MacroItemTarget target : this.load()) {
         if (target != null && (target.hasItemFilter() || target.hasSlot())) {
            return true;
         }
      }

      return false;
   }

   public List<MacroItemTarget> targets() {
      return List.copyOf(this.load());
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.gameMode != null) {
         List<MacroItemTarget> targets = this.load();
         if (!this.hasTargetFilter()) {
            if (this.mode == DropAction.Inner_DataUtil.ALL) {
               for (int i = 0; i < 36; i++) {
                  if (!mc.player.getInventory().getItem(i).isEmpty()) {
                     if (this.useHandlerSlots) {
                        int resolved = autismclient.util.InventorySlotHelper.toScreenSlot(mc, i);
                        if (resolved >= 0) {
                           this.load(mc, resolved, 0);
                        }
                     } else {
                        this.init(mc, i, 0);
                     }
                  }
               }
            } else {
               int selected = Math.max(0, Math.min(8, mc.player.getInventory().getSelectedSlot()));
               int count = Math.max(1, this.dropCount);
               if (this.useHandlerSlots) {
                  int resolved = autismclient.util.InventorySlotHelper.toScreenSlot(mc, selected);
                  if (resolved >= 0) {
                     this.load(mc, resolved, count);
                  }
               } else {
                  this.init(mc, selected, count);
               }
            }
         } else if (!targets.isEmpty()) {
            for (int ei = 0; ei < targets.size(); ei++) {
               MacroItemTarget entryTarget = this.targetAt(ei).resolveTemplates(mc);
               if (entryTarget != null) {
                  int count = this.getItemCount(ei);
                  String entryName = entrySpec(entryTarget.toSpecString());
                  int configuredSlot = entryTarget.hasSlot() ? entryTarget.slot : -1;
                  if (configuredSlot >= 0 && !entryName.isEmpty()) {
                     this.init(mc, configuredSlot, entryTarget, count);
                  } else if (configuredSlot >= 0) {
                     if (this.useHandlerSlots) {
                        int resolvedSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, configuredSlot);
                        if (resolvedSlot >= 0) {
                           this.load(mc, resolvedSlot, count);
                        }
                     } else {
                        this.init(mc, configuredSlot, count);
                     }
                  } else if (!entryName.isEmpty()) {
                     this.init(mc, entryTarget, count);
                  }
               }
            }
         }
      }
   }

   private void init(Minecraft mc, int configuredSlot, String itemName, int count) {
      this.init(mc, configuredSlot, MacroItemTarget.parse(itemName), count);
   }

   private void init(Minecraft mc, int configuredSlot, MacroItemTarget target, int count) {
      if (target.hasItemFilter()) {
         if (this.useHandlerSlots) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler != null) {
               int resolvedSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, configuredSlot);
               if (resolvedSlot >= 0 && resolvedSlot < handler.slots.size()) {
                  Slot slot = handler.slots.get(resolvedSlot);
                  if (slot != null && !slot.getItem().isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, resolvedSlot);
                     if (target.matches(slot.getItem(), visibleSlot)) {
                        int toDrop = count == 0 ? 0 : Math.min(count, slot.getItem().getCount());
                        this.load(mc, resolvedSlot, toDrop);
                     }
                  }
               }
            }
         } else if (configuredSlot >= 0 && configuredSlot < 36) {
            ItemStack stack = mc.player.getInventory().getItem(configuredSlot);
            if (!stack.isEmpty() && target.matches(stack, configuredSlot)) {
               int toDrop = count == 0 ? 0 : Math.min(count, stack.getCount());
               this.init(mc, configuredSlot, toDrop);
            }
         }
      }
   }

   private void init(Minecraft mc, String itemName, int count) {
      this.init(mc, MacroItemTarget.parse(itemName), count);
   }

   private void init(Minecraft mc, MacroItemTarget target, int count) {
      if (target.hasItemFilter()) {
         int remaining = count;
         if (this.useHandlerSlots) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler != null) {
               for (Slot s : handler.slots) {
                  if (s != null && !s.getItem().isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, s.index);
                     if (target.matches(s.getItem(), visibleSlot)) {
                        if (count == 0) {
                           this.load(mc, s.index, 0);
                        } else {
                           if (remaining <= 0) {
                              break;
                           }

                           int toDrop = Math.min(remaining, s.getItem().getCount());
                           this.load(mc, s.index, toDrop);
                           remaining -= toDrop;
                        }
                     }
                  }
               }
            }
         } else {
            for (int i = 0; i < 36; i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty() && target.matches(stack, i)) {
                  if (count == 0) {
                     this.init(mc, i, 0);
                  } else {
                     if (remaining <= 0) {
                        break;
                     }

                     int toDrop = Math.min(remaining, stack.getCount());
                     this.init(mc, i, toDrop);
                     remaining -= toDrop;
                  }
               }
            }
         }
      }
   }

   private void init(Minecraft mc, int invSlot, int count) {
      autismclient.util.InventoryDropHelper.dropFromInventory(mc, invSlot, count);
   }

   private void load(Minecraft mc, int handlerSlotId, int count) {
      autismclient.util.InventoryDropHelper.dropFromHandler(mc, handlerSlotId, count);
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void setWaitForGuiBefore(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void setWaitForGuiAfter(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String getGuiName() {
      return this.guiName;
   }

   @Override
   public void setGuiName(String name) {
      this.guiName = name;
   }

   @Override
   public PacketOrderMode getPacketOrder() {
      return this.packetOrder;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.DROP;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "DROP");
      tag.putString("mode", this.mode.name());
      tag.putInt("count", this.dropCount);
      tag.put("itemNames", MacroItemTarget.toListTag(this.load()));
      ListTag counts = new ListTag();

      for (int c : this.itemCounts) {
         counts.add(StringTag.valueOf(String.valueOf(c)));
      }

      tag.put("itemCounts", counts);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("useHandlerSlots", this.useHandlerSlots);
      tag.putBoolean("enabled", this.init);
      PacketOrderAware.writePacketOrder(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      try {
         this.mode = DropAction.Inner_DataUtil.valueOf(tag.getStringOr("mode", "TIMES"));
      } catch (IllegalArgumentException ignored) {
         this.mode = DropAction.Inner_DataUtil.TIMES;
      }

      this.dropCount = tag.getIntOr("count", 1);
      this.itemTargets.clear();
      this.itemNames.clear();
      if (tag.contains("itemNames")) {
         ListTag nl = tag.getList("itemNames").orElse(new ListTag());
         this.itemTargets.addAll(MacroItemTarget.fromListTag(nl));
      } else if (tag.contains("itemName")) {
         String old = tag.getStringOr("itemName", "");
         if (!old.isEmpty()) {
            this.itemTargets.add(MacroItemTarget.parse(old));
         }
      }

      this.syncItemNames();
      this.itemCounts.clear();
      if (tag.contains("itemCounts")) {
         for (Tag el : tag.getList("itemCounts").orElse(new ListTag())) {
            try {
               this.itemCounts.add(Integer.parseInt(el.asString().orElse("1")));
            } catch (NumberFormatException e) {
               this.itemCounts.add(1);
            }
         }
      }

      while (this.itemCounts.size() < this.itemNames.size()) {
         this.itemCounts.add(this.getItemCount(this.itemCounts.size()));
      }

      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, false);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      this.useHandlerSlots = true;
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = PacketOrderAware.readPacketOrder(tag);
   }

   @Override
   public String getDisplayName() {
      List<MacroItemTarget> targets = this.load();
      if (targets.isEmpty()) {
         String untargeted = this.mode == DropAction.Inner_DataUtil.ALL ? "Drop Entire Inventory" : "Drop Held Item x" + Math.max(1, this.dropCount);
         return untargeted + GuiWaitAware.waitSuffix(this);
      }

      String e0 = targets.get(0).toSpecString();
      int slot = entrySlot(e0);
      String itemName = entrySpec(e0);
      String first;
      if (slot >= 0 && !itemName.isEmpty()) {
         first = "\"" + itemName + "\" @ Slot " + slot;
      } else if (slot >= 0) {
         first = "Slot " + slot;
      } else {
         first = "\"" + itemName + "\"";
      }

      if (targets.size() > 1) {
         first = first + " (+" + (targets.size() - 1) + ")";
      }

      String base = this.mode == DropAction.Inner_DataUtil.ALL ? "Drop All [" + first + "]" : "Drop [" + first + "]";
      return base + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public String getIcon() {
      return "D";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   private static int entrySlot(String entry) {
      if (entry != null && entry.startsWith("#")) {
         int separator = entry.indexOf(124);
         String raw = separator >= 0 ? entry.substring(1, separator) : entry.substring(1);

         try {
            return Integer.parseInt(raw);
         } catch (NumberFormatException ignored) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static String entrySpec(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry.trim();
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private List<MacroItemTarget> load() {
      if (!this.itemTargets.isEmpty()) {
         return this.itemTargets;
      }

      for (String itemName : this.itemNames) {
         MacroItemTarget target = MacroItemTarget.parse(itemName);
         if (target.hasSlot() || target.hasItemFilter()) {
            this.itemTargets.add(target);
         }
      }

      return this.itemTargets;
   }

   private MacroItemTarget targetAt(int index) {
      List<MacroItemTarget> targets = this.load();
      return index >= 0 && index < targets.size() ? targets.get(index) : new MacroItemTarget();
   }

   private void syncItemNames() {
      this.itemNames.clear();

      for (MacroItemTarget target : this.itemTargets) {
         if (target != null) {
            String entry = target.toSpecString();
            if (!entry.isBlank()) {
               this.itemNames.add(entry);
            }
         }
      }
   }

   public enum Inner_DataUtil {
      ALL,
      TIMES;
   }
}
