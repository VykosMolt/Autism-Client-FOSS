package autismclient.util.g;

import autismclient.util.macro.aQ;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

final class UtilCore {
   private static final int[] init_IArr = new int[]{1, 2, 3, 4, 0, 5, 6, 7, 8, 45};
   private static final Set<Integer> init_Set = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 45);
   private static final int init_I = 1024;

   private UtilCore() {
   }

   static List<UtilCore.Inner_DataUtil> init(aQ action, List<ItemStack> inventorySlots, ItemStack initialCursor) {
      if (action != null && inventorySlots != null && inventorySlots.size() >= 46) {
         List<ItemStack> slots = init(inventorySlots);
         ItemStack[] cursor = new ItemStack[]{init(initialCursor)};
         List<UtilCore.Inner_DataUtil> clicks = new ArrayList<>();
         if (!cursor[0].isEmpty()) {
            init(action, slots, cursor, clicks);
            return List.copyOf(clicks);
         }

         List<autismclient.util.macro.ArrayUtil> targets = init(action);
         switch (action.mode == null ? aQ.Inner_StreamUtil.init : action.mode) {
            case init:
               init(action, targets, slots, cursor, clicks);
               break;
            case load:
               init(targets, slots, clicks);
               break;
            case read:
               load(targets, slots, clicks);
         }

         return List.copyOf(clicks);
      } else {
         return List.of();
      }
   }

   private static void init(aQ action, List<ItemStack> slots, ItemStack[] cursor, List<UtilCore.Inner_DataUtil> clicks) {
      int safety = Math.max(0, cursor[0].getCount() - 1);

      while (!cursor[0].isEmpty() && cursor[0].getCount() > 1 && safety-- > 0 && clicks.size() < 1024) {
         int destination = init(slots, cursor[0]);
         if (destination < 0) {
            break;
         }

         init(action, slots, cursor, destination, 1, clicks, cursor[0].getCount() > 2 ? UtilCore.Inner_StreamUtil.load : UtilCore.Inner_StreamUtil.write);
      }
   }

   private static int init(List<ItemStack> slots, ItemStack cursor) {
      for (int slot = 9; slot <= 44; slot++) {
         ItemStack stack = slots.get(slot);
         if (stack.isEmpty()) {
            return slot;
         }

         if (ItemStack.isSameItemSameComponents(stack, cursor) && stack.getCount() < stack.getMaxStackSize()) {
            return slot;
         }
      }

      return -1;
   }

   static List<Integer> init(aQ action, List<ItemStack> menuSlots, int containerSlotCount) {
      if (action != null && action.mode == aQ.Inner_StreamUtil.init && menuSlots != null && containerSlotCount > 0) {
         List<autismclient.util.macro.ArrayUtil> targets = init(action);
         List<Integer> result = new ArrayList<>();
         Set<Integer> used = new HashSet<>();

         for (autismclient.util.macro.ArrayUtil target : targets) {
            if (target != null && !target.init_Z() && target.update()) {
               for (int slot = 0; slot < containerSlotCount && slot < menuSlots.size(); slot++) {
                  if (!used.contains(slot) && init(target, menuSlots.get(slot))) {
                     result.add(slot);
                     used.add(slot);
                     break;
                  }
               }
            }
         }

         return List.copyOf(result);
      } else {
         return List.of();
      }
   }

   private static void init(
      aQ action, List<autismclient.util.macro.ArrayUtil> targets, List<ItemStack> slots, ItemStack[] cursor, List<UtilCore.Inner_DataUtil> clicks
   ) {
      if (!targets.isEmpty()) {
         int limit = Math.min(11, targets.size());
         Set<Integer> claimed = new HashSet<>();
         int autoIndex = 0;
         boolean explicitDestination = false;

         for (int i = 0; i < limit && cursor[0].isEmpty(); i++) {
            if (action.load_I_I(i) == -1) {
               int source = init_I(slots, targets.get(i));
               if (source >= 0) {
                  init(action, slots, cursor, source, clicks);
               }
            }
         }

         List<UtilCore.Inner_CachePool> rows = new ArrayList<>();

         for (int i = 0; i < limit; i++) {
            autismclient.util.macro.ArrayUtil target = targets.get(i);
            int configured = action.load_I_I(i);
            if (configured != -1) {
               int destination;
               if (configured == -2147483648) {
                  destination = -1;

                  while (autoIndex < init_IArr.length) {
                     int candidate = init_IArr[autoIndex++];
                     if (!claimed.contains(candidate)) {
                        ItemStack current = slots.get(candidate);
                        if (current.isEmpty() || init(target, current)) {
                           destination = candidate;
                           break;
                        }
                     }
                  }

                  if (destination < 0) {
                     continue;
                  }
               } else {
                  explicitDestination = true;
                  destination = configured;
                  if (!init_Set.contains(destination) || claimed.contains(destination)) {
                     continue;
                  }
               }

               ItemStack destinationStack = slots.get(destination);
               if (destinationStack.isEmpty() || init(target, destinationStack)) {
                  ItemStack sample = destinationStack.isEmpty() ? init_ItemStack(slots, target) : destinationStack;
                  if (!sample.isEmpty()) {
                     int capacity = Math.max(1, sample.getMaxStackSize());
                     if (destinationStack.getCount() < capacity) {
                        claimed.add(destination);
                        rows.add(
                           new UtilCore.Inner_CachePool(
                              target, destination, action.init_aQ_DataUtil_I(i), action.read_I_I(i), destinationStack.getCount(), capacity
                           )
                        );
                     }
                  }
               }
            }
         }

         init(rows, slots);

         for (UtilCore.Inner_CachePool row : rows) {
            int needed = row.update;

            while (needed > 0 && clicks.size() < 1024) {
               int source = init_I(slots, row.init_ArrayUtil);
               if (source < 0) {
                  break;
               }

               int moved = Math.min(needed, slots.get(source).getCount());
               init(action, slots, cursor, source, row.init_I, moved, clicks);
               needed -= moved;
            }
         }

         if (cursor[0].isEmpty() && !explicitDestination && limit > init_IArr.length) {
            int source = init_I(slots, targets.get(init_IArr.length));
            if (source >= 0) {
               init(action, slots, cursor, source, clicks);
            }
         }
      }
   }

   private static void init(List<UtilCore.Inner_CachePool> rows, List<ItemStack> slots) {
      Map<String, List<UtilCore.Inner_CachePool>> groups = new LinkedHashMap<>();

      for (UtilCore.Inner_CachePool row : rows) {
         groups.computeIfAbsent(init(row.init_ArrayUtil), ignored -> new ArrayList<>()).add(row);
      }

      for (List<UtilCore.Inner_CachePool> group : groups.values()) {
         if (!group.isEmpty()) {
            int available = 0;

            for (int slot = 9; slot <= 44; slot++) {
               ItemStack stack = slots.get(slot);
               if (init(group.getFirst().init_ArrayUtil, stack)) {
                  available += stack.getCount();
               }
            }

            for (UtilCore.Inner_CachePool row : group) {
               available += row.read;
            }

            List<UtilCore.Inner_CachePool> full = new ArrayList<>();

            for (UtilCore.Inner_CachePool row : group) {
               if (row.init_aQ_DataUtil == aQ.Inner_DataUtil.load) {
                  int desired = Math.min(row.write, Math.min(row.load, available));
                  row.update = Math.max(0, desired - row.read);
                  available = Math.max(0, available - Math.max(row.read, desired));
               } else {
                  full.add(row);
               }
            }

            Map<UtilCore.Inner_CachePool, Integer> desired = new HashMap<>();

            for (UtilCore.Inner_CachePool row : full) {
               desired.put(row, 0);
            }

            while (available > 0 && !full.isEmpty()) {
               int active = 0;

               for (UtilCore.Inner_CachePool row : full) {
                  if (desired.get(row) < row.write) {
                     active++;
                  }
               }

               if (active == 0) {
                  break;
               }

               int share = Math.max(1, available / active);
               boolean progressed = false;

               for (UtilCore.Inner_CachePool row : full) {
                  int room = row.write - desired.get(row);
                  if (room > 0 && available > 0) {
                     int take = Math.min(room, Math.min(share, available));
                     desired.put(row, desired.get(row) + take);
                     available -= take;
                     progressed |= take > 0;
                  }
               }

               if (!progressed) {
                  break;
               }
            }

            for (UtilCore.Inner_CachePool row : full) {
               row.update = Math.max(0, desired.getOrDefault(row, 0) - row.read);
            }
         }
      }
   }

   private static void init(aQ action, List<ItemStack> slots, ItemStack[] cursor, int source, int destination, int amount, List<UtilCore.Inner_DataUtil> clicks) {
      if (amount > 0 && cursor[0].isEmpty()) {
         init(action, slots, cursor, source, 0, clicks, UtilCore.Inner_StreamUtil.init);

         while (!cursor[0].isEmpty() && cursor[0].getCount() > amount && clicks.size() < 1024) {
            init(action, slots, cursor, source, 1, clicks, UtilCore.Inner_StreamUtil.load);
         }

         if (!cursor[0].isEmpty()) {
            init(
               action, slots, cursor, destination, 0, clicks, cursor[0].getCount() > amount ? UtilCore.Inner_StreamUtil.read : UtilCore.Inner_StreamUtil.write
            );
         }

         if (!cursor[0].isEmpty()) {
            init(action, slots, cursor, source, 0, clicks, UtilCore.Inner_StreamUtil.write);
         }
      }
   }

   private static void init(aQ action, List<ItemStack> slots, ItemStack[] cursor, int source, List<UtilCore.Inner_DataUtil> clicks) {
      int sourceCount = slots.get(source).getCount();
      init(action, slots, cursor, source, 0, clicks, sourceCount > 1 ? UtilCore.Inner_StreamUtil.init : UtilCore.Inner_StreamUtil.write);

      while (!cursor[0].isEmpty() && cursor[0].getCount() > 1 && clicks.size() < 1024) {
         init(action, slots, cursor, source, 1, clicks, cursor[0].getCount() > 2 ? UtilCore.Inner_StreamUtil.load : UtilCore.Inner_StreamUtil.write);
      }
   }

   private static void init(
      aQ action, List<ItemStack> slots, ItemStack[] cursor, int slot, int button, List<UtilCore.Inner_DataUtil> clicks, UtilCore.Inner_StreamUtil pace
   ) {
      ItemStack[] result = init(cursor[0], slots.get(slot), button == 0);
      cursor[0] = result[0];
      slots.set(slot, result[1]);
      clicks.add(new UtilCore.Inner_DataUtil(slot, button, ContainerInput.PICKUP, init(action, pace)));
   }

   private static void init(List<autismclient.util.macro.ArrayUtil> targets, List<ItemStack> slots, List<UtilCore.Inner_DataUtil> clicks) {
      for (int slot : init_IArr) {
         ItemStack stack = slots.get(slot);
         if (!stack.isEmpty() && init(targets, stack, slot)) {
            clicks.add(new UtilCore.Inner_DataUtil(slot, 0, ContainerInput.QUICK_MOVE, 50L));
         }
      }
   }

   private static void load(List<autismclient.util.macro.ArrayUtil> targets, List<ItemStack> slots, List<UtilCore.Inner_DataUtil> clicks) {
      for (int slot : init_IArr) {
         ItemStack stack = slots.get(slot);
         if (!stack.isEmpty() && init(targets, stack, slot)) {
            clicks.add(new UtilCore.Inner_DataUtil(slot, 1, ContainerInput.THROW, 50L));
         }
      }
   }

   private static boolean init(List<autismclient.util.macro.ArrayUtil> targets, ItemStack stack, int handlerSlot) {
      if (targets.isEmpty()) {
         return true;
      }

      for (autismclient.util.macro.ArrayUtil target : targets) {
         if (target != null
            && (!target.init_Z() || target.init_I == handlerSlot || target.init_I == 100 && handlerSlot == 0)
            && (!target.update() || init(target, stack))) {
            return true;
         }
      }

      return false;
   }

   private static int init_I(List<ItemStack> slots, autismclient.util.macro.ArrayUtil target) {
      if (target != null && target.init_Z()) {
         int handler = init(target.init_I);
         return handler >= 9 && handler <= 44 && init(target, slots.get(handler), handler) ? handler : -1;
      }

      for (int slot = 9; slot <= 44; slot++) {
         if (init(target, slots.get(slot), slot)) {
            return slot;
         }
      }

      return -1;
   }

   private static ItemStack init_ItemStack(List<ItemStack> slots, autismclient.util.macro.ArrayUtil target) {
      int source = init_I(slots, target);
      return source < 0 ? ItemStack.EMPTY : slots.get(source);
   }

   private static boolean init(autismclient.util.macro.ArrayUtil target, ItemStack stack, int handlerSlot) {
      if (stack == null || stack.isEmpty()) {
         return false;
      }

      if (target == null) {
         return true;
      }

      int visible = load(handlerSlot);
      return target.init_I_ItemStack_I(stack, visible) >= 0;
   }

   private static boolean init(autismclient.util.macro.ArrayUtil target, ItemStack stack) {
      if (stack != null && !stack.isEmpty() && target != null) {
         autismclient.util.macro.ArrayUtil identity = target.init_ArrayUtil();
         identity.init_I = -1;
         return !identity.update() || identity.init_Z_ItemStack_I(stack, -1);
      } else {
         return false;
      }
   }

   private static List<autismclient.util.macro.ArrayUtil> init(aQ action) {
      List<autismclient.util.macro.ArrayUtil> result = new ArrayList<>();
      if (!action.entryTargets.isEmpty()) {
         for (autismclient.util.macro.ArrayUtil target : action.entryTargets) {
            if (target != null) {
               result.add(target.init_ArrayUtil());
            }
         }
      } else {
         for (String entry : action.entries) {
            autismclient.util.macro.ArrayUtil target = autismclient.util.macro.ArrayUtil.update(entry);
            if (target.init_Z() || target.update()) {
               result.add(target);
            }
         }
      }

      return result.size() > 11 ? List.copyOf(result.subList(0, 11)) : List.copyOf(result);
   }

   private static String init(autismclient.util.macro.ArrayUtil target) {
      if (target == null) {
         return "";
      }

      autismclient.util.macro.ArrayUtil identity = target.init_ArrayUtil();
      identity.init_I = -1;
      return identity.write_Str().toLowerCase(Locale.ROOT);
   }

   private static long init(aQ action, UtilCore.Inner_StreamUtil pace) {
      aQ.Inner_ConfigStore mode = action.transferMode == null ? aQ.Inner_ConfigStore.init : action.transferMode;
      if (mode == aQ.Inner_ConfigStore.init) {
         return 0L;
      } else if (mode == aQ.Inner_ConfigStore.load) {
         return 50L;
      } else {
         long configured = aQ.write(action.safeClickDelayTicks) * 50L;
         if (configured <= 0L || pace == UtilCore.Inner_StreamUtil.write) {
            return 0L;
         } else if (pace == UtilCore.Inner_StreamUtil.init && !action.safeClickDelayAfterPickup) {
            return 0L;
         } else {
            return pace == UtilCore.Inner_StreamUtil.read && !action.safeClickDelayBeforeReturn ? 0L : configured;
         }
      }
   }

   private static int init(int visible) {
      if (visible >= 0 && visible <= 8) {
         return 36 + visible;
      } else if (visible >= 9 && visible <= 35) {
         return visible;
      } else if (visible >= 36 && visible <= 39) {
         return 44 - visible;
      } else {
         return visible == 40 ? 45 : -1;
      }
   }

   private static int load(int handler) {
      if (handler >= 36 && handler <= 44) {
         return handler - 36;
      } else if (handler >= 9 && handler <= 35) {
         return handler;
      } else if (handler >= 5 && handler <= 8) {
         return 44 - handler;
      } else {
         return handler == 45 ? 40 : handler;
      }
   }

   private static ItemStack[] init(ItemStack cursor, ItemStack slot, boolean primary) {
      ItemStack cur = init(cursor);
      ItemStack target = init(slot);
      if (target.isEmpty()) {
         if (!cur.isEmpty()) {
            int place = primary ? cur.getCount() : 1;
            target = cur.copyWithCount(place);
            cur.shrink(place);
         }
      } else if (cur.isEmpty()) {
         int take = primary ? target.getCount() : (target.getCount() + 1) / 2;
         cur = target.copyWithCount(take);
         target.shrink(take);
      } else if (ItemStack.isSameItemSameComponents(target, cur)) {
         int place = Math.min(primary ? cur.getCount() : 1, Math.max(0, target.getMaxStackSize() - target.getCount()));
         target.grow(place);
         cur.shrink(place);
      } else {
         ItemStack swap = target;
         target = cur;
         cur = swap;
      }

      return new ItemStack[]{load(cur), load(target)};
   }

   private static List<ItemStack> init(List<ItemStack> stacks) {
      List<ItemStack> copy = new ArrayList<>(stacks.size());

      for (ItemStack stack : stacks) {
         copy.add(init(stack));
      }

      return copy;
   }

   private static ItemStack init(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
   }

   private static ItemStack load(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack : ItemStack.EMPTY;
   }

   private static final class Inner_CachePool {
      final autismclient.util.macro.ArrayUtil init_ArrayUtil;
      final int init_I;
      final aQ.Inner_DataUtil init_aQ_DataUtil;
      final int load;
      final int read;
      final int write;
      int update;

      Inner_CachePool(autismclient.util.macro.ArrayUtil target, int destination, aQ.Inner_DataUtil amountMode, int customAmount, int current, int capacity) {
         this.init_ArrayUtil = target;
         this.init_I = destination;
         this.init_aQ_DataUtil = amountMode;
         this.load = customAmount;
         this.read = current;
         this.write = capacity;
      }
   }

   record Inner_DataUtil(int init_I, int load, ContainerInput init_ContainerInput, long init_J) {
   }

   private enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
