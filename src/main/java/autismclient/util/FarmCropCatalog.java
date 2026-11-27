package autismclient.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FarmCropCatalog {
   private static final Set<Block> init_Set = init_Set_BlockArr(Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.BAMBOO);
   private static final Set<Block> load = init_Set_BlockArr(Blocks.PUMPKIN, Blocks.MELON, Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.BAMBOO);
   private static final Map<Block, Item> init_Map = Map.ofEntries(
      Map.entry(Blocks.WHEAT, Items.WHEAT_SEEDS),
      Map.entry(Blocks.CARROTS, Items.CARROT),
      Map.entry(Blocks.POTATOES, Items.POTATO),
      Map.entry(Blocks.BEETROOTS, Items.BEETROOT_SEEDS),
      Map.entry(Blocks.NETHER_WART, Items.NETHER_WART),
      Map.entry(Blocks.COCOA, Items.COCOA_BEANS),
      Map.entry(Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES)
   );
   private static final Set<Block> read = init_Set_BlockArr(
      Blocks.WHEAT,
      Blocks.CARROTS,
      Blocks.POTATOES,
      Blocks.BEETROOTS,
      Blocks.NETHER_WART,
      Blocks.COCOA,
      Blocks.SWEET_BERRY_BUSH,
      Blocks.PUMPKIN,
      Blocks.MELON,
      Blocks.SUGAR_CANE,
      Blocks.CACTUS,
      Blocks.BAMBOO
   );

   private FarmCropCatalog() {
   }

   public static boolean isCrop(Block block) {
      return block != null && read.contains(block);
   }

   public static boolean load(Block block) {
      return block != null && init_Set.contains(block);
   }

   public static boolean read(Block block) {
      return block != null && load.contains(block);
   }

   public static Set<Block> init_Set() {
      return read;
   }

   public static boolean canGrowOn(Block crop, BlockState below) {
      if (crop == null || below == null) {
         return false;
      } else if (crop == Blocks.NETHER_WART) {
         return below.is(Blocks.SOUL_SAND) || below.is(BlockTags.SUPPORTS_NETHER_WART);
      } else if (crop == Blocks.COCOA) {
         return below.is(BlockTags.SUPPORTS_COCOA);
      } else if (crop == Blocks.SWEET_BERRY_BUSH) {
         return below.is(BlockTags.SUPPORTS_VEGETATION);
      } else {
         return !init_Map.containsKey(crop) ? false : below.is(Blocks.FARMLAND) || below.is(BlockTags.SUPPORTS_CROPS);
      }
   }

   public static Item seedFor(Block crop) {
      return crop == null ? null : init_Map.get(crop);
   }

   private static Set<Block> init_Set_BlockArr(Block... blocks) {
      Set<Block> set = Collections.newSetFromMap(new IdentityHashMap<>());
      Collections.addAll(set, blocks);
      return Collections.unmodifiableSet(set);
   }
}
