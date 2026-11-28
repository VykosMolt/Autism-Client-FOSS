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
   static final int MAX_BATCH_CLICKS = 96;
   private static final Map<Item, ItemStack> remainderCache = new ConcurrentHashMap<>();

   private CraftingClickPlanner() {
   }

   static CraftingClickPlanner.PhaseResult planNextPhase(
      CraftingClickPlanner.GridLayout layout, ItemStack[] slots, ItemStack carried, CraftingClickPlanner.RecipeSpec spec, int wantCrafts, int selectedHotbar
   ) {
      if (spec != null && spec.result() != null && !spec.result().isEmpty()) {
         if (!spec.fits(layout)) {
            return new CraftingClickPlanner.PhaseResult(null, "This recipe needs a 3x3 crafting table.");
         }

         int upper = Math.max(0, Math.min(wantCrafts, 64));
         if (upper <= 0) {
            return new CraftingClickPlanner.PhaseResult(null, "Nothing to craft.");
         }

         CraftingClickPlanner.SimulationResult first = simulateCrafts(layout, slots, carried, spec, upper, selectedHotbar);
         if (first.plan != null) {
            return new CraftingClickPlanner.PhaseResult(first.plan, null);
         }

         if (first.fatal) {
            return new CraftingClickPlanner.PhaseResult(null, first.error);
         }

         CraftingClickPlanner.ClickPhase best = null;
         String reason = first.error;
         int lo = 1;
         int hi = upper - 1;

         while (lo <= hi) {
            int mid = lo + hi >>> 1;
            CraftingClickPlanner.SimulationResult attempt = simulateCrafts(layout, slots, carried, spec, mid, selectedHotbar);
            if (attempt.plan != null) {
               best = attempt.plan;
               lo = mid + 1;
            } else {
               if (attempt.fatal) {
                  return new CraftingClickPlanner.PhaseResult(null, attempt.error);
               }

               if (mid == 1 || best == null) {
                  reason = attempt.error;
               }

               hi = mid - 1;
            }
         }

         return best != null ? new CraftingClickPlanner.PhaseResult(best, null) : new CraftingClickPlanner.PhaseResult(null, reason);
      } else {
         return new CraftingClickPlanner.PhaseResult(null, "Unknown recipe.");
      }
   }

   static List<CraftingClickPlanner.ClickPhase> planPhases(
      CraftingClickPlanner.GridLayout layout, ItemStack[] slots, ItemStack carried, CraftingClickPlanner.RecipeSpec spec, int wantCrafts, int selectedHotbar
   ) {
      List<CraftingClickPlanner.ClickPhase> phases = new ArrayList<>();
      ItemStack[] state = copySlots(slots);
      ItemStack hand = carried == null ? ItemStack.EMPTY : carried.copy();
      int left = wantCrafts;

      while (left > 0 && phases.size() < 64) {
         CraftingClickPlanner.PhaseResult outcome = planNextPhase(layout, state, hand, spec, left, selectedHotbar);
         if (outcome.phase() == null) {
            break;
         }

         CraftingClickPlanner.ClickPhase phase = outcome.phase();
         phases.add(phase);
         left -= phase.load_I();
         state = copySlots(phase.finalSlots());
         hand = phase.finalCarried().copy();
      }

      return phases;
   }

   static CraftingClickPlanner.ClickPhase plan(CraftingClickPlanner.GridLayout layout, ItemStack[] slots, ItemStack carried) {
      CraftingClickPlanner.ClickSimulator sim = new CraftingClickPlanner.ClickSimulator(layout, null, slots, carried);
      sim.stowCarried();
      sim.clearGrid();
      return sim.clicks.isEmpty() ? null : sim.toPhase(0);
   }

   private static CraftingClickPlanner.SimulationResult simulateCrafts(
      CraftingClickPlanner.GridLayout layout, ItemStack[] slots, ItemStack carried, CraftingClickPlanner.RecipeSpec spec, int crafts, int selectedHotbar
   ) {
      CraftingClickPlanner.ClickSimulator sim = new CraftingClickPlanner.ClickSimulator(layout, spec, slots, carried);
      if (!sim.stowCarried()) {
         return CraftingClickPlanner.SimulationResult.fatalFailure("The cursor holds an item and the inventory has no room for it.");
      }

      if (!sim.clearGrid()) {
         return CraftingClickPlanner.SimulationResult.fatalFailure("No room to clear the crafting grid.");
      }

      Map<Integer, ItemStack> assignment = assignIngredients(sim, crafts);
      if (assignment == null) {
         return CraftingClickPlanner.SimulationResult.failure("Missing materials for " + spec.result().getHoverName().getString() + ".");
      }

      if (crafts > 1) {
         for (ItemStack key : assignment.values()) {
            if (!craftingRemainder(key).isEmpty()) {
               return CraftingClickPlanner.SimulationResult.failure("one craft at a time");
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

         groups.computeIfAbsent(key == null ? entry.getValue() : key, ignored -> new ArrayList<>()).add(layout.inputSlots()[entry.getKey()]);
      }

      for (Entry<ItemStack, List<Integer>> group : groups.entrySet()) {
         if (!fillCells(sim, group.getKey(), group.getValue(), crafts, selectedHotbar)) {
            return CraftingClickPlanner.SimulationResult.failure("Missing materials for " + spec.result().getHoverName().getString() + ".");
         }
      }

      if (!sim.gridMatchesRecipe()) {
         return CraftingClickPlanner.SimulationResult.fatalFailure("The planned grid does not match the recipe.");
      }

      sim.takeAllResults();
      if (!sim.simulationInvalid && sim.craftsTaken == crafts) {
         sim.clearGrid();
         if (sim.carried.isEmpty() && !sim.simulationInvalid) {
            return crafts > 1 && sim.clicks.size() > 96
               ? CraftingClickPlanner.SimulationResult.failure("too many clicks")
               : new CraftingClickPlanner.SimulationResult(sim.toPhase(crafts), null, false);
         } else {
            return CraftingClickPlanner.SimulationResult.failure("No room in the inventory for the result.");
         }
      } else {
         return CraftingClickPlanner.SimulationResult.failure("No room in the inventory for the result.");
      }
   }

   private static Map<Integer, ItemStack> assignIngredients(CraftingClickPlanner.ClickSimulator sim, int crafts) {
      Ingredient[] cells = sim.cells;
      List<ItemStack> keys = new ArrayList<>();
      List<Integer> avail = new ArrayList<>();

      for (int slot = sim.layout.write(); slot < sim.layout.update(); slot++) {
         ItemStack stack = sim.slots[slot];
         if (isUsableIngredient(stack)) {
            int index = indexOfKey(keys, stack);
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

   private static boolean fillCells(CraftingClickPlanner.ClickSimulator sim, ItemStack key, List<Integer> cells, int crafts, int selectedHotbar) {
      Map<Integer, Integer> need = new LinkedHashMap<>();

      for (int cell : cells) {
         need.put(cell, crafts);
      }

      int total = crafts * cells.size();

      for (int source : sourceSlots(sim, key, total, selectedHotbar)) {
         int remaining = need.values().stream().mapToInt(Integer::intValue).sum();
         if (remaining <= 0) {
            break;
         }

         int count = sim.slots[source].getCount();
         if (count > 0) {
            int half = (count + 1) / 2;
            if (count > remaining && half >= remaining && half - remaining < count - remaining) {
               sim.rightClick(source);
            } else {
               sim.click(source);
            }

            distributeCarried(sim, source, need);
            if (!sim.carried.isEmpty()) {
               sim.click(source);
            }

            if (!sim.carried.isEmpty()) {
               return false;
            }
         }
      }

      return need.values().stream().allMatch(value -> value == 0);
   }

   private static void distributeCarried(CraftingClickPlanner.ClickSimulator sim, int source, Map<Integer, Integer> need) {
      int guard = 0;

      while (!sim.carried.isEmpty() && guard++ < 512) {
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

         int amount = sim.carried.getCount();
         if (low == high) {
            int cellsOpen = open.size();
            int each = low;
            if (amount == cellsOpen * each) {
               spreadCarried(sim, open, need, each);
               return;
            }

            if (amount > cellsOpen * each) {
               int excess = amount - cellsOpen * each;
               int singles = cellsOpen * each + 1;
               int deposit = excess + (cellsOpen == 1 ? 1 : cellsOpen + 2);
               if (deposit < singles) {
                  for (int i = 0; i < excess; i++) {
                     sim.rightClick(source);
                  }

                  spreadCarried(sim, open, need, each);
               } else {
                  for (int cell : open) {
                     for (int i = 0; i < each; i++) {
                        sim.rightClick(cell);
                     }

                     need.put(cell, 0);
                  }
               }

               return;
            }

            if (cellsOpen == 1) {
               int cell = open.get(0);
               sim.click(cell);
               need.merge(cell, -amount, Integer::sum);
               return;
            }

            if (amount >= cellsOpen) {
               int share = amount / cellsOpen;
               sim.dragSplit(open);

               for (int cell : open) {
                  need.merge(cell, -share, Integer::sum);
               }
            } else {
               for (int cell : open) {
                  if (sim.carried.isEmpty()) {
                     break;
                  }

                  sim.rightClick(cell);
                  need.merge(cell, -1, Integer::sum);
               }
            }
         } else {
            for (int cell : open) {
               while (need.get(cell) > low && !sim.carried.isEmpty()) {
                  sim.rightClick(cell);
                  need.merge(cell, -1, Integer::sum);
               }
            }
         }
      }
   }

   private static void spreadCarried(CraftingClickPlanner.ClickSimulator sim, List<Integer> open, Map<Integer, Integer> need, int each) {
      if (open.size() == 1) {
         sim.click(open.get(0));
      } else {
         sim.dragSplit(open);
      }

      for (int cell : open) {
         need.merge(cell, -each, Integer::sum);
      }
   }

   private static List<Integer> sourceSlots(CraftingClickPlanner.ClickSimulator sim, ItemStack key, int total, int selectedHotbar) {
      List<Integer> sources = new ArrayList<>();

      for (int slot = sim.layout.write(); slot < sim.layout.update(); slot++) {
         ItemStack stack = sim.slots[slot];
         if (isUsableIngredient(stack) && ItemStack.isSameItemSameComponents(stack, key)) {
            sources.add(slot);
         }
      }

      int selectedSlot = selectedHotbar < 0 ? -1 : sim.layout.sync() + selectedHotbar;
      Comparator<Integer> tier = Comparator.comparingInt(slotx -> slotx == selectedSlot ? 2 : (slotx >= sim.layout.sync() ? 1 : 0));
      Integer cover = null;

      for (int slot : sources) {
         int count = sim.slots[slot].getCount();
         if (count >= total
            && (cover == null || tier.compare(slot, cover) < 0 || tier.compare(slot, cover) == 0 && count < sim.slots[cover].getCount())) {
            cover = slot;
         }
      }

      if (cover != null) {
         sources.remove(cover);
         sources.add(0, cover);
         return sources;
      } else {
         sources.sort(tier.thenComparing(slotx -> -sim.slots[slotx].getCount()));
         return sources;
      }
   }

   private static int indexOfKey(List<ItemStack> keys, ItemStack stack) {
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

   private static boolean isBundle(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.has(DataComponents.BUNDLE_CONTENTS);
   }

   static ItemStack craftingRemainder(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         return !stack.getComponentsPatch().isEmpty() ? computeRemainder(stack) : remainderCache.computeIfAbsent(stack.getItem(), item -> computeRemainder(stack)).copy();
      } else {
         return ItemStack.EMPTY;
      }
   }

   private static ItemStack computeRemainder(ItemStack stack) {
      try {
         List<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(CraftingInput.of(1, 1, List.of(stack.copyWithCount(1))));
         return remaining.isEmpty() ? ItemStack.EMPTY : remaining.get(0).copy();
      } catch (RuntimeException e) {
         return ItemStack.EMPTY;
      }
   }

   static ItemStack[] copySlots(ItemStack[] slots) {
      ItemStack[] out = new ItemStack[slots.length];

      for (int i = 0; i < slots.length; i++) {
         out[i] = slots[i] != null && !slots[i].isEmpty() ? slots[i].copy() : ItemStack.EMPTY;
      }

      return out;
   }

   static String describePhase(CraftingClickPlanner.ClickPhase phase) {
      if (phase == null) {
         return "none";
      }

      StringBuilder out = new StringBuilder();

      for (CraftingClickPlanner.SlotClick click : phase.clicks()) {
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

   record PhaseResult(CraftingClickPlanner.ClickPhase phase, String error) {
   }

   record GridLayout(int resultSlot, int load, int read, int[] inputSlots, int write, int update, int sync) {
      static CraftingClickPlanner.GridLayout craftingTable() {
         return new CraftingClickPlanner.GridLayout(0, 3, 3, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 10, 46, 37);
      }

      static CraftingClickPlanner.GridLayout inventoryGrid() {
         return new CraftingClickPlanner.GridLayout(0, 2, 2, new int[]{1, 2, 3, 4}, 9, 45, 36);
      }

      boolean isInputSlot(int slot) {
         return slot >= this.inputSlots[0] && slot <= this.inputSlots[this.inputSlots.length - 1];
      }

      public int gridWidth() {
         return this.load;
      }
   }

   record ClickPhase(
      List<CraftingClickPlanner.SlotClick> clicks, int init_I, List<ItemStack[]> predictedSlots, List<ItemStack> read, List<ItemStack> write, int load_I
   ) {
      ItemStack[] finalSlots() {
         return this.predictedSlots.get(this.predictedSlots.size() - 1);
      }

      ItemStack finalCarried() {
         return this.read.get(this.read.size() - 1);
      }

      int clicksAfterFirstTake() {
         return this.load_I < 0 ? 0 : this.clicks.size() - 1 - this.load_I;
      }

      public int load_I() {
         return this.init_I;
      }

      public List<ItemStack> predictedCarried() {
         return this.read;
      }

      public int firstTakeClickIndex() {
         return this.load_I;
      }
   }

   private record SimulationResult(CraftingClickPlanner.ClickPhase plan, String error, boolean fatal) {
      static CraftingClickPlanner.SimulationResult failure(String reason) {
         return new CraftingClickPlanner.SimulationResult(null, reason, false);
      }

      static CraftingClickPlanner.SimulationResult fatalFailure(String reason) {
         return new CraftingClickPlanner.SimulationResult(null, reason, true);
      }
   }

   record RecipeSpec(String id, ItemStack result, int width, int load, boolean shaped, List<Ingredient> ingredients) {
      static CraftingClickPlanner.RecipeSpec fromRecipe(RecipeCatalogLoader.CraftingEntry recipe) {
         return new CraftingClickPlanner.RecipeSpec(
            recipe.recipeKey, recipe.result, recipe.width, recipe.height, recipe.shaped, recipe.shaped ? recipe.gridIngredients : recipe.requirements
         );
      }

      boolean fits(CraftingClickPlanner.GridLayout layout) {
         return !this.shaped ? this.ingredients.size() <= layout.inputSlots().length : this.width <= layout.gridWidth() && this.load <= layout.read();
      }

      Ingredient[] placeIngredients(CraftingClickPlanner.GridLayout layout) {
         Ingredient[] cells = new Ingredient[layout.inputSlots().length];
         if (this.shaped) {
            for (int row = 0; row < this.load; row++) {
               for (int col = 0; col < this.width; col++) {
                  int index = row * this.width + col;
                  if (index < this.ingredients.size()) {
                     cells[row * layout.gridWidth() + col] = this.ingredients.get(index);
                  }
               }
            }
         } else {
            for (int i = 0; i < this.ingredients.size(); i++) {
               cells[i] = this.ingredients.get(i);
            }
         }

         return cells;
      }
   }

   record SlotClick(int slot, int load, ContainerInput input) {
      static CraftingClickPlanner.SlotClick leftClick(int slot) {
         return new CraftingClickPlanner.SlotClick(slot, 0, ContainerInput.PICKUP);
      }

      static CraftingClickPlanner.SlotClick rightClick(int slot) {
         return new CraftingClickPlanner.SlotClick(slot, 1, ContainerInput.PICKUP);
      }

      static CraftingClickPlanner.SlotClick shiftClick(int slot) {
         return new CraftingClickPlanner.SlotClick(slot, 0, ContainerInput.QUICK_MOVE);
      }

      static CraftingClickPlanner.SlotClick quickCraftStart() {
         return new CraftingClickPlanner.SlotClick(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ContainerInput.QUICK_CRAFT);
      }

      static CraftingClickPlanner.SlotClick dragAdd(int slot) {
         return new CraftingClickPlanner.SlotClick(slot, AbstractContainerMenu.getQuickcraftMask(1, 0), ContainerInput.QUICK_CRAFT);
      }

      static CraftingClickPlanner.SlotClick quickCraftEnd() {
         return new CraftingClickPlanner.SlotClick(-999, AbstractContainerMenu.getQuickcraftMask(2, 0), ContainerInput.QUICK_CRAFT);
      }

      public int button() {
         return this.load;
      }
   }

   static final class ClickSimulator {
      final CraftingClickPlanner.GridLayout layout;
      final CraftingClickPlanner.RecipeSpec spec;
      final Ingredient[] cells;
      final ItemStack[] slots;
      ItemStack carried;
      final List<CraftingClickPlanner.SlotClick> clicks = new ArrayList<>();
      final List<ItemStack[]> gridSnapshots = new ArrayList<>();
      final List<ItemStack> carriedSnapshots = new ArrayList<>();
      final List<ItemStack> resultSnapshots = new ArrayList<>();
      int firstTakeIndex = -1;
      boolean simulationInvalid;
      int craftsTaken;

      ClickSimulator(CraftingClickPlanner.GridLayout layout, CraftingClickPlanner.RecipeSpec spec, ItemStack[] slots, ItemStack carried) {
         this.layout = layout;
         this.spec = spec;
         this.cells = spec == null ? new Ingredient[layout.inputSlots().length] : spec.placeIngredients(layout);
         this.slots = CraftingClickPlanner.copySlots(slots);
         this.carried = carried != null && !carried.isEmpty() ? carried.copy() : ItemStack.EMPTY;
      }

      CraftingClickPlanner.ClickPhase toPhase(int crafts) {
         return new CraftingClickPlanner.ClickPhase(
            List.copyOf(this.clicks), crafts, List.copyOf(this.gridSnapshots), List.copyOf(this.carriedSnapshots), List.copyOf(this.resultSnapshots), this.firstTakeIndex
         );
      }

      private ItemStack[] gridSnapshot() {
         ItemStack[] grid = new ItemStack[this.layout.inputSlots().length];

         for (int i = 0; i < grid.length; i++) {
            grid[i] = this.slots[this.layout.inputSlots()[i]].copy();
         }

         return grid;
      }

      private void recordClick(CraftingClickPlanner.SlotClick click, Runnable action) {
         ItemStack[] gridBefore = this.gridSnapshot();
         this.resultSnapshots.add(this.slots[this.layout.resultSlot()].copy());
         action.run();
         if (this.carried.isEmpty()) {
            this.carried = ItemStack.EMPTY;
         }

         this.updateResult();
         this.clicks.add(click);
         if (this.firstTakeIndex < 0) {
            ItemStack[] gridAfter = this.gridSnapshot();

            for (int i = 0; i < gridAfter.length; i++) {
               if (!ItemStack.matches(gridBefore[i], gridAfter[i])) {
                  this.firstTakeIndex = this.clicks.size() - 1;
                  break;
               }
            }
         }

         this.gridSnapshots.add(CraftingClickPlanner.copySlots(this.slots));
         this.carriedSnapshots.add(this.carried.copy());
      }

      void click(int slot) {
         this.recordClick(CraftingClickPlanner.SlotClick.leftClick(slot), () -> this.simulateClick(slot, true));
      }

      void rightClick(int slot) {
         this.recordClick(CraftingClickPlanner.SlotClick.rightClick(slot), () -> this.simulateClick(slot, false));
      }

      void shiftClick(int slot) {
         this.recordClick(CraftingClickPlanner.SlotClick.shiftClick(slot), () -> this.simulateShiftClick(slot));
      }

      void takeAllResults() {
         this.recordClick(CraftingClickPlanner.SlotClick.shiftClick(this.layout.resultSlot()), this::simulateResultTakes);
      }

      void dragSplit(List<Integer> targets) {
         this.recordClick(CraftingClickPlanner.SlotClick.quickCraftStart(), () -> {});

         for (int target : targets) {
            this.recordClick(CraftingClickPlanner.SlotClick.dragAdd(target), () -> {});
         }

         this.recordClick(CraftingClickPlanner.SlotClick.quickCraftEnd(), () -> this.simulateDragEnd(targets));
      }

      boolean stowCarried() {
         int guard = 0;

         while (!this.carried.isEmpty() && guard++ < 64) {
            int target = -1;

            for (int slot = this.layout.write(); slot < this.layout.update() && target < 0; slot++) {
               ItemStack stack = this.slots[slot];
               if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, this.carried) && stack.getCount() < stack.getMaxStackSize()) {
                  target = slot;
               }
            }

            for (int slot = this.layout.write(); slot < this.layout.update() && target < 0; slot++) {
               if (this.slots[slot].isEmpty()) {
                  target = slot;
               }
            }

            if (target < 0) {
               return false;
            }

            this.click(target);
         }

         return this.carried.isEmpty();
      }

      boolean clearGrid() {
         boolean clear = true;

         for (int slot : this.layout.inputSlots()) {
            if (!this.slots[slot].isEmpty()) {
               this.shiftClick(slot);
               if (!this.slots[slot].isEmpty()) {
                  clear = false;
               }
            }
         }

         return clear;
      }

      boolean gridMatchesRecipe() {
         if (this.spec == null) {
            return false;
         }

         for (int pos = 0; pos < this.cells.length; pos++) {
            ItemStack stack = this.slots[this.layout.inputSlots()[pos]];
            if (this.cells[pos] == null) {
               if (!stack.isEmpty()) {
                  return false;
               }
            } else if (stack.isEmpty() || !this.cells[pos].test(stack)) {
               return false;
            }
         }

         return true;
      }

      private void updateResult() {
         this.slots[this.layout.resultSlot()] = this.gridMatchesRecipe() ? this.spec.result().copy() : ItemStack.EMPTY;
      }

      private void simulateClick(int slot, boolean primary) {
         ItemStack clicked = this.slots[slot];
         if ((CraftingClickPlanner.isBundle(clicked) || CraftingClickPlanner.isBundle(this.carried)) && (!primary || !clicked.isEmpty() && !this.carried.isEmpty())) {
            this.simulationInvalid = true;
         }

         if (clicked.isEmpty()) {
            if (!this.carried.isEmpty()) {
               int amount = primary ? this.carried.getCount() : 1;
               int moved = Math.min(Math.min(amount, this.carried.getCount()), this.carried.getMaxStackSize());
               if (moved > 0) {
                  this.slots[slot] = this.carried.split(moved);
               }
            }
         } else if (this.carried.isEmpty()) {
            int amount = primary ? clicked.getCount() : (clicked.getCount() + 1) / 2;
            this.carried = clicked.split(amount);
            if (clicked.isEmpty()) {
               this.slots[slot] = ItemStack.EMPTY;
            }
         } else if (ItemStack.isSameItemSameComponents(clicked, this.carried)) {
            int amount = primary ? this.carried.getCount() : 1;
            int moved = Math.min(Math.min(amount, this.carried.getCount()), this.carried.getMaxStackSize() - clicked.getCount());
            if (moved > 0) {
               this.carried.shrink(moved);
               clicked.grow(moved);
            }
         } else if (this.carried.getCount() <= this.carried.getMaxStackSize()) {
            this.slots[slot] = this.carried;
            this.carried = clicked;
         }
      }

      private void simulateDragEnd(List<Integer> targets) {
         if (!this.carried.isEmpty()) {
            List<Integer> added = new ArrayList<>();

            for (int target : targets) {
               if (canStack(this.slots[target], this.carried) && this.carried.getCount() > added.size()) {
                  added.add(target);
               }
            }

            if (!added.isEmpty()) {
               if (added.size() == 1) {
                  this.simulateClick(added.get(0), true);
               } else {
                  ItemStack source = this.carried.copy();
                  int remaining = this.carried.getCount();

                  for (int target : added) {
                     if (canStack(this.slots[target], this.carried) && this.carried.getCount() >= added.size()) {
                        int carry = this.slots[target].isEmpty() ? 0 : this.slots[target].getCount();
                        int newCount = Math.min(source.getCount() / added.size() + carry, source.getMaxStackSize());
                        remaining -= newCount - carry;
                        this.slots[target] = source.copyWithCount(newCount);
                     }
                  }

                  source.setCount(remaining);
                  this.carried = source.isEmpty() ? ItemStack.EMPTY : source;
               }
            }
         }
      }

      private static boolean canStack(ItemStack inSlot, ItemStack carried) {
         return inSlot.isEmpty() ? true : ItemStack.isSameItemSameComponents(carried, inSlot) && inSlot.getCount() <= carried.getMaxStackSize();
      }

      private void simulateShiftClick(int slot) {
         int guard = 0;

         while (!this.slots[slot].isEmpty() && guard++ < 64) {
            ItemStack stack = this.slots[slot];
            int before = stack.getCount();
            if (!this.layout.isInputSlot(slot)) {
               this.simulationInvalid = true;
               return;
            }

            this.moveItemStackTo(stack, this.layout.write(), this.layout.update(), false);
            if (stack.isEmpty()) {
               this.slots[slot] = ItemStack.EMPTY;
            }

            if (stack.getCount() == before) {
               return;
            }
         }
      }

      private void simulateResultTakes() {
         int guard = 0;

         while (guard++ < 4096) {
            ItemStack result = this.slots[this.layout.resultSlot()];
            if (result.isEmpty()) {
               return;
            }

            ItemStack clicked = result.copy();
            ItemStack moving = result.copy();
            if (!this.moveItemStackTo(moving, this.layout.write(), this.layout.update(), true)) {
               return;
            }

            if (!moving.isEmpty()) {
               this.simulationInvalid = true;
            }

            this.slots[this.layout.resultSlot()] = ItemStack.EMPTY;
            this.consumeIngredients();
            this.craftsTaken++;
            this.updateResult();
            ItemStack now = this.slots[this.layout.resultSlot()];
            if (now.isEmpty() || !ItemStack.isSameItem(now, clicked)) {
               return;
            }
         }
      }

      private void consumeIngredients() {
         for (int slot : this.layout.inputSlots()) {
            ItemStack stack = this.slots[slot];
            if (!stack.isEmpty()) {
               ItemStack remainder = CraftingClickPlanner.craftingRemainder(stack);
               stack.shrink(1);
               if (stack.isEmpty()) {
                  this.slots[slot] = ItemStack.EMPTY;
               }

               if (!remainder.isEmpty()) {
                  ItemStack now = this.slots[slot];
                  if (now.isEmpty()) {
                     this.slots[slot] = remainder;
                  } else if (ItemStack.isSameItemSameComponents(now, remainder)) {
                     now.grow(remainder.getCount());
                  } else {
                     this.simulationInvalid = true;
                  }
               }
            }
         }
      }

      private boolean moveItemStackTo(ItemStack stack, int start, int end, boolean backwards) {
         boolean changed = false;
         int dest = backwards ? end - 1 : start;
         if (stack.isStackable()) {
            for (; !stack.isEmpty() && (backwards ? dest >= start : dest < end); dest += backwards ? -1 : 1) {
               ItemStack target = this.slots[dest];
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
               if (this.slots[var10].isEmpty()) {
                  this.slots[var10] = stack.split(Math.min(stack.getCount(), stack.getMaxStackSize()));
                  changed = true;
                  break;
               }
            }
         }

         return changed;
      }
   }
}
