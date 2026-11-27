package autismclient.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

final class CraftingClickPlanner {
   static final int init_I = 96;
   private static final Map<Item, ItemStack> init_Map = new ConcurrentHashMap<>();

   private CraftingClickPlanner() {
   }

   static CraftingClickPlanner.Inner_BufferHelper planNextPhase(
      CraftingClickPlanner.Inner_CachePool layout, ItemStack[] slots, ItemStack carried, CraftingClickPlanner.Inner_NetHelper spec, int wantCrafts, int selectedHotbar
   ) {
      if (spec != null && spec.init_ItemStack() != null && !spec.init_ItemStack().isEmpty()) {
         if (!spec.init_Z_I_CachePool(layout)) {
            return new CraftingClickPlanner.Inner_BufferHelper(null, "This recipe needs a 3x3 crafting table.");
         }

         int upper = Math.max(0, Math.min(wantCrafts, 64));
         if (upper <= 0) {
            return new CraftingClickPlanner.Inner_BufferHelper(null, "Nothing to craft.");
         }

         CraftingClickPlanner.Inner_DataUtil first = init_I_DataUtil(layout, slots, carried, spec, upper, selectedHotbar);
         if (first.init_I_ConfigStore != null) {
            return new CraftingClickPlanner.Inner_BufferHelper(first.init_I_ConfigStore, null);
         }

         if (first.init_Z) {
            return new CraftingClickPlanner.Inner_BufferHelper(null, first.init_Str);
         }

         CraftingClickPlanner.Inner_ConfigStore best = null;
         String reason = first.init_Str;
         int lo = 1;
         int hi = upper - 1;

         while (lo <= hi) {
            int mid = lo + hi >>> 1;
            CraftingClickPlanner.Inner_DataUtil attempt = init_I_DataUtil(layout, slots, carried, spec, mid, selectedHotbar);
            if (attempt.init_I_ConfigStore != null) {
               best = attempt.init_I_ConfigStore;
               lo = mid + 1;
            } else {
               if (attempt.init_Z) {
                  return new CraftingClickPlanner.Inner_BufferHelper(null, attempt.init_Str);
               }

               if (mid == 1 || best == null) {
                  reason = attempt.init_Str;
               }

               hi = mid - 1;
            }
         }

         return best != null ? new CraftingClickPlanner.Inner_BufferHelper(best, null) : new CraftingClickPlanner.Inner_BufferHelper(null, reason);
      } else {
         return new CraftingClickPlanner.Inner_BufferHelper(null, "Unknown recipe.");
      }
   }

   static List<CraftingClickPlanner.Inner_ConfigStore> planPhases(
      CraftingClickPlanner.Inner_CachePool layout, ItemStack[] slots, ItemStack carried, CraftingClickPlanner.Inner_NetHelper spec, int wantCrafts, int selectedHotbar
   ) {
      List<CraftingClickPlanner.Inner_ConfigStore> phases = new ArrayList<>();
      ItemStack[] state = init(slots);
      ItemStack hand = carried == null ? ItemStack.EMPTY : carried.copy();
      int left = wantCrafts;

      while (left > 0 && phases.size() < 64) {
         CraftingClickPlanner.Inner_BufferHelper outcome = planNextPhase(layout, state, hand, spec, left, selectedHotbar);
         if (outcome.phase() == null) {
            break;
         }

         CraftingClickPlanner.Inner_ConfigStore phase = outcome.phase();
         phases.add(phase);
         left -= phase.load_I();
         state = init(phase.finalSlots());
         hand = phase.finalCarried().copy();
      }

      return phases;
   }

   static CraftingClickPlanner.Inner_ConfigStore init(CraftingClickPlanner.Inner_CachePool layout, ItemStack[] slots, ItemStack carried) {
      CraftingClickPlanner.Inner_TaskQueue sim = new CraftingClickPlanner.Inner_TaskQueue(layout, null, slots, carried);
      sim.init_Z();
      sim.load_Z();
      return sim.init_List.isEmpty() ? null : sim.init_I_ConfigStore(0);
   }

   private static CraftingClickPlanner.Inner_DataUtil init_I_DataUtil(
      CraftingClickPlanner.Inner_CachePool layout, ItemStack[] slots, ItemStack carried, CraftingClickPlanner.Inner_NetHelper spec, int crafts, int selectedHotbar
   ) {
      CraftingClickPlanner.Inner_TaskQueue sim = new CraftingClickPlanner.Inner_TaskQueue(layout, spec, slots, carried);
      if (!sim.init_Z()) {
         return CraftingClickPlanner.Inner_DataUtil.load("The cursor holds an item and the inventory has no room for it.");
      }

      if (!sim.load_Z()) {
         return CraftingClickPlanner.Inner_DataUtil.load("No room to clear the crafting grid.");
      }

      Map<Integer, ItemStack> assignment = init(sim, crafts);
      if (assignment == null) {
         return CraftingClickPlanner.Inner_DataUtil.init("Missing materials for " + spec.init_ItemStack().getHoverName().getString() + ".");
      }

      if (crafts > 1) {
         for (ItemStack key : assignment.values()) {
            if (!init_ItemStack(key).isEmpty()) {
               return CraftingClickPlanner.Inner_DataUtil.init("one craft at a time");
            }
         }
      }

      LinkedHashMap<ItemStack, List<Integer>> groups = new LinkedHashMap<>();

      for (Entry<Integer, ItemStack> entry : assignment.entrySet()) {
         ItemStack key = null;

         for (ItemStack existing : groups.keySet()) {
            if (ItemStack.isSameItemSameComponents(existing, entry.getValue())) {
               key = existing;
               break;
            }
         }

         groups.computeIfAbsent(key == null ? entry.getValue() : key, ignored -> new ArrayList<>()).add(layout.init_IArr()[entry.getKey()]);
      }

      for (Entry<ItemStack, List<Integer>> group : groups.entrySet()) {
         if (!init(sim, group.getKey(), group.getValue(), crafts, selectedHotbar)) {
            return CraftingClickPlanner.Inner_DataUtil.init("Missing materials for " + spec.init_ItemStack().getHoverName().getString() + ".");
         }
      }

      if (!sim.read_Z()) {
         return CraftingClickPlanner.Inner_DataUtil.load("The planned grid does not match the recipe.");
      }

      sim.init_V();
      if (!sim.init_Z && sim.load_I == crafts) {
         sim.load_Z();
         if (sim.init_ItemStack.isEmpty() && !sim.init_Z) {
            return crafts > 1 && sim.init_List.size() > 96
               ? CraftingClickPlanner.Inner_DataUtil.init("too many clicks")
               : new CraftingClickPlanner.Inner_DataUtil(sim.init_I_ConfigStore(crafts), null, false);
         } else {
            return CraftingClickPlanner.Inner_DataUtil.init("No room in the inventory for the result.");
         }
      } else {
         return CraftingClickPlanner.Inner_DataUtil.init("No room in the inventory for the result.");
      }
   }

   private static Map<Integer, ItemStack> init(CraftingClickPlanner.Inner_TaskQueue sim, int crafts) {
      Ingredient[] cells = sim.init_IngredientArr;
      List<ItemStack> keys = new ArrayList<>();
      List<Integer> avail = new ArrayList<>();

      for (int slot = sim.init_I_CachePool.write(); slot < sim.init_I_CachePool.update(); slot++) {
         ItemStack stack = sim.init_ItemStackArr[slot];
         if (isUsableIngredient(stack)) {
            int index = init(keys, stack);
            if (index < 0) {
               keys.add(stack.copyWithCount(1));
               avail.add(stack.getCount());
            } else {
               avail.set(index, avail.get(index) + stack.getCount());
            }
         }
      }

      List<Integer> order = new ArrayList<>();
      int[] options = new int[cells.length];

      for (int pos = 0; pos < cells.length; pos++) {
         if (cells[pos] != null) {
            order.add(pos);

            for (int k = 0; k < keys.size(); k++) {
               if (cells[pos].test(keys.get(k)) && avail.get(k) >= crafts) {
                  options[pos]++;
               }
            }
         }
      }

      order.sort(Comparator.comparingInt(pos -> options[pos]));
      Map<Integer, ItemStack> chosen = new LinkedHashMap<>();
      Map<Integer, Integer> chosenKey = new LinkedHashMap<>();

      for (int pos : order) {
         Ingredient ingredient = cells[pos];
         int best = -1;
         boolean bestShared = false;

         for (int k = 0; k < keys.size(); k++) {
            ItemStack key = keys.get(k);
            if (ingredient.test(key) && avail.get(k) >= crafts && crafts <= key.getMaxStackSize()) {
               boolean shared = false;

               for (Entry<Integer, Integer> used : chosenKey.entrySet()) {
                  if (used.getValue() == k && ingredient.equals(cells[used.getKey()])) {
                     shared = true;
                     break;
                  }
               }

               if (best < 0 || shared && !bestShared || shared == bestShared && avail.get(k) > avail.get(best)) {
                  best = k;
                  bestShared = shared;
               }
            }
         }

         if (best < 0) {
            return null;
         }

         avail.set(best, avail.get(best) - crafts);
         chosenKey.put(pos, best);
      }

      for (int pos = 0; pos < cells.length; pos++) {
         Integer key = chosenKey.get(pos);
         if (key != null) {
            chosen.put(pos, keys.get(key));
         }
      }

      return chosen;
   }

   private static boolean init(CraftingClickPlanner.Inner_TaskQueue sim, ItemStack key, List<Integer> cells, int crafts, int selectedHotbar) {
      Map<Integer, Integer> need = new LinkedHashMap<>();

      for (int cell : cells) {
         need.put(cell, crafts);
      }

      int total = crafts * cells.size();

      for (int source : init(sim, key, total, selectedHotbar)) {
         int remaining = need.values().stream().mapToInt(Integer::intValue).sum();
         if (remaining <= 0) {
            break;
         }

         int count = sim.init_ItemStackArr[source].getCount();
         if (count > 0) {
            int half = (count + 1) / 2;
            if (count > remaining && half >= remaining && half - remaining < count - remaining) {
               sim.load(source);
            } else {
               sim.init_V(source);
            }

            init(sim, source, need);
            if (!sim.init_ItemStack.isEmpty()) {
               sim.init_V(source);
            }

            if (!sim.init_ItemStack.isEmpty()) {
               return false;
            }
         }
      }

      return need.values().stream().allMatch(value -> value == 0);
   }

   private static void init(CraftingClickPlanner.Inner_TaskQueue sim, int source, Map<Integer, Integer> need) {
      int guard = 0;

      while (!sim.init_ItemStack.isEmpty() && guard++ < 512) {
         List<Integer> open = new ArrayList<>();
         int low = 2147483647;
         int high = 0;

         for (Entry<Integer, Integer> entry : need.entrySet()) {
            if (entry.getValue() > 0) {
               open.add(entry.getKey());
               low = Math.min(low, entry.getValue());
               high = Math.max(high, entry.getValue());
            }
         }

         if (open.isEmpty()) {
            return;
         }

         int amount = sim.init_ItemStack.getCount();
         if (low == high) {
            int cellsOpen = open.size();
            int each = low;
            if (amount == cellsOpen * each) {
               init(sim, open, need, each);
               return;
            }

            if (amount > cellsOpen * each) {
               int excess = amount - cellsOpen * each;
               int singles = cellsOpen * each + 1;
               int deposit = excess + (cellsOpen == 1 ? 1 : cellsOpen + 2);
               if (deposit < singles) {
                  for (int i = 0; i < excess; i++) {
                     sim.load(source);
                  }

                  init(sim, open, need, each);
               } else {
                  for (int cell : open) {
                     for (int i = 0; i < each; i++) {
                        sim.load(cell);
                     }

                     need.put(cell, 0);
                  }
               }

               return;
            }

            if (cellsOpen == 1) {
               int cell = open.get(0);
               sim.init_V(cell);
               need.merge(cell, -amount, Integer::sum);
               return;
            }

            if (amount >= cellsOpen) {
               int share = amount / cellsOpen;
               sim.init(open);

               for (int cell : open) {
                  need.merge(cell, -share, Integer::sum);
               }
            } else {
               for (int cell : open) {
                  if (sim.init_ItemStack.isEmpty()) {
                     break;
                  }

                  sim.load(cell);
                  need.merge(cell, -1, Integer::sum);
               }
            }
         } else {
            for (int cell : open) {
               while (need.get(cell) > low && !sim.init_ItemStack.isEmpty()) {
                  sim.load(cell);
                  need.merge(cell, -1, Integer::sum);
               }
            }
         }
      }
   }

   private static void init(CraftingClickPlanner.Inner_TaskQueue sim, List<Integer> open, Map<Integer, Integer> need, int each) {
      if (open.size() == 1) {
         sim.init_V(open.get(0));
      } else {
         sim.init(open);
      }

      for (int cell : open) {
         need.merge(cell, -each, Integer::sum);
      }
   }

   private static List<Integer> init(CraftingClickPlanner.Inner_TaskQueue sim, ItemStack key, int total, int selectedHotbar) {
      List<Integer> sources = new ArrayList<>();

      for (int slot = sim.init_I_CachePool.write(); slot < sim.init_I_CachePool.update(); slot++) {
         ItemStack stack = sim.init_ItemStackArr[slot];
         if (isUsableIngredient(stack) && ItemStack.isSameItemSameComponents(stack, key)) {
            sources.add(slot);
         }
      }

      int selectedSlot = selectedHotbar < 0 ? -1 : sim.init_I_CachePool.sync() + selectedHotbar;
      Comparator<Integer> tier = Comparator.comparingInt(slotx -> slotx == selectedSlot ? 2 : (slotx >= sim.init_I_CachePool.sync() ? 1 : 0));
      Integer cover = null;

      for (int slot : sources) {
         int count = sim.init_ItemStackArr[slot].getCount();
         if (count >= total
            && (cover == null || tier.compare(slot, cover) < 0 || tier.compare(slot, cover) == 0 && count < sim.init_ItemStackArr[cover].getCount())) {
            cover = slot;
         }
      }

      if (cover != null) {
         sources.remove(cover);
         sources.add(0, cover);
         return sources;
      } else {
         sources.sort(tier.thenComparing(slotx -> -sim.init_ItemStackArr[slotx].getCount()));
         return sources;
      }
   }

   private static int init(List<ItemStack> keys, ItemStack stack) {
      for (int i = 0; i < keys.size(); i++) {
         if (ItemStack.isSameItemSameComponents(keys.get(i), stack)) {
            return i;
         }
      }

      return -1;
   }

   static boolean isUsableIngredient(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && Inventory.isUsableForCrafting(stack)) {
         BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
         return bundle == null || bundle.isEmpty();
      } else {
         return false;
      }
   }

   private static boolean load_Z(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.has(DataComponents.BUNDLE_CONTENTS);
   }

   static ItemStack init_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         return !stack.getComponentsPatch().isEmpty() ? load_ItemStack(stack) : init_Map.computeIfAbsent(stack.getItem(), item -> load_ItemStack(stack)).copy();
      } else {
         return ItemStack.EMPTY;
      }
   }

   private static ItemStack load_ItemStack(ItemStack stack) {
      try {
         List<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(CraftingInput.of(1, 1, List.of(stack.copyWithCount(1))));
         return remaining.isEmpty() ? ItemStack.EMPTY : remaining.get(0).copy();
      } catch (RuntimeException e) {
         return ItemStack.EMPTY;
      }
   }

   static ItemStack[] init(ItemStack[] slots) {
      ItemStack[] out = new ItemStack[slots.length];

      for (int i = 0; i < slots.length; i++) {
         out[i] = slots[i] != null && !slots[i].isEmpty() ? slots[i].copy() : ItemStack.EMPTY;
      }

      return out;
   }

   static String init(CraftingClickPlanner.Inner_ConfigStore phase) {
      if (phase == null) {
         return "none";
      }

      StringBuilder out = new StringBuilder();

      for (CraftingClickPlanner.Inner_StreamUtil click : phase.clicks()) {
         if (out.length() > 0) {
            out.append(' ');
         }

         String var10001;
         switch (click.input()) {
            case PICKUP:
               var10001 = click.button() == 0 ? "L" : "R";
               break;
            case QUICK_MOVE:
               var10001 = "S";
               break;
            case QUICK_CRAFT:
               var10001 = "D" + AbstractContainerMenu.getQuickcraftHeader(click.button());
               break;
            default:
               var10001 = click.input().name();
         }

         out.append(var10001);
         if (click.slot() != -999) {
            out.append(click.slot());
         }
      }

      return out + " (" + phase.load_I() + " crafts)";
   }

   record Inner_BufferHelper(CraftingClickPlanner.Inner_ConfigStore phase, String error) {
   }

   record Inner_CachePool(int resultSlot, int load, int read, int[] init_IArr, int write, int update, int sync) {
      static CraftingClickPlanner.Inner_CachePool craftingTable() {
         return new CraftingClickPlanner.Inner_CachePool(0, 3, 3, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 10, 46, 37);
      }

      static CraftingClickPlanner.Inner_CachePool inventoryGrid() {
         return new CraftingClickPlanner.Inner_CachePool(0, 2, 2, new int[]{1, 2, 3, 4}, 9, 45, 36);
      }

      boolean init(int slot) {
         return slot >= this.init_IArr[0] && slot <= this.init_IArr[this.init_IArr.length - 1];
      }

      public int load_I() {
         return this.load;
      }
   }

   record Inner_ConfigStore(
      List<CraftingClickPlanner.Inner_StreamUtil> clicks, int init_I, List<ItemStack[]> predictedSlots, List<ItemStack> read, List<ItemStack> write, int load_I
   ) {
      ItemStack[] finalSlots() {
         return this.predictedSlots.get(this.predictedSlots.size() - 1);
      }

      ItemStack finalCarried() {
         return this.read.get(this.read.size() - 1);
      }

      int init_I_2() {
         return this.load_I < 0 ? 0 : this.clicks.size() - 1 - this.load_I;
      }

      public int load_I() {
         return this.init_I;
      }

      public List<ItemStack> predictedCarried() {
         return this.read;
      }

      public int read_I() {
         return this.load_I;
      }
   }

   private record Inner_DataUtil(CraftingClickPlanner.Inner_ConfigStore init_I_ConfigStore, String init_Str, boolean init_Z) {
      static CraftingClickPlanner.Inner_DataUtil init(String reason) {
         return new CraftingClickPlanner.Inner_DataUtil(null, reason, false);
      }

      static CraftingClickPlanner.Inner_DataUtil load(String reason) {
         return new CraftingClickPlanner.Inner_DataUtil(null, reason, true);
      }
   }

   record Inner_NetHelper(String init_Str, ItemStack init_ItemStack, int init_I, int load, boolean init_Z, List<Ingredient> init_List) {
      static CraftingClickPlanner.Inner_NetHelper fromRecipe(RecipeCatalogLoader.Inner_StreamUtil recipe) {
         return new CraftingClickPlanner.Inner_NetHelper(
            recipe.recipeKey, recipe.result, recipe.width, recipe.height, recipe.shaped, recipe.shaped ? recipe.gridIngredients : recipe.requirements
         );
      }

      boolean init_Z_I_CachePool(CraftingClickPlanner.Inner_CachePool layout) {
         return !this.init_Z ? this.init_List.size() <= layout.init_IArr().length : this.init_I <= layout.load_I() && this.load <= layout.read();
      }

      Ingredient[] init_IngredientArr_I_CachePool(CraftingClickPlanner.Inner_CachePool layout) {
         Ingredient[] cells = new Ingredient[layout.init_IArr().length];
         if (this.init_Z) {
            for (int row = 0; row < this.load; row++) {
               for (int col = 0; col < this.init_I; col++) {
                  int index = row * this.init_I + col;
                  if (index < this.init_List.size()) {
                     cells[row * layout.load_I() + col] = this.init_List.get(index);
                  }
               }
            }
         } else {
            for (int i = 0; i < this.init_List.size(); i++) {
               cells[i] = this.init_List.get(i);
            }
         }

         return cells;
      }
   }

   record Inner_StreamUtil(int slot, int load, ContainerInput input) {
      static CraftingClickPlanner.Inner_StreamUtil init(int slot) {
         return new CraftingClickPlanner.Inner_StreamUtil(slot, 0, ContainerInput.PICKUP);
      }

      static CraftingClickPlanner.Inner_StreamUtil load(int slot) {
         return new CraftingClickPlanner.Inner_StreamUtil(slot, 1, ContainerInput.PICKUP);
      }

      static CraftingClickPlanner.Inner_StreamUtil read(int slot) {
         return new CraftingClickPlanner.Inner_StreamUtil(slot, 0, ContainerInput.QUICK_MOVE);
      }

      static CraftingClickPlanner.Inner_StreamUtil init_I_StreamUtil() {
         return new CraftingClickPlanner.Inner_StreamUtil(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ContainerInput.QUICK_CRAFT);
      }

      static CraftingClickPlanner.Inner_StreamUtil write(int slot) {
         return new CraftingClickPlanner.Inner_StreamUtil(slot, AbstractContainerMenu.getQuickcraftMask(1, 0), ContainerInput.QUICK_CRAFT);
      }

      static CraftingClickPlanner.Inner_StreamUtil load_I_StreamUtil() {
         return new CraftingClickPlanner.Inner_StreamUtil(-999, AbstractContainerMenu.getQuickcraftMask(2, 0), ContainerInput.QUICK_CRAFT);
      }

      public int button() {
         return this.load;
      }
   }

   static final class Inner_TaskQueue {
      final CraftingClickPlanner.Inner_CachePool init_I_CachePool;
      final CraftingClickPlanner.Inner_NetHelper init_I_NetHelper;
      final Ingredient[] init_IngredientArr;
      final ItemStack[] init_ItemStackArr;
      ItemStack init_ItemStack;
      final List<CraftingClickPlanner.Inner_StreamUtil> init_List = new ArrayList<>();
      final List<ItemStack[]> load_List = new ArrayList<>();
      final List<ItemStack> read = new ArrayList<>();
      final List<ItemStack> write = new ArrayList<>();
      int init_I = -1;
      boolean init_Z;
      int load_I;

      Inner_TaskQueue(CraftingClickPlanner.Inner_CachePool layout, CraftingClickPlanner.Inner_NetHelper spec, ItemStack[] slots, ItemStack carried) {
         this.init_I_CachePool = layout;
         this.init_I_NetHelper = spec;
         this.init_IngredientArr = spec == null ? new Ingredient[layout.init_IArr().length] : spec.init_IngredientArr_I_CachePool(layout);
         this.init_ItemStackArr = CraftingClickPlanner.init(slots);
         this.init_ItemStack = carried != null && !carried.isEmpty() ? carried.copy() : ItemStack.EMPTY;
      }

      CraftingClickPlanner.Inner_ConfigStore init_I_ConfigStore(int crafts) {
         return new CraftingClickPlanner.Inner_ConfigStore(
            List.copyOf(this.init_List), crafts, List.copyOf(this.load_List), List.copyOf(this.read), List.copyOf(this.write), this.init_I
         );
      }

      private ItemStack[] init_ItemStackArr() {
         ItemStack[] grid = new ItemStack[this.init_I_CachePool.init_IArr().length];

         for (int i = 0; i < grid.length; i++) {
            grid[i] = this.init_ItemStackArr[this.init_I_CachePool.init_IArr()[i]].copy();
         }

         return grid;
      }

      private void init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil click, Runnable action) {
         ItemStack[] gridBefore = this.init_ItemStackArr();
         this.write.add(this.init_ItemStackArr[this.init_I_CachePool.resultSlot()].copy());
         action.run();
         if (this.init_ItemStack.isEmpty()) {
            this.init_ItemStack = ItemStack.EMPTY;
         }

         this.load_V();
         this.init_List.add(click);
         if (this.init_I < 0) {
            ItemStack[] gridAfter = this.init_ItemStackArr();

            for (int i = 0; i < gridAfter.length; i++) {
               if (!ItemStack.matches(gridBefore[i], gridAfter[i])) {
                  this.init_I = this.init_List.size() - 1;
                  break;
               }
            }
         }

         this.load_List.add(CraftingClickPlanner.init(this.init_ItemStackArr));
         this.read.add(this.init_ItemStack.copy());
      }

      void init_V(int slot) {
         this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.init(slot), () -> this.init_V_I_Z(slot, true));
      }

      void load(int slot) {
         this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.load(slot), () -> this.init_V_I_Z(slot, false));
      }

      void read(int slot) {
         this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.read(slot), () -> this.write(slot));
      }

      void init_V() {
         this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.read(this.init_I_CachePool.resultSlot()), this::read_V);
      }

      void init(List<Integer> targets) {
         this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.init_I_StreamUtil(), () -> {});

         for (int target : targets) {
            this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.write(target), () -> {});
         }

         this.init_V_I_StreamUtil_Runnable(CraftingClickPlanner.Inner_StreamUtil.load_I_StreamUtil(), () -> this.load(targets));
      }

      boolean init_Z() {
         int guard = 0;

         while (!this.init_ItemStack.isEmpty() && guard++ < 64) {
            int target = -1;

            for (int slot = this.init_I_CachePool.write(); slot < this.init_I_CachePool.update() && target < 0; slot++) {
               ItemStack stack = this.init_ItemStackArr[slot];
               if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, this.init_ItemStack) && stack.getCount() < stack.getMaxStackSize()) {
                  target = slot;
               }
            }

            for (int slot = this.init_I_CachePool.write(); slot < this.init_I_CachePool.update() && target < 0; slot++) {
               if (this.init_ItemStackArr[slot].isEmpty()) {
                  target = slot;
               }
            }

            if (target < 0) {
               return false;
            }

            this.init_V(target);
         }

         return this.init_ItemStack.isEmpty();
      }

      boolean load_Z() {
         boolean clear = true;

         for (int slot : this.init_I_CachePool.init_IArr()) {
            if (!this.init_ItemStackArr[slot].isEmpty()) {
               this.read(slot);
               if (!this.init_ItemStackArr[slot].isEmpty()) {
                  clear = false;
               }
            }
         }

         return clear;
      }

      boolean read_Z() {
         if (this.init_I_NetHelper == null) {
            return false;
         }

         for (int pos = 0; pos < this.init_IngredientArr.length; pos++) {
            ItemStack stack = this.init_ItemStackArr[this.init_I_CachePool.init_IArr()[pos]];
            if (this.init_IngredientArr[pos] == null) {
               if (!stack.isEmpty()) {
                  return false;
               }
            } else if (stack.isEmpty() || !this.init_IngredientArr[pos].test(stack)) {
               return false;
            }
         }

         return true;
      }

      private void load_V() {
         this.init_ItemStackArr[this.init_I_CachePool.resultSlot()] = this.read_Z() ? this.init_I_NetHelper.init_ItemStack().copy() : ItemStack.EMPTY;
      }

      private void init_V_I_Z(int slot, boolean primary) {
         ItemStack clicked = this.init_ItemStackArr[slot];
         if ((CraftingClickPlanner.load_Z(clicked) || CraftingClickPlanner.load_Z(this.init_ItemStack)) && (!primary || !clicked.isEmpty() && !this.init_ItemStack.isEmpty())) {
            this.init_Z = true;
         }

         if (clicked.isEmpty()) {
            if (!this.init_ItemStack.isEmpty()) {
               int amount = primary ? this.init_ItemStack.getCount() : 1;
               int moved = Math.min(Math.min(amount, this.init_ItemStack.getCount()), this.init_ItemStack.getMaxStackSize());
               if (moved > 0) {
                  this.init_ItemStackArr[slot] = this.init_ItemStack.split(moved);
               }
            }
         } else if (this.init_ItemStack.isEmpty()) {
            int amount = primary ? clicked.getCount() : (clicked.getCount() + 1) / 2;
            this.init_ItemStack = clicked.split(amount);
            if (clicked.isEmpty()) {
               this.init_ItemStackArr[slot] = ItemStack.EMPTY;
            }
         } else if (ItemStack.isSameItemSameComponents(clicked, this.init_ItemStack)) {
            int amount = primary ? this.init_ItemStack.getCount() : 1;
            int moved = Math.min(Math.min(amount, this.init_ItemStack.getCount()), this.init_ItemStack.getMaxStackSize() - clicked.getCount());
            if (moved > 0) {
               this.init_ItemStack.shrink(moved);
               clicked.grow(moved);
            }
         } else if (this.init_ItemStack.getCount() <= this.init_ItemStack.getMaxStackSize()) {
            this.init_ItemStackArr[slot] = this.init_ItemStack;
            this.init_ItemStack = clicked;
         }
      }

      private void load(List<Integer> targets) {
         if (!this.init_ItemStack.isEmpty()) {
            List<Integer> added = new ArrayList<>();

            for (int target : targets) {
               if (init_Z_ItemStack_ItemStack(this.init_ItemStackArr[target], this.init_ItemStack) && this.init_ItemStack.getCount() > added.size()) {
                  added.add(target);
               }
            }

            if (!added.isEmpty()) {
               if (added.size() == 1) {
                  this.init_V_I_Z(added.get(0), true);
               } else {
                  ItemStack source = this.init_ItemStack.copy();
                  int remaining = this.init_ItemStack.getCount();

                  for (int target : added) {
                     if (init_Z_ItemStack_ItemStack(this.init_ItemStackArr[target], this.init_ItemStack) && this.init_ItemStack.getCount() >= added.size()) {
                        int carry = this.init_ItemStackArr[target].isEmpty() ? 0 : this.init_ItemStackArr[target].getCount();
                        int newCount = Math.min(source.getCount() / added.size() + carry, source.getMaxStackSize());
                        remaining -= newCount - carry;
                        this.init_ItemStackArr[target] = source.copyWithCount(newCount);
                     }
                  }

                  source.setCount(remaining);
                  this.init_ItemStack = source.isEmpty() ? ItemStack.EMPTY : source;
               }
            }
         }
      }

      private static boolean init_Z_ItemStack_ItemStack(ItemStack inSlot, ItemStack carried) {
         return inSlot.isEmpty() ? true : ItemStack.isSameItemSameComponents(carried, inSlot) && inSlot.getCount() <= carried.getMaxStackSize();
      }

      private void write(int slot) {
         int guard = 0;

         while (!this.init_ItemStackArr[slot].isEmpty() && guard++ < 64) {
            ItemStack stack = this.init_ItemStackArr[slot];
            int before = stack.getCount();
            if (!this.init_I_CachePool.init(slot)) {
               this.init_Z = true;
               return;
            }

            this.init(stack, this.init_I_CachePool.write(), this.init_I_CachePool.update(), false);
            if (stack.isEmpty()) {
               this.init_ItemStackArr[slot] = ItemStack.EMPTY;
            }

            if (stack.getCount() == before) {
               return;
            }
         }
      }

      private void read_V() {
         int guard = 0;

         while (guard++ < 4096) {
            ItemStack result = this.init_ItemStackArr[this.init_I_CachePool.resultSlot()];
            if (result.isEmpty()) {
               return;
            }

            ItemStack clicked = result.copy();
            ItemStack moving = result.copy();
            if (!this.init(moving, this.init_I_CachePool.write(), this.init_I_CachePool.update(), true)) {
               return;
            }

            if (!moving.isEmpty()) {
               this.init_Z = true;
            }

            this.init_ItemStackArr[this.init_I_CachePool.resultSlot()] = ItemStack.EMPTY;
            this.write();
            this.load_I++;
            this.load_V();
            ItemStack now = this.init_ItemStackArr[this.init_I_CachePool.resultSlot()];
            if (now.isEmpty() || !ItemStack.isSameItem(now, clicked)) {
               return;
            }
         }
      }

      private void write() {
         for (int slot : this.init_I_CachePool.init_IArr()) {
            ItemStack stack = this.init_ItemStackArr[slot];
            if (!stack.isEmpty()) {
               ItemStack remainder = CraftingClickPlanner.init_ItemStack(stack);
               stack.shrink(1);
               if (stack.isEmpty()) {
                  this.init_ItemStackArr[slot] = ItemStack.EMPTY;
               }

               if (!remainder.isEmpty()) {
                  ItemStack now = this.init_ItemStackArr[slot];
                  if (now.isEmpty()) {
                     this.init_ItemStackArr[slot] = remainder;
                  } else if (ItemStack.isSameItemSameComponents(now, remainder)) {
                     now.grow(remainder.getCount());
                  } else {
                     this.init_Z = true;
                  }
               }
            }
         }
      }

      private boolean init(ItemStack stack, int start, int end, boolean backwards) {
         boolean changed = false;
         int dest = backwards ? end - 1 : start;
         if (stack.isStackable()) {
            for (; !stack.isEmpty() && (backwards ? dest >= start : dest < end); dest += backwards ? -1 : 1) {
               ItemStack target = this.init_ItemStackArr[dest];
               if (!target.isEmpty() && ItemStack.isSameItemSameComponents(stack, target)) {
                  int total = target.getCount() + stack.getCount();
                  int max = target.getMaxStackSize();
                  if (total <= max) {
                     stack.setCount(0);
                     target.setCount(total);
                     changed = true;
                  } else if (target.getCount() < max) {
                     stack.shrink(max - target.getCount());
                     target.setCount(max);
                     changed = true;
                  }
               }
            }
         }

         if (!stack.isEmpty()) {
            for (int var10 = backwards ? end - 1 : start; backwards ? var10 >= start : var10 < end; var10 += backwards ? -1 : 1) {
               if (this.init_ItemStackArr[var10].isEmpty()) {
                  this.init_ItemStackArr[var10] = stack.split(Math.min(stack.getCount(), stack.getMaxStackSize()));
                  changed = true;
                  break;
               }
            }
         }

         return changed;
      }
   }
}
