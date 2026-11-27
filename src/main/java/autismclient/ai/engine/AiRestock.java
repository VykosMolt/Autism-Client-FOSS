package autismclient.ai.engine;

import autismclient.ai.AiContainerTransactions;
import autismclient.ai.AiRunResources;
import autismclient.ai.AiServerEvidence;
import autismclient.modules.aD;
import autismclient.util.AutismLiteVariant;
import autismclient.util.InventorySlotHelper;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public final class AiRestock {
   public static final long SWAP_RETRY_COOLDOWN_NANOS = 2000000000L;
   static final int MAX_WAIT_TICKS = 40;
   static final int MAX_ATTEMPTS = 2;
   private static volatile int jobWoodReserve;
   private static volatile long jobWoodReserveUntilNanos;
   public static final int BASE_STONE_RESERVE = 3;
   private static volatile int jobStoneReserve;
   private static volatile long jobStoneReserveUntilNanos;
   private static volatile int jobStrikeReserve;
   private static volatile long jobStrikeReserveUntilNanos;
   private static volatile Map<Item, Integer> jobItemReserves = Map.of();
   private static volatile long jobItemReservesUntilNanos;
   static final List<String> PRIMARY_SCAFFOLD_BLOCKS = List.of(
      "minecraft:dirt", "minecraft:cobblestone", "minecraft:cobbled_deepslate", "minecraft:netherrack", "minecraft:stone", "minecraft:end_stone"
   );
   static final List<String> SECONDARY_SCAFFOLD_BLOCKS = List.of(
      "minecraft:granite",
      "minecraft:diorite",
      "minecraft:andesite",
      "minecraft:tuff",
      "minecraft:deepslate",
      "minecraft:calcite",
      "minecraft:blackstone",
      "minecraft:basalt",
      "minecraft:smooth_basalt",
      "minecraft:mossy_cobblestone",
      "minecraft:coarse_dirt",
      "minecraft:rooted_dirt",
      "minecraft:grass_block",
      "minecraft:podzol",
      "minecraft:mycelium",
      "minecraft:packed_mud",
      "minecraft:moss_block",
      "minecraft:soul_soil",
      "minecraft:sandstone",
      "minecraft:red_sandstone",
      "minecraft:clay",
      "minecraft:stone_bricks",
      "minecraft:bricks",
      "minecraft:nether_bricks"
   );
   private final AiRestock.Inner_StreamUtil slotOps;
   private final LongSupplier inventoryRevisionSupplier;
   private final LongSupplier clockNanos;
   private AiRestock.Inner_BufferHelper state = AiRestock.Inner_BufferHelper.IDLE;
   private String expectedItemId;
   private int backpackSlot = -1;
   private int hotbarSlot = -1;
   private int attemptCount;
   private int waitTicks;
   private long sentRevision = -1L;
   private long nextAttemptAfterNanos;
   private int completedSwapCount;

   public static void reserveWoodForJob(int planks) {
      jobWoodReserve = Math.max(0, planks);
      jobWoodReserveUntilNanos = planks > 0 ? System.nanoTime() + 3000000000L : 0L;
   }

   public static int reservedWood() {
      int job = System.nanoTime() < jobWoodReserveUntilNanos ? jobWoodReserve : 0;
      return Math.max(job, baseWoodReserve());
   }

   public static int availableSpareWood(Inventory inventory) {
      int reserve = reservedWood();
      int wood = totalWoodUnits(inventory);
      return reserve <= 0 ? wood : Math.max(0, wood - reserve);
   }

   public static int totalWoodUnits(Inventory inventory) {
      int wood = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (stack.is(ItemTags.PLANKS)) {
            wood += stack.getCount();
         } else if (stack.is(ItemTags.LOGS)) {
            wood += 4 * stack.getCount();
         }
      }

      return wood;
   }

   public static int baseWoodReserve() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && !mc.player.hasInfiniteMaterials()) {
         Inventory inventory = mc.player.getInventory();
         int sticks = 0;
         boolean table = false;

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.STICK)) {
               sticks += stack.getCount();
            } else if (stack.is(Items.CRAFTING_TABLE)) {
               table = true;
            }
         }

         return computeBaseWoodReserve(sticks, table);
      } else {
         return 0;
      }
   }

   static int computeBaseWoodReserve(int sticks, boolean tableCarried) {
      return (sticks >= 2 ? 0 : 2) + (tableCarried ? 0 : 4);
   }

   public static int baseStoneReserve() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.player != null && !mc.player.hasInfiniteMaterials() ? 3 : 0;
   }

   public static void reserveStoneForJob(int blocks) {
      jobStoneReserve = Math.max(0, blocks);
      jobStoneReserveUntilNanos = blocks > 0 ? System.nanoTime() + 3000000000L : 0L;
   }

   public static int reservedStone() {
      int job = System.nanoTime() < jobStoneReserveUntilNanos ? jobStoneReserve : 0;
      return Math.max(job, baseStoneReserve());
   }

   public static void reserveStrikesForJob(int strikes) {
      jobStrikeReserve = Math.max(0, strikes);
      jobStrikeReserveUntilNanos = strikes > 0 ? System.nanoTime() + 3000000000L : 0L;
   }

   public static int reservedStrikes() {
      return System.nanoTime() < jobStrikeReserveUntilNanos ? jobStrikeReserve : 0;
   }

   public static boolean isStoneBlock(ItemStack stack) {
      return stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE) || stack.is(Items.BLACKSTONE);
   }

   public static int availableSpareStone(Inventory inventory) {
      int stone = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (isStoneBlock(inventory.getItem(slot))) {
            stone += inventory.getItem(slot).getCount();
         }
      }

      return Math.max(0, stone - reservedStone());
   }

   public static void reserveItemsForJob(Map<Item, Integer> counts) {
      jobItemReserves = Map.copyOf(counts);
      jobItemReservesUntilNanos = counts.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
   }

   public static int reservedItemCount(Item item) {
      int held = System.nanoTime() < jobItemReservesUntilNanos ? jobItemReserves.getOrDefault(item, 0) : 0;
      if (!AutismLiteVariant.enabled()) {
         held = Math.max(held, AiRunResources.mandatoryReservedCount(item));
      }

      return held;
   }

   public static boolean canSpendStack(ItemStack stack, Inventory inventory) {
      return canSpendStack(stack, inventory, false);
   }

   public static boolean canSpendStack(ItemStack stack, Inventory inventory, boolean survival) {
      int held = reservedItemCount(stack.getItem());
      if (held > 0) {
         int count = 0;

         for (int slot = 0; slot < 36; slot++) {
            if (inventory.getItem(slot).is(stack.getItem())) {
               count += inventory.getItem(slot).getCount();
            }
         }

         if (count <= held) {
            return false;
         }
      }

      if (stack.is(ItemTags.PLANKS)) {
         int reserve = survival ? activeJobWoodReserve() : reservedWood();
         return reserve <= 0 || totalWoodUnits(inventory) > reserve;
      }

      if (!isStoneBlock(stack)) {
         return true;
      }

      int reserve = survival ? activeJobStoneReserve() : reservedStone();
      int stone = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (isStoneBlock(inventory.getItem(slot))) {
            stone += inventory.getItem(slot).getCount();
         }
      }

      return reserve <= 0 || stone > reserve;
   }

   public static boolean isConsumableBuildingBlock(ItemStack stack, Inventory inventory) {
      if (!(stack.getItem() instanceof BlockItem block)) {
         return false;
      } else {
         if (stack.is(ItemTags.LOGS)) {
            return false;
         }

         BlockState state = block.getBlock().defaultBlockState();
         if (state.is(BlockTags.BEACON_BASE_BLOCKS)) {
            return false;
         }

         String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
         return path.endsWith("_ore") || path.equals("ancient_debris") || path.startsWith("raw_") && path.endsWith("_block") ? false : canSpendStack(stack, inventory);
      }
   }

   public static int activeJobWoodReserve() {
      return System.nanoTime() < jobWoodReserveUntilNanos ? jobWoodReserve : 0;
   }

   public static int activeJobStoneReserve() {
      return System.nanoTime() < jobStoneReserveUntilNanos ? jobStoneReserve : 0;
   }

   public static int scaffoldRank(String itemId) {
      if (itemId == null) {
         return -1;
      } else {
         int index = PRIMARY_SCAFFOLD_BLOCKS.indexOf(itemId);
         if (index >= 0) {
            return index;
         } else {
            index = SECONDARY_SCAFFOLD_BLOCKS.indexOf(itemId);
            if (index >= 0) {
               return PRIMARY_SCAFFOLD_BLOCKS.size() + index;
            } else {
               int tail = PRIMARY_SCAFFOLD_BLOCKS.size() + SECONDARY_SCAFFOLD_BLOCKS.size();
               if (itemId.startsWith("minecraft:") && itemId.endsWith("terracotta") && !itemId.endsWith("glazed_terracotta")) {
                  return tail;
               } else {
                  return itemId.startsWith("minecraft:") && itemId.endsWith("_planks") ? tail + 1 : -1;
               }
            }
         }
      }
   }

   public static boolean isScaffoldBlockId(String itemId) {
      return scaffoldRank(itemId) >= 0;
   }

   public AiRestock(final Minecraft mc) {
      this(new AiRestock.Inner_StreamUtil() {
         @Override
         public boolean init() {
            return mc.player != null && mc.player.containerMenu.getCarried().isEmpty();
         }

         @Override
         public boolean init(int backpackSlot, int hotbarSlot) {
            if (mc.player != null && !aD.read_Z()) {
               int slot = InventorySlotHelper.toScreenSlot(mc, backpackSlot);
               if (slot < 0) {
                  return false;
               }

               AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, slot, hotbarSlot, ContainerInput.SWAP);
               return true;
            } else {
               return false;
            }
         }

         @Override
         public boolean init(int hotbarSlot, String itemId) {
            if (mc.player == null) {
               return false;
            }

            ItemStack stack = mc.player.getInventory().getItem(hotbarSlot);
            return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(itemId);
         }
      }, () -> mc.player == null ? -1L : AiServerEvidence.containerRevision(mc.player.containerMenu.containerId), System::nanoTime);
   }

   public AiRestock(AiRestock.Inner_StreamUtil slots, LongSupplier inventoryRevision, LongSupplier clock) {
      this.slotOps = slots;
      this.inventoryRevisionSupplier = inventoryRevision;
      this.clockNanos = clock;
   }

   public AiRestock.Inner_BufferHelper state() {
      return this.state;
   }

   public boolean isRunning() {
      return this.state == AiRestock.Inner_BufferHelper.RUNNING;
   }

   public boolean needsReconciliation() {
      return this.state != AiRestock.Inner_BufferHelper.SUCCEEDED
         && this.sentRevision >= 0L
         && (!this.slotOps.init(this.hotbarSlot, this.expectedItemId) || this.inventoryRevisionSupplier.getAsLong() <= this.sentRevision);
   }

   public int completedSwapCount() {
      return this.completedSwapCount;
   }

   public void startSwap(int backpackSlot, int hotbarSlot, String expectedItem) {
      this.backpackSlot = backpackSlot;
      this.hotbarSlot = hotbarSlot;
      this.expectedItemId = expectedItem;
      this.attemptCount = 0;
      this.waitTicks = 0;
      this.sentRevision = -1L;
      this.state = AiRestock.Inner_BufferHelper.RUNNING;
   }

   public void reset() {
      this.state = AiRestock.Inner_BufferHelper.IDLE;
      this.expectedItemId = null;
      this.backpackSlot = -1;
      this.hotbarSlot = -1;
      this.sentRevision = -1L;
   }

   public AiRestock.Inner_BufferHelper tick() {
      if (this.state != AiRestock.Inner_BufferHelper.RUNNING) {
         return this.state;
      }

      if (this.sentRevision < 0L) {
         if (this.attemptCount >= 2) {
            this.state = AiRestock.Inner_BufferHelper.FAILED;
            return this.state;
         } else if (this.clockNanos.getAsLong() < this.nextAttemptAfterNanos) {
            return this.state;
         } else if (!this.slotOps.init()) {
            this.attemptCount++;
            return this.state;
         } else {
            long revision = this.inventoryRevisionSupplier.getAsLong();
            if (!this.slotOps.init(this.backpackSlot, this.hotbarSlot)) {
               this.attemptCount++;
               return this.state;
            } else {
               this.attemptCount++;
               this.sentRevision = revision;
               this.nextAttemptAfterNanos = this.clockNanos.getAsLong() + 2000000000L;
               this.waitTicks = 0;
               return this.state;
            }
         }
      } else {
         if (this.slotOps.init(this.hotbarSlot, this.expectedItemId) && this.inventoryRevisionSupplier.getAsLong() > this.sentRevision) {
            this.completedSwapCount++;
            this.state = AiRestock.Inner_BufferHelper.SUCCEEDED;
            return this.state;
         }

         if (++this.waitTicks > 40) {
            this.state = AiRestock.Inner_BufferHelper.FAILED;
         }

         return this.state;
      }
   }

   public static int pickBestBackpackSlot(List<AiRestock.Inner_CachePool> backpack, String lastUsedId) {
      int best = -1;
      int bestRank = 2147483647;
      int bestCount = -1;

      for (int i = 0; i < backpack.size(); i++) {
         AiRestock.Inner_CachePool view = backpack.get(i);
         if (view != null && view.itemId() != null && view.count() > 0 && !view.falling() && view.fullBlock() && !view.blockEntity()) {
            int rank;
            if (lastUsedId != null && view.itemId().equals(lastUsedId)) {
               rank = 0;
            } else {
               int index = scaffoldRank(view.itemId());
               if (index < 0) {
                  continue;
               }

               rank = index + 1;
            }

            if (rank < bestRank || rank == bestRank && view.count() > bestCount) {
               best = i;
               bestRank = rank;
               bestCount = view.count();
            }
         }
      }

      return best;
   }

   public static int pickTargetHotbarSlot(List<AiRestock.Inner_DataUtil> hotbar) {
      for (int i = 0; i < hotbar.size(); i++) {
         if (hotbar.get(i).empty()) {
            return i;
         }
      }

      int best = -1;
      int bestScore = 2147483647;

      for (int i = 0; i < hotbar.size(); i++) {
         AiRestock.Inner_DataUtil view = hotbar.get(i);
         int score = view.damageable() ? 60 : (view.food() ? 40 : (view.block() ? 20 : 30));
         if (score < bestScore) {
            best = i;
            bestScore = score;
         }
      }

      return best;
   }

   public enum Inner_BufferHelper {
      IDLE,
      RUNNING,
      SUCCEEDED,
      FAILED;
   }

   public record Inner_CachePool(String itemId, int count, boolean falling, boolean fullBlock, boolean blockEntity) {
   }

   public record Inner_DataUtil(boolean empty, boolean block, boolean food, boolean damageable) {
   }

   public interface Inner_StreamUtil {
      boolean init();

      boolean init(int var1, int var2);

      boolean init(int var1, String var2);
   }
}
