package autismclient.util.macro;

import autismclient.util.ck;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class aQ implements MacroAction {
   public static final int CRAFT_RESULT_SLOT = 0;
   public static final int GRID_ENTRY_LIMIT = 4;
   public static final int CRAFT_RESULT_ENTRY_LIMIT = 1;
   public static final int ARMOR_ENTRY_LIMIT = 4;
   public static final int OFFHAND_ENTRY_LIMIT = 1;
   public static final int CURSOR_ENTRY_LIMIT = 1;
   public static final int STORAGE_ENTRY_LIMIT = 10;
   public static final int MAX_ENTRIES = 11;
   private static final int init_I = 11;
   public final List<String> entries = new ArrayList<>();
   public final List<ArrayUtil> entryTargets = new ArrayList<>();
   public aQ.Inner_StreamUtil mode = aQ.Inner_StreamUtil.PUT_IN;
   public aQ.Inner_ConfigStore transferMode = aQ.Inner_ConfigStore.FAST;
   public int safeClickDelayTicks = 1;
   public boolean safeClickDelayAfterPickup = true;
   public boolean safeClickDelayBeforeReturn = true;
   public boolean carryCursor = true;
   public boolean useCrafting = true;
   public boolean useArmor = true;
   public boolean useOffhand = true;
   public static final int DEST_AUTO = -2147483648;
   public static final int DEST_CURSOR = -1;
   private static final int[] init_IArr = new int[]{-2147483648, -1, 1, 2, 3, 4, 0, 5, 6, 7, 8, 45};
   public final List<Integer> entryDestinations = new ArrayList<>();
   public final List<aQ.Inner_DataUtil> entryAmountModes = new ArrayList<>();
   public final List<Integer> entryAmounts = new ArrayList<>();
   private boolean init_Z = true;

   public static int[] init_IArr() {
      return (int[])init_IArr.clone();
   }

   public static List<String> init_List_10() {
      ArrayList<String> labels = new ArrayList<>(init_IArr.length);

      for (int value : init_IArr) {
         labels.add(init_Str_I_3(value));
      }

      return labels;
   }

   public static int init_I_I_6(int destination) {
      for (int i = 0; i < init_IArr.length; i++) {
         if (init_IArr[i] == destination) {
            return i;
         }
      }

      return 0;
   }

   public static String init_Str_I_3(int destination) {
      if (destination == -2147483648) {
         return "Auto";
      }

      if (destination == -1) {
         return "Cursor";
      }

      String var10000;
      switch (destination) {
         case 0:
            var10000 = "Craft 5";
            break;
         case 1:
            var10000 = "Craft 1";
            break;
         case 2:
            var10000 = "Craft 2";
            break;
         case 3:
            var10000 = "Craft 3";
            break;
         case 4:
            var10000 = "Craft 4";
            break;
         case 5:
            var10000 = "Helmet";
            break;
         case 6:
            var10000 = "Chestplate";
            break;
         case 7:
            var10000 = "Leggings";
            break;
         case 8:
            var10000 = "Boots";
            break;
         case 45:
            var10000 = "Offhand";
            break;
         default:
            var10000 = "Auto";
      }

      return var10000;
   }

   private void load_V_4() {
      this.carryCursor = true;
      this.useCrafting = true;
      this.useArmor = true;
      this.useOffhand = true;
   }

   public int load_I_I(int entryIndex) {
      if (entryIndex >= 0 && entryIndex < this.entryDestinations.size()) {
         Integer v = this.entryDestinations.get(entryIndex);
         return v == null ? -2147483648 : v;
      } else {
         return -2147483648;
      }
   }

   public void init_V_I_I_2(int entryIndex, int destSlotId) {
      while (this.entryDestinations.size() <= entryIndex) {
         this.entryDestinations.add(-2147483648);
      }

      this.entryDestinations.set(entryIndex, destSlotId);
   }

   public void init_V_I_2(int targetSize) {
      while (this.entryDestinations.size() < targetSize) {
         this.entryDestinations.add(-2147483648);
      }

      while (this.entryDestinations.size() > targetSize) {
         this.entryDestinations.remove(this.entryDestinations.size() - 1);
      }
   }

   public aQ.Inner_DataUtil init_aQ_DataUtil_I(int entryIndex) {
      if (entryIndex >= 0 && entryIndex < this.entryAmountModes.size()) {
         aQ.Inner_DataUtil mode = this.entryAmountModes.get(entryIndex);
         return mode == null ? aQ.Inner_DataUtil.FULL_STK : mode;
      } else {
         return aQ.Inner_DataUtil.FULL_STK;
      }
   }

   public void init_V_I_aQ_DataUtil(int entryIndex, aQ.Inner_DataUtil mode) {
      this.load_V_I(entryIndex + 1);
      this.entryAmountModes.set(entryIndex, mode == null ? aQ.Inner_DataUtil.FULL_STK : mode);
   }

   public int read_I_I(int entryIndex) {
      if (entryIndex >= 0 && entryIndex < this.entryAmounts.size()) {
         Integer amount = this.entryAmounts.get(entryIndex);
         return Math.max(1, amount == null ? 1 : amount);
      } else {
         return 1;
      }
   }

   public void load(int entryIndex, int amount) {
      this.load_V_I(entryIndex + 1);
      this.entryAmounts.set(entryIndex, Math.max(1, amount));
   }

   public void load_V_I(int targetSize) {
      while (this.entryAmountModes.size() < targetSize) {
         this.entryAmountModes.add(aQ.Inner_DataUtil.FULL_STK);
      }

      while (this.entryAmountModes.size() > targetSize) {
         this.entryAmountModes.remove(this.entryAmountModes.size() - 1);
      }

      while (this.entryAmounts.size() < targetSize) {
         this.entryAmounts.add(1);
      }

      while (this.entryAmounts.size() > targetSize) {
         this.entryAmounts.remove(this.entryAmounts.size() - 1);
      }
   }

   public static int write(int ticks) {
      return Math.max(0, Math.min(10, ticks));
   }

   public Set<Integer> init_Set() {
      LinkedHashSet<Integer> s = new LinkedHashSet<>();
      s.add(1);
      s.add(2);
      s.add(3);
      s.add(4);
      s.add(0);
      s.add(5);
      s.add(6);
      s.add(7);
      s.add(8);
      s.add(45);
      return s;
   }

   private List<Integer> load_List_4() {
      return new ArrayList<>(this.init_Set());
   }

   private Set<Integer> load_Set() {
      LinkedHashSet<Integer> s = new LinkedHashSet<>();
      s.addAll(this.init_Set());
      return s;
   }

   public static boolean init_Z_I_3(int slotId) {
      return slotId == 0;
   }

   public void init_V_12() {
      this.init_V_Z_2(this.carryCursor);
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc != null && mc.player != null && mc.gameMode != null) {
         this.load_V_4();
         ck shared = ck.init_ck();
         boolean prevBypass = shared.close_Z();
         shared.flush(true);

         try {
            if (mc.player.containerMenu != mc.player.inventoryMenu) {
               this.init_V_Minecraft_5(mc);
               return;
            }

            InventoryMenu handler = mc.player.inventoryMenu;
            if (handler.getCarried().isEmpty()) {
               switch (this.mode) {
                  case PUT_IN:
                     this.init_V_InventoryMenu_Minecraft(handler, mc);
                     return;
                  case TAKE_OUT:
                     this.load(handler, mc);
                     return;
                  case DROP:
                     this.read(handler, mc);
                     return;
                  default:
                     return;
               }
            }

            init_Z_InventoryMenu_Minecraft(handler, mc);
            this.init_V_InventoryMenu_Z(handler, true);
         } finally {
            shared.flush(prevBypass);
         }
      }
   }

   private void init_V_Minecraft_5(Minecraft mc) {
      if (mc.getConnection() != null) {
         this.load_V_4();
         autismclient.util.E containerTarget = ck.init_ck().init_E();
         if (containerTarget != null) {
            InventoryMenu playerHandler = mc.player.inventoryMenu;
            if (!playerHandler.getCarried().isEmpty()) {
               init_Z_InventoryMenu_Minecraft(playerHandler, mc);
               this.init_V_InventoryMenu_Z(playerHandler, true);
            } else {
               AbstractContainerMenu containerHandler = mc.player.containerMenu;
               if (this.mode == aQ.Inner_StreamUtil.PUT_IN) {
                  for (int slotId : this.init_List_AbstractContainerM_InventoryMenu(containerHandler, playerHandler)) {
                     mc.gameMode.handleContainerInput(containerHandler.containerId, slotId, 0, ContainerInput.QUICK_MOVE, mc.player);
                  }
               }

               mc.getConnection().send(new ServerboundContainerClosePacket(containerHandler.containerId));
               mc.player.containerMenu = playerHandler;
               switch (this.mode) {
                  case PUT_IN:
                     this.init_V_InventoryMenu_Minecraft(playerHandler, mc);
                     break;
                  case TAKE_OUT:
                     this.load(playerHandler, mc);
                     break;
                  case DROP:
                     this.read(playerHandler, mc);
               }

               mc.player.containerMenu = containerHandler;
               containerTarget.init_Z_Minecraft(mc);
            }
         }
      }
   }

   private void init_V_InventoryMenu_Minecraft(InventoryMenu handler, Minecraft mc) {
      this.load_V_4();
      List<ArrayUtil> targets = this.write();
      if (targets.isEmpty()) {
         if (init_Z_InventoryMenu_Z(handler, true)) {
            this.init_V_InventoryMenu_Z(handler, true);
         }
      } else {
         this.init_V_Z_2(true);
         boolean moved = false;
         boolean cursorClaimed = false;

         for (int i = 0; i < targets.size(); i++) {
            if (this.load_I_I(i) == -1 && !cursorClaimed && handler.getCarried().isEmpty() && this.load(handler, mc, i, i + 1)) {
               moved = true;
               cursorClaimed = true;
            }
         }

         int safety = 0;

         while (safety++ < 512) {
            aQ.Inner_CachePool move = this.init_aQ_CachePool_InventoryMenu(handler);
            if (move == null || !init(handler, mc, move.init, move.load, move.read)) {
               break;
            }

            moved = true;
         }

         int gridCapacity = this.load_List_4().size();
         if (!cursorClaimed && handler.getCarried().isEmpty() && targets.size() > gridCapacity) {
            boolean anyExplicit = false;

            for (int i = 0; i < targets.size(); i++) {
               int dest = this.load_I_I(i);
               if (dest != -2147483648 && dest != -1) {
                  anyExplicit = true;
                  break;
               }
            }

            if (!anyExplicit && this.load(handler, mc, gridCapacity, gridCapacity + 1)) {
               moved = true;
            }
         }

         this.init_V_InventoryMenu_Z(handler, true);
      }
   }

   private void load(InventoryMenu handler, Minecraft mc) {
      this.load_V_4();

      for (Integer slotId : this.load_Set()) {
         if (slotId != null && slotId >= 0 && slotId < handler.slots.size()) {
            Slot slot = handler.slots.get(slotId);
            if (!slot.getItem().isEmpty() && (this.entries.isEmpty() || this.init_Z_Slot_I(slot, slotId))) {
               mc.gameMode.handleContainerInput(handler.containerId, slotId, 0, ContainerInput.QUICK_MOVE, mc.player);
            }
         }
      }

      this.init_V_InventoryMenu_Z(handler, true);
   }

   private void read(InventoryMenu handler, Minecraft mc) {
      this.load_V_4();

      for (Integer slotId : this.load_Set()) {
         if (slotId != null && slotId >= 0 && slotId < handler.slots.size()) {
            Slot slot = handler.slots.get(slotId);
            if (!slot.getItem().isEmpty() && (this.entries.isEmpty() || this.init_Z_Slot_I(slot, slotId))) {
               mc.gameMode.handleContainerInput(handler.containerId, slotId, 1, ContainerInput.THROW, mc.player);
            }
         }
      }

      this.init_V_InventoryMenu_Z(handler, true);
   }

   private boolean init_Z_Slot_I(Slot slot, int slotId) {
      for (ArrayUtil entryTarget : this.write()) {
         if (entryTarget != null) {
            if (entryTarget.init_Z()) {
               if (init_Z_I_I(entryTarget.init_I, slotId) && (!entryTarget.update() || init_Z_ItemStack_ArrayUtil(slot.getItem(), entryTarget))) {
                  return true;
               }
            } else if (entryTarget.update() && init_Z_ItemStack(slot.getItem(), entryTarget, slotId)) {
               return true;
            }
         }
      }

      return false;
   }

   public aQ.Inner_CachePool init_aQ_CachePool_InventoryMenu(InventoryMenu handler) {
      List<aQ.Inner_CachePool> moves = this.init_List_InventoryMenu(handler);
      return moves.isEmpty() ? null : moves.get(0);
   }

   public List<aQ.Inner_CachePool> init_List_InventoryMenu(InventoryMenu handler) {
      ArrayList<aQ.Inner_CachePool> moves = new ArrayList<>();
      if (handler == null) {
         return moves;
      }

      this.load_V_4();
      Set<Integer> storageSlotSet = this.init_Set();
      List<Integer> storageSlots = this.load_List_4();
      List<ArrayUtil> targets = this.write();
      int limit = Math.min(this.init_I_5(), targets.size());
      this.init_V_I_2(limit);
      this.load_V_I(limit);
      int nextTargetIdx = 0;
      Set<Integer> claimedDestinations = new HashSet<>();
      List<aQ.Inner_BufferHelper> rows = new ArrayList<>();

      for (int i = 0; i < limit; i++) {
         int dest = this.load_I_I(i);
         if (dest != -1) {
            int targetSlotId = -1;
            if (dest != -2147483648) {
               targetSlotId = dest;
               if (targetSlotId < 0 || targetSlotId >= handler.slots.size()) {
                  continue;
               }
            } else {
               while (nextTargetIdx < storageSlots.size()) {
                  int candidate = storageSlots.get(nextTargetIdx);
                  if (candidate < 0 || candidate >= handler.slots.size()) {
                     nextTargetIdx++;
                  } else if (claimedDestinations.contains(candidate)) {
                     nextTargetIdx++;
                  } else {
                     ItemStack stack = handler.slots.get(candidate).getItem();
                     if (stack.isEmpty() || init_Z_ItemStack_ArrayUtil(stack, targets.get(i))) {
                        break;
                     }

                     nextTargetIdx++;
                  }
               }

               if (nextTargetIdx >= storageSlots.size()) {
                  continue;
               }

               targetSlotId = storageSlots.get(nextTargetIdx++);
            }

            Slot destSlot = handler.slots.get(targetSlotId);
            ItemStack destStack = destSlot.getItem();
            ArrayUtil target = targets.get(i);
            if (destStack.isEmpty() || init_Z_ItemStack_ArrayUtil(destStack, target)) {
               int maxCount = init_I_Slot_ItemStack(
                  destSlot, destStack.isEmpty() ? this.init_ItemStack_InventoryMenu(handler, target, storageSlotSet) : destStack
               );
               int currentCount = destStack.isEmpty() ? 0 : destStack.getCount();
               if (maxCount > currentCount) {
                  claimedDestinations.add(targetSlotId);
                  rows.add(new aQ.Inner_BufferHelper(i, target, targetSlotId, this.init_aQ_DataUtil_I(i), this.read_I_I(i), currentCount, maxCount));
               }
            }
         }
      }

      if (rows.isEmpty()) {
         return moves;
      }

      init_V_InventoryMenu(handler, rows, storageSlotSet);
      init(handler, rows, storageSlotSet, moves);
      return moves;
   }

   private static void init(InventoryMenu handler, List<aQ.Inner_BufferHelper> rows, Set<Integer> storageSlotSet, List<aQ.Inner_CachePool> moves) {
      if (handler != null && rows != null && !rows.isEmpty() && moves != null) {
         Map<Integer, Integer> sourceUsed = new HashMap<>();
         LinkedHashMap<String, List<aQ.Inner_BufferHelper>> byTarget = new LinkedHashMap<>();

         for (aQ.Inner_BufferHelper row : rows) {
            if (row != null && row.sync > 0 && row.init_ArrayUtil != null) {
               byTarget.computeIfAbsent(init_Str_ArrayUtil(row.init_ArrayUtil), ignored -> new ArrayList<>()).add(row);
            }
         }

         for (List<aQ.Inner_BufferHelper> group : byTarget.values()) {
            if (group != null && !group.isEmpty()) {
               int totalDemand = 0;

               for (aQ.Inner_BufferHelper row : group) {
                  totalDemand += Math.max(0, row.sync);
               }

               if (totalDemand > 0) {
                  int sourceSlot = init(handler, group.get(0).init_ArrayUtil, storageSlotSet, sourceUsed, totalDemand);
                  if (sourceSlot >= 0) {
                     init(handler, group, sourceSlot, sourceUsed, moves);
                  } else {
                     for (aQ.Inner_BufferHelper row : group) {
                        int remaining = row.sync;
                        if (remaining > 0) {
                           for (Slot slot : handler.slots) {
                              if (remaining <= 0) {
                                 break;
                              }

                              if (init(handler, slot, row.init_ArrayUtil, storageSlotSet, sourceUsed) && slot.index != row.load) {
                                 int used = sourceUsed.getOrDefault(slot.index, 0);
                                 int available = slot.getItem().getCount() - used;
                                 if (available > 0) {
                                    int take = Math.min(remaining, available);
                                    moves.add(new aQ.Inner_CachePool(slot.index, row.load, take));
                                    sourceUsed.put(slot.index, used + take);
                                    remaining -= take;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static int init(InventoryMenu handler, ArrayUtil target, Set<Integer> storageSlotSet, Map<Integer, Integer> sourceUsed, int totalDemand) {
      if (handler != null && target != null && totalDemand > 0) {
         int exact = -1;
         int smallestEnough = -1;
         int smallestEnoughCount = 2147483647;

         for (Slot slot : handler.slots) {
            if (init(handler, slot, target, storageSlotSet, sourceUsed)) {
               int available = slot.getItem().getCount() - sourceUsed.getOrDefault(slot.index, 0);
               if (available > 0) {
                  if (available == totalDemand) {
                     exact = slot.index;
                     break;
                  }

                  if (available > totalDemand && available < smallestEnoughCount) {
                     smallestEnough = slot.index;
                     smallestEnoughCount = available;
                  }
               }
            }
         }

         return exact >= 0 ? exact : smallestEnough;
      } else {
         return -1;
      }
   }

   private static boolean init(InventoryMenu handler, Slot slot, ArrayUtil target, Set<Integer> storageSlotSet, Map<Integer, Integer> sourceUsed) {
      if (handler == null || slot == null || target == null || storageSlotSet == null) {
         return false;
      }

      if (storageSlotSet.contains(slot.index) || !init_Z_Slot(slot)) {
         return false;
      }

      if (!init_Z_InventoryMenu_2(handler, slot, target)) {
         return false;
      }

      int available = slot.getItem().getCount() - (sourceUsed == null ? 0 : sourceUsed.getOrDefault(slot.index, 0));
      return available > 0;
   }

   private static void init(
      InventoryMenu handler, List<aQ.Inner_BufferHelper> rows, int sourceSlotId, Map<Integer, Integer> sourceUsed, List<aQ.Inner_CachePool> moves
   ) {
      if (handler != null && rows != null && sourceUsed != null && moves != null && sourceSlotId >= 0 && sourceSlotId < handler.slots.size()) {
         Slot sourceSlot = handler.slots.get(sourceSlotId);
         if (sourceSlot != null && !sourceSlot.getItem().isEmpty()) {
            int used = sourceUsed.getOrDefault(sourceSlotId, 0);
            int available = sourceSlot.getItem().getCount() - used;

            for (aQ.Inner_BufferHelper row : rows) {
               if (row != null && row.sync > 0 && available > 0 && row.load != sourceSlotId) {
                  int take = Math.min(row.sync, available);
                  if (take > 0) {
                     moves.add(new aQ.Inner_CachePool(sourceSlotId, row.load, take));
                     available -= take;
                     used += take;
                  }
               }
            }

            sourceUsed.put(sourceSlotId, used);
         }
      }
   }

   public boolean init_Z_7() {
      this.load_V_4();
      List<ArrayUtil> targets = this.write();
      int limit = Math.min(this.init_I_5(), targets.size());
      boolean anyExplicit = false;
      int autoCount = 0;

      for (int i = 0; i < limit; i++) {
         int dest = this.load_I_I(i);
         if (dest == -1) {
            return true;
         }

         if (dest == -2147483648) {
            autoCount++;
         } else {
            anyExplicit = true;
         }
      }

      return !anyExplicit && autoCount > this.load_List_4().size();
   }

   public List<Integer> init_List_AbstractContainerM(AbstractContainerMenu containerHandler) {
      return this.init_List_AbstractContainerM_InventoryMenu(containerHandler, null);
   }

   public List<Integer> init_List_AbstractContainerM_InventoryMenu(AbstractContainerMenu containerHandler, InventoryMenu playerHandler) {
      List<Integer> slotIds = new ArrayList<>();
      if (containerHandler == null) {
         return slotIds;
      }

      List<ArrayUtil> targets = this.write();
      int limit = Math.min(this.init_I_5(), targets.size());
      Map<String, Integer> neededByKey = new LinkedHashMap<>();
      if (playerHandler != null) {
         List<aQ.Inner_BufferHelper> rows = this.load_List_InventoryMenu(playerHandler);
         init_V_InventoryMenu(playerHandler, rows, this.init_Set());

         for (aQ.Inner_BufferHelper row : rows) {
            if (row.sync > 0 && row.init_ArrayUtil != null && row.init_ArrayUtil.update()) {
               String key = init_Str_ArrayUtil(row.init_ArrayUtil);
               neededByKey.put(key, neededByKey.getOrDefault(key, 0) + row.sync);
            }
         }
      }

      Set<Integer> usedContainerSlots = new HashSet<>();

      for (int i = 0; i < limit; i++) {
         ArrayUtil itemTarget = targets.get(i);
         if (itemTarget != null && !itemTarget.init_Z() && itemTarget.update()) {
            String key = init_Str_ArrayUtil(itemTarget);
            if (playerHandler == null || neededByKey.isEmpty() || neededByKey.getOrDefault(key, 0) > 0) {
               for (Slot slot : containerHandler.slots) {
                  if (!usedContainerSlots.contains(slot.index)
                     && !slot.getItem().isEmpty()
                     && !(slot.container instanceof Inventory)
                     && init_Z_ItemStack(slot.getItem(), itemTarget, -1)) {
                     slotIds.add(slot.index);
                     usedContainerSlots.add(slot.index);
                     if (playerHandler != null) {
                        neededByKey.put(key, neededByKey.getOrDefault(key, 0) - slot.getItem().getCount());
                     }
                     break;
                  }
               }
            }
         }
      }

      return slotIds;
   }

   @Override
   public CompoundTag toTag() {
      this.load_V_4();
      CompoundTag tag = new CompoundTag();
      tag.putString("type", StreamGate.XCARRY.name());
      tag.putString("mode", this.mode.name());
      tag.putString("transferMode", this.transferMode.name());
      tag.putInt("safeClickDelayTicks", write(this.safeClickDelayTicks));
      tag.putBoolean("safeClickDelayAfterPickup", this.safeClickDelayAfterPickup);
      tag.putBoolean("safeClickDelayBeforeReturn", this.safeClickDelayBeforeReturn);
      tag.putBoolean("carryCursor", true);
      tag.putBoolean("useCrafting", true);
      tag.putBoolean("useArmor", true);
      tag.putBoolean("useOffhand", true);
      List<ArrayUtil> targets = this.read_List();
      int savedCount = Math.min(this.init_I_5(), targets.size());
      tag.put("entries", ArrayUtil.init_ListTag_List(targets.subList(0, savedCount)));
      this.init_V_I_2(savedCount);
      this.load_V_I(savedCount);
      ListTag destinations = new ListTag();
      ListTag amountModes = new ListTag();
      ListTag amounts = new ListTag();

      for (int i = 0; i < savedCount; i++) {
         destinations.add(StringTag.valueOf(String.valueOf(this.load_I_I(i))));
         amountModes.add(StringTag.valueOf(this.init_aQ_DataUtil_I(i).name()));
         amounts.add(StringTag.valueOf(String.valueOf(this.read_I_I(i))));
      }

      tag.put("entryDestinations", destinations);
      tag.put("entryAmountModes", amountModes);
      tag.put("entryAmounts", amounts);
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.entries.clear();
      this.entryTargets.clear();
      this.entryDestinations.clear();
      this.entryAmountModes.clear();
      this.entryAmounts.clear();
      String modeStr = tag.getStringOr("mode", "PUT_IN");

      try {
         this.mode = aQ.Inner_StreamUtil.valueOf(modeStr);
      } catch (IllegalArgumentException e) {
         this.mode = aQ.Inner_StreamUtil.PUT_IN;
      }

      String transferModeStr = tag.getStringOr("transferMode", "FAST");

      try {
         this.transferMode = aQ.Inner_ConfigStore.valueOf(transferModeStr);
      } catch (IllegalArgumentException e) {
         this.transferMode = aQ.Inner_ConfigStore.FAST;
      }

      this.safeClickDelayTicks = write(tag.getIntOr("safeClickDelayTicks", 1));
      this.safeClickDelayAfterPickup = tag.getBooleanOr("safeClickDelayAfterPickup", true);
      this.safeClickDelayBeforeReturn = tag.getBooleanOr("safeClickDelayBeforeReturn", true);
      this.load_V_4();
      if (tag.contains("entries")) {
         ListTag list = tag.getList("entries").orElse(new ListTag());

         for (ArrayUtil target : ArrayUtil.init_List_ListTag(list)) {
            if (target != null && (target.init_Z() || target.update())) {
               this.entryTargets.add(target);
               if (this.entryTargets.size() >= this.init_I_5()) {
                  break;
               }
            }
         }
      }

      if (tag.contains("entryDestinations")) {
         for (Tag element : tag.getList("entryDestinations").orElse(new ListTag())) {
            int dest = -2147483648;

            try {
               dest = Integer.parseInt(element.asString().orElse(String.valueOf(-2147483648)));
            } catch (NumberFormatException var10) {
            }

            this.entryDestinations.add(dest);
         }
      }

      if (tag.contains("entryAmountModes")) {
         for (Tag element : tag.getList("entryAmountModes").orElse(new ListTag())) {
            aQ.Inner_DataUtil amountMode = aQ.Inner_DataUtil.FULL_STK;

            try {
               amountMode = aQ.Inner_DataUtil.valueOf(element.asString().orElse(aQ.Inner_DataUtil.FULL_STK.name()));
            } catch (IllegalArgumentException var9) {
            }

            this.entryAmountModes.add(amountMode);
         }
      }

      if (tag.contains("entryAmounts")) {
         for (Tag element : tag.getList("entryAmounts").orElse(new ListTag())) {
            int amount = 1;

            try {
               amount = Integer.parseInt(element.asString().orElse("1"));
            } catch (NumberFormatException var8) {
            }

            this.entryAmounts.add(Math.max(1, amount));
         }
      }

      this.init_V_I_2(this.entryTargets.size());
      this.load_V_I(this.entryTargets.size());
      this.read_V_2();
      this.init_Z = tag.getBooleanOr("enabled", true);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.XCARRY;
   }

   @Override
   public String getDisplayName() {
      String var10000;
      label23:
      switch (this.mode) {
         case PUT_IN:
            switch (this.transferMode) {
               case FAST:
                  var10000 = "XCarry";
                  break label23;
               case CLICK:
                  var10000 = "XCarry Click";
                  break label23;
               case SAFE_CLICK:
                  var10000 = "XCarry Safe";
                  break label23;
               default:
                  throw new MatchException(null, null);
            }
         case TAKE_OUT:
            var10000 = "XCarry Out";
            break;
         case DROP:
            var10000 = "XCarry Drop";
            break;
         default:
            throw new MatchException(null, null);
      }

      String prefix = var10000;
      this.load_V_4();
      if (this.entries.isEmpty()) {
         return prefix;
      }

      String first = load_Str_Str_2(this.entries.get(0));
      if (this.entries.size() > 1) {
         first = first + " (+" + (this.entries.size() - 1) + ")";
      }

      return prefix + " [" + first + "]";
   }

   @Override
   public String getIcon() {
      return "XC";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init_Z = enabled;
   }

   public static String init_Str_Str_7(String raw) {
      if (raw == null) {
         return null;
      }

      String trimmed = raw.trim();
      if (trimmed.isEmpty()) {
         return null;
      }

      if (!trimmed.startsWith("#")) {
         return trimmed;
      }

      int separator = trimmed.indexOf(124);
      String slotPart = separator >= 0 ? trimmed.substring(1, separator) : trimmed.substring(1);

      try {
         int slot = Integer.parseInt(slotPart.trim());
         if (slot < 0) {
            return null;
         }

         String name = separator >= 0 && separator + 1 < trimmed.length() ? trimmed.substring(separator + 1).trim() : "";
         return !name.isEmpty() ? "#" + slot + "|" + name : "#" + slot;
      } catch (NumberFormatException ignored) {
         return null;
      }
   }

   public static String load_Str_Str_2(String entry) {
      String normalized = init_Str_Str_7(entry);
      if (normalized == null) {
         return "";
      } else {
         int slot = init_I_Str_3(normalized);
         String name = read_Str_Str(normalized);
         if (slot >= 0 && !name.isEmpty()) {
            return name + " @ Slot " + slot;
         } else {
            return slot >= 0 ? "Slot " + slot : normalized;
         }
      }
   }

   private static int init_I_InventoryMenu_ArrayUtil(InventoryMenu handler, ArrayUtil target) {
      return init_I_InventoryMenu(handler, target, Set.of());
   }

   private static int init_I_InventoryMenu(InventoryMenu handler, ArrayUtil target, Set<Integer> excludedSlotIds) {
      if (target == null) {
         return -1;
      }

      Set<Integer> excluded = excludedSlotIds == null ? Set.of() : excludedSlotIds;
      if (target.init_Z()) {
         int exactSlot = init_I_AbstractContainerM_I(handler, target.init_I);
         if (exactSlot < 0) {
            return -1;
         }

         if (excluded.contains(exactSlot)) {
            return -1;
         }

         if (!target.update()) {
            return exactSlot;
         }

         Slot slot = handler.slots.get(exactSlot);
         return init_Z_ItemStack(slot.getItem(), target, target.init_I) ? exactSlot : -1;
      } else {
         for (Slot slot : handler.slots) {
            if (!excluded.contains(slot.index) && init_Z_Slot(slot)) {
               int visibleSlot = init_I_InventoryMenu_I(handler, slot.index);
               if (init_Z_ItemStack(slot.getItem(), target, visibleSlot)) {
                  return slot.index;
               }
            }
         }

         return -1;
      }
   }

   public static boolean init(InventoryMenu handler, Minecraft mc, int fromSlotId, int toSlotId, int count) {
      if (count <= 0) {
         return false;
      }

      if (mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (fromSlotId < 0 || fromSlotId >= handler.slots.size()) {
         return false;
      }

      if (toSlotId < 0 || toSlotId >= handler.slots.size()) {
         return false;
      }

      if (fromSlotId == toSlotId) {
         return true;
      }

      if (!handler.getCarried().isEmpty()) {
         return false;
      }

      Slot sourceSlot = handler.slots.get(fromSlotId);
      Slot targetSlot = handler.slots.get(toSlotId);
      ItemStack sourceBefore = sourceSlot.getItem();
      if (sourceBefore.isEmpty()) {
         return false;
      }

      ItemStack expectedSource = sourceBefore.copy();
      ItemStack targetBefore = targetSlot.getItem();
      if (!targetBefore.isEmpty() && !ItemStack.isSameItemSameComponents(sourceBefore, targetBefore)) {
         return false;
      }

      int targetCapacity = init_I_Slot_ItemStack(targetSlot, targetBefore.isEmpty() ? sourceBefore : targetBefore);
      int targetCountBefore = targetBefore.isEmpty() ? 0 : targetBefore.getCount();
      int amount = Math.min(count, Math.min(expectedSource.getCount(), Math.max(0, targetCapacity - targetCountBefore)));
      if (amount <= 0) {
         return false;
      }

      if (amount == expectedSource.getCount() && targetBefore.isEmpty()) {
         return init(handler, mc, fromSlotId, toSlotId);
      }

      mc.gameMode.handleContainerInput(handler.containerId, fromSlotId, 0, ContainerInput.PICKUP, mc.player);
      if (handler.getCarried().isEmpty()) {
         return false;
      }

      int trimSafety = Math.max(0, handler.getCarried().getCount() - amount);

      while (!handler.getCarried().isEmpty() && handler.getCarried().getCount() > amount && trimSafety-- > 0) {
         mc.gameMode.handleContainerInput(handler.containerId, fromSlotId, 1, ContainerInput.PICKUP, mc.player);
      }

      if (!handler.getCarried().isEmpty() && handler.getCarried().getCount() == amount) {
         mc.gameMode.handleContainerInput(handler.containerId, toSlotId, 0, ContainerInput.PICKUP, mc.player);
         if (!handler.getCarried().isEmpty()) {
            init_Z_InventoryMenu(handler, mc, fromSlotId);
         }

         ItemStack targetAfter = targetSlot.getItem();
         return !targetAfter.isEmpty()
            && ItemStack.isSameItemSameComponents(targetAfter, expectedSource)
            && targetAfter.getCount() >= targetCountBefore + amount
            && handler.getCarried().isEmpty();
      } else {
         init_Z_InventoryMenu(handler, mc, fromSlotId);
         return false;
      }
   }

   private static boolean init_Z_InventoryMenu(InventoryMenu handler, Minecraft mc, int preferredSlotId) {
      if (handler == null || mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (handler.getCarried().isEmpty()) {
         return true;
      }

      int returnSlot = preferredSlotId;
      if (returnSlot < 0 || returnSlot >= handler.slots.size() || !init_Z_Slot_ItemStack(handler.slots.get(returnSlot), handler.getCarried())) {
         returnSlot = init_I_InventoryMenu_ItemStack(handler, handler.getCarried());
      }

      if (returnSlot >= 0 && returnSlot < handler.slots.size()) {
         mc.gameMode.handleContainerInput(handler.containerId, returnSlot, 0, ContainerInput.PICKUP, mc.player);
         return handler.getCarried().isEmpty();
      } else {
         return false;
      }
   }

   private static boolean init(InventoryMenu handler, Minecraft mc, int fromSlotId, int toSlotId) {
      if (mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      } else if (fromSlotId < 0 || fromSlotId >= handler.slots.size()) {
         return false;
      } else if (toSlotId < 0 || toSlotId >= handler.slots.size()) {
         return false;
      } else if (fromSlotId == toSlotId) {
         return true;
      } else if (!handler.getCarried().isEmpty()) {
         return false;
      } else if (handler.slots.get(fromSlotId).getItem().isEmpty()) {
         return false;
      } else if (!handler.slots.get(toSlotId).getItem().isEmpty()) {
         return false;
      } else {
         mc.gameMode.handleContainerInput(handler.containerId, fromSlotId, 0, ContainerInput.PICKUP, mc.player);
         if (handler.getCarried().isEmpty()) {
            return false;
         } else {
            mc.gameMode.handleContainerInput(handler.containerId, toSlotId, 0, ContainerInput.PICKUP, mc.player);
            if (!handler.getCarried().isEmpty()) {
               mc.gameMode.handleContainerInput(handler.containerId, fromSlotId, 0, ContainerInput.PICKUP, mc.player);
               return false;
            } else {
               return handler.slots.get(fromSlotId).getItem().isEmpty() && !handler.slots.get(toSlotId).getItem().isEmpty();
            }
         }
      }
   }

   private boolean load(InventoryMenu handler, Minecraft mc, int startTargetIndex, int maxTargets) {
      if (handler != null && mc != null && mc.player != null && mc.gameMode != null) {
         if (!handler.getCarried().isEmpty()) {
            return init_Z_InventoryMenu_Minecraft(handler, mc);
         }

         List<ArrayUtil> targets = this.write();
         Set<Integer> storageSlotSet = this.init_Set();
         int limit = Math.min(maxTargets, targets.size());

         for (int i = Math.max(0, startTargetIndex); i < limit; i++) {
            int sourceSlotId = init_I_InventoryMenu(handler, targets.get(i), storageSlotSet);
            if (sourceSlotId >= 0 && sourceSlotId < handler.slots.size()) {
               Slot sourceSlot = handler.slots.get(sourceSlotId);
               if (sourceSlot != null && !sourceSlot.getItem().isEmpty()) {
                  ItemStack expected = sourceSlot.getItem().copy();
                  mc.gameMode.handleContainerInput(handler.containerId, sourceSlotId, 0, ContainerInput.PICKUP, mc.player);
                  if (handler.getCarried().isEmpty()) {
                     return false;
                  }

                  int safety = Math.max(0, handler.getCarried().getCount() - 1);

                  while (handler.getCarried().getCount() > 1 && safety-- > 0) {
                     mc.gameMode.handleContainerInput(handler.containerId, sourceSlotId, 1, ContainerInput.PICKUP, mc.player);
                  }

                  ItemStack carried = handler.getCarried();
                  return !carried.isEmpty() && carried.getCount() == 1 && ItemStack.isSameItemSameComponents(carried, expected);
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init_Z_InventoryMenu_Minecraft(InventoryMenu handler, Minecraft mc) {
      if (handler == null || mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (handler.getCarried().isEmpty()) {
         return false;
      }

      if (handler.getCarried().getCount() <= 1) {
         return true;
      }

      int safety = Math.max(0, handler.getCarried().getCount() - 1);

      while (!handler.getCarried().isEmpty() && handler.getCarried().getCount() > 1 && safety-- > 0) {
         int returnSlotId = init_I_InventoryMenu_ItemStack(handler, handler.getCarried());
         if (returnSlotId < 0) {
            break;
         }

         mc.gameMode.handleContainerInput(handler.containerId, returnSlotId, 1, ContainerInput.PICKUP, mc.player);
      }

      return !handler.getCarried().isEmpty() && handler.getCarried().getCount() == 1;
   }

   private static int init_I_InventoryMenu_ItemStack(InventoryMenu handler, ItemStack carried) {
      if (handler != null && carried != null && !carried.isEmpty()) {
         for (Slot slot : handler.slots) {
            if (slot != null && slot.index >= 5 && (slot.index < 5 || slot.index > 8) && slot.index != 45 && slot.container instanceof Inventory) {
               ItemStack stack = slot.getItem();
               if (stack.isEmpty()) {
                  return slot.index;
               }

               if (ItemStack.isSameItemSameComponents(stack, carried) && stack.getCount() < Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack))) {
                  return slot.index;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static boolean init_Z_Slot_ItemStack(Slot slot, ItemStack carried) {
      if (slot != null && carried != null && !carried.isEmpty()) {
         ItemStack stack = slot.getItem();
         return stack.isEmpty() ? true : ItemStack.isSameItemSameComponents(stack, carried) && stack.getCount() < init_I_Slot_ItemStack(slot, stack);
      } else {
         return false;
      }
   }

   private static int init_I_Slot_ItemStack(Slot slot, ItemStack stack) {
      return slot != null && stack != null && !stack.isEmpty() ? Math.max(0, Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack))) : 0;
   }

   private ItemStack init_ItemStack_InventoryMenu(InventoryMenu handler, ArrayUtil target, Set<Integer> excludedSlotIds) {
      if (handler != null && target != null) {
         for (Slot slot : handler.slots) {
            if (slot != null && !excludedSlotIds.contains(slot.index) && init_Z_Slot(slot) && init_Z_InventoryMenu_2(handler, slot, target)) {
               return slot.getItem();
            }
         }

         return ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   private List<aQ.Inner_BufferHelper> load_List_InventoryMenu(InventoryMenu handler) {
      List<aQ.Inner_BufferHelper> rows = new ArrayList<>();
      if (handler == null) {
         return rows;
      }

      Set<Integer> storageSlotSet = this.init_Set();
      List<Integer> storageSlots = this.load_List_4();
      List<ArrayUtil> targets = this.write();
      int limit = Math.min(this.init_I_5(), targets.size());
      this.init_V_I_2(limit);
      this.load_V_I(limit);
      int nextTargetIdx = 0;
      Set<Integer> claimedDestinations = new HashSet<>();

      for (int i = 0; i < limit; i++) {
         int dest = this.load_I_I(i);
         if (dest != -1) {
            int targetSlotId;
            if (dest == -2147483648) {
               targetSlotId = -1;

               while (nextTargetIdx < storageSlots.size()) {
                  int candidate = storageSlots.get(nextTargetIdx++);
                  if (candidate >= 0 && candidate < handler.slots.size() && !claimedDestinations.contains(candidate)) {
                     targetSlotId = candidate;
                     break;
                  }
               }

               if (targetSlotId < 0) {
                  continue;
               }
            } else {
               targetSlotId = dest;
               if (targetSlotId < 0 || targetSlotId >= handler.slots.size()) {
                  continue;
               }
            }

            Slot destSlot = handler.slots.get(targetSlotId);
            ItemStack destStack = destSlot.getItem();
            ArrayUtil target = targets.get(i);
            if (destStack.isEmpty() || init_Z_ItemStack_ArrayUtil(destStack, target)) {
               ItemStack sample = destStack.isEmpty() ? this.init_ItemStack_InventoryMenu(handler, target, storageSlotSet) : destStack;
               int maxCount = init_I_Slot_ItemStack(destSlot, sample);
               int currentCount = destStack.isEmpty() ? 0 : destStack.getCount();
               if (maxCount > currentCount) {
                  claimedDestinations.add(targetSlotId);
                  rows.add(new aQ.Inner_BufferHelper(i, target, targetSlotId, this.init_aQ_DataUtil_I(i), this.read_I_I(i), currentCount, maxCount));
               }
            }
         }
      }

      return rows;
   }

   private static void init_V_InventoryMenu(InventoryMenu handler, List<aQ.Inner_BufferHelper> rows, Set<Integer> excludedSlotIds) {
      if (handler != null && rows != null && !rows.isEmpty()) {
         Map<String, Integer> available = new LinkedHashMap<>();

         for (Slot slot : handler.slots) {
            if (slot != null && !excludedSlotIds.contains(slot.index) && init_Z_Slot(slot)) {
               ItemStack stack = slot.getItem();
               if (!stack.isEmpty()) {
                  for (aQ.Inner_BufferHelper row : rows) {
                     if (row.init_ArrayUtil != null && init_Z_InventoryMenu_2(handler, slot, row.init_ArrayUtil)) {
                        String key = init_Str_ArrayUtil(row.init_ArrayUtil);
                        available.put(key, available.getOrDefault(key, 0) + stack.getCount());
                        break;
                     }
                  }
               }
            }
         }

         Map<String, List<aQ.Inner_BufferHelper>> byKey = new LinkedHashMap<>();

         for (aQ.Inner_BufferHelper row : rows) {
            if (row.init_ArrayUtil != null) {
               byKey.computeIfAbsent(init_Str_ArrayUtil(row.init_ArrayUtil), ignored -> new ArrayList<>()).add(row);
            }
         }

         for (Entry<String, List<aQ.Inner_BufferHelper>> entry : byKey.entrySet()) {
            int remaining = available.getOrDefault(entry.getKey(), 0);

            for (aQ.Inner_BufferHelper row : entry.getValue()) {
               remaining += row.write;
            }

            List<aQ.Inner_BufferHelper> fullRows = new ArrayList<>();

            for (aQ.Inner_BufferHelper row : entry.getValue()) {
               row.sync = 0;
               row.flush = row.write;
               if (row.init_aQ_DataUtil == aQ.Inner_DataUtil.CUSTOM) {
                  int desired = Math.min(Math.min(row.read, row.update), Math.max(0, remaining));
                  int reserved = Math.max(row.write, desired);
                  row.flush = desired;
                  row.sync = Math.max(0, desired - row.write);
                  remaining = Math.max(0, remaining - reserved);
               } else {
                  fullRows.add(row);
               }
            }

            if (!fullRows.isEmpty()) {
               int totalForFull = remaining;
               Map<aQ.Inner_BufferHelper, Integer> desired = new LinkedHashMap<>();

               for (aQ.Inner_BufferHelper row : fullRows) {
                  desired.put(row, 0);
               }

               while (totalForFull > 0) {
                  int active = 0;

                  for (aQ.Inner_BufferHelper row : fullRows) {
                     if (desired.get(row) < row.update) {
                        active++;
                     }
                  }

                  if (active <= 0) {
                     break;
                  }

                  int share = Math.max(1, totalForFull / active);
                  boolean progressed = false;

                  for (aQ.Inner_BufferHelper row : fullRows) {
                     if (totalForFull <= 0) {
                        break;
                     }

                     int currentDesired = desired.get(row);
                     int room = row.update - currentDesired;
                     if (room > 0) {
                        int take = Math.min(room, Math.min(share, totalForFull));
                        desired.put(row, currentDesired + take);
                        totalForFull -= take;
                        progressed = true;
                     }
                  }

                  if (!progressed) {
                     break;
                  }
               }

               for (aQ.Inner_BufferHelper row : fullRows) {
                  int finalCount = desired.getOrDefault(row, 0);
                  row.flush = finalCount;
                  row.sync = Math.max(0, finalCount - row.write);
               }
            }
         }
      }
   }

   public static boolean init_Z_InventoryMenu(InventoryMenu handler) {
      if (handler == null) {
         return false;
      }

      for (int slotId = 1; slotId <= 4 && slotId < handler.slots.size(); slotId++) {
         if (!handler.slots.get(slotId).getItem().isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public static boolean load_Z_InventoryMenu(InventoryMenu handler) {
      return handler != null && !handler.getCarried().isEmpty();
   }

   public static boolean init_Z_InventoryMenu_Z(InventoryMenu handler, boolean includeCursor) {
      return init_Z_InventoryMenu(handler) || includeCursor && load_Z_InventoryMenu(handler);
   }

   public static boolean init_Z_InventoryMenu_3(InventoryMenu handler, boolean includeCursor, Set<Integer> slotIds) {
      if (handler == null) {
         return false;
      }

      if (slotIds != null) {
         for (Integer id : slotIds) {
            if (id != null && id >= 0 && id < handler.slots.size() && !handler.slots.get(id).getItem().isEmpty()) {
               return true;
            }
         }
      }

      return includeCursor && load_Z_InventoryMenu(handler);
   }

   private void init_V_InventoryMenu_Z(InventoryMenu handler, boolean includeCursor) {
      boolean active = init_Z_InventoryMenu_3(handler, includeCursor, this.init_Set());
      ck shared = ck.init_ck();
      if (active) {
         shared.load(this.init_Set(), includeCursor);
         shared.sync(true);
         shared.update(true);
      } else {
         if (shared.sync_Z()) {
            boolean stillHeld = init_Z_InventoryMenu_3(handler, shared.flush_Z(), shared.init_Set());
            if (stillHeld) {
               shared.update(true);
               return;
            }

            shared.sync(false);
         }

         shared.update(false);
      }
   }

   private void init_V_Z_2(boolean includeCursor) {
      ck shared = ck.init_ck();
      shared.load(this.init_Set(), includeCursor);
      shared.sync(true);
      shared.update(true);
   }

   public int init_I_5() {
      return 11;
   }

   public static int init_I_Z(boolean carryCursor) {
      return 10 + (carryCursor ? 1 : 0);
   }

   static void init_V_Minecraft_E(Minecraft mc, autismclient.util.E target) {
      if (target != null) {
         target.init_Z_Minecraft(mc);
      }
   }

   private static boolean init_Z_Slot(Slot slot) {
      return slot != null && slot.index >= 5 && !slot.getItem().isEmpty();
   }

   private static int init_I_AbstractContainerM_I(AbstractContainerMenu handler, int configuredSlot) {
      for (Slot slot : handler.slots) {
         if (slot != null
            && slot.index >= 0
            && slot.index < handler.slots.size()
            && slot.index > 4
            && (slot.index < 5 || slot.index > 8)
            && slot.index != 45
            && slot.container != null
            && slot.container instanceof Inventory) {
            int visibleSlot = slot.getContainerSlot();
            if (visibleSlot == configuredSlot) {
               return slot.index;
            }
         }
      }

      return -1;
   }

   private static boolean init_Z_ItemStack(ItemStack stack, ArrayUtil target, int visibleSlot) {
      if (stack != null && !stack.isEmpty()) {
         return target != null && target.update() ? target.init_Z_ItemStack_I(stack, visibleSlot) : true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_InventoryMenu_2(InventoryMenu handler, Slot slot, ArrayUtil target) {
      if (handler != null && slot != null && target != null && !slot.getItem().isEmpty()) {
         int visibleSlot = init_I_InventoryMenu_I(handler, slot.index);
         return target.init_Z() && target.init_I != visibleSlot ? false : init_Z_ItemStack(slot.getItem(), target, visibleSlot);
      } else {
         return false;
      }
   }

   private static boolean init_Z_I_I(int configuredSlot, int handlerSlotId) {
      return configuredSlot == handlerSlotId ? true : configuredSlot == 100 && handlerSlotId == 0;
   }

   private static boolean init_Z_ItemStack_ArrayUtil(ItemStack stack, ArrayUtil target) {
      if (stack == null || stack.isEmpty()) {
         return false;
      } else if (target != null && target.update()) {
         ArrayUtil identityOnly = target.init_ArrayUtil();
         identityOnly.init_I = -1;
         return identityOnly.init_Z_ItemStack_I(stack, -1);
      } else {
         return true;
      }
   }

   private static String init_Str_ArrayUtil(ArrayUtil target) {
      if (target == null) {
         return "";
      } else {
         String legacy = init_Str_Str_7(target.write_Str());
         if (legacy != null && !legacy.isBlank()) {
            return legacy.toLowerCase(Locale.ROOT);
         } else if (target.init_Z()) {
            return "#slot:" + target.init_I;
         } else {
            return target.init_Str() == null ? "" : target.init_Str().toLowerCase(Locale.ROOT);
         }
      }
   }

   private static int init_I_Str_3(String entry) {
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

   private static String read_Str_Str(String entry) {
      if (entry == null || entry.isBlank()) {
         return "";
      }

      if (!entry.startsWith("#")) {
         return entry.trim();
      }

      int separator = entry.indexOf(124);
      return separator >= 0 && separator + 1 < entry.length() ? entry.substring(separator + 1).trim() : "";
   }

   private static int init_I_InventoryMenu_I(InventoryMenu handler, int handlerSlotId) {
      if (handlerSlotId >= 0 && handlerSlotId < handler.slots.size()) {
         Slot slot = handler.slots.get(handlerSlotId);
         return slot != null && slot.container instanceof Inventory ? slot.getContainerSlot() : -1;
      } else {
         return -1;
      }
   }

   private List<ArrayUtil> read_List() {
      if (!this.entryTargets.isEmpty()) {
         return this.entryTargets;
      }

      for (String entry : this.entries) {
         String normalized = init_Str_Str_7(entry);
         if (normalized != null) {
            ArrayUtil target = ArrayUtil.update(normalized);
            if (target.init_Z() || target.update()) {
               this.entryTargets.add(target);
            }

            if (this.entryTargets.size() >= this.init_I_5()) {
               break;
            }
         }
      }

      return this.entryTargets;
   }

   private List<ArrayUtil> write() {
      List<ArrayUtil> runtime = new ArrayList<>();
      Minecraft mc = Minecraft.getInstance();

      for (ArrayUtil target : this.read_List()) {
         if (target != null) {
            ArrayUtil resolved = target.init_ArrayUtil_Minecraft(mc);
            if (resolved != null && (resolved.init_Z() || resolved.update())) {
               runtime.add(resolved);
            }
         }
      }

      return runtime;
   }

   private void read_V_2() {
      this.entries.clear();

      for (ArrayUtil target : this.entryTargets) {
         if (target != null) {
            String entry = init_Str_Str_7(target.write_Str());
            if (entry != null) {
               this.entries.add(entry);
               if (this.entries.size() >= this.init_I_5()) {
                  break;
               }
            }
         }
      }
   }

   private static final class Inner_BufferHelper {
      final int init_I;
      final ArrayUtil init_ArrayUtil;
      final int load;
      final aQ.Inner_DataUtil init_aQ_DataUtil;
      final int read;
      final int write;
      final int update;
      int sync;
      int flush;

      Inner_BufferHelper(int index, ArrayUtil target, int targetSlotId, aQ.Inner_DataUtil amountMode, int customCount, int currentCount, int maxCount) {
         this.init_I = index;
         this.init_ArrayUtil = target;
         this.load = targetSlotId;
         this.init_aQ_DataUtil = amountMode;
         this.read = customCount;
         this.write = currentCount;
         this.update = maxCount;
      }
   }

   public static final class Inner_CachePool {
      final int init;
      final int load;
      final int read;

      Inner_CachePool(int sourceSlotId, int targetSlotId, int count) {
         this.init = sourceSlotId;
         this.load = targetSlotId;
         this.read = count;
      }

      public int init() {
         return this.init;
      }

      public int load() {
         return this.load;
      }

      public int read() {
         return this.read;
      }
   }

   public enum Inner_ConfigStore {
      FAST,
      CLICK,
      SAFE_CLICK;
   }

   public enum Inner_DataUtil {
      FULL_STK,
      CUSTOM;
   }

   public enum Inner_StreamUtil {
      PUT_IN,
      TAKE_OUT,
      DROP;
   }
}
