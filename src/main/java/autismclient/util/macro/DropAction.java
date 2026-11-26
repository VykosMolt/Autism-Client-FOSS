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

public class DropAction implements E, MacroAction, aP {
   public DropAction.Inner_DataUtil mode = DropAction.Inner_DataUtil.TIMES;
   public int dropCount = 1;
   public List<String> itemNames = new ArrayList<>();
   public List<ArrayUtil> itemTargets = new ArrayList<>();
   public List<Integer> itemCounts = new ArrayList<>();
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public boolean useHandlerSlots = true;
   public volatile D packetOrder = D.INSTANT;
   private boolean init = true;

   public int init_I_I_4(int index) {
      if (index >= 0 && index < this.itemCounts.size()) {
         return Math.max(0, this.itemCounts.get(index));
      } else {
         return this.mode == DropAction.Inner_DataUtil.ALL ? 0 : Math.max(1, this.dropCount);
      }
   }

   public boolean init_Z_4() {
      for (ArrayUtil target : this.load()) {
         if (target != null && (target.update() || target.init_Z())) {
            return true;
         }
      }

      return false;
   }

   public List<ArrayUtil> init_List_5() {
      return List.copyOf(this.load());
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.gameMode != null) {
         List<ArrayUtil> targets = this.load();
         if (!this.init_Z_4()) {
            if (this.mode == DropAction.Inner_DataUtil.ALL) {
               for (int i = 0; i < 36; i++) {
                  if (!mc.player.getInventory().getItem(i).isEmpty()) {
                     if (this.useHandlerSlots) {
                        int resolved = autismclient.util.aw.load_I(mc, i);
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
                  int resolved = autismclient.util.aw.load_I(mc, selected);
                  if (resolved >= 0) {
                     this.load(mc, resolved, count);
                  }
               } else {
                  this.init(mc, selected, count);
               }
            }
         } else if (!targets.isEmpty()) {
            for (int ei = 0; ei < targets.size(); ei++) {
               ArrayUtil entryTarget = this.init_ArrayUtil_I_2(ei).init_ArrayUtil_Minecraft(mc);
               if (entryTarget != null) {
                  int count = this.init_I_I_4(ei);
                  String entryName = init_Str_Str_3(entryTarget.write_Str());
                  int configuredSlot = entryTarget.init_Z() ? entryTarget.init_I : -1;
                  if (configuredSlot >= 0 && !entryName.isEmpty()) {
                     this.init(mc, configuredSlot, entryTarget, count);
                  } else if (configuredSlot >= 0) {
                     if (this.useHandlerSlots) {
                        int resolvedSlot = autismclient.util.aw.read_I(mc, configuredSlot);
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
      this.init(mc, configuredSlot, ArrayUtil.update(itemName), count);
   }

   private void init(Minecraft mc, int configuredSlot, ArrayUtil target, int count) {
      if (target.update()) {
         if (this.useHandlerSlots) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler != null) {
               int resolvedSlot = autismclient.util.aw.read_I(mc, configuredSlot);
               if (resolvedSlot >= 0 && resolvedSlot < handler.slots.size()) {
                  Slot slot = handler.slots.get(resolvedSlot);
                  if (slot != null && !slot.getItem().isEmpty()) {
                     int visibleSlot = autismclient.util.aw.init_I(mc, resolvedSlot);
                     if (target.init_Z_ItemStack_I(slot.getItem(), visibleSlot)) {
                        int toDrop = count == 0 ? 0 : Math.min(count, slot.getItem().getCount());
                        this.load(mc, resolvedSlot, toDrop);
                     }
                  }
               }
            }
         } else if (configuredSlot >= 0 && configuredSlot < 36) {
            ItemStack stack = mc.player.getInventory().getItem(configuredSlot);
            if (!stack.isEmpty() && target.init_Z_ItemStack_I(stack, configuredSlot)) {
               int toDrop = count == 0 ? 0 : Math.min(count, stack.getCount());
               this.init(mc, configuredSlot, toDrop);
            }
         }
      }
   }

   private void init(Minecraft mc, String itemName, int count) {
      this.init(mc, ArrayUtil.update(itemName), count);
   }

   private void init(Minecraft mc, ArrayUtil target, int count) {
      if (target.update()) {
         int remaining = count;
         if (this.useHandlerSlots) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler != null) {
               for (Slot s : handler.slots) {
                  if (s != null && !s.getItem().isEmpty()) {
                     int visibleSlot = autismclient.util.aw.init_I(mc, s.index);
                     if (target.init_Z_ItemStack_I(s.getItem(), visibleSlot)) {
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
               if (!stack.isEmpty() && target.init_Z_ItemStack_I(stack, i)) {
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
      autismclient.util.O.load(mc, invSlot, count);
   }

   private void load(Minecraft mc, int handlerSlotId, int count) {
      autismclient.util.O.init(mc, handlerSlotId, count);
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
      return this.guiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.guiName = name;
   }

   @Override
   public D init_D() {
      return this.packetOrder;
   }

   @Override
   public StreamGate getType() {
      return StreamGate.DROP;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "DROP");
      tag.putString("mode", this.mode.name());
      tag.putInt("count", this.dropCount);
      tag.put("itemNames", ArrayUtil.init_ListTag_List(this.load()));
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
      E.init_V_CompoundTag_D(tag, this.packetOrder);
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
         this.itemTargets.addAll(ArrayUtil.init_List_ListTag(nl));
      } else if (tag.contains("itemName")) {
         String old = tag.getStringOr("itemName", "");
         if (!old.isEmpty()) {
            this.itemTargets.add(ArrayUtil.update(old));
         }
      }

      this.init_V_7();
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
         this.itemCounts.add(this.init_I_I_4(this.itemCounts.size()));
      }

      this.waitForGuiBefore = aP.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = aP.load_Z_CompoundTag_Z(tag, false);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      this.useHandlerSlots = true;
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = E.init_D_CompoundTag(tag);
   }

   @Override
   public String getDisplayName() {
      List<ArrayUtil> targets = this.load();
      if (targets.isEmpty()) {
         String untargeted = this.mode == DropAction.Inner_DataUtil.ALL ? "Drop Entire Inventory" : "Drop Held Item x" + Math.max(1, this.dropCount);
         return untargeted + aP.init_Str_aP(this);
      }

      String e0 = targets.get(0).write_Str();
      int slot = init_I_Str_2(e0);
      String itemName = init_Str_Str_3(e0);
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
      return base + aP.init_Str_aP(this);
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

   private static int init_I_Str_2(String entry) {
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

   private static String init_Str_Str_3(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry.trim();
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private List<ArrayUtil> load() {
      if (!this.itemTargets.isEmpty()) {
         return this.itemTargets;
      }

      for (String itemName : this.itemNames) {
         ArrayUtil target = ArrayUtil.update(itemName);
         if (target.init_Z() || target.update()) {
            this.itemTargets.add(target);
         }
      }

      return this.itemTargets;
   }

   private ArrayUtil init_ArrayUtil_I_2(int index) {
      List<ArrayUtil> targets = this.load();
      return index >= 0 && index < targets.size() ? targets.get(index) : new ArrayUtil();
   }

   private void init_V_7() {
      this.itemNames.clear();

      for (ArrayUtil target : this.itemTargets) {
         if (target != null) {
            String entry = target.write_Str();
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
