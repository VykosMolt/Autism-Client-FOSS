package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public class ItemAction implements MacroAction, GuiWaitAware {
   public List<String> itemNames = new ArrayList<>();
   public List<MacroItemTarget> itemTargets = new ArrayList<>();
   public List<Integer> itemTimes = new ArrayList<>();
   public List<Integer> itemActionIdx = new ArrayList<>();
   public List<Integer> itemButtons = new ArrayList<>();
   public List<Integer> itemSearchScopes = new ArrayList<>();
   @Deprecated
   public List<Boolean> preferPlayerInventory = new ArrayList<>();
   public List<Integer> stackAmountModes = new ArrayList<>();
   public int targetSlot = -1;
   public boolean useSlot = false;
   public int actionIndex = 0;
   public int button = 0;
   public int times = 1;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public boolean waitForItem = false;
   public boolean useCursorItemForPickupAll = true;
   public boolean useClickDelay = false;
   public int clickDelayMs = 50;
   private static final Minecraft CLIENT = Minecraft.getInstance();
   private static final autismclient.util.ContainerClickType[] CLICK_TYPES = autismclient.util.ContainerClickType.values();
   private static final int[] CYCLE_ORDER = new int[]{
      autismclient.util.ContainerClickType.PICKUP.ordinal(),
      autismclient.util.ContainerClickType.QUICK_MOVE.ordinal(),
      autismclient.util.ContainerClickType.SWAP.ordinal(),
      autismclient.util.ContainerClickType.CLONE.ordinal(),
      autismclient.util.ContainerClickType.DROP_ITEM.ordinal(),
      autismclient.util.ContainerClickType.DROP_STACK.ordinal()
   };
   private static final int[] ALLOWED_CLICK_TYPES = new int[]{
      autismclient.util.ContainerClickType.PICKUP.ordinal(),
      autismclient.util.ContainerClickType.PICKUP_ALL.ordinal(),
      autismclient.util.ContainerClickType.QUICK_MOVE.ordinal(),
      autismclient.util.ContainerClickType.SWAP.ordinal(),
      autismclient.util.ContainerClickType.CLONE.ordinal(),
      autismclient.util.ContainerClickType.DROP_ITEM.ordinal(),
      autismclient.util.ContainerClickType.DROP_STACK.ordinal()
   };

   public int getItemTimes(int index) {
      return index < this.itemTimes.size() && this.itemTimes.get(index) > 0 ? this.itemTimes.get(index) : 1;
   }

   public int getItemActionIndex(int index) {
      int stored = index < this.itemActionIdx.size() ? this.itemActionIdx.get(index) : this.actionIndex;
      return sanitizeClickType(stored);
   }

   public int getItemButton(int index) {
      int raw = index < this.itemButtons.size() ? this.itemButtons.get(index) : this.button;
      return clampButton(this.getItemClickType(index), raw);
   }

   public autismclient.util.ContainerClickType getItemClickType(int index) {
      return CLICK_TYPES[this.getItemActionIndex(index)];
   }

   public String itemButtonLabel(int index) {
      if (this.getItemClickType(index) == autismclient.util.ContainerClickType.PICKUP_ALL) {
         return "All";
      }

      if (this.getItemClickType(index) == autismclient.util.ContainerClickType.SWAP) {
         int hotbarSlot = Math.max(0, Math.min(8, this.getItemButton(index))) + 1;
         return "Hotbar " + hotbarSlot;
      }

      String var10000;
      switch (this.getItemButton(index)) {
         case 0:
            var10000 = "Left";
            break;
         case 1:
            var10000 = "Right";
            break;
         case 2:
            var10000 = "Middle";
            break;
         default:
            var10000 = "?";
      }

      return var10000;
   }

   public void nextItemAction(int index) {
      while (this.itemActionIdx.size() <= index) {
         this.itemActionIdx.add(this.actionIndex);
      }

      this.itemActionIdx.set(index, cycleActionIndex(this.itemActionIdx.get(index), 1));
      this.normalizeItemButton(index);
   }

   public void previousItemAction(int index) {
      while (this.itemActionIdx.size() <= index) {
         this.itemActionIdx.add(this.actionIndex);
      }

      this.itemActionIdx.set(index, cycleActionIndex(this.itemActionIdx.get(index), -1));
      this.normalizeItemButton(index);
   }

   public void nextItemButton(int index) {
      while (this.itemButtons.size() <= index) {
         this.itemButtons.add(this.button);
      }

      if (this.getItemClickType(index) != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = buttonCount(this.getItemClickType(index));
         if (limit > 1) {
            int current = this.getItemButton(index);
            this.itemButtons.set(index, (current + 1) % limit);
         }
      }
   }

   public void previousItemButton(int index) {
      while (this.itemButtons.size() <= index) {
         this.itemButtons.add(this.button);
      }

      if (this.getItemClickType(index) != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = buttonCount(this.getItemClickType(index));
         if (limit > 1) {
            int current = this.getItemButton(index);
            this.itemButtons.set(index, (current - 1 + limit) % limit);
         }
      }
   }

   public static List<autismclient.util.ContainerClickType> selectableClickTypes() {
      List<autismclient.util.ContainerClickType> list = new ArrayList<>();

      for (int ordinal : CYCLE_ORDER) {
         list.add(CLICK_TYPES[ordinal]);
      }

      return list;
   }

   public int getItemClickTypeOption(int index) {
      int ordinal = this.getItemClickType(index).ordinal();
      List<autismclient.util.ContainerClickType> opts = selectableClickTypes();

      for (int i = 0; i < opts.size(); i++) {
         if (opts.get(i).ordinal() == ordinal) {
            return i;
         }
      }

      return 0;
   }

   public void setItemClickTypeOption(int index, int optionIndex) {
      if (index >= 0) {
         List<autismclient.util.ContainerClickType> opts = selectableClickTypes();
         if (optionIndex >= 0 && optionIndex < opts.size()) {
            while (this.itemActionIdx.size() <= index) {
               this.itemActionIdx.add(this.actionIndex);
            }

            this.itemActionIdx.set(index, sanitizeClickType(opts.get(optionIndex).ordinal()));
            this.normalizeItemButton(index);
         }
      }
   }

   public int getItemButtonCount(int index) {
      return buttonCount(this.getItemClickType(index));
   }

   public List<String> getItemButtonLabels(int index) {
      autismclient.util.ContainerClickType action = this.getItemClickType(index);
      int limit = Math.max(1, buttonCount(action));
      List<String> opts = new ArrayList<>();

      for (int b = 0; b < limit; b++) {
         opts.add(buttonLabel(action, b));
      }

      return opts;
   }

   private static String buttonLabel(autismclient.util.ContainerClickType action, int b) {
      if (action == autismclient.util.ContainerClickType.PICKUP_ALL) {
         return "All";
      }

      if (action == autismclient.util.ContainerClickType.SWAP) {
         return "H" + (Math.max(0, Math.min(8, b)) + 1);
      }

      String var10000;
      switch (b) {
         case 1:
            var10000 = "R";
            break;
         case 2:
            var10000 = "M";
            break;
         default:
            var10000 = "L";
      }

      return var10000;
   }

   public void setItemButton(int index, int optionIndex) {
      if (index >= 0) {
         while (this.itemButtons.size() <= index) {
            this.itemButtons.add(this.button);
         }

         int limit = buttonCount(this.getItemClickType(index));
         if (limit > 1) {
            this.itemButtons.set(index, Math.floorMod(optionIndex, limit));
         }
      }
   }

   public boolean isPlayerInventoryOnly(int index) {
      return this.getItemSearchScope(index) == ItemAction.Inner_DataUtil.PLAYER_INVENTORY;
   }

   public void setPlayerInventoryOnly(int index, boolean value) {
      this.setItemSearchScope(index, value ? ItemAction.Inner_DataUtil.PLAYER_INVENTORY : ItemAction.Inner_DataUtil.BOTH);
   }

   public ItemAction.Inner_DataUtil getItemSearchScope(int index) {
      if (index >= 0 && index < this.itemSearchScopes.size()) {
         return ItemAction.Inner_DataUtil.values()[sanitizeSearchScope(this.itemSearchScopes.get(index))];
      } else {
         return index >= 0 && index < this.preferPlayerInventory.size() && Boolean.TRUE.equals(this.preferPlayerInventory.get(index))
            ? ItemAction.Inner_DataUtil.PLAYER_INVENTORY
            : ItemAction.Inner_DataUtil.BOTH;
      }
   }

   public void cycleSearchScope(int index, int direction) {
      if (index >= 0) {
         int next = Math.floorMod(this.getItemSearchScope(index).ordinal() + direction, ItemAction.Inner_DataUtil.values().length);
         this.setItemSearchScope(index, ItemAction.Inner_DataUtil.values()[next]);
      }
   }

   public void setItemSearchScope(int index, ItemAction.Inner_DataUtil scope) {
      if (index >= 0) {
         ItemAction.Inner_DataUtil safe = scope == null ? ItemAction.Inner_DataUtil.BOTH : scope;

         while (this.itemSearchScopes.size() <= index) {
            this.itemSearchScopes.add(ItemAction.Inner_DataUtil.BOTH.ordinal());
         }

         this.itemSearchScopes.set(index, safe.ordinal());

         while (this.preferPlayerInventory.size() <= index) {
            this.preferPlayerInventory.add(false);
         }

         this.preferPlayerInventory.set(index, safe == ItemAction.Inner_DataUtil.PLAYER_INVENTORY);
      }
   }

   public ItemAction.Inner_CachePool getItemStackAmountMode(int index) {
      int raw = index >= 0 && index < this.stackAmountModes.size()
         ? this.stackAmountModes.get(index)
         : ItemAction.Inner_CachePool.DEFAULT.ordinal();
      return ItemAction.Inner_CachePool.values()[sanitizeAmountMode(raw)];
   }

   public void cycleStackAmountMode(int index, int direction) {
      if (index >= 0) {
         while (this.stackAmountModes.size() <= index) {
            this.stackAmountModes.add(ItemAction.Inner_CachePool.DEFAULT.ordinal());
         }

         int current = sanitizeAmountMode(this.stackAmountModes.get(index));
         int next = (current + direction) % ItemAction.Inner_CachePool.values().length;
         if (next < 0) {
            next += ItemAction.Inner_CachePool.values().length;
         }

         this.stackAmountModes.set(index, next);
      }
   }

   private List<ItemAction.Inner_StreamUtil> init_List_Minecraft_AbstractContainerM(Minecraft mc, AbstractContainerMenu handler) {
      List<ItemAction.Inner_StreamUtil> out = new ArrayList<>();
      if (!this.itemNames.isEmpty()) {
         for (int ei = 0; ei < this.itemNames.size(); ei++) {
            int clickCount = this.getItemTimes(ei);
            autismclient.util.ContainerClickType eiAction = this.getItemClickType(ei);
            int eiButton = this.getItemButton(ei);
            if (hasFixedButton(eiAction)) {
               eiButton = eiAction.defaultButton();
            }

            MacroItemTarget entryTarget = this.targetAt(ei).resolveTemplates(mc);
            if (entryTarget != null) {
               String entryName = entryItemName(entryTarget.toSpecString());
               int configuredEntrySlot = entryTarget.hasSlot() ? entryTarget.slot : -1;
               if (eiAction == autismclient.util.ContainerClickType.PICKUP_ALL && this.useCursorItemForPickupAll) {
                  out.add(new ItemAction.Inner_StreamUtil(eiAction, eiButton, -1, entryTarget, clickCount));
               } else {
                  int slotToUse;
                  if (configuredEntrySlot >= 0 && !entryName.isEmpty()) {
                     int resolvedSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, configuredEntrySlot);
                     if (resolvedSlot < 0) {
                        continue;
                     }

                     slotToUse = this.findSlot(handler, entryTarget, resolvedSlot);
                  } else if (configuredEntrySlot >= 0) {
                     slotToUse = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, configuredEntrySlot);
                  } else if (this.useSlot && this.targetSlot >= 0) {
                     int configuredSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, this.targetSlot);
                     if (configuredSlot < 0) {
                        continue;
                     }

                     slotToUse = this.findSlot(handler, entryTarget, configuredSlot);
                  } else {
                     slotToUse = this.findSlotInScope(handler, entryTarget, this.getItemSearchScope(ei), this.getItemStackAmountMode(ei));
                  }

                  if (slotToUse >= 0) {
                     out.add(new ItemAction.Inner_StreamUtil(eiAction, eiButton, slotToUse, null, clickCount));
                  }
               }
            }
         }
      } else if (this.useSlot && this.targetSlot >= 0) {
         autismclient.util.ContainerClickType action = this.currentClickType();
         int effectiveButton = this.effectiveButton();
         if (hasFixedButton(action)) {
            effectiveButton = action.defaultButton();
         }

         if (action == autismclient.util.ContainerClickType.PICKUP_ALL && this.useCursorItemForPickupAll) {
            out.add(new ItemAction.Inner_StreamUtil(action, effectiveButton, -1, MacroItemTarget.ofSlot(this.targetSlot), this.times));
            return out;
         }

         int resolvedTargetSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, this.targetSlot);
         if (resolvedTargetSlot < 0) {
            return out;
         }

         out.add(new ItemAction.Inner_StreamUtil(action, effectiveButton, resolvedTargetSlot, null, this.times));
      } else if (this.currentClickType() == autismclient.util.ContainerClickType.PICKUP_ALL && this.useCursorItemForPickupAll) {
         out.add(new ItemAction.Inner_StreamUtil(this.currentClickType(), this.effectiveButton(), -1, null, this.times));
      }

      return out;
   }

   private static void init_V_Minecraft_MapStore_StreamUti(Minecraft mc, ItemAction.Inner_StreamUtil e) {
      boolean cursorPickupAll = e.pickupTarget() != null || e.clickType() == autismclient.util.ContainerClickType.PICKUP_ALL && e.load() < 0;
      if (cursorPickupAll) {
         autismclient.util.ContainerPickUpAllHelper.pickUpAll(mc, e.pickupTarget(), e.read());
      } else if (e.clickType() != autismclient.util.ContainerClickType.DROP_ITEM && e.clickType() != autismclient.util.ContainerClickType.DROP_STACK) {
         for (int j = 0; j < e.read(); j++) {
            autismclient.util.ContainerClickExecutor.click(mc, e.load(), e.button(), e.clickType().toContainerInput());
         }
      } else {
         autismclient.util.InventoryDropHelper.dropFromHandler(mc, e.load(), e.clickType() == autismclient.util.ContainerClickType.DROP_STACK ? 0 : e.read());
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.getConnection() != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler != null) {
            for (ItemAction.Inner_StreamUtil e : this.init_List_Minecraft_AbstractContainerM(mc, handler)) {
               init_V_Minecraft_MapStore_StreamUti(mc, e);
            }
         }
      }
   }

   public List<Runnable> buildClickPlan(Minecraft mc) {
      List<Runnable> plan = new ArrayList<>();
      if (mc.player != null && mc.getConnection() != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler == null) {
            return plan;
         }

         for (ItemAction.Inner_StreamUtil e : this.init_List_Minecraft_AbstractContainerM(mc, handler)) {
            boolean cursorPickupAll = e.pickupTarget() != null || e.clickType() == autismclient.util.ContainerClickType.PICKUP_ALL && e.load() < 0;
            if (cursorPickupAll) {
               MacroItemTarget t = e.pickupTarget();

               for (int j = 0; j < e.read(); j++) {
                  plan.add(() -> autismclient.util.ContainerPickUpAllHelper.pickUpAll(mc, t, 1));
               }
            } else if (e.clickType() == autismclient.util.ContainerClickType.DROP_STACK) {
               int fs = e.load();
               plan.add(() -> autismclient.util.InventoryDropHelper.dropFromHandler(mc, fs, 0));
            } else if (e.clickType() == autismclient.util.ContainerClickType.DROP_ITEM) {
               int fs = e.load();

               for (int j = 0; j < e.read(); j++) {
                  plan.add(() -> autismclient.util.InventoryDropHelper.dropFromHandler(mc, fs, 1));
               }
            } else {
               int fs = e.load();
               int fb = e.button();
               ContainerInput input = e.clickType().toContainerInput();

               for (int j = 0; j < e.read(); j++) {
                  plan.add(() -> autismclient.util.ContainerClickExecutor.click(mc, fs, fb, input));
               }
            }
         }

         return plan;
      } else {
         return plan;
      }
   }

   private int findSlotByName(AbstractContainerMenu handler, String name) {
      return this.findSlotForTarget(handler, MacroItemTarget.parse(name));
   }

   private int findSlotForTarget(AbstractContainerMenu handler, MacroItemTarget target) {
      return this.findSlotInScope(handler, target, ItemAction.Inner_DataUtil.BOTH, ItemAction.Inner_CachePool.DEFAULT);
   }

   private int findSlotInScope(AbstractContainerMenu handler, MacroItemTarget target, ItemAction.Inner_DataUtil searchScope, ItemAction.Inner_CachePool amountMode) {
      if (!target.hasItemFilter()) {
         return -1;
      }

      ItemAction.Inner_CachePool mode = amountMode == null ? ItemAction.Inner_CachePool.DEFAULT : amountMode;
      ItemAction.Inner_DataUtil scope = searchScope == null ? ItemAction.Inner_DataUtil.BOTH : searchScope;
      int var10000;
      switch (scope) {
         case BOTH:
            var10000 = this.findBestSlot(handler, target, mode, null);
            break;
         case GUI:
            var10000 = this.findBestSlot(handler, target, mode, false);
            break;
         case PLAYER_INVENTORY:
            var10000 = this.findBestSlot(handler, target, mode, true);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int findBestSlot(AbstractContainerMenu handler, MacroItemTarget target, ItemAction.Inner_CachePool mode, Boolean playerInventoryOnly) {
      int bestSlot = -1;
      int bestScore = -1;
      int bestCount = mode == ItemAction.Inner_CachePool.LEAST ? 2147483647 : -1;

      for (Slot slot : handler.slots) {
         if (slot != null && !slot.getItem().isEmpty()) {
            if (playerInventoryOnly != null) {
               boolean isPlayerInventory = autismclient.util.InventorySlotHelper.isPlayerInventorySlot(CLIENT, slot);
               if (playerInventoryOnly != isPlayerInventory) {
                  continue;
               }
            }

            int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(CLIENT, slot.index);
            int score = target.matchScore(slot.getItem(), visibleSlot);
            if (score >= 0) {
               int count = slot.getItem().getCount();
               if (this.isBetterMatch(mode, score, count, bestScore, bestCount)) {
                  bestScore = score;
                  bestCount = count;
                  bestSlot = slot.index;
               }
            }
         }
      }

      return bestScore >= 0 ? bestSlot : -1;
   }

   private boolean isBetterMatch(ItemAction.Inner_CachePool mode, int score, int count, int bestScore, int bestCount) {
      if (bestScore < 0) {
         return true;
      } else if (mode == ItemAction.Inner_CachePool.LEAST) {
         return count != bestCount ? count < bestCount : score > bestScore;
      } else if (mode == ItemAction.Inner_CachePool.MOST) {
         return count != bestCount ? count > bestCount : score > bestScore;
      } else {
         return score > bestScore;
      }
   }

   private int findSlotByName(AbstractContainerMenu handler, String name, int requiredSlot) {
      return this.findSlot(handler, MacroItemTarget.parse(name), requiredSlot);
   }

   private int findSlot(AbstractContainerMenu handler, MacroItemTarget target, int requiredSlot) {
      if (requiredSlot >= 0 && requiredSlot < handler.slots.size()) {
         Slot slot = handler.slots.get(requiredSlot);
         if (slot == null || slot.getItem().isEmpty()) {
            return -1;
         }

         if (!target.hasItemFilter()) {
            return requiredSlot;
         }

         int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(CLIENT, requiredSlot);
         return target.matches(slot.getItem(), visibleSlot) ? requiredSlot : -1;
      } else {
         return -1;
      }
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
   public boolean g_() {
      return true;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.ITEM;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "ITEM");
      tag.put("itemNames", MacroItemTarget.toListTag(this.effectiveTargets()));
      ListTag times2 = new ListTag();

      for (int t : this.itemTimes) {
         times2.add(StringTag.valueOf(String.valueOf(t)));
      }

      tag.put("itemTimes", times2);
      ListTag aidx = new ListTag();

      for (int a : this.itemActionIdx) {
         aidx.add(StringTag.valueOf(String.valueOf(a)));
      }

      tag.put("itemActionIdx", aidx);
      ListTag btns = new ListTag();

      for (int b : this.itemButtons) {
         btns.add(StringTag.valueOf(String.valueOf(b)));
      }

      tag.put("itemButtons", btns);
      ListTag preferInv = new ListTag();
      ListTag searchScopes = new ListTag();

      for (int i = 0; i < this.itemNames.size(); i++) {
         preferInv.add(StringTag.valueOf(String.valueOf(this.isPlayerInventoryOnly(i))));
         searchScopes.add(StringTag.valueOf(String.valueOf(this.getItemSearchScope(i).ordinal())));
      }

      tag.put("preferPlayerInventory", preferInv);
      tag.put("itemSearchScopes", searchScopes);
      ListTag stackModes = new ListTag();

      for (int i = 0; i < this.itemNames.size(); i++) {
         stackModes.add(StringTag.valueOf(String.valueOf(this.getItemStackAmountMode(i).ordinal())));
      }

      tag.put("stackAmountModes", stackModes);
      tag.putInt("targetSlot", this.targetSlot);
      tag.putBoolean("useSlot", this.useSlot);
      tag.putInt("actionIndex", this.actionIndex);
      tag.putInt("button", this.button);
      tag.putInt("times", this.times);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("waitForItem", this.waitForItem);
      tag.putBoolean("useCursorItemForPickupAll", this.useCursorItemForPickupAll);
      tag.putBoolean("useClickDelay", this.useClickDelay);
      tag.putInt("clickDelayMs", Math.max(0, this.clickDelayMs));
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
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
      this.itemTimes.clear();
      if (tag.contains("itemTimes")) {
         for (Tag el : tag.getList("itemTimes").orElse(new ListTag())) {
            try {
               this.itemTimes.add(Integer.parseInt(el.asString().orElse("1")));
            } catch (NumberFormatException e) {
               this.itemTimes.add(1);
            }
         }
      }

      while (this.itemTimes.size() < this.itemNames.size()) {
         this.itemTimes.add(1);
      }

      this.itemActionIdx.clear();
      if (tag.contains("itemActionIdx")) {
         for (Tag el : tag.getList("itemActionIdx").orElse(new ListTag())) {
            try {
               this.itemActionIdx.add(Integer.parseInt(el.asString().orElse("0")));
            } catch (NumberFormatException e) {
               this.itemActionIdx.add(0);
            }
         }
      }

      while (this.itemActionIdx.size() < this.itemNames.size()) {
         this.itemActionIdx.add(0);
      }

      for (int i = 0; i < this.itemActionIdx.size(); i++) {
         this.itemActionIdx.set(i, sanitizeClickType(this.itemActionIdx.get(i)));
      }

      this.itemButtons.clear();
      if (tag.contains("itemButtons")) {
         for (Tag el : tag.getList("itemButtons").orElse(new ListTag())) {
            try {
               this.itemButtons.add(Integer.parseInt(el.asString().orElse("0")));
            } catch (NumberFormatException e) {
               this.itemButtons.add(0);
            }
         }
      }

      while (this.itemButtons.size() < this.itemNames.size()) {
         this.itemButtons.add(0);
      }

      for (int i = 0; i < this.itemButtons.size(); i++) {
         this.itemButtons.set(i, clampButton(this.getItemClickType(i), this.itemButtons.get(i)));
      }

      this.preferPlayerInventory.clear();
      if (tag.contains("preferPlayerInventory")) {
         for (Tag el : tag.getList("preferPlayerInventory").orElse(new ListTag())) {
            this.preferPlayerInventory.add(Boolean.parseBoolean(el.asString().orElse("false")));
         }
      }

      while (this.preferPlayerInventory.size() < this.itemNames.size()) {
         this.preferPlayerInventory.add(false);
      }

      while (this.preferPlayerInventory.size() > this.itemNames.size()) {
         this.preferPlayerInventory.remove(this.preferPlayerInventory.size() - 1);
      }

      this.itemSearchScopes.clear();
      if (tag.contains("itemSearchScopes")) {
         for (Tag el : tag.getList("itemSearchScopes").orElse(new ListTag())) {
            try {
               this.itemSearchScopes.add(sanitizeSearchScope(Integer.parseInt(el.asString().orElse("0"))));
            } catch (NumberFormatException ignored) {
               this.itemSearchScopes.add(ItemAction.Inner_DataUtil.BOTH.ordinal());
            }
         }
      } else {
         for (int i = 0; i < this.itemNames.size(); i++) {
            this.itemSearchScopes
               .add(
                  Boolean.TRUE.equals(this.preferPlayerInventory.get(i))
                     ? ItemAction.Inner_DataUtil.PLAYER_INVENTORY.ordinal()
                     : ItemAction.Inner_DataUtil.BOTH.ordinal()
               );
         }
      }

      while (this.itemSearchScopes.size() < this.itemNames.size()) {
         this.itemSearchScopes.add(ItemAction.Inner_DataUtil.BOTH.ordinal());
      }

      while (this.itemSearchScopes.size() > this.itemNames.size()) {
         this.itemSearchScopes.remove(this.itemSearchScopes.size() - 1);
      }

      for (int i = 0; i < this.itemSearchScopes.size(); i++) {
         this.setItemSearchScope(i, ItemAction.Inner_DataUtil.values()[sanitizeSearchScope(this.itemSearchScopes.get(i))]);
      }

      this.stackAmountModes.clear();
      if (tag.contains("stackAmountModes")) {
         for (Tag el : tag.getList("stackAmountModes").orElse(new ListTag())) {
            try {
               this.stackAmountModes.add(sanitizeAmountMode(Integer.parseInt(el.asString().orElse("0"))));
            } catch (NumberFormatException ignored) {
               this.stackAmountModes.add(ItemAction.Inner_CachePool.DEFAULT.ordinal());
            }
         }
      }

      while (this.stackAmountModes.size() < this.itemNames.size()) {
         this.stackAmountModes.add(ItemAction.Inner_CachePool.DEFAULT.ordinal());
      }

      while (this.stackAmountModes.size() > this.itemNames.size()) {
         this.stackAmountModes.remove(this.stackAmountModes.size() - 1);
      }

      this.targetSlot = tag.getIntOr("targetSlot", -1);
      this.useSlot = tag.getBooleanOr("useSlot", false);
      this.actionIndex = sanitizeClickType(tag.getIntOr("actionIndex", 0));
      this.button = clampButton(this.currentClickType(), tag.getIntOr("button", 0));
      this.times = tag.getIntOr("times", 1);
      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, false);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      if (tag.contains("waitForItem")) {
         this.waitForItem = tag.getBooleanOr("waitForItem", false);
      }

      this.useCursorItemForPickupAll = tag.getBooleanOr("useCursorItemForPickupAll", true);
      this.useClickDelay = tag.getBooleanOr("useClickDelay", false);
      this.clickDelayMs = Math.max(0, tag.getIntOr("clickDelayMs", 50));
   }

   @Override
   public String getDisplayName() {
      List<MacroItemTarget> targets = this.effectiveTargets();
      if (targets.isEmpty()) {
         return this.useSlot && this.targetSlot >= 0
            ? "#" + this.targetSlot + " (" + this.currentClickType().load_Str + ")" + GuiWaitAware.waitSuffix(this)
            : "Item (empty)" + GuiWaitAware.waitSuffix(this);
      }

      String actionName = this.getItemClickType(0).load_Str;
      String e0 = targets.get(0).toSpecString();
      int slotNumber = parseSlotEntry(e0);
      String itemName = entryItemName(e0);
      String first;
      if (slotNumber >= 0 && !itemName.isEmpty()) {
         first = itemName + " @ " + slotNumber;
      } else if (slotNumber >= 0) {
         first = "#" + slotNumber;
      } else {
         first = itemName;
      }

      String shortName = first.length() > 12 ? first.substring(0, 10) + ".." : first;
      String suffix = this.itemNames.size() > 1 ? " (+" + (this.itemNames.size() - 1) + ")" : "";
      String base = "Item " + shortName + suffix + " (" + actionName + ")";
      if (this.waitForItem) {
         base = base + " [wait]";
      }

      return base + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public String getIcon() {
      return "I";
   }

   public autismclient.util.ContainerClickType currentClickType() {
      return CLICK_TYPES[sanitizeClickType(this.actionIndex)];
   }

   public void nextClickAction() {
      this.actionIndex = cycleActionIndex(this.actionIndex, 1);
      this.button = clampButton(this.currentClickType(), this.button);
   }

   public void previousClickAction() {
      this.actionIndex = cycleActionIndex(this.actionIndex, -1);
      this.button = clampButton(this.currentClickType(), this.button);
   }

   public String currentButtonLabel() {
      if (this.currentClickType() == autismclient.util.ContainerClickType.PICKUP_ALL) {
         return "All";
      }

      if (this.currentClickType() == autismclient.util.ContainerClickType.SWAP) {
         int hotbarSlot = Math.max(0, Math.min(8, this.effectiveButton())) + 1;
         return "Hotbar " + hotbarSlot;
      }

      String var10000;
      switch (this.effectiveButton()) {
         case 0:
            var10000 = "Left";
            break;
         case 1:
            var10000 = "Right";
            break;
         case 2:
            var10000 = "Middle";
            break;
         default:
            var10000 = "?";
      }

      return var10000;
   }

   public void nextButton() {
      if (this.currentClickType() != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = buttonCount(this.currentClickType());
         if (limit > 1) {
            this.button = (this.effectiveButton() + 1) % limit;
         }
      }
   }

   public void previousButton() {
      if (this.currentClickType() != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = buttonCount(this.currentClickType());
         if (limit > 1) {
            this.button = (this.effectiveButton() - 1 + limit) % limit;
         }
      }
   }

   public int effectiveButton() {
      return clampButton(this.currentClickType(), this.button);
   }

   private void normalizeItemButton(int index) {
      while (this.itemButtons.size() <= index) {
         this.itemButtons.add(this.button);
      }

      this.itemButtons.set(index, clampButton(this.getItemClickType(index), this.itemButtons.get(index)));
   }

   private static boolean hasFixedButton(autismclient.util.ContainerClickType action) {
      return action != autismclient.util.ContainerClickType.QUICK_MOVE && action.hasDefaultButton();
   }

   private static int buttonCount(autismclient.util.ContainerClickType action) {
      byte var10000;
      switch (action) {
         case SWAP:
            var10000 = 9;
            break;
         case PICKUP:
            var10000 = 3;
            break;
         case QUICK_MOVE:
            var10000 = 2;
            break;
         default:
            var10000 = 1;
      }

      return var10000;
   }

   private static int clampButton(autismclient.util.ContainerClickType action, int rawButton) {
      if (action == null) {
         action = autismclient.util.ContainerClickType.PICKUP;
      }

      if (hasFixedButton(action)) {
         return action.defaultButton();
      }

      int limit = buttonCount(action);
      if (limit <= 1) {
         return 0;
      }

      int normalized = rawButton % limit;
      return normalized < 0 ? normalized + limit : normalized;
   }

   private static int sanitizeClickType(int rawIndex) {
      for (int allowed : ALLOWED_CLICK_TYPES) {
         if (rawIndex == allowed) {
            return rawIndex;
         }
      }

      return autismclient.util.ContainerClickType.PICKUP.ordinal();
   }

   private static int sanitizeAmountMode(int rawMode) {
      return rawMode >= 0 && rawMode < ItemAction.Inner_CachePool.values().length ? rawMode : ItemAction.Inner_CachePool.DEFAULT.ordinal();
   }

   private static int sanitizeSearchScope(int rawScope) {
      return rawScope >= 0 && rawScope < ItemAction.Inner_DataUtil.values().length ? rawScope : ItemAction.Inner_DataUtil.BOTH.ordinal();
   }

   private static int cycleActionIndex(int current, int direction) {
      int selected = direction >= 0 ? -1 : 0;

      for (int i = 0; i < CYCLE_ORDER.length; i++) {
         if (CYCLE_ORDER[i] == current) {
            selected = i;
            break;
         }
      }

      int next = (selected + direction) % CYCLE_ORDER.length;
      if (next < 0) {
         next += CYCLE_ORDER.length;
      }

      return CYCLE_ORDER[next];
   }

   private static int parseSlotEntry(String entry) {
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

   private static String entryItemName(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry;
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private List<MacroItemTarget> effectiveTargets() {
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
      List<MacroItemTarget> targets = this.effectiveTargets();
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

   public enum Inner_CachePool {
      DEFAULT("Default"),
      LEAST("Least"),
      MOST("Most");

      public final String init_Str;

      Inner_CachePool(String label) {
         this.init_Str = label;
      }
   }

   public enum Inner_DataUtil {
      BOTH("Both"),
      GUI("GUI"),
      PLAYER_INVENTORY("Player Inventory");

      public final String init_Str;

      Inner_DataUtil(String label) {
         this.init_Str = label;
      }
   }

   private record Inner_StreamUtil(autismclient.util.ContainerClickType clickType, int button, int load, MacroItemTarget pickupTarget, int read) {
   }
}
