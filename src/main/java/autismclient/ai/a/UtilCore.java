package autismclient.ai.a;

import autismclient.ai.PkgCls_b;
import autismclient.ai.bK;
import autismclient.ai.ca;
import autismclient.modules.aD;
import autismclient.util.AutismLiteVariant;
import autismclient.util.aw;
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

public final class UtilCore {
   public static final long init_J = 2000000000L;
   static final int init_I = 40;
   static final int load_I = 2;
   private static volatile int write_I;
   private static volatile long load_J;
   public static final int read_I = 3;
   private static volatile int update_I;
   private static volatile long read_J;
   private static volatile int sync_I;
   private static volatile long write_J;
   private static volatile Map<Item, Integer> init_Map = Map.of();
   private static volatile long update_J;
   static final List<String> init_List = List.of(
      "minecraft:dirt", "minecraft:cobblestone", "minecraft:cobbled_deepslate", "minecraft:netherrack", "minecraft:stone", "minecraft:end_stone"
   );
   static final List<String> load_List = List.of(
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
   private final UtilCore.Inner_StreamUtil init_UtilCore_StreamUti;
   private final LongSupplier init_LongSupplier;
   private final LongSupplier load_LongSupplier;
   private UtilCore.Inner_BufferHelper init_UtilCore_BufferHel = UtilCore.Inner_BufferHelper.init;
   private String init_Str;
   private int flush_I = -1;
   private int close = -1;
   private int open;
   private int poll;
   private long sync_J = -1L;
   private long flush_J;
   private int push;

   public static void init(int planks) {
      write_I = Math.max(0, planks);
      load_J = planks > 0 ? System.nanoTime() + 3000000000L : 0L;
   }

   public static int init_I() {
      int job = System.nanoTime() < load_J ? write_I : 0;
      return Math.max(job, load_I());
   }

   public static int init(Inventory inventory) {
      int reserve = init_I();
      int wood = load(inventory);
      return reserve <= 0 ? wood : Math.max(0, wood - reserve);
   }

   public static int load(Inventory inventory) {
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

   public static int load_I() {
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

         return init(sticks, table);
      } else {
         return 0;
      }
   }

   static int init(int sticks, boolean tableCarried) {
      return (sticks >= 2 ? 0 : 2) + (tableCarried ? 0 : 4);
   }

   public static int read() {
      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.player != null && !mc.player.hasInfiniteMaterials() ? 3 : 0;
   }

   public static void load(int blocks) {
      update_I = Math.max(0, blocks);
      read_J = blocks > 0 ? System.nanoTime() + 3000000000L : 0L;
   }

   public static int write() {
      int job = System.nanoTime() < read_J ? update_I : 0;
      return Math.max(job, read());
   }

   public static void read(int strikes) {
      sync_I = Math.max(0, strikes);
      write_J = strikes > 0 ? System.nanoTime() + 3000000000L : 0L;
   }

   public static int update() {
      return System.nanoTime() < write_J ? sync_I : 0;
   }

   public static boolean init(ItemStack stack) {
      return stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE) || stack.is(Items.BLACKSTONE);
   }

   public static int read(Inventory inventory) {
      int stone = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (init(inventory.getItem(slot))) {
            stone += inventory.getItem(slot).getCount();
         }
      }

      return Math.max(0, stone - write());
   }

   public static void init(Map<Item, Integer> counts) {
      init_Map = Map.copyOf(counts);
      update_J = counts.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
   }

   public static int init(Item item) {
      int held = System.nanoTime() < update_J ? init_Map.getOrDefault(item, 0) : 0;
      if (!AutismLiteVariant.enabled()) {
         held = Math.max(held, bK.load(item));
      }

      return held;
   }

   public static boolean init(ItemStack stack, Inventory inventory) {
      return init_Z_ItemStack(stack, inventory, false);
   }

   public static boolean init_Z_ItemStack(ItemStack stack, Inventory inventory, boolean survival) {
      int held = init(stack.getItem());
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
         int reserve = survival ? sync() : init_I();
         return reserve <= 0 || load(inventory) > reserve;
      }

      if (!init(stack)) {
         return true;
      }

      int reserve = survival ? flush() : write();
      int stone = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (init(inventory.getItem(slot))) {
            stone += inventory.getItem(slot).getCount();
         }
      }

      return reserve <= 0 || stone > reserve;
   }

   public static boolean load(ItemStack stack, Inventory inventory) {
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
         return path.endsWith("_ore") || path.equals("ancient_debris") || path.startsWith("raw_") && path.endsWith("_block") ? false : init(stack, inventory);
      }
   }

   public static int sync() {
      return System.nanoTime() < load_J ? write_I : 0;
   }

   public static int flush() {
      return System.nanoTime() < read_J ? update_I : 0;
   }

   public static int init_I(String itemId) {
      if (itemId == null) {
         return -1;
      } else {
         int index = init_List.indexOf(itemId);
         if (index >= 0) {
            return index;
         } else {
            index = load_List.indexOf(itemId);
            if (index >= 0) {
               return init_List.size() + index;
            } else {
               int tail = init_List.size() + load_List.size();
               if (itemId.startsWith("minecraft:") && itemId.endsWith("terracotta") && !itemId.endsWith("glazed_terracotta")) {
                  return tail;
               } else {
                  return itemId.startsWith("minecraft:") && itemId.endsWith("_planks") ? tail + 1 : -1;
               }
            }
         }
      }
   }

   public static boolean init_Z(String itemId) {
      return init_I(itemId) >= 0;
   }

   public UtilCore(final Minecraft mc) {
      this(new UtilCore.Inner_StreamUtil() {
         @Override
         public boolean init() {
            return mc.player != null && mc.player.containerMenu.getCarried().isEmpty();
         }

         @Override
         public boolean init(int backpackSlot, int hotbarSlot) {
            if (mc.player != null && !aD.read_Z()) {
               int slot = aw.load_I(mc, backpackSlot);
               if (slot < 0) {
                  return false;
               }

               PkgCls_b.init(mc.player.containerMenu.containerId, slot, hotbarSlot, ContainerInput.SWAP);
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
      }, () -> mc.player == null ? -1L : ca.init_J_I(mc.player.containerMenu.containerId), System::nanoTime);
   }

   public UtilCore(UtilCore.Inner_StreamUtil slots, LongSupplier inventoryRevision, LongSupplier clock) {
      this.init_UtilCore_StreamUti = slots;
      this.init_LongSupplier = inventoryRevision;
      this.load_LongSupplier = clock;
   }

   public UtilCore.Inner_BufferHelper init_UtilCore_BufferHel() {
      return this.init_UtilCore_BufferHel;
   }

   public boolean init_Z() {
      return this.init_UtilCore_BufferHel == UtilCore.Inner_BufferHelper.load;
   }

   public boolean load_Z() {
      return this.init_UtilCore_BufferHel != UtilCore.Inner_BufferHelper.read
         && this.sync_J >= 0L
         && (!this.init_UtilCore_StreamUti.init(this.close, this.init_Str) || this.init_LongSupplier.getAsLong() <= this.sync_J);
   }

   public int close() {
      return this.push;
   }

   public void init_V_I(int backpackSlot, int hotbarSlot, String expectedItem) {
      this.flush_I = backpackSlot;
      this.close = hotbarSlot;
      this.init_Str = expectedItem;
      this.open = 0;
      this.poll = 0;
      this.sync_J = -1L;
      this.init_UtilCore_BufferHel = UtilCore.Inner_BufferHelper.load;
   }

   public void init_V() {
      this.init_UtilCore_BufferHel = UtilCore.Inner_BufferHelper.init;
      this.init_Str = null;
      this.flush_I = -1;
      this.close = -1;
      this.sync_J = -1L;
   }

   public UtilCore.Inner_BufferHelper load_UtilCore_BufferHel() {
      if (this.init_UtilCore_BufferHel != UtilCore.Inner_BufferHelper.load) {
         return this.init_UtilCore_BufferHel;
      }

      if (this.sync_J < 0L) {
         if (this.open >= 2) {
            this.init_UtilCore_BufferHel = UtilCore.Inner_BufferHelper.write;
            return this.init_UtilCore_BufferHel;
         } else if (this.load_LongSupplier.getAsLong() < this.flush_J) {
            return this.init_UtilCore_BufferHel;
         } else if (!this.init_UtilCore_StreamUti.init()) {
            this.open++;
            return this.init_UtilCore_BufferHel;
         } else {
            long revision = this.init_LongSupplier.getAsLong();
            if (!this.init_UtilCore_StreamUti.init(this.flush_I, this.close)) {
               this.open++;
               return this.init_UtilCore_BufferHel;
            } else {
               this.open++;
               this.sync_J = revision;
               this.flush_J = this.load_LongSupplier.getAsLong() + 2000000000L;
               this.poll = 0;
               return this.init_UtilCore_BufferHel;
            }
         }
      } else {
         if (this.init_UtilCore_StreamUti.init(this.close, this.init_Str) && this.init_LongSupplier.getAsLong() > this.sync_J) {
            this.push++;
            this.init_UtilCore_BufferHel = UtilCore.Inner_BufferHelper.read;
            return this.init_UtilCore_BufferHel;
         }

         if (++this.poll > 40) {
            this.init_UtilCore_BufferHel = UtilCore.Inner_BufferHelper.write;
         }

         return this.init_UtilCore_BufferHel;
      }
   }

   public static int init(List<UtilCore.Inner_CachePool> backpack, String lastUsedId) {
      int best = -1;
      int bestRank = 2147483647;
      int bestCount = -1;

      for (int i = 0; i < backpack.size(); i++) {
         UtilCore.Inner_CachePool view = backpack.get(i);
         if (view != null && view.itemId() != null && view.count() > 0 && !view.falling() && view.fullBlock() && !view.blockEntity()) {
            int rank;
            if (lastUsedId != null && view.itemId().equals(lastUsedId)) {
               rank = 0;
            } else {
               int index = init_I(view.itemId());
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

   public static int init(List<UtilCore.Inner_DataUtil> hotbar) {
      for (int i = 0; i < hotbar.size(); i++) {
         if (hotbar.get(i).empty()) {
            return i;
         }
      }

      int best = -1;
      int bestScore = 2147483647;

      for (int i = 0; i < hotbar.size(); i++) {
         UtilCore.Inner_DataUtil view = hotbar.get(i);
         int score = view.damageable() ? 60 : (view.food() ? 40 : (view.block() ? 20 : 30));
         if (score < bestScore) {
            best = i;
            bestScore = score;
         }
      }

      return best;
   }

   public enum Inner_BufferHelper {
      init,
      load,
      read,
      write;
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
