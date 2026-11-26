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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final autismclient.util.ContainerClickType[] init_NArr = autismclient.util.ContainerClickType.values();
   private static final int[] init_IArr = new int[]{
      autismclient.util.ContainerClickType.PICKUP.ordinal(),
      autismclient.util.ContainerClickType.QUICK_MOVE.ordinal(),
      autismclient.util.ContainerClickType.SWAP.ordinal(),
      autismclient.util.ContainerClickType.CLONE.ordinal(),
      autismclient.util.ContainerClickType.DROP_ITEM.ordinal(),
      autismclient.util.ContainerClickType.DROP_STACK.ordinal()
   };
   private static final int[] load = new int[]{
      autismclient.util.ContainerClickType.PICKUP.ordinal(),
      autismclient.util.ContainerClickType.PICKUP_ALL.ordinal(),
      autismclient.util.ContainerClickType.QUICK_MOVE.ordinal(),
      autismclient.util.ContainerClickType.SWAP.ordinal(),
      autismclient.util.ContainerClickType.CLONE.ordinal(),
      autismclient.util.ContainerClickType.DROP_ITEM.ordinal(),
      autismclient.util.ContainerClickType.DROP_STACK.ordinal()
   };

   public int init_I_I_3(int index) {
      return index < this.itemTimes.size() && this.itemTimes.get(index) > 0 ? this.itemTimes.get(index) : 1;
   }

   public int load_I(int index) {
      int stored = index < this.itemActionIdx.size() ? this.itemActionIdx.get(index) : this.actionIndex;
      return sync(stored);
   }

   public int read_I(int index) {
      int raw = index < this.itemButtons.size() ? this.itemButtons.get(index) : this.button;
      return init_I_N_I(this.init_N_I(index), raw);
   }

   public autismclient.util.ContainerClickType init_N_I(int index) {
      return init_NArr[this.load_I(index)];
   }

   public String init_Str_I_2(int index) {
      if (this.init_N_I(index) == autismclient.util.ContainerClickType.PICKUP_ALL) {
         return "All";
      }

      if (this.init_N_I(index) == autismclient.util.ContainerClickType.SWAP) {
         int hotbarSlot = Math.max(0, Math.min(8, this.read_I(index))) + 1;
         return "Hotbar " + hotbarSlot;
      }

      String var10000;
      switch (this.read_I(index)) {
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

   public void init_V_I(int index) {
      while (this.itemActionIdx.size() <= index) {
         this.itemActionIdx.add(this.actionIndex);
      }

      this.itemActionIdx.set(index, init_I_I_I(this.itemActionIdx.get(index), 1));
      this.update_V(index);
   }

   public void load_V(int index) {
      while (this.itemActionIdx.size() <= index) {
         this.itemActionIdx.add(this.actionIndex);
      }

      this.itemActionIdx.set(index, init_I_I_I(this.itemActionIdx.get(index), -1));
      this.update_V(index);
   }

   public void read_V(int index) {
      while (this.itemButtons.size() <= index) {
         this.itemButtons.add(this.button);
      }

      if (this.init_N_I(index) != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = init_I_N(this.init_N_I(index));
         if (limit > 1) {
            int current = this.read_I(index);
            this.itemButtons.set(index, (current + 1) % limit);
         }
      }
   }

   public void write_V(int index) {
      while (this.itemButtons.size() <= index) {
         this.itemButtons.add(this.button);
      }

      if (this.init_N_I(index) != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = init_I_N(this.init_N_I(index));
         if (limit > 1) {
            int current = this.read_I(index);
            this.itemButtons.set(index, (current - 1 + limit) % limit);
         }
      }
   }

   public static List<autismclient.util.ContainerClickType> init_List_4() {
      List<autismclient.util.ContainerClickType> list = new ArrayList<>();

      for (int ordinal : init_IArr) {
         list.add(init_NArr[ordinal]);
      }

      return list;
   }

   public int write_I(int index) {
      int ordinal = this.init_N_I(index).ordinal();
      List<autismclient.util.ContainerClickType> opts = init_List_4();

      for (int i = 0; i < opts.size(); i++) {
         if (opts.get(i).ordinal() == ordinal) {
            return i;
         }
      }

      return 0;
   }

   public void init_V_I_I(int index, int optionIndex) {
      if (index >= 0) {
         List<autismclient.util.ContainerClickType> opts = init_List_4();
         if (optionIndex >= 0 && optionIndex < opts.size()) {
            while (this.itemActionIdx.size() <= index) {
               this.itemActionIdx.add(this.actionIndex);
            }

            this.itemActionIdx.set(index, sync(opts.get(optionIndex).ordinal()));
            this.update_V(index);
         }
      }
   }

   public int update_I(int index) {
      return init_I_N(this.init_N_I(index));
   }

   public List<String> init_List_I(int index) {
      autismclient.util.ContainerClickType action = this.init_N_I(index);
      int limit = Math.max(1, init_I_N(action));
      List<String> opts = new ArrayList<>();

      for (int b = 0; b < limit; b++) {
         opts.add(init_Str_N_I(action, b));
      }

      return opts;
   }

   private static String init_Str_N_I(autismclient.util.ContainerClickType action, int b) {
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

   public void load_V_I_I(int index, int optionIndex) {
      if (index >= 0) {
         while (this.itemButtons.size() <= index) {
            this.itemButtons.add(this.button);
         }

         int limit = init_I_N(this.init_N_I(index));
         if (limit > 1) {
            this.itemButtons.set(index, Math.floorMod(optionIndex, limit));
         }
      }
   }

   public boolean init_Z_I_2(int index) {
      return this.init_MapStore_DataUtil_I(index) == ItemAction.Inner_DataUtil.PLAYER_INVENTORY;
   }

   public void init_V_I_Z(int index, boolean value) {
      this.init_V_I_MapStore_DataUtil(index, value ? ItemAction.Inner_DataUtil.PLAYER_INVENTORY : ItemAction.Inner_DataUtil.BOTH);
   }

   public ItemAction.Inner_DataUtil init_MapStore_DataUtil_I(int index) {
      if (index >= 0 && index < this.itemSearchScopes.size()) {
         return ItemAction.Inner_DataUtil.values()[close(this.itemSearchScopes.get(index))];
      } else {
         return index >= 0 && index < this.preferPlayerInventory.size() && Boolean.TRUE.equals(this.preferPlayerInventory.get(index))
            ? ItemAction.Inner_DataUtil.PLAYER_INVENTORY
            : ItemAction.Inner_DataUtil.BOTH;
      }
   }

   public void read(int index, int direction) {
      if (index >= 0) {
         int next = Math.floorMod(this.init_MapStore_DataUtil_I(index).ordinal() + direction, ItemAction.Inner_DataUtil.values().length);
         this.init_V_I_MapStore_DataUtil(index, ItemAction.Inner_DataUtil.values()[next]);
      }
   }

   public void init_V_I_MapStore_DataUtil(int index, ItemAction.Inner_DataUtil scope) {
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

   public ItemAction.Inner_CachePool init_MapStore_CachePool_I(int index) {
      int raw = index >= 0 && index < this.stackAmountModes.size()
         ? this.stackAmountModes.get(index)
         : ItemAction.Inner_CachePool.DEFAULT.ordinal();
      return ItemAction.Inner_CachePool.values()[flush(raw)];
   }

   public void write(int index, int direction) {
      if (index >= 0) {
         while (this.stackAmountModes.size() <= index) {
            this.stackAmountModes.add(ItemAction.Inner_CachePool.DEFAULT.ordinal());
         }

         int current = flush(this.stackAmountModes.get(index));
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
            int clickCount = this.init_I_I_3(ei);
            autismclient.util.ContainerClickType eiAction = this.init_N_I(ei);
            int eiButton = this.read_I(ei);
            if (init_Z_N(eiAction)) {
               eiButton = eiAction.init_I();
            }

            MacroItemTarget entryTarget = this.init_ArrayUtil_I(ei).init_ArrayUtil_Minecraft(mc);
            if (entryTarget != null) {
               String entryName = init_Str_Str(entryTarget.write_Str());
               int configuredEntrySlot = entryTarget.init_Z() ? entryTarget.init_I : -1;
               if (eiAction == autismclient.util.ContainerClickType.PICKUP_ALL && this.useCursorItemForPickupAll) {
                  out.add(new ItemAction.Inner_StreamUtil(eiAction, eiButton, -1, entryTarget, clickCount));
               } else {
                  int slotToUse;
                  if (configuredEntrySlot >= 0 && !entryName.isEmpty()) {
                     int resolvedSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, configuredEntrySlot);
                     if (resolvedSlot < 0) {
                        continue;
                     }

                     slotToUse = this.init(handler, entryTarget, resolvedSlot);
                  } else if (configuredEntrySlot >= 0) {
                     slotToUse = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, configuredEntrySlot);
                  } else if (this.useSlot && this.targetSlot >= 0) {
                     int configuredSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, this.targetSlot);
                     if (configuredSlot < 0) {
                        continue;
                     }

                     slotToUse = this.init(handler, entryTarget, configuredSlot);
                  } else {
                     slotToUse = this.init(handler, entryTarget, this.init_MapStore_DataUtil_I(ei), this.init_MapStore_CachePool_I(ei));
                  }

                  if (slotToUse >= 0) {
                     out.add(new ItemAction.Inner_StreamUtil(eiAction, eiButton, slotToUse, null, clickCount));
                  }
               }
            }
         }
      } else if (this.useSlot && this.targetSlot >= 0) {
         autismclient.util.ContainerClickType action = this.init_N();
         int effectiveButton = this.init_I_2();
         if (init_Z_N(action)) {
            effectiveButton = action.init_I();
         }

         if (action == autismclient.util.ContainerClickType.PICKUP_ALL && this.useCursorItemForPickupAll) {
            out.add(new ItemAction.Inner_StreamUtil(action, effectiveButton, -1, MacroItemTarget.init_ArrayUtil_I(this.targetSlot), this.times));
            return out;
         }

         int resolvedTargetSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, this.targetSlot);
         if (resolvedTargetSlot < 0) {
            return out;
         }

         out.add(new ItemAction.Inner_StreamUtil(action, effectiveButton, resolvedTargetSlot, null, this.times));
      } else if (this.init_N() == autismclient.util.ContainerClickType.PICKUP_ALL && this.useCursorItemForPickupAll) {
         out.add(new ItemAction.Inner_StreamUtil(this.init_N(), this.init_I_2(), -1, null, this.times));
      }

      return out;
   }

   private static void init_V_Minecraft_MapStore_StreamUti(Minecraft mc, ItemAction.Inner_StreamUtil e) {
      boolean cursorPickupAll = e.init_ArrayUtil() != null || e.init_N() == autismclient.util.ContainerClickType.PICKUP_ALL && e.load() < 0;
      if (cursorPickupAll) {
         autismclient.util.ContainerPickUpAllHelper.init(mc, e.init_ArrayUtil(), e.read());
      } else if (e.init_N() != autismclient.util.ContainerClickType.DROP_ITEM && e.init_N() != autismclient.util.ContainerClickType.DROP_STACK) {
         for (int j = 0; j < e.read(); j++) {
            autismclient.util.ContainerClickExecutor.init(mc, e.load(), e.init_I(), e.init_N().init_ContainerInput());
         }
      } else {
         autismclient.util.InventoryDropHelper.init(mc, e.load(), e.init_N() == autismclient.util.ContainerClickType.DROP_STACK ? 0 : e.read());
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

   public List<Runnable> init_List_Minecraft(Minecraft mc) {
      List<Runnable> plan = new ArrayList<>();
      if (mc.player != null && mc.getConnection() != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler == null) {
            return plan;
         }

         for (ItemAction.Inner_StreamUtil e : this.init_List_Minecraft_AbstractContainerM(mc, handler)) {
            boolean cursorPickupAll = e.init_ArrayUtil() != null || e.init_N() == autismclient.util.ContainerClickType.PICKUP_ALL && e.load() < 0;
            if (cursorPickupAll) {
               MacroItemTarget t = e.init_ArrayUtil();

               for (int j = 0; j < e.read(); j++) {
                  plan.add(() -> autismclient.util.ContainerPickUpAllHelper.init(mc, t, 1));
               }
            } else if (e.init_N() == autismclient.util.ContainerClickType.DROP_STACK) {
               int fs = e.load();
               plan.add(() -> autismclient.util.InventoryDropHelper.init(mc, fs, 0));
            } else if (e.init_N() == autismclient.util.ContainerClickType.DROP_ITEM) {
               int fs = e.load();

               for (int j = 0; j < e.read(); j++) {
                  plan.add(() -> autismclient.util.InventoryDropHelper.init(mc, fs, 1));
               }
            } else {
               int fs = e.load();
               int fb = e.init_I();
               ContainerInput input = e.init_N().init_ContainerInput();

               for (int j = 0; j < e.read(); j++) {
                  plan.add(() -> autismclient.util.ContainerClickExecutor.init(mc, fs, fb, input));
               }
            }
         }

         return plan;
      } else {
         return plan;
      }
   }

   private int init_I_AbstractContainerM_Str(AbstractContainerMenu handler, String name) {
      return this.init_I_AbstractContainerM_ArrayUtil(handler, MacroItemTarget.update(name));
   }

   private int init_I_AbstractContainerM_ArrayUtil(AbstractContainerMenu handler, MacroItemTarget target) {
      return this.init(handler, target, ItemAction.Inner_DataUtil.BOTH, ItemAction.Inner_CachePool.DEFAULT);
   }

   private int init(AbstractContainerMenu handler, MacroItemTarget target, ItemAction.Inner_DataUtil searchScope, ItemAction.Inner_CachePool amountMode) {
      if (!target.update()) {
         return -1;
      }

      ItemAction.Inner_CachePool mode = amountMode == null ? ItemAction.Inner_CachePool.DEFAULT : amountMode;
      ItemAction.Inner_DataUtil scope = searchScope == null ? ItemAction.Inner_DataUtil.BOTH : searchScope;
      int var10000;
      switch (scope) {
         case BOTH:
            var10000 = this.init(handler, target, mode, null);
            break;
         case GUI:
            var10000 = this.init(handler, target, mode, false);
            break;
         case PLAYER_INVENTORY:
            var10000 = this.init(handler, target, mode, true);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int init(AbstractContainerMenu handler, MacroItemTarget target, ItemAction.Inner_CachePool mode, Boolean playerInventoryOnly) {
      int bestSlot = -1;
      int bestScore = -1;
      int bestCount = mode == ItemAction.Inner_CachePool.LEAST ? 2147483647 : -1;

      for (Slot slot : handler.slots) {
         if (slot != null && !slot.getItem().isEmpty()) {
            if (playerInventoryOnly != null) {
               boolean isPlayerInventory = autismclient.util.InventorySlotHelper.isPlayerInventorySlot(init_Minecraft, slot);
               if (playerInventoryOnly != isPlayerInventory) {
                  continue;
               }
            }

            int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(init_Minecraft, slot.index);
            int score = target.init_I_ItemStack_I(slot.getItem(), visibleSlot);
            if (score >= 0) {
               int count = slot.getItem().getCount();
               if (this.init(mode, score, count, bestScore, bestCount)) {
                  bestScore = score;
                  bestCount = count;
                  bestSlot = slot.index;
               }
            }
         }
      }

      return bestScore >= 0 ? bestSlot : -1;
   }

   private boolean init(ItemAction.Inner_CachePool mode, int score, int count, int bestScore, int bestCount) {
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

   private int init(AbstractContainerMenu handler, String name, int requiredSlot) {
      return this.init(handler, MacroItemTarget.update(name), requiredSlot);
   }

   private int init(AbstractContainerMenu handler, MacroItemTarget target, int requiredSlot) {
      if (requiredSlot >= 0 && requiredSlot < handler.slots.size()) {
         Slot slot = handler.slots.get(requiredSlot);
         if (slot == null || slot.getItem().isEmpty()) {
            return -1;
         }

         if (!target.update()) {
            return requiredSlot;
         }

         int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(init_Minecraft, requiredSlot);
         return target.init_Z_ItemStack_I(slot.getItem(), visibleSlot) ? requiredSlot : -1;
      } else {
         return -1;
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
      return this.guiName;
   }

   @Override
   public void init_V_Str(String name) {
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
      tag.put("itemNames", MacroItemTarget.init_ListTag_List(this.load_List_3()));
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
         preferInv.add(StringTag.valueOf(String.valueOf(this.init_Z_I_2(i))));
         searchScopes.add(StringTag.valueOf(String.valueOf(this.init_MapStore_DataUtil_I(i).ordinal())));
      }

      tag.put("preferPlayerInventory", preferInv);
      tag.put("itemSearchScopes", searchScopes);
      ListTag stackModes = new ListTag();

      for (int i = 0; i < this.itemNames.size(); i++) {
         stackModes.add(StringTag.valueOf(String.valueOf(this.init_MapStore_CachePool_I(i).ordinal())));
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
         this.itemTargets.addAll(MacroItemTarget.init_List_ListTag(nl));
      } else if (tag.contains("itemName")) {
         String old = tag.getStringOr("itemName", "");
         if (!old.isEmpty()) {
            this.itemTargets.add(MacroItemTarget.update(old));
         }
      }

      this.update_V();
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
         this.itemActionIdx.set(i, sync(this.itemActionIdx.get(i)));
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
         this.itemButtons.set(i, init_I_N_I(this.init_N_I(i), this.itemButtons.get(i)));
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
               this.itemSearchScopes.add(close(Integer.parseInt(el.asString().orElse("0"))));
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
         this.init_V_I_MapStore_DataUtil(i, ItemAction.Inner_DataUtil.values()[close(this.itemSearchScopes.get(i))]);
      }

      this.stackAmountModes.clear();
      if (tag.contains("stackAmountModes")) {
         for (Tag el : tag.getList("stackAmountModes").orElse(new ListTag())) {
            try {
               this.stackAmountModes.add(flush(Integer.parseInt(el.asString().orElse("0"))));
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
      this.actionIndex = sync(tag.getIntOr("actionIndex", 0));
      this.button = init_I_N_I(this.init_N(), tag.getIntOr("button", 0));
      this.times = tag.getIntOr("times", 1);
      this.waitForGuiBefore = GuiWaitAware.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = GuiWaitAware.load_Z_CompoundTag_Z(tag, false);
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
      List<MacroItemTarget> targets = this.load_List_3();
      if (targets.isEmpty()) {
         return this.useSlot && this.targetSlot >= 0
            ? "#" + this.targetSlot + " (" + this.init_N().load_Str + ")" + GuiWaitAware.init_Str_aP(this)
            : "Item (empty)" + GuiWaitAware.init_Str_aP(this);
      }

      String actionName = this.init_N_I(0).load_Str;
      String e0 = targets.get(0).write_Str();
      int slotNumber = init_I_Str(e0);
      String itemName = init_Str_Str(e0);
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

      return base + GuiWaitAware.init_Str_aP(this);
   }

   @Override
   public String getIcon() {
      return "I";
   }

   public autismclient.util.ContainerClickType init_N() {
      return init_NArr[sync(this.actionIndex)];
   }

   public void init_V_5() {
      this.actionIndex = init_I_I_I(this.actionIndex, 1);
      this.button = init_I_N_I(this.init_N(), this.button);
   }

   public void load_V_3() {
      this.actionIndex = init_I_I_I(this.actionIndex, -1);
      this.button = init_I_N_I(this.init_N(), this.button);
   }

   public String load_Str() {
      if (this.init_N() == autismclient.util.ContainerClickType.PICKUP_ALL) {
         return "All";
      }

      if (this.init_N() == autismclient.util.ContainerClickType.SWAP) {
         int hotbarSlot = Math.max(0, Math.min(8, this.init_I_2())) + 1;
         return "Hotbar " + hotbarSlot;
      }

      String var10000;
      switch (this.init_I_2()) {
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

   public void read_V() {
      if (this.init_N() != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = init_I_N(this.init_N());
         if (limit > 1) {
            this.button = (this.init_I_2() + 1) % limit;
         }
      }
   }

   public void write_V() {
      if (this.init_N() != autismclient.util.ContainerClickType.PICKUP_ALL) {
         int limit = init_I_N(this.init_N());
         if (limit > 1) {
            this.button = (this.init_I_2() - 1 + limit) % limit;
         }
      }
   }

   public int init_I_2() {
      return init_I_N_I(this.init_N(), this.button);
   }

   private void update_V(int index) {
      while (this.itemButtons.size() <= index) {
         this.itemButtons.add(this.button);
      }

      this.itemButtons.set(index, init_I_N_I(this.init_N_I(index), this.itemButtons.get(index)));
   }

   private static boolean init_Z_N(autismclient.util.ContainerClickType action) {
      return action != autismclient.util.ContainerClickType.QUICK_MOVE && action.load();
   }

   private static int init_I_N(autismclient.util.ContainerClickType action) {
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

   private static int init_I_N_I(autismclient.util.ContainerClickType action, int rawButton) {
      if (action == null) {
         action = autismclient.util.ContainerClickType.PICKUP;
      }

      if (init_Z_N(action)) {
         return action.init_I();
      }

      int limit = init_I_N(action);
      if (limit <= 1) {
         return 0;
      }

      int normalized = rawButton % limit;
      return normalized < 0 ? normalized + limit : normalized;
   }

   private static int sync(int rawIndex) {
      for (int allowed : load) {
         if (rawIndex == allowed) {
            return rawIndex;
         }
      }

      return autismclient.util.ContainerClickType.PICKUP.ordinal();
   }

   private static int flush(int rawMode) {
      return rawMode >= 0 && rawMode < ItemAction.Inner_CachePool.values().length ? rawMode : ItemAction.Inner_CachePool.DEFAULT.ordinal();
   }

   private static int close(int rawScope) {
      return rawScope >= 0 && rawScope < ItemAction.Inner_DataUtil.values().length ? rawScope : ItemAction.Inner_DataUtil.BOTH.ordinal();
   }

   private static int init_I_I_I(int current, int direction) {
      int selected = direction >= 0 ? -1 : 0;

      for (int i = 0; i < init_IArr.length; i++) {
         if (init_IArr[i] == current) {
            selected = i;
            break;
         }
      }

      int next = (selected + direction) % init_IArr.length;
      if (next < 0) {
         next += init_IArr.length;
      }

      return init_IArr[next];
   }

   private static int init_I_Str(String entry) {
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

   private static String init_Str_Str(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry;
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private List<MacroItemTarget> load_List_3() {
      if (!this.itemTargets.isEmpty()) {
         return this.itemTargets;
      }

      for (String itemName : this.itemNames) {
         MacroItemTarget target = MacroItemTarget.update(itemName);
         if (target.init_Z() || target.update()) {
            this.itemTargets.add(target);
         }
      }

      return this.itemTargets;
   }

   private MacroItemTarget init_ArrayUtil_I(int index) {
      List<MacroItemTarget> targets = this.load_List_3();
      return index >= 0 && index < targets.size() ? targets.get(index) : new MacroItemTarget();
   }

   private void update_V() {
      this.itemNames.clear();

      for (MacroItemTarget target : this.itemTargets) {
         if (target != null) {
            String entry = target.write_Str();
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

   private record Inner_StreamUtil(autismclient.util.ContainerClickType init_N, int init_I, int load, MacroItemTarget init_ArrayUtil, int read) {
   }
}
